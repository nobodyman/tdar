package org.tdar.core.bean.resource.sensory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.SensoryData;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Entity
@Table(name = "sensory_data_image")
@XStreamAlias("sensoryDataImage")
public class SensoryDataImage extends Persistable.Sequence<SensoryDataImage> implements HasResource<SensoryData> {
    private static final long serialVersionUID = -9115746507586171584L;

    @Column(nullable = false)
    private String filename;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "sensory_data_id")
    private SensoryData resource;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(filename);
    }

    @XmlTransient
    public SensoryData getResource() {
        return resource;
    }

    public void setResource(SensoryData resource) {
        this.resource = resource;
    }

    @Override
    public boolean isValidForController() {
        return true;
    }
}
