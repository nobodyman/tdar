package org.tdar.struts.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.collection.AbstractCollectionController;
import org.tdar.struts.action.collection.AbstractCollectionRightsController;
import org.tdar.struts.action.collection.ListCollectionController;
import org.tdar.struts.action.collection.ListCollectionRightsController;
import org.tdar.struts.action.collection.ShareCollectionController;
import org.tdar.struts.action.collection.ShareCollectionRightsController;
import org.tdar.utils.PersistableUtils;

import com.google.common.base.Objects;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public interface TestResourceCollectionHelper {

    Logger logger_ = LoggerFactory.getLogger(TestResourceCollectionHelper.class);

    default SharedCollection generateResourceCollection(String name, String description, boolean visible, List<AuthorizedUser> users,
            List<? extends Resource> resources, Long parentId)
            throws Exception {
        return generateResourceCollection(name, description, visible, users, getUser(), resources, parentId);
    }

    TdarUser getUser();

    default SharedCollection generateResourceCollection(String name, String description, boolean visible, List<AuthorizedUser> users,
            TdarUser owner, List<? extends Resource> resources, Long parentId) throws Exception {
        return generateResourceCollection(name, description, visible, users, owner, resources, parentId, ShareCollectionController.class,
                SharedCollection.class);
    }

    @SuppressWarnings("deprecation")
    default <C extends HierarchicalCollection, D extends AbstractCollectionController> C generateResourceCollection(String name, String description,
            boolean visible, List<AuthorizedUser> users,
            TdarUser owner, List<? extends Resource> resources, Long parentId, Class<D> ctlClss, Class<C> cls) throws Exception {
        D controller = generateNewInitializedController(ctlClss, owner);
        controller.setServletRequest(getServletPostRequest());

        // controller.setSessionData(getSessionData());
        logger_.info("{}", getUser());
        assertEquals(controller.getAuthenticatedUser(), owner);
        C resourceCollection = (C) controller.getResourceCollection();
        resourceCollection.setName(name);

        controller.setAsync(false);
        resourceCollection.setHidden(!visible);
        resourceCollection.setDescription(description);
        if (CollectionUtils.isNotEmpty(resources)) {
            if (controller instanceof ShareCollectionController) {
                ((ShareCollectionController) controller).getToAdd().addAll(PersistableUtils.extractIds(resources));
            }
            if (controller instanceof ListCollectionController) {
                ((ListCollectionController) controller).getToAdd().addAll(PersistableUtils.extractIds(resources));
            }
        }

        if (parentId != null) {
            controller.setParentId(parentId);
        }

        if (resourceCollection instanceof CustomizableCollection) {
            ((CustomizableCollection) resourceCollection).setSortBy(SortOption.RESOURCE_TYPE);
        }
        controller.setServletRequest(getServletPostRequest());

        // A better replication of the struts lifecycle would include calls to prepare() and validate(), however, this
        // method currently generates resources that would ultimately generate ActionErrors, as well as Constraint
        // Violation errors. To fix this, we should make the following changes:

        // FIXME: remove actionError checks from controller.execute() methods (they are implicitly performed by struts and/or our test runner),
        // FIXME: improve generateResourceCollection() so that it constructs valid resources (vis a vis validator.validate() and dao.enforceValidation())
        controller.prepare();
        controller.validate();

        String save = controller.save();
        assertTrue(save.equals(Action.SUCCESS));
        getGenericService().synchronize();
        Long id = resourceCollection.getId();
        logger_.debug("{}", resourceCollection.getAuthorizedUsers());
        getGenericService().evictFromCache(resourceCollection);

        if (users != null) {
            AbstractCollectionRightsController sc = generateNewInitializedController(ShareCollectionRightsController.class, owner);
            if (controller instanceof ListCollectionController) {
                sc = generateNewInitializedController(ListCollectionRightsController.class, owner);
            }
            sc.setId(id);
            sc.prepare();
            sc.edit();
            Iterator<UserRightsProxy> iterator = sc.getProxies().iterator();
            while (iterator.hasNext()) {
                UserRightsProxy proxy = iterator.next();
                if (!Objects.equal(proxy.getId(), owner.getId())) {
                    iterator.remove();
                }
            }
            for (AuthorizedUser au : users) {
                sc.getProxies().add(new UserRightsProxy(au));
            }
            assertTrue(sc.save().equals(Action.SUCCESS));
            getGenericService().synchronize();
        }

        resourceCollection = null;
        resourceCollection = getGenericService().find(cls, id);
        logger_.debug("parentId: {}", parentId);
        logger_.debug("Resources: {}", resources);
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            assertEquals(parentId, resourceCollection.getParent().getId());
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            if (resourceCollection instanceof SharedCollection) {
                assertThat(((SharedCollection) resourceCollection).getResources(), containsInAnyOrder(resources.toArray()));
            }
            if (resourceCollection instanceof ListCollection) {
                assertThat(((ListCollection) resourceCollection).getUnmanagedResources(), containsInAnyOrder(resources.toArray()));
            }
        }
        return resourceCollection;
    }

    GenericService getGenericService();

    HttpServletRequest getServletPostRequest();

    <D extends ActionSupport> D generateNewInitializedController(Class<D> ctlClss, TdarUser owner);

}
