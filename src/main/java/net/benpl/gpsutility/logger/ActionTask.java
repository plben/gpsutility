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
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;

/**
 * ActionTask is the wrapper of action performed by FX controller, and to be executed by logger entity.
 */
abstract public class ActionTask<T extends GpsLogger> {

    /**
     * The pre-defined task execution results.
     */
    public enum CAUSE {
        SUCCESS, HANDLE_NMEA_FAIL, NO_RESP, SEND_DATA_FAIL
    }

    /**
     * Name of this task.
     */
    protected final String name;
    /**
     * Logger entity to execute this task.
     */
    protected final T gpsLogger;
    /**
     * Listener on task execution.
     */
    protected final ActionListener actionListener;

    /**
     * Constructor.
     *
     * @param name           Name of this task.
     * @param gpsLogger      Logger entity to execute this task.
     * @param actionListener Listener on task execution.
     */
    public ActionTask(String name, T gpsLogger, ActionListener actionListener) {
        this.name = name;
        this.gpsLogger = gpsLogger;
        this.actionListener = actionListener;
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
     * Get action listener of this task.
     *
     * @return The action listener.
     */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /**
     * Precondition checking for this task.
     *
     * @return TRUE - task accepted, FALSE - task rejected.
     */
    public boolean precondition() {
        return true;
    }

    /**
     * Execute this task. (invoked by logger entity)
     *
     * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
     * FALSE - task started, waiting for response.
     */
    boolean exec() {
        // Save task reference
        gpsLogger.actionTask = this;

        Logging.infoln("\n%s...start", name);

        // Task pre-start callback (the chance for UI to enable/disable components before task actually started)
        actionListener.onStart();

        // Run this task
        return run();
    }

    /**
     * Task execution body.
     *
     * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
     * FALSE - task started, waiting for response.
     */
    abstract protected boolean run();

    /**
     * Callback to listen on logger state changed.
     *
     * @param state The new state.
     */
    protected void stateChanged(int state) {
    }

    /**
     * Post task execution with SUCCESS or ERROR result. (invoked by logger entity)
     *
     * @param result Result of task execution.
     */
    protected void postExec(CAUSE result) {
        if (result == CAUSE.SUCCESS) {
            Logging.infoln("%s...success", name);
        } else {
            Logging.errorln("%s...failed", name);
        }

        Platform.runLater(() -> {
            // Notify FX the success or failure.
            if (result == CAUSE.SUCCESS) actionListener.onSuccess();
            else actionListener.onFail(result);

            // Post execution handling
            // Task specific implementation to cleanup resources, stop logger entity, or ...
            if (result == CAUSE.SEND_DATA_FAIL) {
                // Stop logger entity on fatal error.
                if (gpsLogger.loggerThread != null) {
                    gpsLogger.loggerThread.stopThread();
                }
            } else {
                // Or let each task to take further action.
                postRun(result);
            }
        });
    }

    /**
     * Post task execution body.
     *
     * @param result Result of task execution.
     */
    protected void postRun(CAUSE result) {
        if (result != CAUSE.SUCCESS) {
            if (gpsLogger.loggerThread != null) {
                gpsLogger.loggerThread.stopThread();
            }
        } else {
            gpsLogger.actionTask = null;
        }
    }

    /**
     * Connect task - Connect to GPS Data Logger.
     *
     * @param <P> Class type of logger entity.
     */
    abstract static public class Connect<P extends GpsLogger> extends ActionTask<P> {
        /**
         * Constructor.
         *
         * @param gpsLogger       Logger entity to execute this task.
         * @param actionListener  Listener on task execution.
         * @param commPort        Serial port to talk with this logger.
         * @param commBaudRateIdx Index of {@link CommProperty#commBaudRateList}
         * @param commDataBitsIdx Index of {@link CommProperty#commDataBitsList}
         * @param commParityIdx   Index of {@link CommProperty#commParityList}
         * @param commStopBitsIdx Index of {@link CommProperty#commStopBitsList}
         * @param commFlowCtrlIdx Index of {@link CommProperty#commFlowCtrlList}
         * @param stateListener   Listener on state changed of logger entity..
         */
        public Connect(P gpsLogger, ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener stateListener) {
            super("Connect Logger", gpsLogger, actionListener);
            this.gpsLogger.commPort = commPort;
            this.gpsLogger.commBaudRateIdx = commBaudRateIdx;
            this.gpsLogger.commDataBitsIdx = commDataBitsIdx;
            this.gpsLogger.commParityIdx = commParityIdx;
            this.gpsLogger.commStopBitsIdx = commStopBitsIdx;
            this.gpsLogger.commFlowCtrlIdx = commFlowCtrlIdx;
            this.gpsLogger.stateListener = stateListener;
        }

        /**
         * Precondition checking for this task.
         *
         * @return TRUE - task accepted, FALSE - task rejected.
         */
        @Override
        public boolean precondition() {
            // Validate logger entity state
            if (gpsLogger.state == GpsLogger.STATE_IDLE) {
                return true;
            } else {
                Logging.errorln("ConnectLogger: invalid state %d", gpsLogger.state);
                return false;
            }
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            // Set serial port parameters
            gpsLogger.commPort.setParameters(
                    CommProperty.commBaudRateList.get(gpsLogger.commBaudRateIdx).getData(),
                    CommProperty.commDataBitsList.get(gpsLogger.commDataBitsIdx).getData(),
                    CommProperty.commParityList.get(gpsLogger.commParityIdx).getData(),
                    CommProperty.commStopBitsList.get(gpsLogger.commStopBitsIdx).getData(),
                    CommProperty.commFlowCtrlList.get(gpsLogger.commFlowCtrlIdx).getData()
            );

            // Listen on serial port
            gpsLogger.commPort.setNmeaListener(gpsLogger);

            // Transit logger entity state
            gpsLogger.setState(GpsLogger.STATE_SERIALPORT_OPENING);

            // Open serial port in new Thread. (non-blocking)
            new Thread(() -> {
                // Open serial port
                boolean success = gpsLogger.commPort.openPort();

                if (success) {
                    // Success
                    // Create LoggerThread and start it
                    Logging.infoln("Starting thread [%s]...", gpsLogger.getName());
                    gpsLogger.loggerThread = new LoggerThread(gpsLogger);
                    gpsLogger.loggerThread.start();

                    // Wait for LoggerThread ready
                    // Otherwise, SendJobs triggered by STATE_SERIALPORT_OPENED can not be handled properly
                    while (!gpsLogger.loggerThread.running) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                    // Transit logger entity state
                    gpsLogger.setState(GpsLogger.STATE_SERIALPORT_OPENED);
                } else {
                    // Failed
                    // Reset logger state and variables
                    gpsLogger.resetLogger();
                }
            }, "Thread-ConnectLogger").start();

            // Keep waiting, postExec() can not be invoked yet.
            return false;
        }
    }

    /**
     * Disconnect task - Disconnect from GPS Data Logger.
     *
     * @param <P> Class type of logger entity.
     */
    abstract public static class Disconnect<P extends GpsLogger> extends ActionTask<P> {
        /**
         * Constructor.
         *
         * @param gpsLogger      Logger entity to execute this task.
         * @param actionListener Listener on task execution.
         */
        public Disconnect(P gpsLogger, ActionListener actionListener) {
            super("Disconnect Logger", gpsLogger, actionListener);
        }

        /**
         * Post task execution body.
         *
         * @param result Result of task execution.
         */
        @Override
        protected void postRun(CAUSE result) {
            gpsLogger.stopThread();
        }
    }

    /**
     * DebugNmea task - Send NMEA sentence to GPS Data Logger for debug purpose.
     *
     * @param <P> Class type of logger entity.
     */
    abstract public static class DebugNmea<P extends GpsLogger> extends ActionTask<P> {
        /**
         * The NMEA sentence to be sent for debug purpose.
         */
        private final String nmea;

        /**
         * Constructor.
         *
         * @param gpsLogger      Logger entity to execute this task.
         * @param actionListener Listener on task execution.
         * @param nmea           The NMEA sentence to be sent for debug purpose.
         */
        public DebugNmea(P gpsLogger, ActionListener actionListener, String nmea) {
            super("Debug NMEA", gpsLogger, actionListener);
            this.nmea = nmea;
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            // Wrap the NMEA sentence into SendJob and enqueue to working thread of logger entity.
            gpsLogger.enqueueSendJob(new SendJob(gpsLogger, null, nmea, null, true));
            return false;
        }
    }

    /**
     * UploadTrack task - Upload log data from GPS Data Logger.
     *
     * @param <P> Class type of logger entity.
     */
    abstract public static class UploadTrack<P extends GpsLogger> extends ActionTask<P> {
        /**
         * Constructor.
         *
         * @param gpsLogger      Logger entity to execute this task.
         * @param actionListener Listener on task execution.
         */
        public UploadTrack(P gpsLogger, ActionListener actionListener) {
            super("Upload Track", gpsLogger, actionListener);
        }
    }
}
