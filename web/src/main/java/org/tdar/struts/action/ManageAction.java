package org.tdar.struts.action;

import com.opensymphony.xwork2.Preparable;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.*;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.AdhocShare;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

import java.io.IOException;
import java.util.*;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata
 * (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("")
@Component
@Scope("prototype")
public class ManageAction extends AbstractAuthenticatableAction implements DataTableResourceDisplay, Preparable {

    private static final long serialVersionUID = 5576550365349636811L;
    private List<Resource> filteredFullUserProjects;
    private List<Resource> fullUserProjects;
    private List<SharedCollection> allResourceCollections = new ArrayList<>();
    private List<SharedCollection> sharedResourceCollections = new ArrayList<>();
    private List<InternalCollection> internalCollections = new ArrayList<>();
    private AdhocShare adhocShare = new AdhocShare();
    
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ProjectService projectService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient BillingAccountService billingAccountService;

    private List<Project> allSubmittedProjects;
    private String statusData;
    private String resourceTypeData;


    //TODO: get rid of this execute method. We are going to break out separate actions for sharing a colection and sharing a resource, which will extend from this class and implement their own execute() method.
    @Override
    @Action(value = "manage", results = { @Result(name = SUCCESS, location = "dashboard/manage.ftl") })
    public String execute() throws SolrServerException, IOException {

        return SUCCESS;
    }

    @Override
    public void validate() {

        //fixme: this seems unecessary for actions in the "secured" package - doesn't the interceptor authenticate the current user?
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            addActionError(getText("dashboardController.user_must_login"));
        }
        super.validate();
    }

    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public void setFullUserProjects(List<Resource> projects) {
        fullUserProjects = projects;
    }

    public void setAllSubmittedProjects(List<Project> projects) {
        allSubmittedProjects = projects;
    }

    public void setFilteredFullUserProjects(List<Resource> projects) {
        filteredFullUserProjects = projects;
    }

    public void setEditableProjects(Set<Resource> projects) {
        editableProjects = projects;
    }

    public List<Resource> getFilteredFullUserProjects() {
        return filteredFullUserProjects;
    }

    private Set<Resource> editableProjects = new HashSet<>();

    public Set<Resource> getEditableProjects() {
        return editableProjects;
    }

    public void prepare() {
       internalCollections = resourceCollectionService.findAllInternalCollections(getAuthenticatedUser());
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public List<ResourceType> getResourceTypes() {
        return resourceService.getAllResourceTypes();
    }

    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @DoNotObfuscate(reason = "not needed / performance test")
    public List<SharedCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<SharedCollection> resourceCollections) {
        this.allResourceCollections = resourceCollections;
    }

    /**
     * @return the sharedResourceCollections
     */
    @DoNotObfuscate(reason = "not needed / performance test")
    public List<SharedCollection> getSharedResourceCollections() {
        return sharedResourceCollections;
    }

    /**
     * @param sharedResourceCollections
     *            the sharedResourceCollections to set
     */
    public void setSharedResourceCollections(List<SharedCollection> sharedResourceCollections) {
        this.sharedResourceCollections = sharedResourceCollections;
    }

    public String getResourceTypeData() {
        return resourceTypeData;
    }

    public void setResourceTypeData(String resourceTypeData) {
        this.resourceTypeData = resourceTypeData;
    }

    public String getStatusData() {
        return statusData;
    }

    public void setStatusData(String statusData) {
        this.statusData = statusData;
    }


    @Override
    public boolean isRightSidebar() {
        return true;
    }

    public List<InternalCollection> getInternalCollections() {
        return internalCollections;
    }

    public void setInternalCollections(List<InternalCollection> internalCollections) {
        this.internalCollections = internalCollections;
    }

    public AdhocShare getAdhocShare() {
        return adhocShare;
    }

    public void setAdhocShare(AdhocShare adhocShare) {
        this.adhocShare = adhocShare;
    }

    public List<BillingAccount> getBillingAccounts() {
        List<BillingAccount> accts = new ArrayList<>();
        accts.addAll(billingAccountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));
        return accts;
    }

}
