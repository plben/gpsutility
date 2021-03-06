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
 * <p>Java class for PlacemarkType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PlacemarkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractGeometryGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PlacemarkSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PlacemarkObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlacemarkType", propOrder = {
        "abstractGeometryGroup",
        "placemarkSimpleExtensionGroup",
        "placemarkObjectExtensionGroup"
})
public class PlacemarkType
        extends AbstractFeatureType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElementRef(name = "AbstractGeometryGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractGeometryType> abstractGeometryGroup;
    @XmlElement(name = "PlacemarkSimpleExtensionGroup")
    protected List<Object> placemarkSimpleExtensionGroup;
    @XmlElement(name = "PlacemarkObjectExtensionGroup")
    protected List<AbstractObjectType> placemarkObjectExtensionGroup;

    /**
     * Gets the value of the abstractGeometryGroup property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiTrackType }{@code >}
     * {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link TrackType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     */
    public JAXBElement<? extends AbstractGeometryType> getAbstractGeometryGroup() {
        return abstractGeometryGroup;
    }

    /**
     * Sets the value of the abstractGeometryGroup property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     *              {@link JAXBElement }{@code <}{@link MultiTrackType }{@code >}
     *              {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     *              {@link JAXBElement }{@code <}{@link PointType }{@code >}
     *              {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     *              {@link JAXBElement }{@code <}{@link TrackType }{@code >}
     *              {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     *              {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     *              {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     */
    public void setAbstractGeometryGroup(JAXBElement<? extends AbstractGeometryType> value) {
        this.abstractGeometryGroup = value;
    }

    /**
     * Gets the value of the placemarkSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placemarkSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlacemarkSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getPlacemarkSimpleExtensionGroup() {
        if (placemarkSimpleExtensionGroup == null) {
            placemarkSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.placemarkSimpleExtensionGroup;
    }

    /**
     * Gets the value of the placemarkObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the placemarkObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlacemarkObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getPlacemarkObjectExtensionGroup() {
        if (placemarkObjectExtensionGroup == null) {
            placemarkObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.placemarkObjectExtensionGroup;
    }

}
