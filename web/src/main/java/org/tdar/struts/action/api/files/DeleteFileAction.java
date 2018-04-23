package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class DeleteFileAction extends AbstractJsonApiAction {


    private static final long serialVersionUID = -7706315527740653556L;
    private Long fileId;
    private AbstractFile file;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (fileId != null) {
            file = getGenericService().find(AbstractFile.class, fileId);
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (file == null) {
            addActionError("deleteFileAction.no_file");
        }
    }

    @Action(value = "delete",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.deleteFile(file, getAuthenticatedUser());
        setResultObject(true);
        return SUCCESS;
    }

}
