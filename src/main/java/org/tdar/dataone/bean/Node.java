//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.15 at 02:23:18 PM MST 
//


package org.tdar.dataone.bean;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * A set of values that describe a member or coordinating
 *       node, its Internet location, and the services it supports. Several nodes
 *       may exist on a single physical device or hostname. 
 * 
 * <p>Java class for Node complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Node">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identifier" type="{http://ns.dataone.org/service/types/v1}NodeReference"/>
 *         &lt;element name="name" type="{http://ns.dataone.org/service/types/v1}NonEmptyString"/>
 *         &lt;element name="description" type="{http://ns.dataone.org/service/types/v1}NonEmptyString"/>
 *         &lt;element name="baseURL" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="services" type="{http://ns.dataone.org/service/types/v1}Services" minOccurs="0"/>
 *         &lt;element name="synchronization" type="{http://ns.dataone.org/service/types/v1}Synchronization" minOccurs="0"/>
 *         &lt;element name="nodeReplicationPolicy" type="{http://ns.dataone.org/service/types/v1}NodeReplicationPolicy" minOccurs="0"/>
 *         &lt;element name="ping" type="{http://ns.dataone.org/service/types/v1}Ping" minOccurs="0"/>
 *         &lt;element name="subject" type="{http://ns.dataone.org/service/types/v1}Subject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="contactSubject" type="{http://ns.dataone.org/service/types/v1}Subject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="replicate" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="synchronize" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="type" use="required" type="{http://ns.dataone.org/service/types/v1}NodeType" />
 *       &lt;attribute name="state" use="required" type="{http://ns.dataone.org/service/types/v1}NodeState" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Node", propOrder = {
    "identifier",
    "name",
    "description",
    "baseURL",
    "services",
    "synchronization",
    "nodeReplicationPolicy",
    "ping",
    "subject",
    "contactSubject"
})
public class Node {

    @XmlElement(required = true)
    protected NodeReference identifier;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String baseURL;
    protected Services services;
    protected Synchronization synchronization;
    protected NodeReplicationPolicy nodeReplicationPolicy;
    protected Ping ping;
    protected List<Subject> subject;
    @XmlElement(required = true)
    protected List<Subject> contactSubject;
    @XmlAttribute(name = "replicate", required = true)
    protected boolean replicate;
    @XmlAttribute(name = "synchronize", required = true)
    protected boolean synchronize;
    @XmlAttribute(name = "type", required = true)
    protected NodeType type;
    @XmlAttribute(name = "state", required = true)
    protected NodeState state;

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link NodeReference }
     *     
     */
    public NodeReference getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeReference }
     *     
     */
    public void setIdentifier(NodeReference value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the baseURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Sets the value of the baseURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseURL(String value) {
        this.baseURL = value;
    }

    /**
     * Gets the value of the services property.
     * 
     * @return
     *     possible object is
     *     {@link Services }
     *     
     */
    public Services getServices() {
        return services;
    }

    /**
     * Sets the value of the services property.
     * 
     * @param value
     *     allowed object is
     *     {@link Services }
     *     
     */
    public void setServices(Services value) {
        this.services = value;
    }

    /**
     * Gets the value of the synchronization property.
     * 
     * @return
     *     possible object is
     *     {@link Synchronization }
     *     
     */
    public Synchronization getSynchronization() {
        return synchronization;
    }

    /**
     * Sets the value of the synchronization property.
     * 
     * @param value
     *     allowed object is
     *     {@link Synchronization }
     *     
     */
    public void setSynchronization(Synchronization value) {
        this.synchronization = value;
    }

    /**
     * Gets the value of the nodeReplicationPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link NodeReplicationPolicy }
     *     
     */
    public NodeReplicationPolicy getNodeReplicationPolicy() {
        return nodeReplicationPolicy;
    }

    /**
     * Sets the value of the nodeReplicationPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeReplicationPolicy }
     *     
     */
    public void setNodeReplicationPolicy(NodeReplicationPolicy value) {
        this.nodeReplicationPolicy = value;
    }

    /**
     * Gets the value of the ping property.
     * 
     * @return
     *     possible object is
     *     {@link Ping }
     *     
     */
    public Ping getPing() {
        return ping;
    }

    /**
     * Sets the value of the ping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ping }
     *     
     */
    public void setPing(Ping value) {
        this.ping = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Subject }
     * 
     * 
     */
    public List<Subject> getSubject() {
        if (subject == null) {
            subject = new ArrayList<Subject>();
        }
        return this.subject;
    }

    /**
     * Gets the value of the contactSubject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contactSubject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContactSubject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Subject }
     * 
     * 
     */
    public List<Subject> getContactSubject() {
        if (contactSubject == null) {
            contactSubject = new ArrayList<Subject>();
        }
        return this.contactSubject;
    }

    /**
     * Gets the value of the replicate property.
     * 
     */
    public boolean isReplicate() {
        return replicate;
    }

    /**
     * Sets the value of the replicate property.
     * 
     */
    public void setReplicate(boolean value) {
        this.replicate = value;
    }

    /**
     * Gets the value of the synchronize property.
     * 
     */
    public boolean isSynchronize() {
        return synchronize;
    }

    /**
     * Sets the value of the synchronize property.
     * 
     */
    public void setSynchronize(boolean value) {
        this.synchronize = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link NodeType }
     *     
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeType }
     *     
     */
    public void setType(NodeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link NodeState }
     *     
     */
    public NodeState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeState }
     *     
     */
    public void setState(NodeState value) {
        this.state = value;
    }

}
