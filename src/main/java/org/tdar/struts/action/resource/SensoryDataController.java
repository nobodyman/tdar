package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.ScannerTechnologyType;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.struts.action.TdarActionException;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/sensory-data")
public class SensoryDataController extends AbstractInformationResourceController<SensoryData> {

    private static final long serialVersionUID = -7329500931137726805L;

    private List<SensoryDataImage> sensoryDataImages;
    private List<SensoryDataScan> sensoryDataScans;

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        super.loadCustomMetadata();
         sensoryDataScans = new ArrayList<SensoryDataScan>(getPersistable().getSensoryDataScans());
         sensoryDataImages = new ArrayList<SensoryDataImage>(getPersistable().getSensoryDataImages());
         Collections.sort(sensoryDataImages);
         Collections.sort(sensoryDataScans);
         loadScannerInformationResourceFile();
    }
    
    private void loadScannerInformationResourceFile() {
        for(InformationResourceFile irfile : getSensoryData().getInformationResourceFiles()) {
            if(irfile.getFileName().endsWith("xls") || irfile.getFileName().endsWith("xlsx")) {
                getSensoryData().setScannerInformationResourceFile(irfile);
                break;
            }
        }
    }

    /**
     * Save basic metadata of the registering concept.
     * 
     * @param concept
     */
    @Override
    protected String save(SensoryData sensoryData) {
        saveBasicResourceMetadata();
        saveInformationResourceProperties();

        saveCustomMetadata();

        getGenericService().saveOrUpdate(sensoryData);
        handleUploadedFiles();
        getGenericService().saveOrUpdate(sensoryData);
        return SUCCESS;
    }

    private void saveCustomMetadata() {
        Persistable.Sequence.applySequence(getSensoryDataImages());
        getResourceService().saveHasResources(getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, getSensoryDataImages(),
                getPersistable().getSensoryDataImages(), SensoryDataImage.class);
        Persistable.Sequence.applySequence(getSensoryDataScans());
        getResourceService().saveHasResources(getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, getSensoryDataScans(),
                getPersistable().getSensoryDataScans(), SensoryDataScan.class);

    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public SensoryData getSensoryData() {
        return getPersistable();
    }

    @Override
    public Collection<String> getValidFileExtensions() {
        List<String> validExtensions = new ArrayList<String>();
        validExtensions.addAll(analyzer.getExtensionsForTypes(ResourceType.SENSORY_DATA, ResourceType.CODING_SHEET));
        return validExtensions;
    }

    public List<SensoryDataImage> getSensoryDataImages() {
        if (sensoryDataImages == null)
            sensoryDataImages = new ArrayList<SensoryDataImage>();
        return sensoryDataImages;
    }

    public void setSensoryDataImages(List<SensoryDataImage> sensoryDataImages) {
        this.sensoryDataImages = sensoryDataImages;
    }

    public List<SensoryDataScan> getSensoryDataScans() {
        if (sensoryDataScans == null)
            sensoryDataScans = new ArrayList<SensoryDataScan>();
        return sensoryDataScans;
    }

    public void setSensoryDataScans(List<SensoryDataScan> sensoryDataScans) {
        this.sensoryDataScans = sensoryDataScans;
    }

    public List<SensoryDataScan> getBlankSensoryDataScan() {
        List<SensoryDataScan> blanklist = new ArrayList<SensoryDataScan>();
        blanklist.add(new SensoryDataScan());
        return blanklist;
    }

    public List<SensoryDataImage> getBlankSensoryDataImage() {
        List<SensoryDataImage> blanklist = new ArrayList<SensoryDataImage>();
        blanklist.add(new SensoryDataImage());
        return blanklist;
    }

    public List<ScannerTechnologyType> getScannerTechnologyTypes() {
        return Arrays.asList(ScannerTechnologyType.values());
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public void setSensoryData(SensoryData sensoryData) {
        setPersistable(sensoryData);
    }

    public Class<SensoryData> getPersistableClass() {
        return SensoryData.class;
    }

    @Override
    public SensoryData getResource() {
        if (getPersistable() == null)
            setPersistable(createPersistable());
        return getPersistable();
    }

}
