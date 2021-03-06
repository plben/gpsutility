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
//         Generated on: 2018.10.25 at 01:47:32 AM UTC 
//


package net.benpl.gpsutility.gpx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
 *
 *
 * <p>Java class for trksegType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="trksegType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="trkpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trksegType", propOrder = {
        "trkpt",
        "extensions"
})
public class TrksegType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    protected List<WptType> trkpt;
    protected ExtensionsType extensions;

    /**
     * Gets the value of the trkpt property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trkpt property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrkpt().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WptType }
     */
    public List<WptType> getTrkpt() {
        if (trkpt == null) {
            trkpt = new ArrayList<WptType>();
        }
        return this.trkpt;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is
     * {@link ExtensionsType }
     */
    public ExtensionsType getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is
     *              {@link ExtensionsType }
     */
    public void setExtensions(ExtensionsType value) {
        this.extensions = value;
    }

}
