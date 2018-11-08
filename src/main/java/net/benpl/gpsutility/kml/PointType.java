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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PointType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PointType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractGeometryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}extrude" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}coordinates" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PointSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PointObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PointType", propOrder = {
        "extrude",
        "altitudeModeGroup",
        "coordinates",
        "pointSimpleExtensionGroup",
        "pointObjectExtensionGroup"
})
public class PointType
        extends AbstractGeometryType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(defaultValue = "0")
    protected Boolean extrude;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlList
    protected List<String> coordinates;
    @XmlElement(name = "PointSimpleExtensionGroup")
    protected List<Object> pointSimpleExtensionGroup;
    @XmlElement(name = "PointObjectExtensionGroup")
    protected List<AbstractObjectType> pointObjectExtensionGroup;

    /**
     * Gets the value of the extrude property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isExtrude() {
        return extrude;
    }

    /**
     * Sets the value of the extrude property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setExtrude(Boolean value) {
        this.extrude = value;
    }

    /**
     * Gets the value of the altitudeModeGroup property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link AltitudeModeEnumTypeRevised }{@code >}
     * {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    public JAXBElement<?> getAltitudeModeGroup() {
        return altitudeModeGroup;
    }

    /**
     * Sets the value of the altitudeModeGroup property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link AltitudeModeEnumTypeRevised }{@code >}
     *              {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *              {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = value;
    }

    /**
     * Gets the value of the coordinates property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the coordinates property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCoordinates().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCoordinates() {
        if (coordinates == null) {
            coordinates = new ArrayList<String>();
        }
        return this.coordinates;
    }

    /**
     * Gets the value of the pointSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pointSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPointSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getPointSimpleExtensionGroup() {
        if (pointSimpleExtensionGroup == null) {
            pointSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.pointSimpleExtensionGroup;
    }

    /**
     * Gets the value of the pointObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pointObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPointObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getPointObjectExtensionGroup() {
        if (pointObjectExtensionGroup == null) {
            pointObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.pointObjectExtensionGroup;
    }

}
