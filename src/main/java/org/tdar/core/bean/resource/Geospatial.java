package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Indexed;

/**
 * $Id$
 * <p>
 * (What kind of files are allowed to be Images?
 * </p>
 * 
 * @author Adam Brin
 * @version $Revision: 543$
 */
@Entity
@Indexed
@Table(name = "geospatial")
@XmlRootElement(name = "geospatial")
public class Geospatial extends Dataset {

    /**
     * 
     */
    private static final long serialVersionUID = -7898164729934403851L;

    public Geospatial() {
        setResourceType(ResourceType.GEOSPATIAL);
    }

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String currentnessUpdateNotes;

    @Column(name = "spatial_reference_system", length = 50)
    private String spatialReferenceSystem;

    @Column(name = "projection", length = 50)
    private String projection;

    @Column(name = "scale", length = 100)
    private String scale;

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }

    @Override
    @Transient
    public boolean isHasBrowsableImages() {
        return true;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getSpatialReferenceSystem() {
        return spatialReferenceSystem;
    }

    public void setSpatialReferenceSystem(String spatialReferenceSystem) {
        this.spatialReferenceSystem = spatialReferenceSystem;
    }

    public String getCurrentnessUpdateNotes() {
        return currentnessUpdateNotes;
    }

    public void setCurrentnessUpdateNotes(String currentnessUpdateNotes) {
        this.currentnessUpdateNotes = currentnessUpdateNotes;
    }

    @Override
    public boolean isValidForController() {
        // if (StringUtils.isEmpty(getSpatialReferenceSystem())) {
        // throw new TdarValidationException("A spatial reference system is required for this " + getResourceType());
        // }
        // if (StringUtils.isEmpty(getProjection())) {
        // throw new TdarValidationException("A projection is required for this " + getResourceType());
        // }
        return super.isValidForController();
    }

}