package org.tdar.struts.action.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;

public class CollectionControllerITCase extends AbstractControllerITCase implements TestResourceCollectionHelper {

    @Autowired
    private GenericService genericService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    ResourceCollectionController controller;

    static int indexCount = 0;

    @Before
    public void setup() {
        controller = generateNewInitializedController(ResourceCollectionController.class);
        if (indexCount < 1) {
            reindex();
        }
        indexCount++;
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testPublicCollection() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        String email = "abc" + currentTimeMillis + "@ab.com";
        final TdarUser testPerson = createAndSaveNewPerson(email, "" + currentTimeMillis);
        String name = "test collection";
        String description = "test description";

        InformationResource normal = createAndSaveDocumentWithFileAndUseDefaultUser();
        InformationResource draft = createAndSaveDocumentWithFileAndUseDefaultUser();
        final Long normalId = normal.getId();
        final Long draftId = draft.getId();
        draft.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(draft);
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.ADD_TO_COLLECTION)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft));
        ResourceCollection collection = generateResourceCollection(name, description, false, users, testPerson, new ArrayList<>(), null);

        final Long id = collection.getId();
        String slug = collection.getSlug();
        collection = null;
        collection = null;
        resources = null;
        normal = null;
        draft = null;
        genericService.synchronize();
        collection = genericService.find(ResourceCollection.class, id);
        normal = genericService.find(InformationResource.class, normalId);
        normal.getUnmanagedResourceCollections().add(collection);
        draft = genericService.find(InformationResource.class, draftId);
        draft.getUnmanagedResourceCollections().add(collection);
        // collection.getUnmanagedResources().addAll(resources);
        // genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(normal);
        genericService.saveOrUpdate(draft);

        // -----------------------
        ResourceCollectionRightsController cc = generateNewInitializedController(ResourceCollectionRightsController.class, getAdminUser());
        cc.setId(id);
        cc.prepare();
        cc.edit();
        cc.setServletRequest(getServletPostRequest());
        cc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));
        // cc.setAsync(false);
        cc.save();
        cc = null;
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, normal, Permissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testPerson, draft, Permissions.MODIFY_RECORD));
        assertFalse(authenticationAndAuthorizationService.canViewResource(testPerson, draft));
        assertTrue(authenticationAndAuthorizationService.canViewResource(testPerson, normal));
        /*
        */
    }

    @Test
    @Rollback
    public void testResourceShareController() throws Exception {
        TdarUser testPerson = createAndSaveNewPerson("a@basda.com", "1234");
        String name = "test collection";
        String description = "test description";

        InformationResource generateInformationResourceWithFile = generateDocumentWithUser();
        InformationResource generateInformationResourceWithFile2 = generateDocumentWithUser();
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>(Arrays.asList(
                new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD),
                new AuthorizedUser(getAdminUser(), testPerson, Permissions.MODIFY_RECORD)));
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(generateInformationResourceWithFile, generateInformationResourceWithFile2));
        ResourceCollection collection = generateResourceCollection(name, description, true, users, getUser(), resources, null,
                ResourceCollectionController.class, ResourceCollection.class);
        Long collectionid = collection.getId();
        logger.info("{}", collection.getManagedResources());
        assertFalse(collectionid.equals(-1L));
        collection = null;
        ResourceCollection foundCollection = genericService.find(ResourceCollection.class, collectionid);
        assertNotNull(foundCollection);
        assertEquals(3, foundCollection.getAuthorizedUsers().size());
        assertEquals(2, foundCollection.getManagedResources().size());

        assertEquals(name, foundCollection.getName());
        assertEquals(description, foundCollection.getDescription());
        assertEquals(SortOption.RESOURCE_TYPE, foundCollection.getSortBy());

        assertTrue(foundCollection.getManagedResources().contains(generateInformationResourceWithFile2));
        assertTrue(foundCollection.getManagedResources().contains(generateInformationResourceWithFile));

        int count = 0;
        for (AuthorizedUser user : foundCollection.getAuthorizedUsers()) {
            if (user.getUser().equals(testPerson)) {
                count++;
                assertEquals(Permissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getAdminUser())) {
                count++;
                assertEquals(Permissions.MODIFY_RECORD, user.getGeneralPermission());
            }
            if (user.getUser().equals(getBasicUser())) {
                count++;
                assertEquals(Permissions.ADMINISTER_COLLECTION, user.getGeneralPermission());
            }
        }
        assertEquals(3, count);
    }

}
