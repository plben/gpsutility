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

import net.benpl.gpsutility.export.ExportType;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.serialport.SPort;
import net.benpl.gpsutility.serialport.SPortProperty;
import net.benpl.gpsutility.type.ILoggerStateListener;

import java.util.List;

/**
 * Task issued by controller and executed by Logger entity.
 */
abstract public class LoggerTask<T extends GpsLogger> {
    protected final String name;

    /**
     * Constructor of task.
     *
     * @param name The name of this task.
     */
    public LoggerTask(String name) {
        this.name = name;
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
     * @param gpsLogger The logger to execute this task.
     * @return TRUE - task accepted, FALSE - task rejected.
     */
    public boolean preRun(T gpsLogger) {
        return true;
    }

    /**
     * Callback before task execution to update UI components state.
     */
    abstract public void onStart();

    /**
     * The task execution body.
     *
     * @param gpsLogger The logger to execute this task.
     */
    abstract public void run(T gpsLogger);

    /**
     * Callback on task execution success.
     */
    abstract public void onSuccess();

    /**
     * Callback on task execution failure.
     */
    abstract public void onFail();

    /**
     * ConnectLogger Task
     */
    abstract public static class Connect extends LoggerTask<GpsLogger> {
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
        public Connect(SPort sPort, int serialPortBaudRateIdx, int serialPortDataBitsIdx, int serialPortParityIdx, int serialPortStopBitsIdx, int serialPortFlowCtrlIdx, ILoggerStateListener loggerStateListener) {
            super("Connect Logger");
            this.sPort = sPort;
            this.serialPortBaudRateIdx = serialPortBaudRateIdx;
            this.serialPortDataBitsIdx = serialPortDataBitsIdx;
            this.serialPortParityIdx = serialPortParityIdx;
            this.serialPortStopBitsIdx = serialPortStopBitsIdx;
            this.serialPortFlowCtrlIdx = serialPortFlowCtrlIdx;
            this.loggerStateListener = loggerStateListener;
        }

        @Override
        public boolean preRun(GpsLogger gpsLogger) {
            return gpsLogger.preConnect();
        }

        @Override
        public void run(GpsLogger gpsLogger) {
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
    abstract public static class Disconnect extends LoggerTask<GpsLogger> {
        /**
         * Manually stop GPS Logger.
         */
        public Disconnect() {
            super("Disconnect Logger");
        }

        @Override
        public void run(GpsLogger gpsLogger) {
            if (gpsLogger.preDisconnect()) {
                gpsLogger.postDisconnect();
            }
        }
    }

    /**
     * DebugNmea Task
     */
    abstract public static class DebugNmea extends LoggerTask<GpsLogger> {
        private final String nmea;

        /**
         * Debug NMEA command received from user input.
         * Wrap into SendJob and enqueue it to the job queue.
         *
         * @param nmea The NMEA command.
         */
        public DebugNmea(String nmea) {
            super("Debug NMEA");
            this.nmea = nmea;
        }

        @Override
        public void run(GpsLogger gpsLogger) {
            gpsLogger.loggerThread.enqueueSendJob(new SendJob(gpsLogger, null, nmea, null));
        }
    }

    /**
     * UploadTrack Task
     */
    abstract public static class UploadTrack extends LoggerTask<GpsLogger> {
        private final String filePath;
        private final List<ExportType> exportTypes;

        /**
         * Upload logger data operation performed by user.
         *
         * @param filePath    Folder the logger data will be uploaded to.
         * @param exportTypes The export file formats.
         */
        public UploadTrack(String filePath, List<ExportType> exportTypes) {
            super("Upload Track");
            this.filePath = filePath;
            this.exportTypes = exportTypes;
        }

        @Override
        public void run(GpsLogger gpsLogger) {
            gpsLogger.uploadTrack(filePath, exportTypes);
        }

        /**
         * Callback on upload track progress update.
         *
         * @param progress The upload progress.
         */
        abstract public void onProgress(double progress);
    }

}
