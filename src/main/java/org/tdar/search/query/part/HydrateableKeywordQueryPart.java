package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.service.search.Operator;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * {@link QueryPart} which builds a query string looking for
 * keywords associated with a project and it's resources.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev: 1728 $
 * 
 */
public class HydrateableKeywordQueryPart<K extends Keyword> extends AbstractHydrateableQueryPart<K> {

    private static final String ID = ".id";
    private static final String LABEL_KEYWORD = ".labelKeyword";
    private static final String LABEL = ".label";
    private static final String INFORMATION_RESOURCES = "informationResources.";
    private boolean includeChildren = true;

    @SuppressWarnings("unchecked")
    public HydrateableKeywordQueryPart(KeywordType type, List<K> fieldValues_) {
        setOperator(Operator.OR);
        setActualClass((Class<K>) type.getKeywordClass());
        setFieldName(type.getFieldName());
        setFieldValues(fieldValues_);
    }

    @Override
    public Query generateQuery(QueryBuilder builder) {
        QueryPartGroup topLevel = generateRawQuery();
        if (topLevel == null) {
            return null;
        }
            return topLevel.generateQuery(builder);
    }
    
    @Override
    public String generateQueryString() {
        QueryPartGroup topLevel = generateRawQuery();
        if (topLevel == null) {
            return "";
        }
        return topLevel.generateQueryString();
    }

    private QueryPartGroup generateRawQuery() {
        List<String> labels = new ArrayList<String>();
        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (getFieldValues().get(i) == null) {
                continue;
            }
            if (PersistableUtils.isNotNullOrTransient(getFieldValues().get(i))) {
                ids.add(getFieldValues().get(i).getId());
            } else if (StringUtils.isNotBlank(getFieldValues().get(i).getLabel())) {
                labels.add(getFieldValues().get(i).getLabel());
            }
        }
        FieldQueryPart<String> labelPart = new FieldQueryPart<String>(getFieldName() + LABEL, getOperator(), labels);
        FieldQueryPart<String> labelKeyPart = new FieldQueryPart<String>(getFieldName() + LABEL_KEYWORD, getOperator(), labels);
        labelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        FieldQueryPart<Long> idPart = new FieldQueryPart<Long>(getFieldName() + ID, getOperator(), ids);
        labelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        QueryPartGroup field = new QueryPartGroup(getOperator(), idPart, labelPart, labelKeyPart);

        QueryPartGroup topLevel = new QueryPartGroup(Operator.AND, field);
        if (includeChildren) {
            topLevel.setOperator(Operator.OR);
            FieldQueryPart<Long> irIdPart = new FieldQueryPart<Long>(INFORMATION_RESOURCES + getFieldName() + ID, getOperator(), ids);
            FieldQueryPart<String> irLabelPart = new FieldQueryPart<String>(INFORMATION_RESOURCES + getFieldName() + LABEL, getOperator(), labels);
            irLabelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            FieldQueryPart<String> irLabelKeyPart = new FieldQueryPart<String>(INFORMATION_RESOURCES + getFieldName() + LABEL_KEYWORD, getOperator(),
                    labels);
            irLabelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            QueryPartGroup group = new QueryPartGroup(getOperator(), irLabelPart, irIdPart, irLabelKeyPart);
            topLevel.append(group);
        }
        return topLevel;
    }

    public String getDescriptionLabel(TextProvider provider) {
        return provider.getText("searchParameters." + getFieldName());
    }

    @Override
    public String getDescription(TextProvider provider) {
        String strValues = StringUtils.join(getFieldValues(), getDescriptionOperator(provider));
        if (StringUtils.isNotBlank(strValues)) {
            return String.format("%s: \"%s\"", getDescriptionLabel(provider), strValues);
        }
        return "";
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

}
