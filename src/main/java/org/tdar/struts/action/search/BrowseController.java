package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.cache.BrowseYearCountCache;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.struts.data.ResourceCreatorProxy;

/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
@Namespace("/browse")
@ParentPackage("default")
@Component
@Scope("prototype")
public class BrowseController extends AbstractLookupController {

    private static final String ALL_TDAR_COLLECTIONS = "All Collections";
    private static final long serialVersionUID = -128651515783098910L;
    private Creator creator;

    private List<InvestigationType> investigationTypes = new ArrayList<InvestigationType>();
    private List<CultureKeyword> cultureKeywords = new ArrayList<CultureKeyword>();
    private List<SiteTypeKeyword> siteTypeKeywords = new ArrayList<SiteTypeKeyword>();
    private List<MaterialKeyword> materialTypes = new ArrayList<MaterialKeyword>();
    private List<String> alphabet = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
    private List<BrowseYearCountCache> timelineData;

    // private Keyword keyword;

    @Action("explore")
    public String explore() {
        setMaterialTypes(getGenericKeywordService().findAllWithCache(MaterialKeyword.class));
        setInvestigationTypes(getGenericKeywordService().findAllWithCache(InvestigationType.class));
        setCultureKeywords(getGenericKeywordService().findAllApprovedWithCache(CultureKeyword.class));
        setSiteTypeKeywords(getGenericKeywordService().findAllApprovedWithCache(SiteTypeKeyword.class));
        setTimelineData(getGenericService().findAll(BrowseYearCountCache.class));
        return SUCCESS;
    }

    // FIXME: if we had real facets, this would not be needed
    // @Action(value = "places", results = { @Result(location = "results.ftl") })
    // public String browsePlaces() {
    // setResults(getResourceService().findResourceLinkedValues(GeographicKeyword.class));
    // return SUCCESS;
    // }
    //
    // @Action(value = "cultures", results = { @Result(location = "results.ftl") })
    // public String browseCultures() {
    // setResults(getResourceService().findResourceLinkedValues(CultureKeyword.class));
    // return SUCCESS;
    // }

    @Action("collections")
    public String browseCollections() throws ParseException {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart(QueryFieldNames.COLLECTION_VISIBLE, "true"));
        qb.append(new FieldQueryPart(QueryFieldNames.TOP_LEVEL, "true"));
        setMode("browseCollections");
        handleSearch(qb);
        setSearchDescription(ALL_TDAR_COLLECTIONS);
        setSearchTitle(ALL_TDAR_COLLECTIONS);
        return SUCCESS;
    }

    @Action(value = "creators", results = { @Result(location = "results.ftl") })
    public String browseCreators() throws ParseException {
        if (!Persistable.Base.isNullOrTransient(getId())) {
            creator = getGenericService().find(Creator.class, getId());
            QueryBuilder queryBuilder = new ResourceQueryBuilder();
            queryBuilder.setOperator(Operator.AND);

            SearchParameters params = new SearchParameters(Operator.OR);
            params.getResourceCreatorProxies().add(new ResourceCreatorProxy(creator, null));

            queryBuilder.append(params);
            ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
            getAuthenticationAndAuthorizationService().initializeReservedSearchParameters(reservedSearchParameters, getAuthenticatedUser());
            queryBuilder.append(reservedSearchParameters);

            setMode("browseCreators");
            setSortField(SortOption.RESOURCE_TYPE);
            String descr = String.format("All Resources from %s", creator.getProperName());
            setSearchDescription(descr);
            setSearchTitle(descr);
            setRecordsPerPage(50);
            handleSearch(queryBuilder);
        }
        // setResults(getResourceService().findResourceLinkedValues(Creator.class));
        return SUCCESS;
    }

    // @Action(value = "materials", results = { @Result(location = "results.ftl") })
    // public String browseMaterialTypes() {
    // setResults(getResourceService().findResourceLinkedValues(MaterialKeyword.class));
    // return SUCCESS;
    // }
    //
    // @Action(value = "places", results = { @Result(location = "results.ftl") })
    // public String browseInvestigationTypes() {
    // setResults(getResourceService().findResourceLinkedValues(InvestigationType.class));
    // return SUCCESS;
    // }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public List<SiteTypeKeyword> getSiteTypeKeywords() {
        return siteTypeKeywords;
    }

    public void setSiteTypeKeywords(List<SiteTypeKeyword> siteTypeKeywords) {
        this.siteTypeKeywords = siteTypeKeywords;
    }

    public List<CultureKeyword> getCultureKeywords() {
        return cultureKeywords;
    }

    public void setCultureKeywords(List<CultureKeyword> cultureKeywords) {
        this.cultureKeywords = cultureKeywords;
    }

    public List<InvestigationType> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(List<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public List<MaterialKeyword> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<MaterialKeyword> materialTypes) {
        this.materialTypes = materialTypes;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(List<String> alphabet) {
        this.alphabet = alphabet;
    }

    public List<BrowseYearCountCache> getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(List<BrowseYearCountCache> list) {
        this.timelineData = list;
    }

    @Override
    public List<String> getProjections() {
        return ListUtils.EMPTY_LIST;
    }
}
