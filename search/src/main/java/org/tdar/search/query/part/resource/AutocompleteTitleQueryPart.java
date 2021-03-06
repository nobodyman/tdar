package org.tdar.search.query.part.resource;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.StringAutocompletePart;

import com.opensymphony.xwork2.TextProvider;

/**
 * Provides Lucene query string for autocomplete lookups.
 * 
 * @author Adam Brin
 */
public class AutocompleteTitleQueryPart implements QueryPart<String> {
    private static final float TITLE_SORT_BOOST = 4f;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final float TITLE_BOOST = 6f;
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    private final String title;

    public AutocompleteTitleQueryPart(String title) {
        this.title = title;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(title);
    }

    protected QueryPart<?> getQueryPart() {
        QueryPartGroup titleGroup = new QueryPartGroup(Operator.OR);
        // look up quoted leading match in autocomplete index
        StringAutocompletePart autoPart = new StringAutocompletePart(QueryFieldNames.NAME_AUTOCOMPLETE, Arrays.asList(title));
        // FIXME: while allowed, I'm not sure it's helpful to include non-analyzed fields in a search, especially considering the fact that it will use a
        // default analyzer at search-time. arguments otherwise?
        FieldQueryPart<String> titleSortPart = new FieldQueryPart<String>(QueryFieldNames.NAME_PHRASE, title).setPhraseFormatters(PhraseFormatter.ESCAPED,
                PhraseFormatter.WILDCARD);
        // keyword match on title
        FieldQueryPart<String> keywordTitlePart = new FieldQueryPart<String>(QueryFieldNames.NAME, title).setPhraseFormatters(PhraseFormatter.ESCAPED);
        if (title.length() > 2) {
            // FIXME: if title is over 2 characters, use escaped wildcard formatter?
            keywordTitlePart.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
        }
        titleGroup.append(autoPart.setBoost(TITLE_BOOST));
        titleGroup.append(titleSortPart.setBoost(TITLE_SORT_BOOST));
        titleGroup.append(keywordTitlePart);
        logger.trace("{}", titleGroup);
        return titleGroup;

    }

    @Override
    public String generateQueryString() {
        return isEmpty() ? "" : getQueryPart().generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return "Title: " + title;
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    @Override
    public boolean isDescriptionVisible() {
        return false;
    }

    @Override
    public void setDescriptionVisible(boolean visible) {
    }

    @Override
    public Operator getOperator() {
        return Operator.AND;
    }
}
