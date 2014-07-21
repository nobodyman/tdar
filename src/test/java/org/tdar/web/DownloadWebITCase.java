package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;

public class DownloadWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String DOWNLOAD_REGISTRATION = "downloadRegistration";
    private String url;

    @Before
    public void uploadFile() {
        createDocumentAndUploadFile("my first document", null);
        url = getCurrentUrlPath();
        logout();
    }
    
    @Test
    public void testDownloadLoginFailed() {
        gotoPage(url);
        DomNodeList<DomNode> all = htmlPage.getDocumentElement().querySelectorAll(".download-file");
        clickLinkWithText(all.get(0).getTextContent().trim());
        
        setInput("downloadUserLogin.loginUsername", getAdminUsername());
        // wrong on purpose
        setInput("downloadUserLogin.loginPassword", getAdminUsername());
        submitFormWithoutErrorCheck("submit");
        assertTextPresent("Please check that your username and password were entered correctly");
        checkForFreemarkerExceptions();

        setInput("downloadUserLogin.h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        // try successfully
        setInput("downloadUserLogin.loginUsername", getAdminUsername());
        // wrong on purpose
        setInput("downloadUserLogin.loginPassword", getAdminPassword());
        submitForm("submit");
        logger.debug(webClient.getCurrentWindow().getEnclosedPage().getWebResponse().getContentType());
        assertCurrentUrlContains("filestore/confirm");
    }

    @Test
    public void testDownloadRegistrationFailed() {
        gotoPage(url);
        DomNodeList<DomNode> all = htmlPage.getDocumentElement().querySelectorAll(".download-file");
        clickLinkWithText(all.get(0).getTextContent().trim());
        
        setInput(DOWNLOAD_REGISTRATION + ".person.username", getAdminUsername());
        // wrong on purpose
        setInput(DOWNLOAD_REGISTRATION + ".password", getAdminUsername());
        setInput(DOWNLOAD_REGISTRATION + ".h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        submitFormWithoutErrorCheck("submitAction");
        checkForFreemarkerExceptions();
        assertTextPresent("This email address is not valid");
        assertTextPresent("You must accept the Terms of Service");
        assertTextNotPresent("Could not authenticate at this time");
        
        Map<String, String> personmap = new HashMap<>();
        setupBasicUser(personmap , "downloadWebTest",DOWNLOAD_REGISTRATION);
        personmap.put(DOWNLOAD_REGISTRATION +".acceptTermsOfUse", "true");
        deleteUser(personmap.get(DOWNLOAD_REGISTRATION + ".person.username"));
        personmap.remove("downloadRegistration.contributorReason");
        personmap.remove("downloadRegistration.person.phone");
        for (String key : personmap.keySet()) {
            setInput(key, personmap.get(key));
        }
        submitForm("submitAction");
        
        // complete
        logger.debug(webClient.getCurrentWindow().getEnclosedPage().getWebResponse().getContentType());
        assertCurrentUrlContains("filestore/confirm");
    }

}
