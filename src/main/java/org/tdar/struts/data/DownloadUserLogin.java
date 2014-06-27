package org.tdar.struts.data;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.external.RecaptchaService;

public class DownloadUserLogin extends UserLogin {

    public DownloadUserLogin(RecaptchaService recaptchaService) {
        super(recaptchaService);
    }

    private static final long serialVersionUID = -6157970041213328371L;
    private InformationResourceFileVersion version;
    private InformationResource resource;

    public InformationResourceFileVersion getVersion() {
        return version;
    }

    public void setVersion(InformationResourceFileVersion version) {
        this.version = version;
    }

    public InformationResource getResource() {
        return resource;
    }

    public void setResource(InformationResource resource) {
        this.resource = resource;
    }

    public String getReturnUrl() {
        String versionid = "";
        if (Persistable.Base.isNotNullOrTransient(version)) {
            versionid = version.getId().toString();
        }
        String resourceid = "";
        if (Persistable.Base.isNotNullOrTransient(resource)) {
            resourceid = resource.getId().toString();
        }
        return String.format("/download/download?informationResourceFileVersionId=%s&resourceId=%s", versionid, resourceid);
    }
    
    public String getFailureUrl() {
        return getReturnUrl();
    }
}
