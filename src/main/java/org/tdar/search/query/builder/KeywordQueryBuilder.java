package org.tdar.search.query.builder;

import org.tdar.core.service.search.Operator;
import org.tdar.search.index.LookupSource;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class KeywordQueryBuilder extends QueryBuilder {

    public KeywordQueryBuilder() {
        this.setClasses(LookupSource.KEYWORD.getClasses());
    }

    public KeywordQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }
}
