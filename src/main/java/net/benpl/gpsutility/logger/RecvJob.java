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

import javafx.application.Platform;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;

/**
 * RecvJob is the task for serial port incoming string validating, NMEA unpacking, {@link SendJob} task no response checking,
 * and dispatching to logger entity for NMEA handling.
 * <p>
 * After received string segmentation, {@link net.benpl.gpsutility.serialport.SPort} wraps the segmented string into
 * RecvJob task one by one and enqueues to FIFO queue {@link LoggerThread#ingressQueue}. Logger thread {@link LoggerThread}
 * is responsible for task execution scheduling.
 */
final public class RecvJob implements Runnable {

    static final public int RECV_JOB_NMEA_DATA = 1;

    private final GpsLogger logger;
    private final int jobType;
    private String nmea;

    /**
     * If NMEA is handled by handler correctly.
     */
    boolean success = true;

    /**
     * The job to handle incoming NMEA string from serial port..
     *
     * @param logger  Logger object to receive the final unpacked NMEA package.
     * @param jobType RecvJob type.
     * @param nmea    NMEA string.
     */
    public RecvJob(GpsLogger logger, int jobType, String nmea) {
        this.logger = logger;
        this.jobType = jobType;
        this.nmea = nmea;
    }

    /**
     * If this job is executed successfully. (NMEA handled without error)
     *
     * @return TRUE - success, FALSE - otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Job executed by logger thread {@link LoggerThread}.
     * Validate the NMEA string, SendJob no response check, unpack the NMEA package and pass to logger object  for later
     * NMEA handling.
     */
    @Override
    public void run() {
        switch (jobType) {
            case RECV_JOB_NMEA_DATA:
                Logging.debugln("==> %s", nmea);

                // HOLUX extends it for other purpose.
                if (nmea.startsWith("===")) {
                    // Something like:
                    // ======ShowTLog_Destructor========
                    // ======USB_Constructor========
                    break;
                }

                if ("".equals(nmea.trim())) break;

                int length = nmea.length();

                if (length < 7) {
                    Logging.errorln("Invalid NMEA: string too short");
                    break;
                }

                if (!nmea.startsWith("$")) {
                    Logging.errorln("Invalid NMEA: not started with '$'");
                    break;
                }

                if (!nmea.matches("(.*)*[0-9A-Fa-f]{2}$")) {
                    Logging.errorln("Invalid NMEA: ended with [%s]", nmea.substring(length - 3));
                    break;
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
                    break;
                }

                boolean expected = logger.sendJob != null && logger.sendJob.isRespExpected(dataField);
                if (expected) {
                    // If the expected response of last SendJob
                    // Cancel the NoResp timer immediately, otherwise the long time NMEA handling (like export gpx/kml) may cause timeout.
                    logger.sendJob.cancelNoRespTimer();
                }

                // Dispatch NMEA to relevant handler.
                success = logger.dispatchNmea(dataField.split(","));

                if (expected) {
                    // SendJob level
                    if (Utils.isNotEmpty(logger.sendJob.desc)) {
                        Logging.debugln("%s...%s", logger.sendJob.desc, success ? "success" : "failed");
                    }

                    // Task level
                    // Task unrelated SendJob. Nothing to do.
                    if (logger.sendJob instanceof SendJob.NonTask) {
                        logger.sendJob = null;
                        return;
                    }

                    // Task related SendJob. Need to determine if task is done.
                    logger.sendJob = null;
                    if (logger.loggerTask != null) {
                        if (success) {
                            if (logger.isSendJobQueueEmpty()) {
                                LoggerTask task = logger.loggerTask;
                                logger.loggerTask = null;
                                Logging.infoln("%s...success", task.name);
                                // Notify caller the success.
                                Platform.runLater(task::onSuccess);
                            }
                        } else {
                            Logging.errorln("%s...failed", logger.loggerTask.name);
                            // LoggerThread will take care this failure case
                        }
                    }
                } else {
                    // Don't care other not intended incoming NMEAs
                    success = true;
                }

                break;

            default:
                break;
        }
    }
}
