/*
 * Copyright 2018 Ben Peng
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1-b171012.0423 
//         See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.11.07 at 12:28:20 PM UTC 
//


package net.benpl.gpsutility.kml;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for LargeMailUserType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="LargeMailUserType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressLine" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="LargeMailUserName" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="Code" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;anyAttribute namespace='##other'/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LargeMailUserIdentifier" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/&gt;
 *                 &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="Indicator" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;anyAttribute namespace='##other'/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="BuildingName" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}BuildingNameType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Department" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostBox" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Thoroughfare" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostalCode" minOccurs="0"/&gt;
 *         &lt;any namespace='##other' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LargeMailUserType", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", propOrder = {
        "addressLine",
        "largeMailUserName",
        "largeMailUserIdentifier",
        "buildingName",
        "department",
        "postBox",
        "thoroughfare",
        "postalCode",
        "any"
})
public class LargeMailUserType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "AddressLine")
    protected List<AddressLine> addressLine;
    @XmlElement(name = "LargeMailUserName")
    protected List<LargeMailUserType.LargeMailUserName> largeMailUserName;
    @XmlElement(name = "LargeMailUserIdentifier")
    protected LargeMailUserType.LargeMailUserIdentifier largeMailUserIdentifier;
    @XmlElement(name = "BuildingName")
    protected List<BuildingNameType> buildingName;
    @XmlElement(name = "Department")
    protected Department department;
    @XmlElement(name = "PostBox")
    protected PostBox postBox;
    @XmlElement(name = "Thoroughfare")
    protected Thoroughfare thoroughfare;
    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(name = "Type")
    protected String type;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the addressLine property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addressLine property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddressLine().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddressLine }
     */
    public List<AddressLine> getAddressLine() {
        if (addressLine == null) {
            addressLine = new ArrayList<AddressLine>();
        }
        return this.addressLine;
    }

    /**
     * Gets the value of the largeMailUserName property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the largeMailUserName property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLargeMailUserName().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LargeMailUserType.LargeMailUserName }
     */
    public List<LargeMailUserType.LargeMailUserName> getLargeMailUserName() {
        if (largeMailUserName == null) {
            largeMailUserName = new ArrayList<LargeMailUserType.LargeMailUserName>();
        }
        return this.largeMailUserName;
    }

    /**
     * Gets the value of the largeMailUserIdentifier property.
     *
     * @return possible object is
     * {@link LargeMailUserType.LargeMailUserIdentifier }
     */
    public LargeMailUserType.LargeMailUserIdentifier getLargeMailUserIdentifier() {
        return largeMailUserIdentifier;
    }

    /**
     * Sets the value of the largeMailUserIdentifier property.
     *
     * @param value allowed object is
     *              {@link LargeMailUserType.LargeMailUserIdentifier }
     */
    public void setLargeMailUserIdentifier(LargeMailUserType.LargeMailUserIdentifier value) {
        this.largeMailUserIdentifier = value;
    }

    /**
     * Gets the value of the buildingName property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the buildingName property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBuildingName().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BuildingNameType }
     */
    public List<BuildingNameType> getBuildingName() {
        if (buildingName == null) {
            buildingName = new ArrayList<BuildingNameType>();
        }
        return this.buildingName;
    }

    /**
     * Gets the value of the department property.
     *
     * @return possible object is
     * {@link Department }
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Sets the value of the department property.
     *
     * @param value allowed object is
     *              {@link Department }
     */
    public void setDepartment(Department value) {
        this.department = value;
    }

    /**
     * Gets the value of the postBox property.
     *
     * @return possible object is
     * {@link PostBox }
     */
    public PostBox getPostBox() {
        return postBox;
    }

    /**
     * Sets the value of the postBox property.
     *
     * @param value allowed object is
     *              {@link PostBox }
     */
    public void setPostBox(PostBox value) {
        this.postBox = value;
    }

    /**
     * Gets the value of the thoroughfare property.
     *
     * @return possible object is
     * {@link Thoroughfare }
     */
    public Thoroughfare getThoroughfare() {
        return thoroughfare;
    }

    /**
     * Sets the value of the thoroughfare property.
     *
     * @param value allowed object is
     *              {@link Thoroughfare }
     */
    public void setThoroughfare(Thoroughfare value) {
        this.thoroughfare = value;
    }

    /**
     * Gets the value of the postalCode property.
     *
     * @return possible object is
     * {@link PostalCode }
     */
    public PostalCode getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     *
     * @param value allowed object is
     *              {@link PostalCode }
     */
    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

    /**
     * Gets the value of the any property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and
     * the value is the string value of the attribute.
     * <p>
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected         content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/&gt;
     *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="Indicator" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;anyAttribute namespace='##other'/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "content"
    })
    public static class LargeMailUserIdentifier
            implements Serializable {

        private final static long serialVersionUID = 1L;
        @XmlValue
        protected String content;
        @XmlAttribute(name = "Type")
        protected String type;
        @XmlAttribute(name = "Indicator")
        @XmlSchemaType(name = "anySimpleType")
        protected String indicator;
        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the content property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the value of the content property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setContent(String value) {
            this.content = value;
        }

        /**
         * Gets the value of the type property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the indicator property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIndicator() {
            return indicator;
        }

        /**
         * Sets the value of the indicator property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIndicator(String value) {
            this.indicator = value;
        }

        /**
         * Gets the value of the code property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         *
         * <p>
         * the map is keyed by the name of the attribute and
         * the value is the string value of the attribute.
         * <p>
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         *
         * @return always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected         content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="Code" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;anyAttribute namespace='##other'/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "content"
    })
    public static class LargeMailUserName
            implements Serializable {

        private final static long serialVersionUID = 1L;
        @XmlValue
        protected String content;
        @XmlAttribute(name = "Type")
        protected String type;
        @XmlAttribute(name = "Code")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the content property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the value of the content property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setContent(String value) {
            this.content = value;
        }

        /**
         * Gets the value of the type property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the code property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         *
         * <p>
         * the map is keyed by the name of the attribute and
         * the value is the string value of the attribute.
         * <p>
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         *
         * @return always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}
