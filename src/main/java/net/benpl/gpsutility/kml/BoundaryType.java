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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for BoundaryType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="BoundaryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LinearRing" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}BoundarySimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}BoundaryObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundaryType", propOrder = {
        "linearRing",
        "boundarySimpleExtensionGroup",
        "boundaryObjectExtensionGroup"
})
public class BoundaryType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "LinearRing")
    protected LinearRingType linearRing;
    @XmlElement(name = "BoundarySimpleExtensionGroup")
    protected List<Object> boundarySimpleExtensionGroup;
    @XmlElement(name = "BoundaryObjectExtensionGroup")
    protected List<AbstractObjectType> boundaryObjectExtensionGroup;

    /**
     * Gets the value of the linearRing property.
     *
     * @return possible object is
     * {@link LinearRingType }
     */
    public LinearRingType getLinearRing() {
        return linearRing;
    }

    /**
     * Sets the value of the linearRing property.
     *
     * @param value allowed object is
     *              {@link LinearRingType }
     */
    public void setLinearRing(LinearRingType value) {
        this.linearRing = value;
    }

    /**
     * Gets the value of the boundarySimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundarySimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundarySimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getBoundarySimpleExtensionGroup() {
        if (boundarySimpleExtensionGroup == null) {
            boundarySimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.boundarySimpleExtensionGroup;
    }

    /**
     * Gets the value of the boundaryObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundaryObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundaryObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getBoundaryObjectExtensionGroup() {
        if (boundaryObjectExtensionGroup == null) {
            boundaryObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.boundaryObjectExtensionGroup;
    }

}