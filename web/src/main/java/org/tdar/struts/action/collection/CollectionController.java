package org.tdar.struts.action.collection;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractCollectionController;
import org.tdar.struts.action.DataTableResourceDisplay;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionController extends AbstractCollectionController<ListCollection> implements DataTableResourceDisplay {

    private static final long serialVersionUID = -8283085022665254507L;

    /**
     * Threshold that defines a "big" collection.
     */
    public static final int BIG_COLLECTION_CHILDREN_COUNT = 3_000;

    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    
    private List<Long> toRemove = new ArrayList<>();
    private List<Long> toAdd = new ArrayList<>();

    public List<Long> getToRemove() {
        return toRemove;
    }


    public void setToRemove(List<Long> toRemove) {
        this.toRemove = toRemove;
    }


    public List<Long> getToAdd() {
        return toAdd;
    }


    public void setToAdd(List<Long> toAdd) {
        this.toAdd = toAdd;
    }


    /**
     * Returns a list of all resource collections that can act as candidate parents for the current resource collection.
     * 
     * @return
     */
    public List<ListCollection> getCandidateParentResourceCollections() {
        List<ListCollection> publicResourceCollections = resourceCollectionService.findPotentialParentCollections(getAuthenticatedUser(),
                getPersistable(), ListCollection.class);
        return publicResourceCollections;
    }


    @Override
    protected String save(ListCollection persistable) {
        // FIXME: may need some potential check for recursive loops here to prevent self-referential parent-child loops
        // FIXME: if persistable's parent is different from current parent; then need to reindex all of the children as well

        resourceCollectionService.saveCollectionForController(getPersistable(), getParentId(), getParentCollection(), getAuthenticatedUser(), getAuthorizedUsers(), getToAdd(),
                getToRemove(), shouldSaveResource(), generateFileProxy(getFileFileName(), getFile()), ListCollection.class, getStartTime());
        setSaveSuccessPath(getPersistable().getUrlNamespace());
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
            searchIndexService.partialIndexAllResourcesInCollectionSubTreeAsync(getPersistable());
        } else {
            searchIndexService.partialIndexAllResourcesInCollectionSubTree(getPersistable());
        }
    }

    public ListCollection getResourceCollection() {
        if (getPersistable() == null) {
            setPersistable(new ListCollection());
        }
        return getPersistable();
    }

    public void setResourceCollection(ListCollection rc) {
        setPersistable(rc);
    }

    @Override
    public Class<ListCollection> getPersistableClass() {
        return ListCollection.class;
    }

    /**
     * A hint to the view-layer that this resource collection is "big". The view-layer may choose to gracefully degrade the presentation to save on bandwidth
     * and/or
     * client resources.
     * 
     * @return
     */
    public boolean isBigCollection() {
        return (getPersistable().getUnmanagedResources().size() + getAuthorizedUsers().size()) > BIG_COLLECTION_CHILDREN_COUNT;
    }
}
