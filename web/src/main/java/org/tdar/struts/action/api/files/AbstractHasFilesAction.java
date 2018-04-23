package org.tdar.struts.action.api.files;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public abstract class AbstractHasFilesAction<C extends AbstractFile> extends AbstractJsonApiAction {

    private static final long serialVersionUID = -6840830466386793366L;
    private List<Long> ids = new ArrayList<>();
    private List<C> files = new ArrayList<>();
    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        for (Long id : ids) {
            files.add((C)getGenericService().find(AbstractFile.class, id));
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (files.size() != ids.size()) {
            addActionError("moveFileAction.not_all_files_valid");
        }
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<C> getFiles() {
        return files;
    }

    public void setFiles(List<C> files) {
        this.files = files;
    }

    

}
