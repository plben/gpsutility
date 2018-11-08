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
 * <p>Java class for IconStyleType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="IconStyleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractColorStyleType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}scaleRevised" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}heading" minOccurs="0"/&gt;
 *         &lt;element name="Icon" type="{http://www.opengis.net/kml/2.2}BasicLinkType" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}hotSpot" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}IconStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}IconStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IconStyleType", propOrder = {
        "scaleRevised",
        "heading",
        "icon",
        "hotSpot",
        "iconStyleSimpleExtensionGroup",
        "iconStyleObjectExtensionGroup"
})
public class IconStyleType
        extends AbstractColorStyleType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "scale", defaultValue = "1.0")
    protected Double scaleRevised;
    @XmlElement(defaultValue = "0.0")
    protected Double heading;
    @XmlElement(name = "Icon")
    protected BasicLinkType icon;
    protected Vec2Type hotSpot;
    @XmlElement(name = "IconStyleSimpleExtensionGroup")
    protected List<Object> iconStyleSimpleExtensionGroup;
    @XmlElement(name = "IconStyleObjectExtensionGroup")
    protected List<AbstractObjectType> iconStyleObjectExtensionGroup;

    /**
     * Gets the value of the scaleRevised property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getScaleRevised() {
        return scaleRevised;
    }

    /**
     * Sets the value of the scaleRevised property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setScaleRevised(Double value) {
        this.scaleRevised = value;
    }

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
     * Gets the value of the icon property.
     *
     * @return possible object is
     * {@link BasicLinkType }
     */
    public BasicLinkType getIcon() {
        return icon;
    }

    /**
     * Sets the value of the icon property.
     *
     * @param value allowed object is
     *              {@link BasicLinkType }
     */
    public void setIcon(BasicLinkType value) {
        this.icon = value;
    }

    /**
     * Gets the value of the hotSpot property.
     *
     * @return possible object is
     * {@link Vec2Type }
     */
    public Vec2Type getHotSpot() {
        return hotSpot;
    }

    /**
     * Sets the value of the hotSpot property.
     *
     * @param value allowed object is
     *              {@link Vec2Type }
     */
    public void setHotSpot(Vec2Type value) {
        this.hotSpot = value;
    }

    /**
     * Gets the value of the iconStyleSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iconStyleSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIconStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getIconStyleSimpleExtensionGroup() {
        if (iconStyleSimpleExtensionGroup == null) {
            iconStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.iconStyleSimpleExtensionGroup;
    }

    /**
     * Gets the value of the iconStyleObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iconStyleObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIconStyleObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getIconStyleObjectExtensionGroup() {
        if (iconStyleObjectExtensionGroup == null) {
            iconStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.iconStyleObjectExtensionGroup;
    }

}
