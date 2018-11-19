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

package net.benpl.gpsutility.logger.holux_m241;

import javafx.application.Platform;
import net.benpl.gpsutility.logger.ActionListener;
import net.benpl.gpsutility.logger.SendJob;
import net.benpl.gpsutility.logger.StateListener;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;

import java.util.LinkedList;

/**
 * Holux M-241 implementation of {@link net.benpl.gpsutility.logger.ActionTask}.
 */
abstract public class ActionTask extends net.benpl.gpsutility.logger.ActionTask<GpsLogger> {
    /**
     * Constructor.
     *
     * @param name           Name of this task.
     * @param gpsLogger      Logger entity to execute this task.
     * @param actionListener Listener on task execution.
     */
    public ActionTask(String name, GpsLogger gpsLogger, ActionListener actionListener) {
        super(name, gpsLogger, actionListener);
    }

    /**
     * Connect task - Connect to external GPS Data Logger.
     */
    public static class Connect extends net.benpl.gpsutility.logger.ActionTask.Connect<GpsLogger> {
        /**
         * Perform start GPS logger operation.
         *
         * @param gpsLogger       The logger entity to execute this task.
         * @param actionListener  Listener on task execution.
         * @param commPort        Serial port to talk with this logger.
         * @param commBaudRateIdx Index of {@link CommProperty#commBaudRateList}
         * @param commDataBitsIdx Index of {@link CommProperty#commDataBitsList}
         * @param commParityIdx   Index of {@link CommProperty#commParityList}
         * @param commStopBitsIdx Index of {@link CommProperty#commStopBitsList}
         * @param commFlowCtrlIdx Index of {@link CommProperty#commFlowCtrlList}
         * @param stateListener   Listener on state changed of logger entity..
         */
        public Connect(GpsLogger gpsLogger, ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener stateListener) {
            super(gpsLogger, actionListener, commPort, commBaudRateIdx, commDataBitsIdx, commParityIdx, commStopBitsIdx, commFlowCtrlIdx, stateListener);
        }

        /**
         * Callback to listen on logger state changed.
         *
         * @param state The new state.
         */
        @Override
        protected void stateChanged(int state) {
            if (state == GpsLogger.STATE_SERIALPORT_OPENED) {
                // Once serial port is ready, launch a batch of NMEA commands.
                gpsLogger.enqueueSendJob(
                        new SendJob(gpsLogger, "Handshake", "PHLX810", "PHLX852") {
                            @Override
                            public boolean handle(String nmea) {
                                // Validate logger ID
                                if (!GpsLogger.LOGGER_ID.equals(nmea)) {
                                    Logging.errorln("Invalid logger ID: [%s]", nmea);
                                    return false;
                                }

                                // Transit logger state to HANDSHAKED
                                gpsLogger.setState(GpsLogger.STATE_HANDSHAKED);
                                return true;
                            }
                        }, // Query logger for module ID
                        new SendJob(gpsLogger, "Switch to USB-Mode", "HOLUX241,1", "HOLUX001,1") {
                            @Override
                            public boolean handle(String nmea) {
                                // Transit logger entity to USB_MODE state
                                gpsLogger.setState(GpsLogger.STATE_USB_MODE);
                                // Schedule TimerTask to keep USB_MODE alive.
                                gpsLogger.startUsbModeTimer();
                                return true;
                            }
                        }, // Transit HoluxM241 into USB_MODE; Stop logging if started (TODO: Is it PHLX826->PHLX859 for GR245???)
                        new SendJob(gpsLogger, "Query SPI status", "PMTK182,2,1", "PMTK182,3,1") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.spiStatus = Integer.parseInt(nmea);
                                return true;
                            }
                        }, // Query HoluxM241 for SPI status
                        new SendJob(gpsLogger, "Query FmtReg", "PMTK182,2,2", "PMTK182,3,2") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.fmtReg = Long.parseLong(nmea, 16);
                                return true;
                            }
                        }, // Query HoluxM241 for log format register
                        new SendJob(gpsLogger, "Query BySec", "PMTK182,2,3", "PMTK182,3,3") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.bySec = Integer.parseInt(nmea);
                                if (gpsLogger.configPaneController != null)
                                    gpsLogger.configPaneController.setRecordBySec(gpsLogger.bySec);
                                return true;
                            }
                        }, // Query HoluxM241 for interval (in 0.1 second) of BySEC
                        new SendJob(gpsLogger, "Query ByDist", "PMTK182,2,4", "PMTK182,3,4") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.byDist = Integer.parseInt(nmea);
                                if (gpsLogger.configPaneController != null)
                                    gpsLogger.configPaneController.setRecordByDist(gpsLogger.byDist);
                                return true;
                            }
                        }, // Query HoluxM241 for interval (in 0.1 meter) of ByDistance
                        new SendJob(gpsLogger, "Query BySpeed", "PMTK182,2,5", "PMTK182,3,5") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.bySpeed = Integer.parseInt(nmea);
                                return true;
                            }
                        }, // Query HoluxM241 for interval (in 0.1 km/h) of BySpeed
                        new SendJob(gpsLogger, "Query RcdMethod", "PMTK182,2,6", "PMTK182,3,6") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.rcdMethod = Integer.parseInt(nmea);
                                if (gpsLogger.configPaneController != null)
                                    gpsLogger.configPaneController.setRcdMethod(gpsLogger.rcdMethod);
                                return true;
                            }
                        }, // Query HoluxM241 for record method. (Overlap or StopOnFull)
                        new SendJob(gpsLogger, "Query LoggerState", "PMTK182,2,7", "PMTK182,3,7") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.loggerStatus = Integer.parseInt(nmea);
                                // TODO: need to take care
                                // Bit [10]: logger need format bit
                                // Bit [11]: logger full bit
                                return true;
                            }
                        }, // Query HoluxM241 for log status. (Start/Stop)
                        new SendJob(gpsLogger, "Query RcdAddr", "PMTK182,2,8", "PMTK182,3,8") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.rcdAddr = Long.parseLong(nmea, 16);
                                return true;
                            }
                        }, // Query HoluxM241 for record address. (next write)
                        new SendJob(gpsLogger, "Query flashID", "PMTK182,2,9", "PMTK182,3,9") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.flashId = nmea;
                                return true;
                            }
                        }, // Query HoluxM241 for flash ID
                        new SendJob(gpsLogger, "Query RcdTotal", "PMTK182,2,10", "PMTK182,3,10") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.rcdRcnt = Long.parseLong(nmea, 16);
                                return true;
                            }
                        }, // Query HoluxM241 for total records
                        new SendJob(gpsLogger, "Query FailSectors", "PMTK182,2,11", "PMTK182,3,11") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.failSector = Utils.hexStringToByteArray(nmea);
                                return true;
                            }
                        }, // Query HoluxM241 for FailSector in flash
                        new SendJob(gpsLogger, "Query MtkVersion", "PMTK182,2,12", "PMTK182,3,12") {
                            @Override
                            public boolean handle(String nmea) {
                                gpsLogger.mtkVersion = Integer.parseInt(nmea);
                                return true;
                            }
                        }, // Query HoluxM241 for MTK hardware version
                        new SendJob(gpsLogger, "Query FwVer", "HOLUX241,3", "HOLUX001,3") {
                            @Override
                            public boolean handle(String nmea) {
                                // Firmware version info
                                gpsLogger.fwVer = String.format("%.02f", Float.parseFloat(nmea) / 100);
                                return true;
                            }
                        }, // Query HoluxM241 for firmware version
                        new SendJob(gpsLogger, "Query HwVer", "HOLUX241,7", "HOLUX001,7") {
                            @Override
                            public boolean handle(String nmea) {
                                // Hardware version
                                gpsLogger.hwVer = nmea;
                                return true;
                            }
                        }, // Query HoluxM241 for hardware version
                        new SendJob(gpsLogger, "Query UserName", "HOLUX241,5", "HOLUX001,5") {
                            @Override
                            public boolean handle(String nmea) {
                                // User name
                                gpsLogger.userName = nmea;
                                return true;
                            }
                        }, // Query HoluxM241 for user name
                        new SendJob(gpsLogger, "Query RcdBy", "HOLUX241,8", "HOLUX001,8", true) {
                            @Override
                            public boolean handle(String nmea) {
                                // Record By
                                gpsLogger.rcdBy = Integer.parseInt(nmea);
                                return true;
                            }
                        } // Query HoluxM241 for record method. (0: BySec, 1: ByDist)
                );
            }
        }
    }

    /**
     * Disconnect task - Disconnect from external GPS Data Logger.
     */
    public static class Disconnect extends net.benpl.gpsutility.logger.ActionTask.Disconnect<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         */
        public Disconnect(GpsLogger gpsLogger, ActionListener actionListener) {
            super(gpsLogger, actionListener);
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            if (gpsLogger.getState() == GpsLogger.STATE_USB_MODE) {
                // If in USB_MODE, transit HOLUX M-241 out of it.
                gpsLogger.enqueueSendJob(
                        new SendJob(gpsLogger, "Exit USB-Mode", "HOLUX241,2", "HOLUX001,2", true) {
                            @Override
                            public boolean handle(String nmea) {
                                // Cancel any pending USB_MODE TimerTask
                                gpsLogger.cancelUsbModeTimer();
                                // Transit logger entity to SERIALPORT_READY state
                                gpsLogger.setState(GpsLogger.STATE_SERIALPORT_OPENED);
                                return true;
                            }
                        } // Transit HoluxM241 out from USB_MODE (TODO: Is it PHLX827->PHLX860 for GR245???)
                );
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * DebugNmea task - Send NMEA sentence to external GPS Data Logger for debug purpose.
     */
    public static class DebugNmea extends net.benpl.gpsutility.logger.ActionTask.DebugNmea<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         * @param nmea           The NMEA command to be sent for debug purpose.
         */
        public DebugNmea(GpsLogger gpsLogger, ActionListener actionListener, String nmea) {
            super(gpsLogger, actionListener, nmea);
        }
    }

    /**
     * UploadTrack task - Upload log data from external GPS Data Logger.
     */
    public static class UploadTrack extends net.benpl.gpsutility.logger.ActionTask.UploadTrack<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         */
        public UploadTrack(GpsLogger gpsLogger, ActionListener actionListener) {
            super(gpsLogger, actionListener);
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            Logging.infoln("Reading data from [%s]...", gpsLogger.toString());

            LinkedList<SendJob> jobs = new LinkedList<>();

            if ((gpsLogger.loggerStatus & 0x0002) != 0) {
                // If auto-log is started, stop it
                jobs.add(new SendJob(gpsLogger, "Stop logging", "PMTK182,5", "PMTK001,182,5,3")); // Stop logging
                jobs.add(new SendJob(gpsLogger, "Query LoggerState", "PMTK182,2,7", "PMTK182,3,7") {
                    @Override
                    public boolean handle(String nmea) {
                        gpsLogger.loggerStatus = Integer.parseInt(nmea);
                        // TODO: need to take care
                        // Bit [10]: logger need format bit
                        // Bit [11]: logger full bit
                        return true;
                    }
                }); // Query HoluxM241 for log status.
            }
            jobs.add(new SendJob(gpsLogger, null, "PMTK182,2,8", "PMTK182,3,8") {
                @Override
                public boolean handle(String nmea) {
                    gpsLogger.rcdAddr = Long.parseLong(nmea, 16);

                    gpsLogger.totalBlocks = (int) (gpsLogger.rcdAddr / 0x400) + 1;
                    gpsLogger.readAddr = 0;
                    gpsLogger.logData = null;
                    gpsLogger.enqueueSendJob(new SendJob(gpsLogger, null, String.format("PMTK182,7,%08X,00000400", gpsLogger.readAddr), String.format("PMTK182,8,%08X", gpsLogger.readAddr), (gpsLogger.readAddr + 0x400) >= gpsLogger.rcdAddr) {
                        @Override
                        public boolean handle(String nmea) {
                            handleUploadData(gpsLogger, (ActionListener.UploadTrack) getActionListener(), nmea);
                            return true;
                        }
                    }); // Read log of 1KB size

                    return true;
                }
            }); // Query HoluxM241 for record address. (next write)

            // Send these NMEA commands to HOLUX M-241
            gpsLogger.enqueueSendJob(jobs.toArray(new SendJob[0]));
            return false;
        }

        /**
         * Post task execution body.
         *
         * @param cause The cause of task execution.
         */
        @Override
        protected void postRun(CAUSE cause) {
            if (cause == CAUSE.SUCCESS) {
                gpsLogger.postUploadTrack();
            } else {
                super.postRun(cause);
            }
        }
    }

    /**
     * Method to handle log data received from serial port.
     *
     * @param gpsLogger      The associated logger entity.
     * @param actionListener Listener on task execution.
     * @param nmea           The received NMEA sentence.
     */
    private static void handleUploadData(GpsLogger gpsLogger, ActionListener.UploadTrack actionListener, String nmea) {
        Platform.runLater(() -> actionListener.onProgress(((double) gpsLogger.readAddr / 1024.0 + 1.0) / (double) gpsLogger.totalBlocks));

        byte[] logSeg = Utils.hexStringToByteArray(nmea);
        gpsLogger.logData = Utils.concatByteArray(gpsLogger.logData, logSeg);

        gpsLogger.readAddr += 0x400;
        if (gpsLogger.readAddr < gpsLogger.rcdAddr) {
            gpsLogger.enqueueSendJob(
                    new SendJob(gpsLogger, null, String.format("PMTK182,7,%08X,00000400", gpsLogger.readAddr), String.format("PMTK182,8,%08X", gpsLogger.readAddr), (gpsLogger.readAddr + 0x400) >= gpsLogger.rcdAddr) {
                        @Override
                        public boolean handle(String nmea) {
                            handleUploadData(gpsLogger, actionListener, nmea);
                            return true;
                        }
                    } // Read log of 1KB size
            );
        } else {
            Logging.infoln("Read data from [%s]...success", gpsLogger.toString());
        }
    }

    /**
     * SaveConfig task - Save config to external GPS Data Logger.
     */
    public static class SaveConfig extends ActionTask {
        private final int rcdMethod;
        private final int rcdBy;
        private final int bySec;
        private final int byDist;

        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         * @param rcdMethod      Config - record method
         * @param rcdBy          Config - record by
         * @param bySec          Config - value of record by seconds
         * @param byDist         Config - value of record by distance
         */
        public SaveConfig(GpsLogger gpsLogger, ActionListener actionListener, int rcdMethod, int rcdBy, int bySec, int byDist) {
            super("Save Config", gpsLogger, actionListener);
            this.rcdMethod = rcdMethod;
            this.rcdBy = rcdBy;
            this.bySec = bySec;
            this.byDist = byDist;
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            SendJob[] jobs = new SendJob[3];

            jobs[0] = new SendJob(gpsLogger, "Save RcdMethod", "PMTK182,1,6," + rcdMethod, "PMTK001,182,1,3"); // Change record method. (1: BySec, 2: ByDist)

            if (rcdBy == 0) {
                jobs[1] = new SendJob(gpsLogger, "Save BySec", "PMTK182,1,3," + bySec * 10, "PMTK001,182,1,3"); // Change value of record by second
            } else {
                jobs[1] = new SendJob(gpsLogger, "Save ByDist", "PMTK182,1,4," + byDist * 10, "PMTK001,182,1,3"); // Change value of record by distance
            }

            jobs[2] = new SendJob(gpsLogger, "Save RcdBy", "HOLUX241,9," + rcdBy, "HOLUX001,9", true); // Change record by. (0: BySec, 1: ByDist)

            // Send these NMEA commands to HOLUX M-241
            gpsLogger.enqueueSendJob(jobs);
            return false;
        }
    }

    /**
     * ModUserName task - Modify user name of external GPS Data Logger.
     */
    public static class ModUserName extends ActionTask {
        private final String userName;

        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         * @param userName       The new user name.
         */
        public ModUserName(GpsLogger gpsLogger, ActionListener actionListener, String userName) {
            super("Modify UserName", gpsLogger, actionListener);
            this.userName = userName;
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            gpsLogger.enqueueSendJob(new SendJob(gpsLogger, null, "HOLUX241,4," + userName, "HOLUX001,4", true) {
                @Override
                public boolean handle(String nmea) {
                    gpsLogger.userName = userName;
                    return true;
                }
            }); // Change logger user name

            return false;
        }
    }
}
