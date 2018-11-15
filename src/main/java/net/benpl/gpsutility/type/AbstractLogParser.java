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

package net.benpl.gpsutility.type;

import net.benpl.gpsutility.kml.*;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.misc.XsdDateTimeConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;

/**
 * Log parser to parse the log data read from external Logger.
 * <p>
 * Different logger model may have different data structure, so you need to extend this class to implement the logic for
 * your logger.
 */
abstract public class AbstractLogParser {

    protected final byte[] logData;

    /**
     * The track list to store all decoded log records.
     */
    protected final LinkedList<LinkedList<AbstractLogRecord>> trackList = new LinkedList<>();
    /**
     * The list to store POI.
     */
    protected final LinkedList<AbstractLogRecord> poiList = new LinkedList<>();

    // Temporary variables for parsing log data.
    protected LinkedList<AbstractLogRecord> track = new LinkedList<>();

    // Temporary variables for exporting data to external file
    private double minlat = 90;
    private double maxlat = -90;
    private double minlon = 180;
    private double maxlon = -180;
    private int trackId;
    private int tpId;
    private int wpId;
    private AbstractLogRecord lastRecord;
    private double trackDistance;

    /**
     * The type of export file.
     */
    public enum ExportType {
        GPX, KML
    }

    /**
     * Constructor of log parser.
     *
     * @param logData Binary log data to be parsed.
     */
    public AbstractLogParser(byte[] logData) {
        this.logData = logData;
    }

    /**
     * Method called by {@link net.benpl.gpsutility.logger.PrimaryController} to parse the log data {@link #logData}.
     */
    abstract public void parse();

    /**
     * Export log data to .gpx file.
     *
     * @param exportFile The export target file.
     * @param timestamp  When this .gpx file is created.
     * @return The file path of exported .gpx file.
     * @throws PropertyException Failed on setting property to {@link javax.xml.bind.Marshaller}
     * @throws JAXBException     Failed on marshaling the {@link net.benpl.gpsutility.gpx.GpxType} object.
     */
    public String toGpx(File exportFile, Date timestamp) throws PropertyException, JAXBException {
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

        // gpx<-TrkList
        trackList.stream().map((rcdList) -> {
            net.benpl.gpsutility.gpx.TrkType trk = gpxFactory.createTrkType();
            net.benpl.gpsutility.gpx.TrksegType trkseg = gpxFactory.createTrksegType();

            // gpx<-TrkList<-Trk<-Trkseg<-...
            rcdList.forEach((record) -> {
                // Bounds calculation
                minlat = Math.min(minlat, record.getLatitude());
                minlon = Math.min(minlon, record.getLongitude());
                maxlat = Math.max(maxlat, record.getLatitude());
                maxlon = Math.max(maxlon, record.getLongitude());
                // Track length calculation
                if (lastRecord != null) {
                    trackDistance += Utils.sphericalDistance(lastRecord.getLatitude(), lastRecord.getLongitude(), record.getLatitude(), record.getLongitude());
                }
                lastRecord = record;

                // gpx<-TrkList<-Trk<-Trkseg<-GpxWpt
                trkseg.getTrkpt().add(record.toGpxWpt(tpId));
                tpId++;
            });

            if (trackDistance >= 1000) {
                Logging.infoln("Track-%d: %.1f (km)", trackId, trackDistance / 1000);
            } else {
                Logging.infoln("Track-%d: %.1f (m)", trackId, trackDistance);
            }

            // gpx<-TrkList<-Trk<-Trkseg
            trk.getTrkseg().add(trkseg);
            return trk;
        }).forEachOrdered((trk) -> {
            // gpx<-TrkList<-Trk<-Name
            if (trackDistance >= 1000) {
                trk.setName(String.format("track-%d, %.1f(km)", trackId, trackDistance / 1000));
            } else {
                trk.setName(String.format("track-%d, %.1f(m)", trackId, trackDistance));
            }

            // gpx<-TrkList<-Trk
            root.getTrk().add(trk);

            lastRecord = null;
            trackId++;
            trackDistance = 0;
        });

        // gpx<-WptList<-...GpxWpt(POI)
        if (poiList.size() > 0) {
            wpId = 1;
            poiList.forEach(record -> {
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
        metadata.setTime(timestamp);
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
     * @param timestamp  When this .kml file is created.
     * @return The file path of exported .kml file.
     * @throws JAXBException Failed on marshaling the {@link net.benpl.gpsutility.kml.KmlType} object.
     */
    public String toKml(File exportFile, Date timestamp) throws JAXBException {
        net.benpl.gpsutility.kml.ObjectFactory kmlFactory = new net.benpl.gpsutility.kml.ObjectFactory();

        KmlType root = kmlFactory.createKmlType();
        DocumentType document = kmlFactory.createDocumentType();
        FolderType trackFolder = kmlFactory.createFolderType();

        // kml<-Document<-Name
        document.setNameRevised("GPS Device");
        // kml<-Document<-Snippet
        document.setSnippetRevised("Created " + XsdDateTimeConverter.sdf.format(timestamp));

        // kml<-Document<-TrackStyle(Normal)
        StyleType trackStyleNormal = kmlFactory.createStyleType();
        LineStyleType lineStyleNormal = new LineStyleType();
        lineStyleNormal.setWidth(3.0);
        lineStyleNormal.setColor(new byte[]{(byte) 0x99, (byte) 0x00, (byte) 0x00, (byte) 0xff});
        trackStyleNormal.setLineStyle(lineStyleNormal);
        trackStyleNormal.setId("lineStyle_n");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(trackStyleNormal));

        // kml<-Document<-TrackStyle(Highlight)
        StyleType trackStyleHighlight = kmlFactory.createStyleType();
        LineStyleType lineStyleHighlight = new LineStyleType();
        lineStyleHighlight.setWidth(3.0);
        lineStyleHighlight.setColor(new byte[]{(byte) 0x99, (byte) 0x00, (byte) 0xff, (byte) 0x00});
        trackStyleHighlight.setLineStyle(lineStyleHighlight);
        trackStyleHighlight.setId("lineStyle_h");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(trackStyleHighlight));

        // kml<-Document<-TrackStyleMap
        StyleMapType trackStyleMap = kmlFactory.createStyleMapType();
        PairType linePairNormal = new PairType();
        linePairNormal.setKey(StyleStateEnumType.NORMAL);
        linePairNormal.setStyleUrl("#lineStyle_n");
        trackStyleMap.getPair().add(linePairNormal);
        PairType linePairHighlight = new PairType();
        linePairHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
        linePairHighlight.setStyleUrl("#lineStyle_h");
        trackStyleMap.getPair().add(linePairHighlight);
        trackStyleMap.setId("trackStyle");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyleMap(trackStyleMap));

        // kml<-Document<-PoiStyle(Normal)
        StyleType poiStyleNormal = kmlFactory.createStyleType();
        IconStyleType iconStyleNormal = new IconStyleType();
        BasicLinkType linkNormal = new BasicLinkType();
        linkNormal.setHref("http://maps.google.com/mapfiles/kml/pushpin/grn-pushpin.png");
        iconStyleNormal.setScaleRevised(1.0);
        iconStyleNormal.setIcon(linkNormal);
        poiStyleNormal.setIconStyle(iconStyleNormal);
        poiStyleNormal.setId("poiStyle_n");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(poiStyleNormal));

        // kml<-Document<-PoiStyle(Highlight)
        StyleType poiStyleHighlight = kmlFactory.createStyleType();
        IconStyleType iconStyleHighlight = new IconStyleType();
        BasicLinkType linkHighlight = new BasicLinkType();
        linkHighlight.setHref("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
        iconStyleHighlight.setScaleRevised(1.0);
        iconStyleHighlight.setIcon(linkHighlight);
        poiStyleHighlight.setIconStyle(iconStyleHighlight);
        poiStyleHighlight.setId("poiStyle_h");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyle(poiStyleHighlight));

        // kml<-Document<-PoiStyleMap
        StyleMapType poiStyleMap = kmlFactory.createStyleMapType();
        PairType poiPairNormal = new PairType();
        poiPairNormal.setKey(StyleStateEnumType.NORMAL);
        poiPairNormal.setStyleUrl("#poiStyle_n");
        poiStyleMap.getPair().add(poiPairNormal);
        PairType poiPairHighlight = new PairType();
        poiPairHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
        poiPairHighlight.setStyleUrl("#poiStyle_h");
        poiStyleMap.getPair().add(poiPairHighlight);
        poiStyleMap.setId("poiStyle");
        document.getAbstractStyleSelectorGroup().add(kmlFactory.createStyleMap(poiStyleMap));

        tpId = 1;
        trackId = 1;
        lastRecord = null;
        trackDistance = 0;

        // kml<-Document<-TrackFolder<-Name
        trackFolder.setNameRevised("Tracks");
        // kml<-Document<-TrackFolder<-...
        trackList.stream().map((rcdList) -> {
            TrackType track = kmlFactory.createTrackType();

            // kml<-Document<-TrackFolder<-Placemark<-Track<-...
            rcdList.forEach((record) -> {
                // Track length calculation
                if (lastRecord != null) {
                    trackDistance += Utils.sphericalDistance(lastRecord.getLatitude(), lastRecord.getLongitude(), record.getLatitude(), record.getLongitude());
                }
                lastRecord = record;

                // kml<-Document<-TrackFolder<-Placemark<-Track<-When
                track.getWhen().add(XsdDateTimeConverter.sdf.format(record.getUtc()));
                // kml<-Document<-TrackFolder<-Placemark<-Track<-coord
                track.getCoord().add(String.format("%f %f %f", record.getLongitude(), record.getLatitude(), record.getHeight()));
                tpId++;
            });

            return track;
        }).forEachOrdered((trackType) -> {
            PlacemarkType placemark = kmlFactory.createPlacemarkType();
            // kml<-Document<-TrackFolder<-Placemark<-Track
            placemark.getAbstractFeatureSimpleExtensionGroup().add(kmlFactory.createTrack(trackType));
            // kml<-Document<-TrackFolder<-Placemark<-Name
            if (trackDistance >= 1000) {
                placemark.setNameRevised(String.format("track-%d, %.1f(km)", trackId, trackDistance / 1000));
            } else {
                placemark.setNameRevised(String.format("track-%d, %.1f(m)", trackId, trackDistance));
            }
            // kml<-Document<-TrackFolder<-Placemark<-StyleUrl
            placemark.setStyleUrl("#trackStyle");

            // kml<-Document<-TrackFolder<-Placemark
            trackFolder.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));

            lastRecord = null;
            trackId++;
            trackDistance = 0;
        });
        // kml<-Document<-TrackFolder
        document.getAbstractFeatureGroup().add(kmlFactory.createFolder(trackFolder));

        // kml<-Document<-PoiFolder<-...(POI)
        if (poiList.size() > 0) {
            FolderType poiFolder = kmlFactory.createFolderType();
            // kml<-Document<-PoiFolder<-Name
            poiFolder.setNameRevised("POI List");

            wpId = 1;
            poiList.forEach(record -> {
                PlacemarkType placemark = kmlFactory.createPlacemarkType();
                // kml<-Document<-PoiFolder<-Placemark<-Name
                placemark.setNameRevised(String.format("POI%06d", wpId));
                // kml<-Document<-PoiFolder<-Placemark<-StyleUrl
                placemark.setStyleUrl("#poiStyle");
                // kml<-Document<-PoiFolder<-Placemark<-TimeStamp
                TimeStampType timeStamp = kmlFactory.createTimeStampType();
                timeStamp.setWhen(XsdDateTimeConverter.sdf.format(record.getUtc()));
                placemark.setAbstractTimePrimitiveGroup(kmlFactory.createTimeStamp(timeStamp));
                // kml<-Document<-PoiFolder<-Placemark<-Point
                PointType point = kmlFactory.createPointType();
                point.getCoordinates().add(String.format("%f,%f,%f", record.getLongitude(), record.getLatitude(), record.getHeight()));
                placemark.getAbstractFeatureSimpleExtensionGroup().add(kmlFactory.createPoint(point));
                // kml<-Document<-PoiFolder<-Placemark
                poiFolder.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));

                wpId++;
            });

            // kml<-Document<-PoiFolder
            document.getAbstractFeatureGroup().add(kmlFactory.createFolder(poiFolder));
        }

        // kml<-Document
        root.setAbstractFeatureGroup(kmlFactory.createDocument(document));

        // .kml file<-kml
        JAXBContext jaxbContext = JAXBContext.newInstance(net.benpl.gpsutility.kml.KmlType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(kmlFactory.createKml(root), exportFile);

        // Return exported file name+path for prompt in log window
        return exportFile.getAbsolutePath();
    }
}
