package org.tdar.web.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.exception.StatusCode;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
public class ThumbnailWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    public static HashMap<String, String> docValMap = new HashMap<String, String>();
    public static HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();
    public static String PROJECT_ID_FIELDNAME = "projectId";
    public static String IMAGE_TITLE_FIELDNAME = "image.title";
    public static String DESCRIPTION_FIELDNAME = "image.description";
    public static final String TEST_IMAGE_NAME = "handbook_of_archaeology.jpg";
    public static final String TEST_IMAGE = TestConstants.TEST_IMAGE_DIR + TEST_IMAGE_NAME;

    public static String PROJECT_ID = "2";
    public static String IMAGE_TITLE = "a thumb test";
    public static String DESCRIPTION = "this is a test";

    public static String REGEX_IMAGE_VIEW = "\\/image\\/\\d+\\/(.+)$";

    @SuppressWarnings("unused")
    @Test
    // create image as confidential, then log out and see if we see the image.
    public void testThumbnailOnViewPage() {

        // simulate an async file upload
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TEST_IMAGE);

        // ADMIN
        createImageAndAddConfidentialFile(ticketId);

        // the logged in creator should be able to see the image
        String path = internalPage.getUrl().getPath().toLowerCase();
        assertTrue("expecting to be on view page. Actual path:" + path, path.matches(REGEX_IMAGE_VIEW));
        logger.trace("source of view page: {}", getPageCode());
        assertTextPresent(RESTRICTED_ACCESS_TEXT);
        String viewPage = path;
        path = path.substring(0, path.lastIndexOf("/"));
        String editPage = path + "/edit";
        logger.debug("view:" + viewPage);
        logger.debug("edit:" + editPage);
        logger.trace(getPageText());
        // we're dealing with a confidential file, should not be there
        assertTextNotPresent("/img/sm");
        String pageCode = getPageCode();
        Pattern p = Pattern.compile("/filestore/(\\d+)/(\\d+)(/?)");
        Matcher m = p.matcher(pageCode);
        List<Long> irFileIds = new ArrayList<Long>();
        List<Long> irFileVersionIds = new ArrayList<Long>();
        while (m.find()) {
            // logger.info(m.group(1));
            irFileIds.add(Long.parseLong(m.group(1)));
            irFileVersionIds.add(Long.parseLong(m.group(2)));
        }

        // ONCE WE LOG OUT THE THUMBNAIL SHOULDN'T BE PRESENT BECAUSE THE RESOURCE IS CONFIDENTIAL
        logout();
        gotoPage(viewPage);
        assertTextNotPresent("/img/sm");

        // LOG IN, BUT AS A USER THAT SHOULDN'T HAVE RIGHTS TO THE RESOURCE. NO THUMBNAIL.
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPage(viewPage);
        assertTextNotPresent("/img/sm");

        assertDeniedAccess(irFileIds, irFileVersionIds);

        logout();
        // LOGIN, CHANGE FROM CONFIDENTIAL TO PUBLIC THEN LOGOUT... WE SHOULD SEE THE THUMBNAIL
        makeFilePublic(editPage);

        gotoPage(viewPage);
        assertTextNotPresent("/img/sm");

        assertLoginPrompt(irFileIds, irFileVersionIds);

        // LOG IN, AS A USER THAT SHOULD HAVE RIGHTS TO THE RESOURCE THUMBNAIL.
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPage(viewPage);
        // not present because not showing only one thumbnail
        assertTextNotPresent("/img/sm");
        assertTextPresentInCode("/filestore/");

        assertAllowedToViewIRVersionIds(irFileIds, irFileVersionIds);

        logout();

        makeFileEmbargoed(editPage);
        gotoPage(viewPage);
        assertTextNotPresent("/img/sm");

        assertLoginPrompt(irFileIds, irFileVersionIds);
        logger.debug(getCurrentUrlPath());
        gotoPage(editPage);
        // LOG IN, BUT AS A USER THAT SHOULDN'T HAVE RIGHTS TO THE RESOURCE. NO THUMBNAIL.
        logger.debug(getCurrentUrlPath());
        int statusCode = login(CONFIG.getUsername(), CONFIG.getPassword(), true);
        logger.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.debug(getCurrentUrlPath());
        logger.debug("statusCode: {} ", statusCode);
        logger.debug(getPageCode());
        logger.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(), statusCode);
        // FIXME: change from Gone->Forbidden changed how tDAR responds and thus
        // redirects to a different page... current URL is null?
        logger.trace(getPageText());
        assertTrue(getPageText().contains("Unauthorized") || getPageText().contains("Forbidden")); // we can be on the "edit" page with an error message
        logger.info(getPageText());
        assertFalse(statusCode == 200); // make sure we have a "bad" status code though
        gotoPage(viewPage);
        assertTextNotPresent("/img/sm");

        Long imageId = extractTdarIdFromCurrentURL();

        assertDeniedAccess(irFileIds, irFileVersionIds);

    }

    private void makeFileEmbargoed(String editPage) {
        // NOW MAKE THE PAGE EMBARGED -- THE THUMBNAIL SHOULD NOT BE VISIBLE
        loginAdmin();
        gotoPage(editPage);
        setInput("fileProxies[0].action", FileAction.MODIFY_METADATA.name());
        setInput("fileProxies[0].restriction", FileAccessRestriction.EMBARGOED_FIVE_YEARS.name());
        submitForm();
        logout();
    }

    private void makeFilePublic(String editPage) {
        loginAdmin();
        gotoPage(editPage);
        setInput("fileProxies[0].action", FileAction.MODIFY_METADATA.name());
        setInput("fileProxies[0].restriction", FileAccessRestriction.PUBLIC.name());
        submitForm();
        logout();
    }

    private void createImageAndAddConfidentialFile(String ticketId) {
        gotoPage("/image/add");
        setInput(PROJECT_ID_FIELDNAME, PROJECT_ID);
        setInput("ticketId", ticketId);
        setInput(IMAGE_TITLE_FIELDNAME, IMAGE_TITLE);
        setInput(DESCRIPTION_FIELDNAME, DESCRIPTION);
        setInput("image.date", "1984");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        // FIXME: need to create input
        addFileProxyFields(0, FileAccessRestriction.CONFIDENTIAL, TEST_IMAGE_NAME);
        // setInput("resourceAvailability", "Public");
        submitForm();
    }

    @Test
    public void testImageGalleryConfidentialityRules() {
        // if user uploads confidential image:
        // only users with view access should be able to see image in image galery

    }

    public void assertDeniedAccess(List<Long> irFileIds, List<Long> irFileVersionIds) {
        for (int i = 0; i < irFileIds.size(); i++) {
            String pth = getBaseUrl() + "/filestore/" + irFileIds.get(i) + "/" + irFileVersionIds.get(i);
            int status = gotoPageWithoutErrorCheck(pth);
            logger.info(pth + ":" + status + " -" + getCurrentUrlPath());
            assertEquals("should not be allowed", 403, status);
        }
    }

    public void assertLoginPrompt(List<Long> irFileIds, List<Long> irFileVersionIds) {
        for (int i = 0; i < irFileIds.size(); i++) {
            String pth = getBaseUrl() + "/filestore/" + irFileIds.get(i) + "/" + irFileVersionIds.get(i);
            int status = gotoPageWithoutErrorCheck(pth);
            logger.info(pth + ":" + status + " -" + getCurrentUrlPath());
            assertFalse("Should always be a login prompt", getCurrentUrlPath().equals(pth));
        }
    }

    public void assertAllowedToViewIRVersionIds(List<Long> irFileIds, List<Long> irFileVersionIds) {
        for (int i = 0; i < irFileIds.size(); i++) {
            String pth = getBaseUrl() + "/filestore/" + irFileIds.get(i) + "/" + irFileVersionIds.get(i);
            int status = gotoPageWithoutErrorCheck(pth);
            logger.info(pth + ":" + status + " -" + getCurrentUrlPath());
            assertEquals("Should always be allowed", 200, status);
        }
    }

    // TODO: add tests for hacked urls (e.g. using /image/xyx/thumnail where xyz is restricted and/or is not actually a thumbnail)

}
