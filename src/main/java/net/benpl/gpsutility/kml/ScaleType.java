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
 * <p>Java class for ScaleType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ScaleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}xRevised" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}yRevised" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}z" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScaleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}ScaleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScaleType", propOrder = {
        "xRevised",
        "yRevised",
        "z",
        "scaleSimpleExtensionGroup",
        "scaleObjectExtensionGroup"
})
public class ScaleType
        extends AbstractObjectType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "x", defaultValue = "1.0")
    protected Double xRevised;
    @XmlElement(name = "y", defaultValue = "1.0")
    protected Double yRevised;
    @XmlElement(defaultValue = "1.0")
    protected Double z;
    @XmlElement(name = "ScaleSimpleExtensionGroup")
    protected List<Object> scaleSimpleExtensionGroup;
    @XmlElement(name = "ScaleObjectExtensionGroup")
    protected List<AbstractObjectType> scaleObjectExtensionGroup;

    /**
     * Gets the value of the xRevised property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getXRevised() {
        return xRevised;
    }

    /**
     * Sets the value of the xRevised property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setXRevised(Double value) {
        this.xRevised = value;
    }

    /**
     * Gets the value of the yRevised property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getYRevised() {
        return yRevised;
    }

    /**
     * Sets the value of the yRevised property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setYRevised(Double value) {
        this.yRevised = value;
    }

    /**
     * Gets the value of the z property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setZ(Double value) {
        this.z = value;
    }

    /**
     * Gets the value of the scaleSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scaleSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScaleSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getScaleSimpleExtensionGroup() {
        if (scaleSimpleExtensionGroup == null) {
            scaleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.scaleSimpleExtensionGroup;
    }

    /**
     * Gets the value of the scaleObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scaleObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScaleObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getScaleObjectExtensionGroup() {
        if (scaleObjectExtensionGroup == null) {
            scaleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.scaleObjectExtensionGroup;
    }

}