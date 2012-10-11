package org.tdar.search.query.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Video;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class ResourceQueryBuilder extends QueryBuilder {

    public ResourceQueryBuilder() {
        this.setClasses(new Class[] { InformationResource.class, Document.class, Dataset.class, Ontology.class, CodingSheet.class, Project.class,
                SensoryData.class, Video.class });
        List<DynamicQueryComponent> dqc = new ArrayList<DynamicQueryComponent>();
        dqc.add(new DynamicQueryComponent(QueryFieldNames.ACTIVE_CULTURE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.IR_ACTIVE_CULTURE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        dqc.add(new DynamicQueryComponent(QueryFieldNames.IR_ACTIVE_SITE_TYPE_KEYWORDS_LABEL, NonTokenizingLowercaseKeywordAnalyzer.class, ""));
        setOverrides(dqc);
        getOverrides().add(new DynamicQueryComponent(QueryFieldNames.RESOURCE_ACCESS_TYPE, TdarCaseSensitiveStandardAnalyzer.class, ""));
    }

    public void addResourceOmits(List<String> omits) {
        omits.add("auto");
        omits.add("keywordType");
        omits.add("endDate");
        omits.add("submitter");
        omits.add("updatedBy");
        omits.add("startDate");
        omits.add("pairedValue");
        omits.add("resourceNotes.type");
        omits.add(".id");
    }

    @Override
    protected Map<String, Class<? extends Analyzer>> createPartialLabelOverrides() {
        Map<String, Class<? extends Analyzer>> map = super.createPartialLabelOverrides();
        // indicate we just want to use the default analyzer for labels that end with the following strings
        map.put("keywordType", null);
        map.put("endDate", null);
        map.put("submitter", null);
        map.put("updatedBy", null);
        map.put("startDate", null);
        map.put("pairedValue", null);
        map.put("resourceNotes.type", null);
        map.put(".id", null);
        return map;
    }

}
