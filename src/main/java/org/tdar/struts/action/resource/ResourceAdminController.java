package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DateGranularity;
import org.tdar.struts.data.UsageStats;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class ResourceAdminController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -2071449250711089300L;
    public static final String ADMIN = "admin";
    private List<ResourceRevisionLog> resourceLogEntries;

    private List<AggregateViewStatistic> usageStatsForResources = new ArrayList<>();
    private Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<>();
    private List<ResourceRevisionLog> logEntries;

    private Resource resource;
    private Long id;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private XmlService xmlService;

    @Action(value = ADMIN, results = {
            @Result(name = SUCCESS, location = "../resource/admin.ftl")
    })
    public String viewAdmin() throws TdarActionException {
        setResourceLogEntries(resourceService.getLogsForResource(resource));
        setUsageStatsForResources(resourceService.getUsageStatsForResources(DateGranularity.WEEK, new Date(0L), new Date(), 1L,
                Arrays.asList(resource.getId())));
        if (resource instanceof InformationResource) {
            int i = 0;
            for (InformationResourceFile file : ((InformationResource) resource).getInformationResourceFiles()) {
                i++;
                getDownloadStats().put(String.format("%s. %s", i, file.getFilename()),
                        resourceService.getAggregateDownloadStatsForFile(DateGranularity.WEEK, new Date(0L), new Date(), 1L, file.getId()));
            }
        }
        return SUCCESS;
    }


    @Override
    public void prepare() throws Exception {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            resource = resourceService.find(getId());
        } else {
            addActionError(getText("resourceAdminController.valid_resource_required"));
        }
    }
    
    public String getJsonStats() {
        String json = "null";
        // FIXME: what is the goal of this null check; shouldn't the UsageStats object handle this? Also, why bail if only one is null?
        if ((usageStatsForResources == null) || (downloadStats == null)) {
            return json;
        }

        try {
            json = xmlService.convertToJson(new UsageStats(usageStatsForResources, downloadStats));
        } catch (IOException e) {
            getLogger().error("failed to convert stats to json", e);
            json = String.format("{'error': '%s'}", StringEscapeUtils.escapeEcmaScript(e.getMessage()));
        }
        return json;
    }


    public List<ResourceRevisionLog> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<ResourceRevisionLog> logEntries) {
        this.logEntries = logEntries;
    }

    public List<ResourceRevisionLog> getResourceLogEntries() {
        return resourceLogEntries;
    }

    public void setResourceLogEntries(List<ResourceRevisionLog> resourceLogEntries) {
        this.resourceLogEntries = resourceLogEntries;
    }

    public List<AggregateViewStatistic> getUsageStatsForResources() {
        return usageStatsForResources;
    }

    public void setUsageStatsForResources(List<AggregateViewStatistic> usageStatsForResources) {
        this.usageStatsForResources = usageStatsForResources;
    }

    public Map<String, List<AggregateDownloadStatistic>> getDownloadStats() {
        return downloadStats;
    }

    public void setDownloadStats(Map<String, List<AggregateDownloadStatistic>> downloadStats) {
        this.downloadStats = downloadStats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
