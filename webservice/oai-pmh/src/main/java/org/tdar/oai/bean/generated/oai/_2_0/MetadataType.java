//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.11 at 04:55:08 PM CDT 
//


package org.tdar.oai.bean.generated.oai._2_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.tools.ant.IntrospectionHelper.Creator;
import org.purl.dc.terms.ElementOrRefinementContainer;
import org.tdar.core.bean.resource.Resource;

import edu.asu.lib.dc.DublinCoreDocument;
import edu.asu.lib.mods.ModsDocument;
import edu.asu.lib.qdc.QualifiedDublinCoreDocument;
import gov.loc.mods.v3.ModsType;


/**
 * Metadata must be expressed in XML that complies
 *        with another XML Schema (namespace=#other). Metadata must be 
 *        explicitly qualified in the response.
 * 
 * <p>Java class for metadataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="metadataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any namespace='##other'/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metadataType", propOrder = {
    "any"
})
@XmlSeeAlso(value={Resource.class, Creator.class, DublinCoreDocument.class, ModsDocument.class, ModsType.class, QualifiedDublinCoreDocument.class, ElementOrRefinementContainer.class})
public class MetadataType {

    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
