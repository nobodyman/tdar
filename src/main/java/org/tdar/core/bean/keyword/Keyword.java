package org.tdar.core.bean.keyword;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.index.analyzer.AutocompleteAnalyzer;
import org.tdar.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.query.QueryFieldNames;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface Keyword extends Persistable, Indexable {

    public String getLabel();

    public void setLabel(String label);

    public String getDefinition();

    public String getKeywordType();

    public void setDefinition(String definition);

    @MappedSuperclass
    @XmlType(name = "kwdbase")
    public static abstract class Base <T extends Base<?>> extends Persistable.Base implements Keyword, Comparable<T> {

        private static final long serialVersionUID = -7516574981065004043L;

        @Transient
        private final static String[] JSON_PROPERTIES = { "id", "label" };

        @Column(nullable = false, unique = true)
        @Fields({ @Field(name = "label", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
                @Field(name = "label_auto", analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
                @Field(name = "labelKeyword", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)),
                @Field(name = QueryFieldNames.LABEL_SORT, index= Index.UN_TOKENIZED, store = Store.YES)})
        private String label;

        @Lob
        @Type(type = "org.hibernate.type.StringClobType")
        private String definition;

        @Field
        @Transient
        public String getKeywordType() {
            return getClass().getSimpleName();
        }

        private transient Float score = -1f;
        private transient Explanation explanation;

        
        @Override
        public int compareTo(T o) {
            return getLabel().compareTo(o.getLabel());
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String toString() {
            return label;
        }

        @Override
        public String[] getIncludedJsonProperties() {
            return JSON_PROPERTIES;
        }

        @Transient
        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        @Transient
        public Explanation getExplanation() {
            return explanation;
        }

        public void setExplanation(Explanation explanation) {
            this.explanation = explanation;
        }

    }
}
