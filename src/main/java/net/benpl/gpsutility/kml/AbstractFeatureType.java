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
 * <p>Java class for AbstractFeatureType complex type.
 *
 * <p>The following schema fragment specifies the expected         content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AbstractFeatureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}nameRevised" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}visibility" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}open" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.w3.org/2005/Atom}author" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.w3.org/2005/Atom}link" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}address" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressDetails" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}phoneNumber" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Snippet" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}snippetRevised" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}description" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractTimePrimitiveGroup" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}styleUrl" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Region" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Metadata" minOccurs="0"/&gt;
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}ExtendedData" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
        "nameRevised",
        "visibility",
        "open",
        "author",
        "link",
        "address",
        "addressDetails",
        "phoneNumber",
        "snippet",
        "snippetRevised",
        "description",
        "abstractViewGroup",
        "abstractTimePrimitiveGroup",
        "styleUrl",
        "abstractStyleSelectorGroup",
        "region",
        "metadata",
        "extendedData",
        "abstractFeatureSimpleExtensionGroup",
        "abstractFeatureObjectExtensionGroup"
})
@XmlSeeAlso({
        TourType.class,
        AbstractContainerType.class,
        PlacemarkType.class,
        NetworkLinkType.class,
        AbstractOverlayType.class
})
public abstract class AbstractFeatureType
        extends AbstractObjectType
        implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "name")
    protected String nameRevised;
    @XmlElement(defaultValue = "1")
    protected Boolean visibility;
    @XmlElement(defaultValue = "0")
    protected Boolean open;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    protected AtomPersonConstruct author;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    protected Link link;
    protected String address;
    @XmlElement(name = "AddressDetails", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected AddressDetails addressDetails;
    protected String phoneNumber;
    @XmlElement(name = "Snippet")
    protected SnippetType snippet;
    @XmlElement(name = "snippet")
    protected String snippetRevised;
    protected String description;
    @XmlElementRef(name = "AbstractViewGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractViewType> abstractViewGroup;
    @XmlElementRef(name = "AbstractTimePrimitiveGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractTimePrimitiveType> abstractTimePrimitiveGroup;
    @XmlSchemaType(name = "anyURI")
    protected String styleUrl;
    @XmlElementRef(name = "AbstractStyleSelectorGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroup;
    @XmlElement(name = "Region")
    protected RegionType region;
    @XmlElement(name = "Metadata")
    protected MetadataType metadata;
    @XmlElement(name = "ExtendedData")
    protected ExtendedDataType extendedData;
    @XmlElementRef(name = "AbstractFeatureSimpleExtensionGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<?>> abstractFeatureSimpleExtensionGroup;
    @XmlElement(name = "AbstractFeatureObjectExtensionGroup")
    protected List<AbstractObjectType> abstractFeatureObjectExtensionGroup;

    /**
     * Gets the value of the nameRevised property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getNameRevised() {
        return nameRevised;
    }

    /**
     * Sets the value of the nameRevised property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNameRevised(String value) {
        this.nameRevised = value;
    }

    /**
     * Gets the value of the visibility property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setVisibility(Boolean value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the open property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isOpen() {
        return open;
    }

    /**
     * Sets the value of the open property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setOpen(Boolean value) {
        this.open = value;
    }

    /**
     * Gets the value of the author property.
     *
     * @return possible object is
     * {@link AtomPersonConstruct }
     */
    public AtomPersonConstruct getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value allowed object is
     *              {@link AtomPersonConstruct }
     */
    public void setAuthor(AtomPersonConstruct value) {
        this.author = value;
    }

    /**
     * Gets the value of the link property.
     *
     * @return possible object is
     * {@link Link }
     */
    public Link getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     *
     * @param value allowed object is
     *              {@link Link }
     */
    public void setLink(Link value) {
        this.link = value;
    }

    /**
     * Gets the value of the address property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the addressDetails property.
     *
     * @return possible object is
     * {@link AddressDetails }
     */
    public AddressDetails getAddressDetails() {
        return addressDetails;
    }

    /**
     * Sets the value of the addressDetails property.
     *
     * @param value allowed object is
     *              {@link AddressDetails }
     */
    public void setAddressDetails(AddressDetails value) {
        this.addressDetails = value;
    }

    /**
     * Gets the value of the phoneNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    /**
     * Gets the value of the snippet property.
     *
     * @return possible object is
     * {@link SnippetType }
     */
    public SnippetType getSnippet() {
        return snippet;
    }

    /**
     * Sets the value of the snippet property.
     *
     * @param value allowed object is
     *              {@link SnippetType }
     */
    public void setSnippet(SnippetType value) {
        this.snippet = value;
    }

    /**
     * Gets the value of the snippetRevised property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSnippetRevised() {
        return snippetRevised;
    }

    /**
     * Sets the value of the snippetRevised property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSnippetRevised(String value) {
        this.snippetRevised = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the abstractViewGroup property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     * {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     */
    public JAXBElement<? extends AbstractViewType> getAbstractViewGroup() {
        return abstractViewGroup;
    }

    /**
     * Sets the value of the abstractViewGroup property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *              {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *              {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     */
    public void setAbstractViewGroup(JAXBElement<? extends AbstractViewType> value) {
        this.abstractViewGroup = value;
    }

    /**
     * Gets the value of the abstractTimePrimitiveGroup property.
     *
     * @return possible object is
     * {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     */
    public JAXBElement<? extends AbstractTimePrimitiveType> getAbstractTimePrimitiveGroup() {
        return abstractTimePrimitiveGroup;
    }

    /**
     * Sets the value of the abstractTimePrimitiveGroup property.
     *
     * @param value allowed object is
     *              {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     *              {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     *              {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     */
    public void setAbstractTimePrimitiveGroup(JAXBElement<? extends AbstractTimePrimitiveType> value) {
        this.abstractTimePrimitiveGroup = value;
    }

    /**
     * Gets the value of the styleUrl property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStyleUrl() {
        return styleUrl;
    }

    /**
     * Sets the value of the styleUrl property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStyleUrl(String value) {
        this.styleUrl = value;
    }

    /**
     * Gets the value of the abstractStyleSelectorGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractStyleSelectorGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractStyleSelectorGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractStyleSelectorType }{@code >}
     */
    public List<JAXBElement<? extends AbstractStyleSelectorType>> getAbstractStyleSelectorGroup() {
        if (abstractStyleSelectorGroup == null) {
            abstractStyleSelectorGroup = new ArrayList<JAXBElement<? extends AbstractStyleSelectorType>>();
        }
        return this.abstractStyleSelectorGroup;
    }

    /**
     * Gets the value of the region property.
     *
     * @return possible object is
     * {@link RegionType }
     */
    public RegionType getRegion() {
        return region;
    }

    /**
     * Sets the value of the region property.
     *
     * @param value allowed object is
     *              {@link RegionType }
     */
    public void setRegion(RegionType value) {
        this.region = value;
    }

    /**
     * Gets the value of the metadata property.
     *
     * @return possible object is
     * {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     *
     * @param value allowed object is
     *              {@link MetadataType }
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the extendedData property.
     *
     * @return possible object is
     * {@link ExtendedDataType }
     */
    public ExtendedDataType getExtendedData() {
        return extendedData;
    }

    /**
     * Sets the value of the extendedData property.
     *
     * @param value allowed object is
     *              {@link ExtendedDataType }
     */
    public void setExtendedData(ExtendedDataType value) {
        this.extendedData = value;
    }

    /**
     * Gets the value of the abstractFeatureSimpleExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureSimpleExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureSimpleExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Double }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    public List<JAXBElement<?>> getAbstractFeatureSimpleExtensionGroup() {
        if (abstractFeatureSimpleExtensionGroup == null) {
            abstractFeatureSimpleExtensionGroup = new ArrayList<JAXBElement<?>>();
        }
        return this.abstractFeatureSimpleExtensionGroup;
    }

    /**
     * Gets the value of the abstractFeatureObjectExtensionGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureObjectExtensionGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureObjectExtensionGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     */
    public List<AbstractObjectType> getAbstractFeatureObjectExtensionGroup() {
        if (abstractFeatureObjectExtensionGroup == null) {
            abstractFeatureObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.abstractFeatureObjectExtensionGroup;
    }

}