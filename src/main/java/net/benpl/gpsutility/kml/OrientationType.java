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
 * <p>Java class for OrientationType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="OrientationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}heading" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}tilt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}roll" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}OrientationSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}OrientationObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrientationType", propOrder = {
        "heading",
        "tilt",
        "roll",
        "orientationSimpleExtensionGroup",
        "orientationObjectExtensionGroup"
})
public class OrientationType
        extends AbstractObjectType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(defaultValue = "0.0")
    protected Double heading;
    @XmlElement(defaultValue = "0.0")
    protected Double tilt;
    @XmlElement(defaultValue = "0.0")
    protected Double roll;
    @XmlElement(name = "OrientationSimpleExtensionGroup")
    protected List<Object> orientationSimpleExtensionGroup;
    @XmlElement(name = "OrientationObjectExtensionGroup")
    protected List<AbstractObjectType> orientationObjectExtensionGroup;

    /**
     * Gets the value of the heading property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setHeading(Double value) {
        this.heading = value;
    }

    /**
     * Gets the value of the tilt property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getTilt() {
        return tilt;
    }

    /**
     * Sets the value of the tilt property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setTilt(Double value) {
        this.tilt = value;
    }

    /**
     * Gets the value of the roll property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getRoll() {
        return roll;
    }

    /**
     * Sets the value of the roll property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setRoll(Double value) {
        this.roll = value;
    }

    /**
     * Gets the value of the orientationSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orientationSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrientationSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getOrientationSimpleExtensionGroup() {
        if (orientationSimpleExtensionGroup == null) {
            orientationSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.orientationSimpleExtensionGroup;
    }

    /**
     * Gets the value of the orientationObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orientationObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrientationObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getOrientationObjectExtensionGroup() {
        if (orientationObjectExtensionGroup == null) {
            orientationObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.orientationObjectExtensionGroup;
    }

}
