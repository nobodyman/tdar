package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.geospatial.GeospatialController;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonIdNameFilter;
import org.tdar.web.service.ResourceEditControllerService;

import com.opensymphony.xwork2.Preparable;

public class ResourceEditAction<P extends Persistable & Addressable> extends AbstractAuthenticatableAction implements Preparable {

    @Autowired
    private SerializationService serializationService;
    @Autowired
    private ResourceEditControllerService editService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private GenericService genericService;

    private static final long serialVersionUID = 1467806719681708555L;

    private Long id;
    private Resource resource = new Resource();
    private String json = "";
    private ArrayList fileProxies;
    private List<TdarFile> tdarFiles;
    private List<Long> fileIds;

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(id)) {
            resource = getGenericService().find(Resource.class, id);
        }
        json = serializationService.convertToJson(resource);
        
        setTdarFiles(getGenericService().findAll(TdarFile.class, getFileIds()));
//        for (TdarFile file : getTdarFiles()) {
//            if (file.getAccount() != null) {
//                setAccountId(file.getAccount().getId());
//            }
//        }

    }

    @Override
    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "edit.ftl") })
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getInvestigationTypes() throws IOException {
        List<InvestigationType> types = genericService.findAll(InvestigationType.class);
        return serializationService.convertToJson(types);
    }

    public String getSubmitter() throws IOException {
        if (resource != null) {
            return serializationService.convertToJson(resource.getSubmitter());
        }
        return "";
    }

    public String getActiveAccounts() throws IOException {
        String result = "";
        ArrayList<BillingAccount> activeAccounts = new ArrayList<>(editService.determineActiveAccounts(getAuthenticatedUser(), resource));
        getLogger().debug("{}", activeAccounts);
        result = serializationService.convertToFilteredJson(activeAccounts, JsonIdNameFilter.class);
        return result;
    }

    public String getMaterialTypes() throws IOException {
        List<MaterialKeyword> types = genericKeywordService.findAllApproved(MaterialKeyword.class);
        return serializationService.convertToJson(types);
    }


    private void initializeFileProxies() {
        fileProxies = new ArrayList<>();
        if (resource instanceof InformationResource) {
            for (InformationResourceFile informationResourceFile : ((InformationResource)resource).getInformationResourceFiles()) {
                if (!informationResourceFile.isDeleted()) {
                    fileProxies.add(new FileProxy(informationResourceFile));
                }
            }
        }

        for (TdarFile file : getTdarFiles()) {
            fileProxies.add(new FileProxy(file));
        }
    }


    public String getFileUploadSettings() {
        initializeFileProxies();
        FileUploadSettings settings = new FileUploadSettings();
        settings.setAbleToUpload(true);
//        if (this instanceof DatasetController) {
//            settings.setDataTableEnabled(true);
//        }
//        if (this instanceof GeospatialController) {
//            settings.setSideCarOnly(true);
//        }
        
        getLogger().debug("proxies: {} ({})", fileProxies, fileProxies.size());
        settings.getFiles().addAll(fileProxies);
        settings.setMultipleUpload(true);
        settings.setMaxNumberOfFiles(getMaxUploadFilesPerRecord());
        settings.setResourceId(getId());
//        settings.setTicketId(getTicketId());
        settings.setUserId(getAuthenticatedUser().getId());
        settings.getValidFormats().addAll(Arrays.asList("pdf", "doc","docx"));
//        settings.getRequiredOptionalPairs().addAll(getRequiredOptionalPairs());
        try {
            return serializationService.convertToJson(settings);
        } catch (Throwable t) {
            getLogger().error("{}", t, t);
            return "{}";
        }
    }

    public String getFilesJson() {
        if (resource instanceof InformationResource) {
        String filesJson = editService.loadFilesJson((InformationResource) resource);
        return filesJson;
        } 
        return null;
    }

    public List<TdarFile> getTdarFiles() {
        return tdarFiles;
    }

    public void setTdarFiles(List<TdarFile> tdarFiles) {
        this.tdarFiles = tdarFiles;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
}