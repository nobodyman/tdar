package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;

/**
 * $Id$
 * 
 * 
 * @author Jim deVos
 * @version $Rev$
 */
@Service
public class DownloadService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    PdfService pdfService;

    @Autowired
    DatasetDao datasetDao;

    @Autowired
    GenericDao genericDao;

    // TODO
    private String slugify(InformationResource resource) {
        return "ir-archive";
    }

    public void generateZipArchive(Map<File, String> files, File destinationFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(destinationFile);
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout)); // what is apache ZipOutputStream? It's probably better.
        for (Entry<File, String> entry : files.entrySet()) {
            String filename = entry.getValue();
            if (filename == null) {
                filename = entry.getKey().getName();
            }
            ZipEntry zentry = new ZipEntry(filename);
            zout.putNextEntry(zentry);
            FileInputStream fin = new FileInputStream(entry.getKey());
            logger.debug("adding to archive: {}", entry.getKey());
            IOUtils.copy(fin, zout);
            IOUtils.closeQuietly(fin);
        }
        IOUtils.closeQuietly(zout);
    }

    public void generateZipArchive(InformationResource resource, File destinationFile) throws IOException {
        Collection<File> files = new LinkedList<File>();
        for (InformationResourceFile irf : resource.getActiveInformationResourceFiles()) {
            for (InformationResourceFileVersion file : irf.getLatestVersions()) {
                files.add(TdarConfiguration.getInstance().getFilestore().retrieveFile(file));
            }
        }
    }

    public void generateZipArchive(InformationResource resource) throws IOException {
        generateZipArchive(resource, File.createTempFile(slugify(resource), ".zip", TdarConfiguration.getInstance().getTempDirectory()));
    }

    @Transactional
    public void handleDownload(Person authenticatedUser, DownloadHandler dh, InformationResourceFileVersion... irFileVersions) throws TdarActionException {
        if (ArrayUtils.isEmpty((irFileVersions))) {
            throw new TdarRecoverableRuntimeException("unsupported action");
        }

        Map<File, String> files = new HashMap<>();
        for (InformationResourceFileVersion irFileVersion : irFileVersions) {
            InformationResource informationResource = datasetDao.findInformationResourceByFileVersionId(irFileVersion.getId());
            InformationResourceFile irFile = datasetDao.findInformationResourceFileByFileVersionId(irFileVersion.getId());
            irFileVersion.setInformationResourceFileId(irFile.getId());
            irFileVersion.setInformationResourceId(informationResource.getId());
            addFileToDownload(files, authenticatedUser, informationResource, irFile, irFileVersion);
            if (!irFileVersion.isDerivative()) {
                FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
                genericDao.save(stat);
                if (irFileVersions.length > 1) {
                    initDispositionPrefix(irFile.getInformationResourceFileType(), dh);
                } else {
                    initDispositionPrefix(FileType.FILE_ARCHIVE, dh);
                }
            }
        }

        try {
            File resourceFile = null;
            String mimeType = null;
            if (irFileVersions.length > 1) {
                resourceFile = File.createTempFile("archiveDownload", ".zip", TdarConfiguration.getInstance().getTempDirectory());
                generateZipArchive(files, resourceFile);
                mimeType = "application/zip";
                // although in temp, it might be quite large, so let's not leave it lying around
                dh.setInputStream(new DeleteOnCloseFileInputStream(resourceFile));
            } else {
                mimeType = irFileVersions[0].getMimeType();
                resourceFile = (File) files.keySet().toArray()[0];
                dh.setInputStream(new FileInputStream(resourceFile));
            }
            dh.setFileName(resourceFile.getName());
            dh.setContentLength(resourceFile.length());
            dh.setContentType(mimeType);
            logger.debug("downloading file:" + resourceFile.getCanonicalPath());
        } catch (FileNotFoundException ex) {
            logger.error("Could not generate zip file to download: file not found", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, "Could not generate zip file to download");
        } catch (IOException ex) {
            logger.error("Could not generate zip file to download: IO exeption", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, "Could not generate zip file to download");
        }
    }

    private void addFileToDownload(Map<File, String> downloadMap, Person authenticatedUser, InformationResource informationResource, InformationResourceFile irFile, InformationResourceFileVersion irFileVersion)
            throws TdarActionException {
        File resourceFile = null;
        try {
            resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(irFileVersion);
        } catch (FileNotFoundException e1) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }
        if (resourceFile == null || !resourceFile.exists()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }

        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (irFileVersion.getExtension().equalsIgnoreCase("PDF")) {
            try {
                // this will be in the temp directory, so it will be scavenged at some stage.
                resourceFile = pdfService.mergeCoverPage(authenticatedUser, informationResource, irFile, irFileVersion);
            } catch (PdfCoverPageGenerationException cpge) {
                logger.trace("Error occurred while merging cover page onto " + irFileVersion, cpge);
            } catch (Exception e) {
                logger.error("Error occurred while merging cover page onto " + irFileVersion, e);
            }
        }
        downloadMap.put(resourceFile, irFileVersion.getFilename());
    }

    // indicate in the header whether the file should be received as an attachment (e.g. give user download prompt)
    private void initDispositionPrefix(InformationResourceFile.FileType fileType, DownloadHandler dh) {
        if (InformationResourceFile.FileType.IMAGE != fileType) {
            dh.setDispositionPrefix("attachment;");
        }
    }

}