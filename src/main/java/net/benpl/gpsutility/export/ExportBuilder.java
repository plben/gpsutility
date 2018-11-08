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

package net.benpl.gpsutility.export;

import net.benpl.gpsutility.kml.*;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.misc.XsdDateTimeConverter;
import net.benpl.gpsutility.type.AbstractLogParser;
import net.benpl.gpsutility.type.AbstractLogRecord;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Export builder to parse the log data read from external Logger, and export to external file with desired format.
 *
 * @param <T> Class type of log parser.
 */
public class ExportBuilder<T extends AbstractLogParser> {

    private final T logParser;

    private double minlat = 90;
    private double maxlat = -90;
    private double minlon = 180;
    private double maxlon = -180;
    private int trackId;
    private int tpId;
    private int wpId;
    private AbstractLogRecord lastRecord;
    private double trackDistance;

    public ExportBuilder(byte[] logData, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        this.logParser = clazz.newInstance();
        this.logParser.setLogData(logData);
        this.logParser.parse();
    }

    /**
     * Export log data to .gpx file.
     *
     * @param exportFile The export target file.
     * @return The file path of exported .gpx file.
     * @throws PropertyException Failed on setting property to
     *                           {@link javax.xml.bind.Marshaller}
     * @throws JAXBException     Failed on marshaling the
     *                           {@link net.benpl.gpsutility.gpx.GpxType} object.
     */
    public String toGpx(File exportFile) throws PropertyException, JAXBException {
        net.benpl.gpsutility.gpx.ObjectFactory gpxFactory = new net.benpl.gpsutility.gpx.ObjectFactory();
        net.benpl.gpsutility.gpx.GpxType root = gpxFactory.createGpxType();

        // gpx<-Creator
        root.setCreator("GPSUtility - https://www.benpl.net/gpsutility");
        // gpx<-Version
        root.setVersion(root.getVersion());

        tpId = 1;
        trackId = 1;
        lastRecord = null;
        trackDistance = 0;

        // gpx<-GpxTrk
        logParser.getTrackList().stream().map((rcdList) -> {
            net.benpl.gpsutility.gpx.TrkType trkType = gpxFactory.createTrkType();
            net.benpl.gpsutility.gpx.TrksegType trksegType = gpxFactory.createTrksegType();

            // gpx<-GpxTrk<-TrkType<-Trkseg<-...
            rcdList.forEach((record) -> {
                minlat = Math.min(minlat, record.getLatitude());
                minlon = Math.min(minlon, record.getLongitude());
                maxlat = Math.max(maxlat, record.getLatitude());
                maxlon = Math.max(maxlon, record.getLongitude());

                if (lastRecord != null) {
                    trackDistance += Utils.sphericalDistance(lastRecord.getLatitude(), lastRecord.getLongitude(), record.getLatitude(), record.getLongitude());
                }
                lastRecord = record;

                // gpx<-GpxTrk<-TrkType<-Trkseg<-GpxWpt
                trksegType.getTrkpt().add(record.toGpxWpt(tpId));
                tpId++;
            });

            if (trackDistance >= 1000) {
                Logging.infoln("Track-%d: %.1f (km)", trackId, trackDistance / 1000);
            } else {
                Logging.infoln("Track-%d: %.1f (m)", trackId, trackDistance);
            }

            // gpx<-GpxTrk<-TrkType<-Trkseg
            trkType.getTrkseg().add(trksegType);
            return trkType;
        }).forEachOrdered((trkType) -> {
            // gpx<-GpxTrk<-TrkType<-Name
            if (trackDistance >= 1000) {
                trkType.setName(String.format("track-%d, %.1f(km)", trackId, trackDistance / 1000));
            } else {
                trkType.setName(String.format("track-%d, %.1f(m)", trackId, trackDistance));
            }

            // gpx<-GpxTrk<-TrkType
            root.getTrk().add(trkType);

            lastRecord = null;
            trackId++;
            trackDistance = 0;
        });

        // gpx<-Wpt<-...(POI)
        if (logParser.getPoiList().size() > 0) {
            wpId = 1;
            logParser.getPoiList().forEach(record -> {
                root.getWpt().add(record.toGpxWpt(wpId));
                wpId++;
            });
        }

        // Metadata portion, track's bounds and export date
        net.benpl.gpsutility.gpx.MetadataType metadata = gpxFactory.createMetadataType();
        // gpx<-Metadata<-Bounds
        net.benpl.gpsutility.gpx.BoundsType bounds = gpxFactory.createBoundsType();
        bounds.setMinlat(BigDecimal.valueOf(minlat));
        bounds.setMinlon(BigDecimal.valueOf(minlon));
        bounds.setMaxlat(BigDecimal.valueOf(maxlat));
        bounds.setMaxlon(BigDecimal.valueOf(maxlon));
        metadata.setBounds(bounds);
        // gpx<-Metadata<-Time
        metadata.setTime(new Date());
        // gpx<-Metadata
        root.setMetadata(metadata);

        // Export to .gpx file
        JAXBContext jaxbContext = JAXBContext.newInstance(net.benpl.gpsutility.gpx.GpxType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(gpxFactory.createGpx(root), exportFile);

        // Return exported file name+path for prompt in log window
        return exportFile.getAbsolutePath();
    }

    /**
     * Export log data to .kml file.
     *
     * @param exportFile The export target file.
     * @return The file path of exported .kml file.
     * @throws JAXBException Failed on marshaling the
     *                       {@link net.benpl.gpsutility.kml.KmlType} object.
     */
    public String toKml(File exportFile) throws JAXBException {
        net.benpl.gpsutility.kml.ObjectFactory kmlFactory = new net.benpl.gpsutility.kml.ObjectFactory();

        KmlType root = kmlFactory.createKmlType();
        DocumentType documentType = kmlFactory.createDocumentType();
        FolderType trackFolderType = kmlFactory.createFolderType();

        // kml<-Document<-Name
        documentType.setNameRevised("GPS Logger");
        // kml<-Document<-Snippet
        documentType.setSnippetRevised("Exported " + XsdDateTimeConverter.sdf.format(new Date()));

        // kml<-Document<-LineStyle(Normal)
        StyleType lineStyleNormal = kmlFactory.createStyleType();
        LineStyleType styleTypeNormal = new LineStyleType();
        styleTypeNormal.setWidth(3.0);
        styleTypeNormal.setColor(new byte[]{(byte) 0x99, (byte) 0x00, (byte) 0x00, (byte) 0xff});
        lineStyleNormal.setLineStyle(styleTypeNormal);
        lineStyleNormal.setId("lineStyle_n");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(lineStyleNormal));

        // kml<-Document<-LineStyle(Highlight)
        StyleType lineStyleHighlight = kmlFactory.createStyleType();
        LineStyleType styleTypeHighlight = new LineStyleType();
        styleTypeHighlight.setWidth(3.0);
        styleTypeHighlight.setColor(new byte[]{(byte) 0x99, (byte) 0x00, (byte) 0xff, (byte) 0x00});
        lineStyleHighlight.setLineStyle(styleTypeHighlight);
        lineStyleHighlight.setId("lineStyle_h");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(lineStyleHighlight));

        // kml<-Document<-LineStyleMap
        StyleMapType lineStyleMap = kmlFactory.createStyleMapType();
        PairType linePairTypeNormal = new PairType();
        linePairTypeNormal.setKey(StyleStateEnumType.NORMAL);
        linePairTypeNormal.setStyleUrl("#lineStyle_n");
        lineStyleMap.getPair().add(linePairTypeNormal);
        PairType linePairTypeHighlight = new PairType();
        linePairTypeHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
        linePairTypeHighlight.setStyleUrl("#lineStyle_h");
        lineStyleMap.getPair().add(linePairTypeHighlight);
        lineStyleMap.setId("trackStyle");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyleMap(lineStyleMap));

        // kml<-Document<-PoiStyle(Normal)
        StyleType poiStyleNormal = kmlFactory.createStyleType();
        IconStyleType iconStyleTypeNormal = new IconStyleType();
        BasicLinkType linkTypeNormal = new BasicLinkType();
        linkTypeNormal.setHref("http://maps.google.com/mapfiles/kml/pushpin/grn-pushpin.png");
        iconStyleTypeNormal.setScaleRevised(1.0);
        iconStyleTypeNormal.setIcon(linkTypeNormal);
        poiStyleNormal.setIconStyle(iconStyleTypeNormal);
        poiStyleNormal.setId("poiStyle_n");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(poiStyleNormal));

        // kml<-Document<-PoiStyle(Highlight)
        StyleType poiStyleHighlight = kmlFactory.createStyleType();
        IconStyleType iconStyleTypeHighlight = new IconStyleType();
        BasicLinkType linkTypeHighlight = new BasicLinkType();
        linkTypeHighlight.setHref("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
        iconStyleTypeHighlight.setScaleRevised(1.0);
        iconStyleTypeHighlight.setIcon(linkTypeHighlight);
        poiStyleHighlight.setIconStyle(iconStyleTypeHighlight);
        poiStyleHighlight.setId("poiStyle_h");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(poiStyleHighlight));

        // kml<-Document<-PoiStyleMap
        StyleMapType poiStyleMap = kmlFactory.createStyleMapType();
        PairType poiPairTypeNormal = new PairType();
        poiPairTypeNormal.setKey(StyleStateEnumType.NORMAL);
        poiPairTypeNormal.setStyleUrl("#poiStyle_n");
        poiStyleMap.getPair().add(poiPairTypeNormal);
        PairType poiPairTypeHighlight = new PairType();
        poiPairTypeHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
        poiPairTypeHighlight.setStyleUrl("#poiStyle_h");
        poiStyleMap.getPair().add(poiPairTypeHighlight);
        poiStyleMap.setId("poiStyle");
        documentType.getAbstractStyleSelectorGroup().add(kmlFactory.createStyleMap(poiStyleMap));

        tpId = 1;
        trackId = 1;
        lastRecord = null;
        trackDistance = 0;

        // kml<-Document<-TrackFolder<-Name
        trackFolderType.setNameRevised("Tracks");
        // kml<-Document<-TrackFolder<-...
        logParser.getTrackList().stream().map((rcdList) -> {
            TrackType trackType = kmlFactory.createTrackType();

            rcdList.forEach((record) -> {
                if (lastRecord != null) {
                    trackDistance += Utils.sphericalDistance(lastRecord.getLatitude(), lastRecord.getLongitude(), record.getLatitude(), record.getLongitude());
                }
                lastRecord = record;

                // kml<-Document<-TrackFolder<-Placemark<-Track<-When
                trackType.getWhen().add(XsdDateTimeConverter.sdf.format(record.getUtc()));
                // kml<-Document<-TrackFolder<-Placemark<-Track<-coord
                trackType.getCoord().add(String.format("%f %f %f", record.getLongitude(), record.getLatitude(), record.getHeight()));
                tpId++;
            });

            return trackType;
        }).forEachOrdered((trackType) -> {
            PlacemarkType placemarkType = kmlFactory.createPlacemarkType();
            // kml<-Document<-TrackFolder<-Placemark<-Name
            if (trackDistance >= 1000) {
                placemarkType.setNameRevised(String.format("track-%d, %.1f(km)", trackId, trackDistance / 1000));
            } else {
                placemarkType.setNameRevised(String.format("track-%d, %.1f(m)", trackId, trackDistance));
            }
            // kml<-Document<-TrackFolder<-Placemark<-StyleUrl
            placemarkType.setStyleUrl("#trackStyle");
            // kml<-Document<-TrackFolder<-Placemark<-Track
            placemarkType.getAbstractFeatureSimpleExtensionGroup().add(kmlFactory.createTrack(trackType));
            // kml<-Document<-TrackFolder<-Placemark
            trackFolderType.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemarkType));

            lastRecord = null;
            trackId++;
            trackDistance = 0;
        });
        // kml<-Document<-TrackFolder
        documentType.getAbstractFeatureGroup().add(kmlFactory.createFolder(trackFolderType));

        // kml<-Document<-PoiFolder<-...(POI)
        if (logParser.getPoiList().size() > 0) {
            FolderType poiFolderType = kmlFactory.createFolderType();
            poiFolderType.setNameRevised("POI List");

            wpId = 1;
            logParser.getPoiList().forEach(record -> {
                PlacemarkType placemarkType = kmlFactory.createPlacemarkType();
                // kml<-Document<-PoiFolder<-Placemark<-Name
                placemarkType.setNameRevised(String.format("POI%06d", wpId));
                // kml<-Document<-PoiFolder<-Placemark<-StyleUrl
                placemarkType.setStyleUrl("#poiStyle");
                // kml<-Document<-PoiFolder<-Placemark<-TimeStamp
                TimeStampType timeStampType = kmlFactory.createTimeStampType();
                timeStampType.setWhen(XsdDateTimeConverter.sdf.format(record.getUtc()));
                placemarkType.setAbstractTimePrimitiveGroup(kmlFactory.createTimeStamp(timeStampType));
                // kml<-Document<-PoiFolder<-Placemark<-Point
                PointType point = kmlFactory.createPointType();
                point.getCoordinates().add(String.format("%f,%f,%f", record.getLongitude(), record.getLatitude(), record.getHeight()));
                placemarkType.getAbstractFeatureSimpleExtensionGroup().add(kmlFactory.createPoint(point));
                // kml<-Document<-PoiFolder<-Placemark
                poiFolderType.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemarkType));

                wpId++;
            });

            // kml<-Document<-PoiFolder
            documentType.getAbstractFeatureGroup().add(kmlFactory.createFolder(poiFolderType));
        }

        // kml<-Document
        root.setAbstractFeatureGroup(kmlFactory.createDocument(documentType));

        // .kml file<-kml
        JAXBContext jaxbContext = JAXBContext.newInstance(net.benpl.gpsutility.kml.KmlType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(kmlFactory.createKml(root), exportFile);

        // Return exported file name+path for prompt in log window
        return exportFile.getAbsolutePath();
    }
}
