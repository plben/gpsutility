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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PlaylistType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PlaylistType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.google.com/kml/ext/2.2}AbstractTourPrimitiveGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaylistType", namespace = "http://www.google.com/kml/ext/2.2", propOrder = {
        "abstractTourPrimitiveGroup"
})
public class PlaylistType
        extends AbstractObjectType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElementRef(name = "AbstractTourPrimitiveGroup", namespace = "http://www.google.com/kml/ext/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractTourPrimitiveType>> abstractTourPrimitiveGroup;

    /**
     * Gets the value of the abstractTourPrimitiveGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractTourPrimitiveGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractTourPrimitiveGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SoundCueType }{@code >}
     * {@link JAXBElement }{@code <}{@link FlyToType }{@code >}
     * {@link JAXBElement }{@code <}{@link WaitType }{@code >}
     * {@link JAXBElement }{@code <}{@link AnimatedUpdateType }{@code >}
     * {@link JAXBElement }{@code <}{@link TourControlType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTourPrimitiveType }{@code >}
     */
    public List<JAXBElement<? extends AbstractTourPrimitiveType>> getAbstractTourPrimitiveGroup() {
        if (abstractTourPrimitiveGroup == null) {
            abstractTourPrimitiveGroup = new ArrayList<JAXBElement<? extends AbstractTourPrimitiveType>>();
        }
        return this.abstractTourPrimitiveGroup;
    }

}
