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

package net.benpl.gpsutility.logger;

import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;

/**
 * RecvJob is the wrapper of incoming NMEA sentence, to be executed by working thread of logger entity.
 */
final public class RecvJob implements Runnable {
    /**
     * The logger entity to execute this job.
     */
    private final GpsLogger logger;
    /**
     * NMEA sentence received from serial port.
     */
    private String nmea;
    /**
     * If NMEA sentence is handled correctly.
     */
    boolean success = true;

    /**
     * Constructor..
     *
     * @param logger The logger entity which received this NMEA sentence.
     * @param nmea   The NMEA sentence.
     */
    public RecvJob(GpsLogger logger, String nmea) {
        this.logger = logger;
        this.nmea = nmea;
    }

    /**
     * Test if received NMEA sentence has been handled successfully.
     *
     * @return TRUE - success, FALSE - otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Job body.
     */
    @Override
    public void run() {
        Logging.debugln("==> %s", nmea);

        // HOLUX extends it for other purpose.
        if (nmea.startsWith("===")) {
            // Something like:
            // ======ShowTLog_Destructor========
            // ======USB_Constructor========
            return;
        }

        if ("".equals(nmea.trim())) return;

        int length = nmea.length();

        if (length < 7) {
            Logging.errorln("Invalid NMEA: string too short");
            return;
        }

        if (!nmea.startsWith("$")) {
            Logging.errorln("Invalid NMEA: not started with '$'");
            return;
        }

        if (!nmea.matches("(.*)*[0-9A-Fa-f]{2}$")) {
            Logging.errorln("Invalid NMEA: ended with [%s]", nmea.substring(length - 3));
            return;
        }

        // CheckSum in NMEA package
        int chk0 = Integer.parseInt(nmea.substring(length - 2), 16);
        // DataFiled
        String dataField = nmea.substring(1, length - 3);
        // CheckSum calculated on DataField
        int chk1 = Utils.getCheckSum(dataField);

        // Validate CheckSum
        if (chk0 != chk1) {
            Logging.errorln("Invalid NMEA: checksum failed 0x%02X <-> 0x%02X", chk1, chk0);
            return;
        }

        boolean expected = logger.sendJob != null && logger.sendJob.isRespExpected(dataField);
        if (expected) {
            // If the expected response of last SendJob
            // Cancel NoResp timer at once, otherwise the long time NMEA handling (like export gpx/kml) may cause timeout.
            logger.sendJob.cancelNoRespTimer();

            // Dispatch NMEA to relevant handler.
            success = logger.sendJob.handleResp(dataField);

            // SendJob level
            if (Utils.isNotEmpty(logger.sendJob.desc)) {
                Logging.debugln("%s...%s", logger.sendJob.desc, success ? "success" : "failed");
            }

            // Task level
            if (logger.sendJob instanceof SendJob.NonTask || logger.actionTask == null) {
                // Task unrelated SendJob. Nothing to do.
                logger.sendJob = null;
            } else {
                // Task related SendJob
                // Stop the task if it is done or error occurred.
                if (!success) {
                    // Task failed
                    logger.actionTask.postExec(ActionTask.CAUSE.HANDLE_NMEA_FAIL);
                    logger.actionTask = null;
                } else if (logger.sendJob.isLastJob()) {
                    // Task finished successfully
                    logger.actionTask.postExec(ActionTask.CAUSE.SUCCESS);
                    logger.actionTask = null;
                }

                // Release sendJob reference since expected response received.
                logger.sendJob = null;
            }
        } else {
            // Discard all other not intended incoming NMEAs
            success = true;
        }
    }
}
