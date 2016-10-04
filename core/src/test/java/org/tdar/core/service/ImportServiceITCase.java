package org.tdar.core.service;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;

public class ImportServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ImportService importService;

    @Autowired
    SerializationService serializationService;

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testClone() throws Exception {
        Document document = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        Set<CoverageDate> coverageDates = newDoc.getCoverageDates();
        assertNotEmpty(coverageDates);
        document = genericService.find(Document.class, 4287L);
        Set<CoverageDate> coverageDates2 = document.getCoverageDates();
        assertNotEmpty(coverageDates2);
        assertNotEquals(coverageDates.iterator().next().getId(), coverageDates2.iterator().next().getId());
        logger.debug(serializationService.convertToXML(newDoc));
    }

    
    @Test
    @Rollback
    public void testImportWithAuthorizedUser() throws Exception {
        Document document = new Document();
        document.setTitle("test");
        document.setDescription("test description");
        document.setDocumentType(DocumentType.BOOK);
        document.getResourceCollections().add(new ResourceCollection("internal collection","intenral", SortOption.TITLE, CollectionType.INTERNAL, true, getUser()));
        document.getInternalResourceCollection().getAuthorizedUsers().add(new AuthorizedUser(new TdarUser(null, null, null, getBillingUser().getUsername()), GeneralPermissions.ADMINISTER_GROUP));
        Document newDoc = importService.bringObjectOntoSession(document, getAdminUser(), null, null, true);
        genericService.synchronize();
        Set<AuthorizedUser> authorizedUsers = newDoc.getInternalResourceCollection().getAuthorizedUsers();
        logger.debug("AU:{}",authorizedUsers);
        assertEquals(authorizedUsers.iterator().next().getUser(), getBillingUser());
    }


    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testCloneInternalCollection() throws Exception {
        Document document = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        SharedCollection rc = new SharedCollection(document,getAdminUser());
        rc.setDescription("test");
        rc.setName("name");
        rc.markUpdated(getAdminUser());
        genericService.saveOrUpdate(rc);
        document.getSharedCollections().add(rc);
        
        genericService.saveOrUpdate(document);
        logger.debug("IRC:{}",document.getInternalResourceCollection());
        Long ircid = document.getInternalResourceCollection().getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        logger.debug("oldIrId:"  + ircid.longValue() + " newIRID:"+ newDoc.getInternalResourceCollection().getId().longValue());
        Assert.assertNotEquals(ircid.longValue(), newDoc.getInternalResourceCollection().getId().longValue());
        Assert.assertEquals(newDoc.getId().longValue(), newDoc.getInternalResourceCollection().getResources().iterator().next().getId().longValue());
        logger.debug(serializationService.convertToXML(newDoc));
    }
    

    

}
