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
import java.io.Serializable;


/**
 * <p>Java class for SnippetType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SnippetType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="maxLines" type="{http://www.w3.org/2001/XMLSchema}int" default="2" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SnippetType", propOrder = {
        "value"
})
public class SnippetType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "maxLines")
    protected Integer maxLines;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the maxLines property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public int getMaxLines() {
        if (maxLines == null) {
            return 2;
        } else {
            return maxLines;
        }
    }

    /**
     * Sets the value of the maxLines property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxLines(Integer value) {
        this.maxLines = value;
    }

}