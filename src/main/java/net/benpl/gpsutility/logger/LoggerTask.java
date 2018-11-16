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
import net.benpl.gpsutility.serialport.SPort;
import net.benpl.gpsutility.serialport.SPortProperty;
import net.benpl.gpsutility.type.ILoggerStateListener;

/**
 * Task issued by controller and executed by Logger entity.
 */
abstract public class LoggerTask {

    public enum CAUSE {
        SUCCESS, HANDLE_NMEA_FAIL, NO_RESP, SEND_DATA_FAIL
    }

    protected final String name;
    protected final GpsLogger gpsLogger;

    /**
     * Constructor of task.
     *
     * @param name      The name of this task.
     * @param gpsLogger The logger entity to execute this task.
     */
    public LoggerTask(String name, GpsLogger gpsLogger) {
        this.name = name;
        this.gpsLogger = gpsLogger;
    }

    /**
     * Get name of this task.
     *
     * @return The name of this task.
     */
    public String getName() {
        return name;
    }

    /**
     * Callback prior to task execution, to double check if task can go ahead.
     * You can override this method for your task.
     *
     * @return TRUE - task accepted, FALSE - task rejected.
     */
    public boolean preRun() {
        return true;
    }

    /**
     * Callback before task execution to update UI components state.
     */
    abstract public void onStart();

    /**
     * The task execution body.
     */
    final void run0() {
        Logging.infoln("\n%s...start", name);

        // Task pre-start callback (the chance for UI to enable/disable components before task actually started)
        this.onStart();

        this.run();
    }

    /**
     * The task execution body.
     */
    abstract protected void run();

    /**
     * Callback on task execution success.
     */
    abstract public void onSuccess();

    /**
     * Callback on task execution failure.
     *
     * @param cause The failure cause.
     */
    abstract public void onFail(CAUSE cause);

    /**
     * Callback on task finished or error occurred.
     *
     * @param cause The cause when this event occurred.
     */
    final void postRun0(CAUSE cause) {
        if (cause == CAUSE.SUCCESS) {
            Logging.infoln("%s...success", name);
        } else {
            Logging.errorln("%s...failed", name);
        }

        Platform.runLater(() -> {
            // Notify issuer the success or failure.
            if (cause == CAUSE.SUCCESS) onSuccess();
            else onFail(cause);

            // Stop logger entity on fatal error.
            // Or let each task to take further action.
            if (cause == CAUSE.SEND_DATA_FAIL) {
                gpsLogger.stopLogger();
            } else {
                postRun(cause);
            }
        });
    }

    /**
     * Callback post task execution or error occurred, to release related resources.
     * You can override this method for your task.
     */
    protected void postRun(CAUSE cause) {
        if (cause != CAUSE.SUCCESS) {
            gpsLogger.stopLogger();
        }
    }

    /**
     * ConnectLogger Task
     */
    abstract public static class Connect extends LoggerTask {
        private final SPort sPort;
        private final int serialPortBaudRateIdx;
        private final int serialPortDataBitsIdx;
        private final int serialPortParityIdx;
        private final int serialPortStopBitsIdx;
        private final int serialPortFlowCtrlIdx;
        private final ILoggerStateListener loggerStateListener;

        /**
         * Perform start GPS logger operation.
         *
         * @param gpsLogger             The logger entity to execute this task.
         * @param sPort                 Serial port to talk with this logger.
         * @param serialPortBaudRateIdx The index of
         *                              {@link SPortProperty#serialPortBaudRateList}
         * @param serialPortDataBitsIdx The index of
         *                              {@link SPortProperty#serialPortDataBitsList}
         * @param serialPortParityIdx   The index of
         *                              {@link SPortProperty#serialPortParityList}
         * @param serialPortStopBitsIdx The index of
         *                              {@link SPortProperty#serialPortStopBitsList}
         * @param serialPortFlowCtrlIdx The index of
         *                              {@link SPortProperty#serialPortFlowCtrlList}
         * @param loggerStateListener   Listen to the state changed of Logger entity..
         */
        public Connect(GpsLogger gpsLogger, SPort sPort, int serialPortBaudRateIdx, int serialPortDataBitsIdx, int serialPortParityIdx, int serialPortStopBitsIdx, int serialPortFlowCtrlIdx, ILoggerStateListener loggerStateListener) {
            super("Connect Logger", gpsLogger);
            this.sPort = sPort;
            this.serialPortBaudRateIdx = serialPortBaudRateIdx;
            this.serialPortDataBitsIdx = serialPortDataBitsIdx;
            this.serialPortParityIdx = serialPortParityIdx;
            this.serialPortStopBitsIdx = serialPortStopBitsIdx;
            this.serialPortFlowCtrlIdx = serialPortFlowCtrlIdx;
            this.loggerStateListener = loggerStateListener;
        }

        @Override
        public boolean preRun() {
            return gpsLogger.preConnect();
        }

        @Override
        protected void run() {
            gpsLogger.sPort = sPort;
            // Save SerialPort properties
            gpsLogger.serialPortBaudRateIdx = serialPortBaudRateIdx;
            gpsLogger.serialPortDataBitsIdx = serialPortDataBitsIdx;
            gpsLogger.serialPortParityIdx = serialPortParityIdx;
            gpsLogger.serialPortStopBitsIdx = serialPortStopBitsIdx;
            gpsLogger.serialPortFlowCtrlIdx = serialPortFlowCtrlIdx;
            gpsLogger.loggerStateListener = loggerStateListener;

            this.sPort.setProperties(
                    SPortProperty.serialPortBaudRateList.get(serialPortBaudRateIdx).getData(),
                    SPortProperty.serialPortDataBitsList.get(serialPortDataBitsIdx).getData(),
                    SPortProperty.serialPortParityList.get(serialPortParityIdx).getData(),
                    SPortProperty.serialPortStopBitsList.get(serialPortStopBitsIdx).getData(),
                    SPortProperty.serialPortFlowCtrlList.get(serialPortFlowCtrlIdx).getData()
            );

            // Transit SM to SERIALPORT_OPENING
            gpsLogger.loggerState = GpsLogger.STATE_SERIALPORT_OPENING;

            // Listen on SerialPort
            this.sPort.setNmeaListener(gpsLogger);

            // Open SerialPort in new Thread. (non-blocking)
            new Thread(() -> {
                // Open SerialPort
                boolean success = sPort.openPort();

                if (success) {
                    // Success
                    // Transit SM to SERIALPORT_OPENED
                    gpsLogger.loggerState = GpsLogger.STATE_SERIALPORT_OPENED;

                    // Create LoggerThread and start it
                    Logging.infoln("Starting thread [%s]...", gpsLogger.loggerName);
                    gpsLogger.loggerThread = new LoggerThread(gpsLogger);
                    gpsLogger.loggerThread.start();

                    // Wait for LoggerThread ready
                    // Otherwise, NMEA command sent by serialPortReady() can not be handled properly
                    while (!gpsLogger.loggerThread.isRunning()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                    // Execute associated call hook.
                    gpsLogger.serialPortReady();
                } else {
                    // Failed
                    // Reset logger state and variables
                    gpsLogger.resetLogger();
                }
            }, "Thread-ConnectLogger").start();
        }
    }

    /**
     * DisconnectLogger Task
     */
    abstract public static class Disconnect extends LoggerTask {
        /**
         * Manually stop GPS Logger.
         *
         * @param gpsLogger The logger entity to execute this task.
         */
        public Disconnect(GpsLogger gpsLogger) {
            super("Disconnect Logger", gpsLogger);
        }

        @Override
        protected void run() {
            if (gpsLogger.preDisconnect()) {
                gpsLogger.postDisconnect();
            }
        }

        @Override
        protected void postRun(CAUSE cause) {
            gpsLogger.stopLogger();
        }
    }

    /**
     * DebugNmea Task
     */
    abstract public static class DebugNmea extends LoggerTask {
        private final String nmea;

        /**
         * Debug NMEA command received from user input.
         * Wrap into SendJob and enqueue it to the job queue.
         *
         * @param gpsLogger The logger entity to execute this task.
         * @param nmea      The NMEA command.
         */
        public DebugNmea(GpsLogger gpsLogger, String nmea) {
            super("Debug NMEA", gpsLogger);
            this.nmea = nmea;
        }

        @Override
        protected void run() {
            gpsLogger.loggerThread.enqueueSendJob(new SendJob(gpsLogger, null, nmea, null, true));
        }
    }

    /**
     * UploadTrack Task
     */
    abstract public static class UploadTrack extends LoggerTask {
        /**
         * Upload log data operation performed by user.
         *
         * @param gpsLogger The logger entity to execute this task.
         */
        public UploadTrack(GpsLogger gpsLogger) {
            super("Upload Track", gpsLogger);
        }

        @Override
        protected void run() {
            gpsLogger.uploadTrack();
        }

        /**
         * Callback on upload track progress update.
         *
         * @param progress The upload progress.
         */
        abstract public void onProgress(double progress);

        /**
         * Callback post task execution, to release related resources.
         * You can override this method for your task.
         */
        @Override
        protected void postRun(CAUSE cause) {
            if (cause == CAUSE.SUCCESS) {
                gpsLogger.postUploadTrack();
            } else {
                gpsLogger.stopLogger();
            }
        }
    }

}
