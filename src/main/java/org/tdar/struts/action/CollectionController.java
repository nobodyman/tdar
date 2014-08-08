package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.ResourceCollectionViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.PaginationHelper;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractPersistableController<ResourceCollection> implements SearchResultHandler<Resource>, DataTableResourceDisplay {

    /**
     * Threshold that defines a "big" collection (based on imperical evidence by highly-trained tDAR staff). This number
     * refers to the combined count of authorized users +the count of resources associated with a collection. Big
     * collections may adversely affect save/load times as well as cause rendering problems on the client, and so the
     * system may choose to mitigate these effects (somehow)
     */
    public static final int BIG_COLLECTION_CHILDREN_COUNT = 3_000;

    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient SearchService searchService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private static final long serialVersionUID = 5710621983240752457L;
//    private List<Resource> resources = new ArrayList<>();
    private List<ResourceCollection> allResourceCollections = new ArrayList<>();

    private List<Long> selectedResourceIds = new ArrayList<>();
    private Long parentId;
    private List<Resource> fullUserProjects;
    private List<ResourceCollection> collections = new LinkedList<>();
    private ArrayList<FacetValue> resourceTypeFacets = new ArrayList<>();

    private Long viewCount = 0L;
    private int startRecord = DEFAULT_START;
    private int recordsPerPage = 100;
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "CollectionBrowse";
    private PaginationHelper paginationHelper;
    private String parentCollectionName;
    private ArrayList<ResourceType> selectedResourceTypes = new ArrayList<ResourceType>();

    private List<Long> toRemove = new ArrayList<>();
    private List<Long> toAdd  = new ArrayList<>();
    private List<Project> allSubmittedProjects;

    @Override
    public boolean isEditable() {
        if (isNullOrNew()) {
            return false;
        }
        return authorizationService.canEditCollection(getAuthenticatedUser(), getPersistable());
    }

    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<ResourceCollection> getCandidateParentResourceCollections() {
        List<ResourceCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable());
        return publicResourceCollections;
    }

    @Override
    public boolean isViewable() {
        return isEditable() || authorizationService.canViewCollection(getResourceCollection(), getAuthenticatedUser());
    }

    
    
    @Override
    protected String save(ResourceCollection persistable) {
        if (persistable.getType() == null) {
            persistable.setType(CollectionType.SHARED);
        }
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well
        ResourceCollection parent = resourceCollectionService.find(parentId);
        if (Persistable.Base.isNotNullOrTransient(persistable) && Persistable.Base.isNotNullOrTransient(parent)
                && (parent.getParentIds().contains(persistable.getId()) || parent.getId().equals(persistable.getId()))) {
            addActionError(getText("collectionController.cannot_set_self_parent"));
            return INPUT;
        }

        List<Resource> resourcesToRemove = resourceService.findAll(Resource.class, toRemove);
        List<Resource> resourcesToAdd = resourceService.findAll(Resource.class, toAdd);
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);
        resourceCollectionService.updateCollectionParentTo(getAuthenticatedUser(), persistable, parent);
        resourceCollectionService.reconcileIncomingResourcesForCollection(persistable, getAuthenticatedUser(), resourcesToAdd, resourcesToRemove);
        resourceCollectionService.saveAuthorizedUsersForResourceCollection(persistable, persistable, getAuthorizedUsers(), shouldSaveResource(),
                getAuthenticatedUser());
        return SUCCESS;
    }

    @Override
    public void indexPersistable() {
        /*
         * if we want to be really "aggressive" we only need to do this if
         * (a) permissions change
         * (b) visibility changes
         */
        if (isAsync()) {
            searchIndexService.indexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.indexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    @Override
    public List<? extends Persistable> getDeleteIssues() {
        List<ResourceCollection> findAllChildCollections = resourceCollectionService.findDirectChildCollections(getId(), null, CollectionType.SHARED);
        getLogger().info("we still have children: {}", findAllChildCollections);
        return findAllChildCollections;
    }

    @Override
    protected void delete(ResourceCollection persistable) {
        // should I do something special?
        for (Resource resource : persistable.getResources()) {
            resource.getResourceCollections().remove(persistable);
            getGenericService().saveOrUpdate(resource);
        }
        getGenericService().delete(persistable.getAuthorizedUsers());
        // FIXME: need to handle parents and children
        // getSearchIndexService().index(persistable.getResources().toArray(new Resource[0]));
    }

    public ResourceCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new ResourceCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(ResourceCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

    public List<SortOption> getSortOptions() {
        return SortOption.getOptionsForResourceCollectionPage();
    }

    public List<DisplayOrientation> getResultsOrientations() {
        List<DisplayOrientation> options = Arrays.asList(DisplayOrientation.values());
        return options;
    }

    @Override
    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @Override
    public String loadViewMetadata() {
        setParentId(getPersistable().getParentId());
        if (!isEditor()) {
            ResourceCollectionViewStatistic rcvs = new ResourceCollectionViewStatistic(new Date(), getPersistable());
            getGenericService().saveOrUpdate(rcvs);
        } else {
            setViewCount(resourceCollectionService.getCollectionViewCount(getPersistable()));
        }
        return SUCCESS;
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        super.loadEditMetadata();
        getAuthorizedUsers().addAll(resourceCollectionService.getAuthorizedUsersForCollection(getPersistable(), getAuthenticatedUser()));
        for (AuthorizedUser au : getAuthorizedUsers()) {
            String name = null;
            if (au != null && au.getUser() != null ) {
                name = au.getUser().getProperName();
            }
            getAuthorizedUsersFullNames().add(name);
        }

        prepareDataTableSection();
        resources.addAll(getPersistable().getResources());
        setParentId(getPersistable().getParentId());
        if (Persistable.Base.isNotNullOrTransient(getParentId())) {
            parentCollectionName = getPersistable().getParent().getName();
        }
        return SUCCESS;
    }

    @Override
    public String loadAddMetadata() {
        if (Persistable.Base.isNotNullOrTransient(parentId)) {
            ResourceCollection parent = resourceCollectionService.find(parentId);
            if (parent != null) {
                parentCollectionName = parent.getName();
            }
        }
        prepareDataTableSection();
        return SUCCESS;
    }

    @Override
    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = ADD, type = REDIRECT)
    })
    public String edit() throws TdarActionException {
        String result = super.edit();
        return result;
    }


    @Override
    public void loadExtraViewMetadata() {
        if (Persistable.Base.isNullOrTransient(getPersistable())) {
            return;
        }
        getLogger().debug("child collections: begin");
        Set<ResourceCollection> findAllChildCollections;

        if (isAuthenticated()) {
            resourceCollectionService.buildCollectionTreeForController(getPersistable(), getAuthenticatedUser(), CollectionType.SHARED);
            findAllChildCollections = getPersistable().getTransientChildren();

            if (isEditor()) {
                List<Long> collectionIds = Persistable.Base.extractIds(resourceCollectionService.buildCollectionTreeForController(getPersistable(),
                        getAuthenticatedUser(), CollectionType.SHARED));
                collectionIds.add(getId());
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(null, null, collectionIds, null,
                        Arrays.asList(Status.ACTIVE, Status.DRAFT)));
            }
        } else {
            findAllChildCollections = new LinkedHashSet<ResourceCollection>(resourceCollectionService.findDirectChildCollections(getId(), true,
                    CollectionType.SHARED));
        }
        setCollections(new ArrayList<>(findAllChildCollections));
        getLogger().debug("child collections: sort");
        Collections.sort(collections);
        getLogger().debug("child collections: end");

        // if this collection is public, it will appear in a resource's public collection id list, otherwise it'll be in the shared collection id list
        // String collectionListFieldName = getPersistable().isVisible() ? QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS
        // : QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS;

        // the visibilty fence should take care of visible vs. shared above
        ResourceQueryBuilder qb = searchService.buildResourceContainedInSearch(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS,
                getResourceCollection(), getAuthenticatedUser(), this);
        searchService.addResourceTypeFacetToViewPage(qb, selectedResourceTypes, this);

        setSortField(getPersistable().getSortBy());
        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
            if (getPersistable().getSecondarySortBy() != null) {
                setSecondarySortField(getPersistable().getSecondarySortBy());
            }
        }

        try {
            searchService.handleSearch(qb, this, this);
        } catch (Exception e) {
            addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
        }
        getLogger().debug("lucene: end");
    }

    public List<Long> getSelectedResourceIds() {
        return selectedResourceIds;
    }

    public void setSelectedResourceIds(List<Long> selectedResourceIds) {
        this.selectedResourceIds = selectedResourceIds;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    private void prepareDataTableSection() {
        allSubmittedProjects = projectService.findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        fullUserProjects.removeAll(getAllSubmittedProjects());
        getAllResourceCollections().addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));

    }

    public void setFullUserProjects(List<Resource> projects) {
        this.fullUserProjects = projects;
    }

    @Override
    public List<Resource> getFullUserProjects() {
        if (fullUserProjects == null) {
            boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
        }
        return fullUserProjects;
    }

    @Override
    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    @Override
    public List<ResourceType> getResourceTypes() {
        return resourceService.getAllResourceTypes();
    }

    @Override
    public SortOption getSortField() {
        return this.sortField;
    }

    @Override
    public SortOption getSecondarySortField() {
        return this.secondarySortField;
    }

    @Override
    public void setTotalRecords(int resultSize) {
        this.totalRecords = resultSize;
    }

    @Override
    public int getStartRecord() {
        return this.startRecord;
    }

    @Override
    public int getRecordsPerPage() {
        return this.recordsPerPage;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isShowAll() {
        return false;
    }

    @Override
    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public void setCollections(List<ResourceCollection> findAllChildCollections) {
        getLogger().info("child collections: {}", findAllChildCollections);
        this.collections = findAllChildCollections;
    }

    public List<ResourceCollection> getCollections() {
        return this.collections;
    }

    @Override
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public void setResults(List<Resource> toReturn) {
        getLogger().trace("setResults: {}", toReturn);
        this.results = toReturn;
    }

    @Override
    public List<Resource> getResults() {
        return results;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    @Override
    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#setMode(java.lang.String)
     */
    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#getMode()
     */
    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    @Override
    public int getPrevPageStartRecord() {
        return startRecord - recordsPerPage;
    }

    @Override
    public String getSearchTitle() {
        return String.format("Resources in the %s Collection", getPersistable().getTitle());
    }

    @Override
    public String getSearchDescription() {
        return getSearchTitle();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        List<FacetGroup<? extends Enum>> group = new ArrayList<>();
        // List<FacetGroup<?>> group = new ArrayList<FacetGroup<?>>();
        group.add(new FacetGroup<ResourceType>(ResourceType.class, QueryFieldNames.RESOURCE_TYPE, resourceTypeFacets, ResourceType.DOCUMENT));
        return group;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public String getParentCollectionName() {
        return parentCollectionName;

    }

    public ArrayList<FacetValue> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public void setResourceTypeFacets(ArrayList<FacetValue> resourceTypeFacets) {
        this.resourceTypeFacets = resourceTypeFacets;
    }

    public ArrayList<ResourceType> getSelectedResourceTypes() {
        return selectedResourceTypes;
    }

    public void setSelectedResourceTypes(ArrayList<ResourceType> selectedResourceTypes) {
        this.selectedResourceTypes = selectedResourceTypes;
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return ProjectionModel.RESOURCE_PROXY;
    }

    /**
     * A hint to the view-layer that this resource collection is "big". The view-layer may choose to gracefully degrade the presentation to save on bandwidth
     * and/or
     * client resources.
     * 
     * @return
     */
    public boolean isBigCollection() {
        return (getPersistable().getResources().size() + getAuthorizedUsers().size()) > BIG_COLLECTION_CHILDREN_COUNT;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    @Override
    public List<ResourceCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<ResourceCollection> allResourceCollections) {
        this.allResourceCollections = allResourceCollections;
    }

    public List<Long> getToAdd() {
        return toAdd;
    }

    public void setToAdd(List<Long> toAdd) {
        this.toAdd = toAdd;
    }

    public List<Long> getToRemove() {
        return toRemove;
    }

    public void setToRemove(List<Long> toRemove) {
        this.toRemove = toRemove;
    }

}
