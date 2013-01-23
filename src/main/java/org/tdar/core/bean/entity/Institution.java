package org.tdar.core.bean.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Validatable;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;

/**
 * $Id$
 * 
 * Records the relevant information regarding an institution.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "institution")
@Indexed(index = "Institution")
@DiscriminatorValue("INSTITUTION")
@XmlRootElement(name = "institution")
public class Institution extends Creator implements Comparable<Institution>, Dedupable<Institution>, Validatable {

    private static final long serialVersionUID = 892315581573902067L;

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "name", "url" };

    private static final String ACRONYM_REGEX = "(?:.+)(?:[\\(\\[\\{])(.+)(?:[\\)\\]\\}])(?:.*)";

    @Transient
    private static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "id", "dateCreated", "description", "dateUpdated", "url",
            "location", "parentInstitution", "parentinstitution_id", "synonyms", "status"  };

    @OneToMany(orphanRemoval = true,cascade=CascadeType.ALL)
    @JoinColumn(name = "merge_creator_id")
    private Set<Institution> synonyms = new HashSet<Institution>();

    @Column(nullable = false, unique = true)
    @BulkImportField(label = "Institution Name", comment = BulkImportField.CREATOR_INSTITUTION_DESCRIPTION, order = 10)
    private String name;

    private String url;

    private String location;

    public int compareTo(Institution candidate) {
        return name.compareTo(candidate.name);
    }

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
    private Institution parentInstitution;

    public Institution() {
    }

    public Institution(String name) {
        this.name = name;
    }

    @XmlElement
    // FIXME: this seemingly conflicts w/ @Field annotations on Creator.getName(). Figure out which declaration is working
    @Fields({
            @Field(name = "name_auto", norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
            @Field(analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)) })
    public String getName() {
        if (parentInstitution != null) {
            return parentInstitution.getName() + " : " + name;
        }
        return name;
    }

    public String getProperName() {
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    @Transient
    @Field(name = "acronym", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class))
    public String getAcronym() {
        Pattern p = Pattern.compile(ACRONYM_REGEX);
        Matcher m = p.matcher(getName());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String toString() {
        return name;
    }

    public Institution getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Institution parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    @Override
    public CreatorType getCreatorType() {
        return CreatorType.INSTITUTION;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(name);
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
    }

    public static String[] getIgnorePropertiesForUniqueness() {
        return IGNORE_PROPERTIES_FOR_UNIQUENESS;
    }

    public List<Obfuscatable> obfuscate() {
        return null;
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(name);
    }

    @Override
    public boolean isValid() {
        return isValidForController() && getId() != null;
    }

    public Set<Institution> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<Institution> synonyms) {
        this.synonyms = synonyms;
    }
    
    public boolean hasNoPersistableValues() {
        if (StringUtils.isBlank(getName())) {
            return true;
        }
        return false;
    }
}
