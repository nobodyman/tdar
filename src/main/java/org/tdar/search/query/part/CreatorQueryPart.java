package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.data.ResourceCreatorProxy;

public class CreatorQueryPart<C extends Creator> extends AbstractHydrateableQueryPart<C> {

    private List<ResourceCreatorRole> roles = new ArrayList<ResourceCreatorRole>();

    @SuppressWarnings("unchecked")
    public CreatorQueryPart(String fieldName, Class<C> creatorClass, C creator, List<ResourceCreatorProxy> proxyList) {
        // set default of "or"
        setOperator(Operator.OR);
        setActualClass(creatorClass);
        setFieldName(fieldName);
        setDisplayName("Creator");
        for (int i = 0; i < proxyList.size(); i++) {
            try {
                ResourceCreatorProxy proxy = proxyList.get(i);
                ResourceCreator rc = proxy.getResourceCreator();
                if (proxy.isValid()) {
                    if (Persistable.Base.isTransient(rc.getCreator())) {
                        // user entered a complete-ish creator record but autocomplete callback did fire successfully
                        throw new TdarRecoverableRuntimeException(String.format("Please use autocomplete when looking for creator %s", rc.getCreator()));
                    }
                    this.roles.add(rc.getRole());
                    this.getFieldValues().add((C) rc.getCreator());
                }
            } catch (NullPointerException npe) {
                logger.trace("NPE in creator construction, skipping...", npe);
            }
        }
    }

    @Override
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        List<Integer> trans = new ArrayList<Integer>();
        // iterate through all of the values; if any of them are transient, put those positions off to the side
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (Persistable.Base.isNotNullOrTransient(getFieldValues().get(i))) {
                appendPhrase(sb, i);
            } else {
                trans.add(i);
            }
        }
        if (sb.length() != 0) {
            constructQueryPhrase(sb, getFieldName());
        }
        if (CollectionUtils.isEmpty(trans)) {
            return sb.toString();
        }

        // // for the transient values; we'll grab them via a query using the transientFieldQueryPart --
        // // this will look it up by "title" or "whatever"
        //
        // for (int i = 0; i < getFieldValues().size(); i++) {
        // if (trans.contains(i)) {
        // QueryPartGroup group = new QueryPartGroup(Operator.AND);
        // C c = getFieldValues().get(i);
        // if (Person.class.isAssignableFrom(c.getClass())) {
        // PersonQueryPart part = new PersonQueryPart();
        // part.add((Person) c);
        // group.append(part);
        // } else {
        // InstitutionQueryPart part = new InstitutionQueryPart();
        // part.add((Institution) c);
        // group.append(part);
        // }
        // group.append(new FieldQueryPart(fieldName, fieldValues_))
        // }
        // }
        //
        // if (!transientFieldQueryPart.isEmpty()) {
        // sb.insert(0, "(");
        // if (sb.length() > 1) {
        // sb.append(" OR ");
        // }
        // logger.info(transientFieldQueryPart.generateQueryString());
        // sb.append(transientFieldQueryPart.generateQueryString());
        // sb.append(")");
        // }
        //
        return sb.toString();
    }

    @Override
    protected String formatValueAsStringForQuery(int index) {
        Creator c = getFieldValues().get(index);
        ResourceCreatorRole r = roles.get(index);
        logger.trace("{} {} ", c, r);
        if (r == null) {
            return PhraseFormatter.WILDCARD.format(ResourceCreator.getCreatorRoleIdentifier(c, r));
        }
        return ResourceCreator.getCreatorRoleIdentifier(c, r);
    };

    public List<ResourceCreatorRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ResourceCreatorRole> roles) {
        this.roles = roles;
    }

    @Override
    public String getDescription() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < getFieldValues().size(); i++) {
            Creator creator = getFieldValues().get(i);
            ResourceCreatorRole role = getRoles().get(i);
            if (creator != null && !creator.hasNoPersistableValues()) {
                if (names.length() > 0) {
                    names.append(" " + getOperator().name().toLowerCase()).append(" ");
                }
                names.append(creator.getProperName());
                if (role != null) {
                    names.append(" (").append(role.getLabel()).append(")");
                }
            }
        }
        return String.format("With creator(s): %s", names);
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml4(getDescription());
    }

}
