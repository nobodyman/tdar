package org.tdar.struts.action.bulk;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.bulk.BulkUploadService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.ResourceSaveControllerService;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@HttpsOnly
@Scope("prototype")
@Namespace("/bulk")
public class BulkUploadController extends AbstractInformationResourceController<Image> {

    private static final long serialVersionUID = -6419692259588266839L;

    @Autowired
    private transient FileAnalyzer analyzer;

    @Autowired
    private transient BulkUploadService bulkUploadService;

    @Autowired
    private transient ResourceSaveControllerService resourceSaveControllerService;

    private String bulkFileName;
    private long bulkContentLength;

    protected String bulkUploadSave() throws TdarActionException {
        getGenericService().markReadOnly(getPersistable());
        saveBasicResourceMetadata();
        Status oldStatus = getPersistable().getStatus();
        getPersistable().setStatus(Status.DELETED);
        getLogger().info("saving batches...");
        getPersistable().setStatus(oldStatus);
        if (PersistableUtils.isNullOrTransient(getTicketId())) {
            addActionError(getText("bulkUploadController.no_files"));
            return INPUT;
        }

        // getLogger().debug("ticketId: {} ", getTicketId());
        // getLogger().debug("proxy: {}", getFileProxies());
        // getLogger().info("{} and names {}", getUploadedFiles(), getUploadedFilesFileName());
        //
        AuthWrapper<InformationResource> auth = new AuthWrapper<InformationResource>(getImage(), isAuthenticated(), getAuthenticatedUser(), isEditor());
        //
        // fsw.setBulkUpload(isBulkUpload());
        // fsw.setFileProxies(getFileProxies());
        // fsw.setTextInput(false);
        // fsw.setMultipleFileUploadEnabled(isMultipleFileUploadEnabled());
        // fsw.setTicketId(getTicketId());
        // fsw.setUploadedFilesFileName(getUploadedFilesFileName());
        // fsw.setUploadedFiles(getUploadedFiles());
        getGenericService().detachFromSession(getPersistable());
        Collection<FileProxy> fileProxiesToProcess = resourceSaveControllerService.getFileProxiesToProcess(auth, this, fsw, null);
        getLogger().trace("Received fileproxies: {}", fileProxiesToProcess);
        if(getLogger().isTraceEnabled()) {
            for(FileProxy proxy : fileProxiesToProcess) {
                getLogger().trace("\tproxy:{}", proxy);
            }
        }

        setupAccountForSaving();
        getCreditProxies().clear();
        getGenericService().detachFromSession(getAuthenticatedUser());
        // getGenericService().detachFromSession(getPersistable().getResourceCollections());
        // for (ResourceCreator rc : image.getResourceCreators()) {
        // getLogger().debug("resourceCreators:{} {}", rc, rc.getId());
        // }

        getLogger().info("running asyncronously");
        bulkUploadService.saveAsync(getPersistable(), getAuthenticatedUser().getId(), getTicketId(), fileProxiesToProcess, getAccountId());
        setPersistable(null);
        // } else {
        // getLogger().info("running inline");
        // bulkUploadService.save(getPersistable(), getAuthenticatedUser().getId(), getTicketId(), fileProxiesToProcess, getAccountId());
        // setPersistable(null);
        // }
        // setPersistable(null);
        return SUCCESS_ASYNC;
    }

    private void addAsyncHtmlErrors(List<String> htmlAsyncErrors) {
        if (CollectionUtils.isNotEmpty(htmlAsyncErrors)) {
            for (String error : htmlAsyncErrors) {
                addActionError(error);
            }
        }
    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public Image getImage() {
        return getPersistable();
    }

    public void setImage(Image image) {
        setPersistable(image);
    }

    @Override
    public Collection<String> getValidFileExtensions() {
        return getExtensionsForType(ResourceType.getTypesSupportingBulkUpload());
    }

    @Override
    public boolean shouldSaveResource() {
        return false;
    }

    public void setBulkContentLength(long bulkContentLength) {
        this.bulkContentLength = bulkContentLength;
    }

    public long getBulkContentLength() {
        return bulkContentLength;
    }

    public void setBulkFileName(String bulkFileName) {
        this.bulkFileName = bulkFileName;
    }

    public String getBulkFileName() {
        return bulkFileName;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    @Override
    public Class<Image> getPersistableClass() {
        return Image.class;
    }

    @Override
    public boolean isBulkUpload() {
        return true;
    }

    @Override
    protected void postSaveCleanup(String returnString) {
        // don't clean up personal filestore -- we have called async methods that need access to them and will handle cleanup.
    }

}
