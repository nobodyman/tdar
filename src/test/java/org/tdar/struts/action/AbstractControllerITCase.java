package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.PersonalFilestoreFile;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.CodingSheetController;
import org.tdar.struts.action.resource.DatasetController;
import org.tdar.struts.action.resource.DocumentController;
import org.tdar.struts.action.resource.ImageController;
import org.tdar.struts.action.resource.OntologyController;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Preparable;

public abstract class AbstractControllerITCase extends AbstractIntegrationTestCase {

    private static final String PATH = TestConstants.TEST_ROOT_DIR;

    @Before
    public final void init() {
        init(getController());
    }

    protected abstract TdarActionSupport getController();

    @Autowired
    protected ResourceService resourceService;

    public void bookmarkResource(Resource r) {
        bookmarkResource(r, false);
    }

    public void removeBookmark(Resource r) {
        removeBookmark(r, false);
    }

    public void bookmarkResource(Resource r, boolean ajax) {
        BookmarkResourceAction bookmarkController = generateNewInitializedController(BookmarkResourceAction.class);
        logger.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
        bookmarkController.setResourceId(r.getId());
        if (ajax) {
            bookmarkController.bookmarkResourceAjaxAction();
        } else {
            bookmarkController.bookmarkResourceAction();
        }
        r = resourceService.find(r.getId());
        assertNotNull(r);
        assertTrue(r.getBookmarks().size() > 0);
    }

    public void removeBookmark(Resource r, boolean ajax) {
        BookmarkResourceAction bookmarkController = generateNewInitializedController(BookmarkResourceAction.class);
        int size = r.getBookmarks().size();
        logger.info("removing bookmark " + r.getTitle() + " (" + r.getId() + ")");
        bookmarkController.setResourceId(r.getId());
        logger.info("{}", r.getBookmarks());
        if (ajax) {
            bookmarkController.removeBookmarkAjaxAction();
        } else {
            bookmarkController.removeBookmarkAction();
        }
        r = resourceService.find(r.getId());
        assertNotNull(r);
        assertTrue(r.getBookmarks().isEmpty() || r.getBookmarks().size() == (size - 1));
    }

    public ResourceCollection generateResourceCollection(String name, String description, CollectionType type, boolean visible, List<AuthorizedUser> users, List<? extends Resource> resources, Long parentId)
            throws InstantiationException, IllegalAccessException {
        CollectionController controller = generateNewInitializedController(CollectionController.class);
        controller.prepare();
//        controller.setSessionData(getSessionData());
        logger.info("{}" , getUser());
        assertEquals(getUser(),controller.getAuthenticatedUser());
        ResourceCollection resourceCollection = controller.getResourceCollection();
        resourceCollection.setName(name);
        resourceCollection.setParent(genericService.find(ResourceCollection.class,parentId));
        controller.setParentId(parentId);
        resourceCollection.setType(type);
        resourceCollection.setVisible(visible);
        resourceCollection.setDescription(description);
        if (resources != null) {
            controller.getResources().addAll(resources);
        }

        if (users != null) {
            controller.setAuthorizedUsers(users);
        }
        resourceCollection.setSortBy(SortOption.RESOURCE_TYPE);
        String save = controller.save();
        assertTrue(save.equals(TdarActionSupport.SUCCESS));
        return resourceCollection;
    }

    Long uploadFile(String path, String name) {
        UploadController controller = generateNewInitializedController(UploadController.class);
        controller.setSessionData(getSessionData());
        controller.grabTicket();
        Long ticketId = controller.getPersonalFilestoreTicket().getId();
        logger.info("ticketId {}", ticketId);
        controller = generateNewInitializedController(UploadController.class);
        controller.setUploadFile(Arrays.asList(new File(path + "/" + name)));
        controller.setUploadFileFileName(Arrays.asList(name));
        controller.setTicketId(ticketId);
        controller.upload();
        return ticketId;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <C> C setupAndLoadResource(String filename, Class<C> cls) {
        AbstractInformationResourceController controller = null;
        Long ticketId = -1L;
        if (cls.equals(Ontology.class)) {
            controller = generateNewInitializedController(OntologyController.class);
        } else if (cls.equals(Dataset.class)) {
            controller = generateNewInitializedController(DatasetController.class);
        } else if (cls.equals(Document.class)) {
            controller = generateNewInitializedController(DocumentController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Image.class)) {
            controller = generateNewInitializedController(ImageController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(CodingSheet.class)) {
            controller = generateNewInitializedController(CodingSheetController.class);
        }
        if (controller == null)
            return null;

        controller.prepare();
        controller.getResource().setTitle(filename);
        controller.getResource().setDescription("This resource was created as a result of a test: " + getClass());
        List<File> files = new ArrayList<File>();
        List<String> filenames = new ArrayList<String>();
        File file = new File(getTestFilePath(), filename);
        assertTrue("file not found:" + getTestFilePath() + "/" + filename, file.exists());
        files.add(file);
        filenames.add(filename);
        if (ticketId != -1) {
            controller.setTicketId(ticketId);
            controller.setFileProxies(Arrays.asList(new FileProxy(filename, VersionType.UPLOADED, false)));
        } else {
            controller.setUploadedFiles(files);
            controller.setUploadedFilesFileName(filenames);
        }
        controller.save();
        return (C) controller.getResource();
    }

    protected String getTestFilePath() {
        return PATH;
    }

    public Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles) throws FileNotFoundException {
        UploadController uploadController = generateNewInitializedController(UploadController.class);
        assertEquals(TdarActionSupport.SUCCESS, uploadController.grabTicket());
        PersonalFilestoreTicket ticket = uploadController.getPersonalFilestoreTicket();
        Pair<PersonalFilestoreTicket, List<FileProxy>> toReturn = new Pair<PersonalFilestoreTicket, List<FileProxy>>(ticket, new ArrayList<FileProxy>());
        uploadController = generateNewInitializedController(UploadController.class);
        assertNull(uploadController.getTicketId());

        uploadController.setTicketId(ticket.getId());
        uploadController.setUploadFile(uploadFiles);
        for (File uploadedFile : uploadFiles) {
            uploadController.getUploadFileFileName().add(uploadedFile.getName());
            FileProxy fileProxy = new FileProxy();
            fileProxy.setFilename(uploadedFile.getName());
            fileProxy.setInputStream(new FileInputStream(uploadedFile));
            fileProxy.setAction(FileAction.ADD);
            toReturn.getSecond().add(fileProxy);
        }

        assertEquals(TdarActionSupport.SUCCESS, uploadController.upload());
        List<PersonalFilestoreFile> files = filestoreService.retrieveAllPersonalFilestoreFiles(uploadController.getTicketId());
        assertEquals(files.size(), uploadFiles.size());
        // XXX: potentially assert that md5s and/or filenames are same across both file lists
        for (PersonalFilestoreFile personalFilestoreFile : files) {
            String filename = personalFilestoreFile.getFile().getName();
            boolean equal = false;
            for (File uploadFile : uploadFiles) {
                if (filename.equals(uploadFile.getName())) {
                    equal = true;
                }
            }
            assertTrue(filename + " not found in uploadFiles: " + uploadFiles, equal);
        }
        return toReturn;
    }

    @Override
    protected Person getUser() {
        return getUser(getUserId());
    }

    @Override
    protected Long getUserId() {
        return TestConstants.USER_ID;
    }
}
