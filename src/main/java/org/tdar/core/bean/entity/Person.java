package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Check;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * Basic person class within tDAR. Can represent a registered user or a person
 * (with identifying email) entered as a contact or attributed reference, etc.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Entity
@Table(name = "person", indexes = { @Index(name = "person_instid", columnList = "institution_id, id") })
@Indexed(index = "Person")
@XmlRootElement(name = "person")
@Check(constraints="email <> ''")
public class Person extends Creator implements Comparable<Person>, Dedupable<Person>, Validatable {

    @Transient
    private static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "id", "institution", "dateCreated", "dateUpdated", 
            "emailPublic", "phonePublic", "status", "synonyms", "occurrence" };

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_creator_id")
    private Set<Person> synonyms = new HashSet<Person>();

    private static final long serialVersionUID = -3863573773250268081L;

    @Transient
    private transient String tempDisplayName;

    public Person() {
    }

    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    private transient String wildcardName;

    @JsonView(JsonLookupFilter.class)
    @Column(nullable = false, name = "last_name")
    @BulkImportField(label = "Last Name", comment = BulkImportField.CREATOR_LNAME_DESCRIPTION, order = 2)
    @Fields({ @Field(name = QueryFieldNames.LAST_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = QueryFieldNames.LAST_NAME_SORT, norms = Norms.NO, store = Store.YES) })
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String lastName;

    @Column(nullable = false, name = "first_name")
    @BulkImportField(label = "First Name", comment = BulkImportField.CREATOR_FNAME_DESCRIPTION, order = 1)
    @Fields({ @Field(name = QueryFieldNames.FIRST_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = QueryFieldNames.FIRST_NAME_SORT, norms = Norms.NO, store = Store.YES) })
    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String firstName;

    // http://support.orcid.org/knowledgebase/articles/116780-structure-of-the-orcid-identifier
    @Column(name = "orcid_id")
    private String orcidId;

    @Column(unique = true, nullable = true)
    @Field(name = "email", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class))
    @BulkImportField(label = "Email", order = 3)
    @Length(min = 1, max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String email;

    @Column(nullable = false, name = "email_public", columnDefinition = "boolean default FALSE")
    private Boolean emailPublic = Boolean.FALSE;

    @IndexedEmbedded(depth = 1)
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, optional = true)
    @BulkImportField(label = "Resource Creator's ", comment = BulkImportField.CREATOR_PERSON_INSTITUTION_DESCRIPTION, order = 50)
    @JsonView(JsonLookupFilter.class)
    private Institution institution;

    // rpanet.org "number"
    @Column(name = "rpa_number")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String rpaNumber;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String phone;

    @Column(nullable = false, name = "phone_public", columnDefinition = "boolean default FALSE")
    private Boolean phonePublic = Boolean.FALSE;

    /**
     * Returns the person's name in [last name, first name] format.
     * 
     * @return formatted String name
     */
    @Override
    @Transient
    @JsonView(JsonLookupFilter.class)
    public String getName() {
        return lastName + ", " + firstName;
    }

    @Override
    @Transient
    @JsonView(JsonLookupFilter.class)
    public String getProperName() {
        return firstName + " " + lastName;
    }

    /**
     * set the user firstname, lastname from string in "last first" format. anything other than simple
     * two word string is ignored.
     * 
     * @param properName
     */
    public void setName(String name) {
        String[] names = Person.split(name);
        if (names.length == 2) {
            setLastName(names[0]);
            setFirstName(names[1]);
        }
    }

    /**
     * FIXME: Only handles names in the form "FirstName LastName". So a name
     * like "Colin McGregor McCoy" would have "Colin" as the first name and
     * "McGregor McCoy" as the last name
     * 
     * @param name
     *            a full name in 'FirstName LastName' or 'LastName, FirstName'
     *            format.
     * @return String array of length 2 - [LastName, FirstName]
     */
    public static String[] split(String name) {
        String firstName = "";
        String lastName = "";
        int splitIndex = name.indexOf(',');
        if (splitIndex == -1) {
            splitIndex = name.indexOf(' ');
            if (splitIndex == -1) {
                // give up, warn?
                return new String[0];
            }
            firstName = name.substring(0, splitIndex);
            lastName = name.substring(splitIndex + 1, name.length());
        } else {
            lastName = name.substring(0, splitIndex);
            firstName = name.substring(splitIndex + 1, name.length());
        }
        return new String[] { lastName, firstName };
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null) {
            return;
        }
        this.lastName = lastName.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null) {
            return;
        }
        this.firstName = firstName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (StringUtils.isBlank(email)) {
            this.email = null;
        } else {
            this.email = email.toLowerCase();
        }
    }

    public Boolean getEmailPublic() {
        return emailPublic;

    }

    public void setEmailPublic(Boolean toggle) {
        this.emailPublic = toggle;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    @Override
    public String toString() {
        if ((institution != null) && !StringUtils.isBlank(institution.toString())) {
            return String.format("%s [%s | %s | %s]", getName(), getId(), email, institution);
        }
        return String.format("%s [%s | %s]", getName(), getId(), "No institution specified.");
    }

    /**
     * Compares by last name, first name, and then finally email.
     */
    @Override
    public int compareTo(Person otherPerson) {
        if (this == otherPerson) {
            return 0;
        }
        int comparison = lastName.compareTo(otherPerson.lastName);
        if (comparison == 0) {
            comparison = firstName.compareTo(otherPerson.firstName);
            if (comparison == 0) {
                // last straw is email
                comparison = email.compareTo(otherPerson.email);
            }
        }
        return comparison;
    }

    public String getRpaNumber() {
        return rpaNumber;
    }

    public void setRpaNumber(String rpaNumber) {
        this.rpaNumber = rpaNumber;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getPhonePublic() {
        return phonePublic;
    }

    public void setPhonePublic(Boolean toggle) {
        this.phonePublic = toggle;
    }


    @Override
    public CreatorType getCreatorType() {
        return CreatorType.PERSON;
    }

    @Override
    @Transient
    public String getInstitutionName() {
        String name = null;
        if (institution != null) {
            name = institution.getName();
        }
        return name;
    }

    @Override
    public boolean isDedupable() {
        return true;
    }

    public static String[] getIgnorePropertiesForUniqueness() {
        return IGNORE_PROPERTIES_FOR_UNIQUENESS;
    }

    @Override
    public Set<Obfuscatable> obfuscate() {
        setObfuscated(true);
        setObfuscatedObjectDifferent(false);
        // check if email and phone are actually confidential
        Set<Obfuscatable> set = new HashSet<>();
        if (!getEmailPublic()) {
            setEmail(null);
            setObfuscatedObjectDifferent(true);
        }
        if (!getPhonePublic()) {
            setObfuscatedObjectDifferent(true);
            setPhone(null);
        }
        setRpaNumber(null);
        set.add(getInstitution());
        return set;
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName);
    }

    @Transient
    @Override
    public boolean hasNoPersistableValues() {
        if (StringUtils.isBlank(email) && ((institution == null) || StringUtils.isBlank(institution.getName())) && StringUtils.isBlank(lastName) &&
                StringUtils.isBlank(firstName) && Persistable.Base.isNullOrTransient(getId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return isValidForController() && (getId() != null);
    }

    @Override
    public Set<Person> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<Person> synonyms) {
        this.synonyms = synonyms;
    }

    @Transient
    @XmlTransient
    public String getWildcardName() {
        return wildcardName;
    }

    public void setWildcardName(String wildcardName) {
        this.wildcardName = wildcardName;
    }

    @Override
    @Field(norms = Norms.NO, store = Store.YES)
    @DateBridge(resolution = Resolution.MILLISECOND)
    public Date getDateUpdated() {
        return super.getDateUpdated();
    }

    /**
     * convenience for struts in case of error on INPUT, better than "NULL NULL"
     * 
     * @deprecated Do not use this method in new code. Its behavior will change to fix legacy issues until it is removed from the API
     * */
    @Deprecated
    @JsonView(JsonLookupFilter.class)
    public String getTempDisplayName() {
        if(StringUtils.isNotBlank(tempDisplayName)) {
            return tempDisplayName;
        }
        if (StringUtils.isBlank(firstName)) {
            return "";
        }
        if (StringUtils.isBlank(lastName)) {
            return "";
        }
        if (StringUtils.isBlank(tempDisplayName) && StringUtils.isNotBlank(getProperName())) {
            setTempDisplayName(getProperName());
        }
        return tempDisplayName;
    }

    /**
     * convenience for struts in case of error on INPUT, better than "NULL NULL"
     * 
     * @deprecated Do not use this method in new code. Its behavior will change to fix legacy issues until it is removed from the API
     * */
    @Deprecated
    public void setTempDisplayName(String tempName) {
        this.tempDisplayName = tempName;
    }

    public String getOrcidId() {
        return orcidId;
    }

    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

}
