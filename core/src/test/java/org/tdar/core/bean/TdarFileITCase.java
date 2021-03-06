package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.FileProcessingDao;
import org.tdar.core.service.GenericService;

public class TdarFileITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private FileProcessingDao fileProcessingDao;

    @Test
    @Rollback
    public void testFileSearch() {
        BillingAccount act = new BillingAccount();
        act.setName("test");
        act.markUpdated(getAdminUser());
        genericService.saveOrUpdate(act);
        // setup two files with similar names
        TdarFile file = new TdarFile("atest.pdf", getAdminUser(), act);
        TdarFile file2 = new TdarFile("test.pdf", getAdminUser(), act);
        // and a third file in a directory
        TdarDir dir = new TdarDir();
        dir.setFilename("cookie jar");
        dir.setDateCreated(new Date());
        dir.setUploader(getAdminUser());
        dir.setAccount(act);
        genericService.saveOrUpdate(dir);
        TdarFile file3 = new TdarFile("cookies.pdf", getAdminUser(), act);
        genericService.saveOrUpdate(file);
        genericService.saveOrUpdate(file2);
        file3.setParent(dir);
        genericService.saveOrUpdate(file3);
        List<AbstractFile> list = fileProcessingDao.listFilesFor(null, act, "test", null, getAdminUser());
        for (AbstractFile f : list) {
            logger.debug("{} - {}", f.getName(), f);
        }
        assertFalse(list.contains(file3));
        assertTrue(list.contains(file));
        assertTrue(list.contains(file2));
        list = fileProcessingDao.listFilesFor(null, null, "ook", null, getAdminUser());
        for (AbstractFile f : list) {
            logger.debug("{} - {}", f.getName(), f);
        }
        assertTrue(list.contains(file3));
        assertFalse(list.contains(file));
        assertFalse(list.contains(file2));
    }

}
