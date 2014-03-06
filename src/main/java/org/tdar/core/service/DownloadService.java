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
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;

import com.opensymphony.xwork2.TextProvider;

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
    private PdfService pdfService;

    @Autowired
    private GenericService genericService;

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

    @Transactional
    public void handleDownload(Person authenticatedUser, DownloadHandler dh, Long informationResourceId, InformationResourceFileVersion... irFileVersions) throws TdarActionException {
        if (ArrayUtils.isEmpty((irFileVersions))) {
            throw new TdarRecoverableRuntimeException("error.unsupported_action");
        }

        Map<File, String> files = new HashMap<>();
        String mimeType = null;
        String fileName = null;
        for (InformationResourceFileVersion irFileVersion : irFileVersions) {
            if (irFileVersion.getInformationResourceFile().isDeleted()) {
                continue;
            }
            addFileToDownload(dh, files, authenticatedUser, irFileVersion, dh.isCoverPageIncluded());
            if (!irFileVersion.isDerivative()) {
                InformationResourceFile irFile = irFileVersion.getInformationResourceFile();
                // don't count download stats if you're downloading your own stuff
                if (!Persistable.Base.isEqual(irFile.getInformationResource().getSubmitter(), authenticatedUser) && !dh.isEditor()) {
                    FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
                    genericService.save(stat);
                }
                if (irFileVersions.length > 1) {
                    initDispositionPrefix(FileType.FILE_ARCHIVE, dh);
                    fileName = String.format("files-%s.zip",informationResourceId);

                    mimeType = "application/zip";
                } else {
                    initDispositionPrefix(irFile.getInformationResourceFileType(), dh);
                    mimeType = irFileVersions[0].getMimeType();
                    fileName = irFileVersions[0].getFilename();
                }
            }
        }

        try {
            File resourceFile = null;
            if (irFileVersions.length > 1) {
                resourceFile = File.createTempFile("archiveDownload", ".zip", TdarConfiguration.getInstance().getTempDirectory());
                generateZipArchive(files, resourceFile);
                // although in temp, it might be quite large, so let's not leave it lying around
                dh.setInputStream(new DeleteOnCloseFileInputStream(resourceFile));
            } else {
                resourceFile = (File) files.keySet().toArray()[0];
                dh.setInputStream(new FileInputStream(resourceFile));
            }
            dh.setFileName(fileName);
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

    private void addFileToDownload(TextProvider provider, Map<File, String> downloadMap, Person authenticatedUser, InformationResourceFileVersion irFileVersion, boolean includeCoverPage)
            throws TdarActionException {
        File resourceFile = null;
        try {
            resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(ObjectType.RESOURCE, irFileVersion);
        } catch (FileNotFoundException e1) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }
        if (resourceFile == null || !resourceFile.exists()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }

        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (irFileVersion.getExtension().equalsIgnoreCase("PDF") && includeCoverPage) {
            try {
                // this will be in the temp directory, so it will be scavenged at some stage.
                resourceFile = pdfService.mergeCoverPage(provider, authenticatedUser, irFileVersion);
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