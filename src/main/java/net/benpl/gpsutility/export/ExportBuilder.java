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

import net.benpl.gpsutility.gpx.*;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.type.AbstractLogParser;
import net.benpl.gpsutility.type.AbstractLogRecord;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Export builder to parse the log data read from external Logger, and export to external file with desired format.
 *
 * @param <T> Class type of log parser.
 */
public class ExportBuilder<T extends AbstractLogParser> {

    private final T logParser;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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
     * @return The file path of exported .gpx file.
     * @throws PropertyException Failed on setting property to
     *                           {@link javax.xml.bind.Marshaller}
     * @throws JAXBException     Failed on marshaling the
     *                           {@link net.benpl.gpsutility.gpx.GpxType} object.
     */
    public String toGpx(String filePath) throws PropertyException, JAXBException {
        ObjectFactory gpxFactory = new ObjectFactory();

        // Create GPX Root
        GpxType root = gpxFactory.createGpxType();
        root.setCreator("GPSUtility - https://www.benpl.net/gpsutility");
        root.setVersion("1.1");

        tpId = 1;
        trackId = 1;
        lastRecord = null;
        trackDistance = 0;

        // Build the GPX tree
        // Log segment ==> GpxTrk->GpxTrkseg
        logParser.getTrackList().stream().map((rcdList) -> {
            TrkType gpxTrk = gpxFactory.createTrkType();
            TrksegType gpxSeg = gpxFactory.createTrksegType();

            // Record segment ==> GpxTrk->GpxTrkseg
            rcdList.forEach((record) -> {
                minlat = Math.min(minlat, record.getLatitude());
                minlon = Math.min(minlon, record.getLongitude());
                maxlat = Math.max(maxlat, record.getLatitude());
                maxlon = Math.max(maxlon, record.getLongitude());

                if (lastRecord != null) {
                    trackDistance += Utils.sphericalDistance(lastRecord.getLatitude(), lastRecord.getLongitude(), record.getLatitude(), record.getLongitude());
                }
                lastRecord = record;

                gpxSeg.getTrkpt().add(record.toGpxWpt(tpId));
                tpId++;
            });

            if (trackDistance >= 1000) {
                Logging.infoln("Track-%d: %.1f (km)", trackId, trackDistance / 1000);
            } else {
                Logging.infoln("Track-%d: %.1f (m)", trackId, trackDistance);
            }

            gpxTrk.getTrkseg().add(gpxSeg);
            return gpxTrk;
        }).forEachOrdered((gpxTrk) -> {
            if (trackDistance >= 1000) {
                gpxTrk.setName(String.format("track-%d, %.1f(km)", trackId, trackDistance / 1000));
            } else {
                gpxTrk.setName(String.format("track-%d, %.1f(m)", trackId, trackDistance));
            }
            // Store this GpxTrk
            root.getTrk().add(gpxTrk);
            lastRecord = null;
            trackId++;
            trackDistance = 0;
        });

        // POI list => WayPoint list
        if (logParser.getPoiList().size() > 0) {
            wpId = 1;
            logParser.getPoiList().forEach(record -> {
                root.getWpt().add(record.toGpxWpt(wpId));
                wpId++;
            });
        }

        // Metadata portion, track's bounds and export date
        MetadataType metadata = gpxFactory.createMetadataType();
        BoundsType bounds = gpxFactory.createBoundsType();
        bounds.setMinlat(BigDecimal.valueOf(minlat));
        bounds.setMinlon(BigDecimal.valueOf(minlon));
        bounds.setMaxlat(BigDecimal.valueOf(maxlat));
        bounds.setMaxlon(BigDecimal.valueOf(maxlon));
        metadata.setBounds(bounds);
        metadata.setTime(new Date());
        root.setMetadata(metadata);

        // Prepare the output .gpx file.
        File exportFile = new File(filePath);
        if (exportFile.isDirectory()) {
            exportFile = new File(exportFile, sdf.format(new Date()) + ".gpx");
        }

        // Export to .gpx file
        JAXBContext jaxbContext = JAXBContext.newInstance(GpxType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(gpxFactory.createGpx(root), exportFile);

        // Return exported file name+path for prompt in log window
        return exportFile.getAbsolutePath();
    }

}
