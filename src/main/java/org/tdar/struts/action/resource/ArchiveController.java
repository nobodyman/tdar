package org.tdar.struts.action.resource;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.TdarActionException;

/**
 * Wraps an archive file, such as a zip file or a tarball.
 * 
 * @author Martin Paulo
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/archive")
public class ArchiveController extends AbstractInformationResourceController<Archive> {

    private static final long serialVersionUID = -8015985036235809392L;

    @Override
    protected String save(Archive persistable) throws TdarActionException {
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        handleUploadedFiles();
        return SUCCESS;
    }

    @Override
    public Class<Archive> getPersistableClass() {
        return Archive.class;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.ARCHIVE);
    }

    public void setArchive(final Archive archive) {
        setPersistable(archive);
    }

    public Archive getArchive() {
        return getPersistable();
    }

    @Override
    public Archive getResource() {
        if (getPersistable() == null)
            setPersistable(createPersistable());
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

}