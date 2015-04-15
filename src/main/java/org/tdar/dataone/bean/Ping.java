//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.15 at 02:23:18 PM MST 
//


package org.tdar.dataone.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Store results from the :func:`MNCore.ping`
 *       method.
 * 
 * <p>Java class for Ping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Ping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="success" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="lastSuccess" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ping")
public class Ping {

    @XmlAttribute(name = "success")
    protected Boolean success;
    @XmlAttribute(name = "lastSuccess")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastSuccess;

    /**
     * Gets the value of the success property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSuccess(Boolean value) {
        this.success = value;
    }

    /**
     * Gets the value of the lastSuccess property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastSuccess() {
        return lastSuccess;
    }

    /**
     * Sets the value of the lastSuccess property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastSuccess(XMLGregorianCalendar value) {
        this.lastSuccess = value;
    }

}
