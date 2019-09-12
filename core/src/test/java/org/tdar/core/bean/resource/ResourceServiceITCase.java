package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.core.service.resource.ResourceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolationException;

public class ResourceServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceService resourceService;

    @Test
    @Rollback
    public void testSaveHasResource() throws InstantiationException, IllegalAccessException {
        Resource doc = generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());
    }

    @Test
    @Rollback
    public void testSaveHasResourceExistingNull() throws InstantiationException, IllegalAccessException {

        Resource doc = generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        doc.setCoverageDates(null);
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());

    }

    @Test
    @Rollback
    public void testSaveHasResourceIncomingNulLCase() throws InstantiationException, IllegalAccessException {

        Resource doc = generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = null;
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());
    }

    @Test
    @Rollback
    public void testRevisionLogEntryMessage() {
        String nah = "Nah ";
        int repeatCount = FieldLength.FIELD_LENGTH_8196 / nah.length();
        String message = StringUtils.repeat(nah, repeatCount);
        resourceService.logResourceModification(null, getUser(), message, RevisionLogType.REQUEST);
    }


    @Test(expected = ConstraintViolationException.class)
    @Rollback
    public void testRevisionLogEntryWithOversizeMessage() {
        String nah = "Nah ";
        int repeatCount = FieldLength.FIELD_LENGTH_8196 / nah.length();
        String message = StringUtils.repeat(nah, repeatCount) + "BATMAN!!!!!!!!!!!!";
        resourceService.logResourceModification(null, getUser(), message, RevisionLogType.REQUEST);
    }
}
