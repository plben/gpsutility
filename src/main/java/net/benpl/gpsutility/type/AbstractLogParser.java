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

import java.util.LinkedList;

/**
 * Log parser to parse the log data read from external Logger.
 * <p>
 * Different logger model may have different data structure, so you need to extend this class to implement the logic for
 * your logger.
 */
abstract public class AbstractLogParser {

    /**
     * The track list to store all decoded log records.
     */
    protected final LinkedList<LinkedList<AbstractLogRecord>> trackList = new LinkedList<>();
    /**
     * The list to store POI.
     */
    protected final LinkedList<AbstractLogRecord> poiList = new LinkedList<>();

    // Log parser internal variables.
    protected LinkedList<AbstractLogRecord> track = new LinkedList<>();
    protected byte[] logData;

    /**
     * Method to inject log data for parsing.
     *
     * @param logData Log data for parsing.
     */
    public void setLogData(byte[] logData) {
        this.logData = logData;
    }

    /**
     * Method called by {@link net.benpl.gpsutility.export.ExportBuilder#ExportBuilder(byte[], Class)} to parse the log
     * data {@link #logData}.
     */
    abstract public void parse();

    public LinkedList<LinkedList<AbstractLogRecord>> getTrackList() {
        return trackList;
    }

    public LinkedList<AbstractLogRecord> getPoiList() {
        return poiList;
    }
}
