package org.tdar.core.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.tdar.utils.json.JsonIdNameFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * Marks all classes that can be persisted to the database via our ORM.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface Persistable extends Serializable {

    @JsonView({ JsonIdNameFilter.class })
    Long getId();

    void setId(Long number);

    /**
     * Returns the list of property objects used for equality comparison and
     * hashCode generation.
     */
    /**
     * By default, base the hashcode off of object's inherent hashcode.
     */
    @XmlTransient
    public default List<?> getEqualityFields() {
        return Collections.emptyList();
    }

}
