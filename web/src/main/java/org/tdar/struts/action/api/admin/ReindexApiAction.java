package org.tdar.struts.action.api.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

/**
 * Creates a method to reindex a resource or collection of resources via an API
 * 
 * @author abrin
 *
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/admin/")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
@HttpsOnly
public class ReindexApiAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 6402649954856208287L;
    private List<Long> ids = new ArrayList<>();
    private Long collectionId;

    @Autowired
    private SearchIndexService searchIndexService;

    @Override
    public void prepare() {

    }

    @Action(value = "reindex", results = {
            @Result(name = SUCCESS, type = JSONRESULT)
    })
    @PostOnly
    @HttpForbiddenErrorResponseOnly
    public String execute() throws SearchIndexException, IOException {
        if (PersistableUtils.isNotNullOrTransient(getCollectionId())) {
            ResourceCollection c = getGenericService().find(ResourceCollection.class, getCollectionId());
            searchIndexService.index(c);
            searchIndexService.indexAllResourcesInCollectionSubTree(c);
        }

        if (!CollectionUtils.isEmpty(getIds())) {
            List<Resource> resources = getGenericService().findAll(Resource.class, getIds());
            searchIndexService.indexCollection(resources);
        }
        return SUCCESS;

    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

}
