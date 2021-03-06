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
import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * GpsLogger is the entity responsible for communication with GPS Data Logger.
 * <p>
 * It accepts action performed by FX controller, talks with GPS Data Logger via bound serial port, and notifies
 * FX controller on execution result (success/failure/progress/...)
 */
abstract public class GpsLogger implements NmeaListener {

    // Pre-defined GpsLogger states
    public static final int STATE_IDLE = 1001;
    public static final int STATE_SERIALPORT_OPENING = 1002;
    public static final int STATE_SERIALPORT_OPENED = 1003;
    public static final int STATE_HANDSHAKED = 1004;

    /**
     * State of this logger entity
     */
    protected int state;
    /**
     * Name of this logger entity
     */
    protected final String name;
    /**
     * Serial port bound to this logger entity
     */
    protected CommPort commPort;
    /**
     * Serial port BaudRate (Index of {@link CommProperty#commBaudRateList})
     */
    protected int commBaudRateIdx;
    /**
     * Serial port DataBits (Index of {@link CommProperty#commDataBitsList})
     */
    protected int commDataBitsIdx;
    /**
     * Serial port Parity (Index of {@link CommProperty#commParityList})
     */
    protected int commParityIdx;
    /**
     * Serial port StopBits (Index of {@link CommProperty#commStopBitsList})
     */
    protected int commStopBitsIdx;
    /**
     * Serial port FlowCtrl (Index of {@link CommProperty#commFlowCtrlList})
     */
    protected int commFlowCtrlIdx;

    /**
     * Working thread of this logger entity.
     * It is created when serial port is ready for communication, and stopped when user perform a Disconnect action or in case of failure.
     */
    protected LoggerThread loggerThread;
    /**
     * ActionTask is being executed.
     */
    protected ActionTask actionTask;
    /**
     * SendJob is being executed, and waiting for expected response.
     */
    protected SendJob sendJob;
    /**
     * Listener on logger entity state changed.
     */
    protected StateListener stateListener;

    /**
     * Constructor.
     *
     * @param name            Name of this logger.
     * @param commBaudRateIdx The index of {@link CommProperty#commBaudRateList}
     * @param commDataBitsIdx The index of {@link CommProperty#commDataBitsList}
     * @param commParityIdx   The index of {@link CommProperty#commParityList}
     * @param commStopBitsIdx The index of {@link CommProperty#commStopBitsList}
     * @param commFlowCtrlIdx The index of {@link CommProperty#commFlowCtrlList}
     */
    public GpsLogger(String name, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx) {
        this.name = name;
        this.commBaudRateIdx = commBaudRateIdx;
        this.commDataBitsIdx = commDataBitsIdx;
        this.commParityIdx = commParityIdx;
        this.commStopBitsIdx = commStopBitsIdx;
        this.commFlowCtrlIdx = commFlowCtrlIdx;

        this.commPort = null;
        this.loggerThread = null;
        this.actionTask = null;
        this.sendJob = null;
        this.stateListener = null;
        this.state = STATE_IDLE;
    }

    /**
     * Used by {@link PrimaryController#loggerChooser}.
     *
     * @return Pre-configured logger name.
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Get name of this logger entity.
     *
     * @return Name of logger entity.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get current state of this logger entity.
     *
     * @return State of logger entity.
     */
    public int getState() {
        return this.state;
    }

    /**
     * Transit logger entity to new state.
     *
     * @param state New state.
     */
    public void setState(int state) {
        StateListener listener = this.stateListener;
        ActionTask task = this.actionTask;

        Platform.runLater(() -> {
            if (listener != null) {
                listener.stateChanged(state);
            }
            if (task != null) {
                task.stateChanged(state);
            }
        });

        this.state = state;
    }

    /**
     * Get LogParser for log data uploaded from GPS Data Logger.
     *
     * @return The LogParser.
     */
    abstract protected LogParser getParser();

    /**
     * Call hook to reset subclass state & variables.
     */
    abstract protected void preResetLogger();

    /**
     * Method to reset logger state and all variables.
     */
    void resetLogger() {
        // The chance for subclass to cleanup resources.
        this.preResetLogger();

        // Close associated serial port
        if (this.commPort != null) {
            this.commPort.closePort();
            this.commPort = null;
        }

        // Stop this thread if still running
        if (this.loggerThread != null) {
            this.loggerThread.stopThread();
            this.loggerThread = null;
        }

        // Transit logger entity state.
        // This may cause state changed notifications.
        this.setState(STATE_IDLE);

        this.stateListener = null;
        this.actionTask = null;
        this.sendJob = null;
    }

    /**
     * Method to stop working thread.
     */
    public void stopThread() {
        if (this.loggerThread != null) {
            this.loggerThread.stopThread();
        }
    }

    /**
     * Method to enqueue SendJobs to working thread and get it notified.
     *
     * @param jobs The jobs to be executed.
     */
    public void enqueueSendJob(@NotNull SendJob... jobs) {
        this.loggerThread.enqueueSendJob(jobs);
    }

    /**
     * Method to cancel all pending SendJobs in working thread.
     */
    void cancelAllSendJobs() {
        if (this.loggerThread != null) {
            this.loggerThread.cancelSendJobs();
        }
    }

    /**
     * Execute task performed by FX controller.
     *
     * @param actionTask The task to be executed.
     */
    protected void execActionTask(ActionTask actionTask) {
        // Reject if a task is still being executed.
        if (this.actionTask != null) {
            Logging.errorln("ActionTask [%s] is still being executed.", this.actionTask.getName());
            return;
        }

        // Reject if task precondition checking failed.
        if (!actionTask.precondition()) return;

        // Execute task
        if (actionTask.exec()) {
            // Task done
            // Post execution handling
            actionTask.postExec(ActionTask.CAUSE.SUCCESS);
        }
    }

    /**
     * Perform Connect action.
     *
     * @param actionListener      Listener on action performed.
     * @param commPort            Serial port connected to GPS Data Logger.
     * @param commBaudRateIdx     The index of {@link CommProperty#commBaudRateList}
     * @param commDataBitsIdx     The index of {@link CommProperty#commDataBitsList}
     * @param commParityIdx       The index of {@link CommProperty#commParityList}
     * @param commStopBitsIdx     The index of {@link CommProperty#commStopBitsList}
     * @param commFlowCtrlIdx     The index of {@link CommProperty#commFlowCtrlList}
     * @param loggerStateListener Listener on logger entity state changed.
     */
    abstract protected void performConnect(ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener loggerStateListener);

    /**
     * Method to create and return logger specific Config/Control panels.
     *
     * @return The created panels.
     */
    abstract public LinkedHashMap<String, AnchorPane> createLoggerPanel();

    /**
     * Perform Disconnect action.
     *
     * @param actionListener Listener on action performed.
     */
    abstract protected void performDisconnect(ActionListener actionListener);

    /**
     * Perform DebugNmea action.
     *
     * @param actionListener Listener on action performed.
     * @param nmea           NMEA sentence to be sent to GPS Data Logger.
     */
    abstract protected void performDebugNmea(ActionListener actionListener, String nmea);

    /**
     * Perform UploadTrack action.
     *
     * @param actionListener Listener on action performed.
     */
    abstract protected void performUploadTrack(ActionListener actionListener);

    /**
     * NMEA string received from serial port.
     *
     * @param nmea NMEA string received from serial port.
     */
    @Override
    final public void recvNmea(String nmea) {
        if (this.loggerThread != null) {
            // Wrap it into RecvJob and enqueue to working thread.
            this.loggerThread.enqueueRecvJob(new RecvJob(this, nmea));
        }
    }

}
