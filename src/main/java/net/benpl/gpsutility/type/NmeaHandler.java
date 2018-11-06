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

import net.benpl.gpsutility.misc.Logging;

/**
 * Handler template of NMEA package.
 * <p>
 * You should override the {@link #handle(Object, String[], int)} method for each NMEA package.
 */
public class NmeaHandler<T> {

    /**
     * Handle particular segment of NMEA package. Here is the default implementation. You should override this method to
     * achieve your implementation logic.
     *
     * @param logger Logger object which is handling the NMEA package.
     * @param segs   String segments of NMEA data field. (split by ',')
     * @param idx    Which segment to be handled.
     * @return TRUE - success, FALSE - failed
     */
    public boolean handle(T logger, String[] segs, int idx) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int i = 0; i <= idx; i++) {
            sb.append(segs[i]);
            if (i != idx) {
                sb.append(",");
            }
        }
        sb.append("] ...handler should be override!");

        Logging.debugln(sb.toString());
        return true;
    }

    /**
     * Print NMEA package followed by debug message. (For logging purpose only)
     *
     * @param segs    The source NMEA package. (data field split by ',')
     * @param message Debug message.
     */
    public static void debug(String[] segs, String message) {
        Logging.debugln(from(segs).append(message).toString());
    }

    /**
     * Print NMEA package followed by error message. (For logging purpose only)
     *
     * @param segs    The source NMEA package. (data field split by ',')
     * @param message Error message.
     */
    public static void error(String[] segs, String message) {
        Logging.errorln(from(segs).append(message).toString());
    }

    /**
     * Form output NMEA string from segmented.
     *
     * @param segs Segmented NMEA data field strings.
     * @return StringBuilder formed with NMEA package.
     */
    private static StringBuilder from(String[] segs) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int i = 0; i < segs.length; i++) {
            sb.append(segs[i]);
            if (i != (segs.length - 1)) {
                sb.append(",");
            }
        }
        sb.append("] ...");

        return sb;
    }
}
