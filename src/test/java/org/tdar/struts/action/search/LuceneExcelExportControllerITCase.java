package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.web.SessionData;

@Transactional
public class LuceneExcelExportControllerITCase extends AbstractSearchControllerITCase {
	
	//the first few rows of the export have stats, column names, spacing, yada yada...
	private static final int EXCEL_EXPORT_HEADER_ROWCOUNT = 5; 

    @Autowired
    private AdvancedSearchController controller;

    @Autowired
    SearchIndexService searchIndexService;
    
    @Autowired
    ExcelService excelService;

    private Person currentUser = null;

    @Test
    @Rollback(true)
    public void testExcelExport() throws InstantiationException, IllegalAccessException, ParseException, FileNotFoundException, IOException, InvalidFormatException {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        currentUser = getBasicUser();
        controller.setSessionData(new SessionData()); // create unauthenticated session
        getServletRequest().setAttribute("RequestURI", "http://www.test.com");
        controller = generateNewInitializedController(AdvancedSearchController.class);

        controller.setServletRequest(getServletRequest());
        doSearch("");
        assertEquals(TdarActionSupport.SUCCESS, controller.viewExcelReport());
        assertFalse(controller.getSearchPhrase() + " should not have bold tag", controller.getSearchPhrase().toLowerCase().contains("<b>"));
        File tempFile = File.createTempFile("report", ".xls");
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        long copyLarge = IOUtils.copyLarge(controller.getInputStream(), fileOutputStream);
        
        fileOutputStream.close();
        logger.debug("tempFile: {}", tempFile);

        Workbook workbook = WorkbookFactory.create(new FileInputStream(tempFile));
        Sheet sheet = workbook.getSheet("results");
        Assert.assertEquals(TdarConfiguration.getInstance().getSearchExcelExportRecordMax(), sheet.getLastRowNum() - EXCEL_EXPORT_HEADER_ROWCOUNT);
    }

    @Test
    @Rollback(true)
    public void testExcelFailUnauthenticatedExport() throws InstantiationException, IllegalAccessException, ParseException, FileNotFoundException, IOException {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        currentUser = null;
        controller.setSessionData(new SessionData()); // create unauthenticated session
        getServletRequest().setAttribute("RequestURI", "http://www.test.com");
        controller = generateNewInitializedController(AdvancedSearchController.class);

        controller.setServletRequest(getServletRequest());
        doSearch("");
        assertEquals(TdarActionSupport.UNAUTHORIZED, controller.viewExcelReport());
        setIgnoreActionErrors(true);
    }

    @Override
    public Person getSessionUser() {
        return currentUser;
    }

}
