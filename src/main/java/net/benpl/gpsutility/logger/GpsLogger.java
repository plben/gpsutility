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
import net.benpl.gpsutility.export.ExportType;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.serialport.SPort;
import net.benpl.gpsutility.serialport.SPortProperty;
import net.benpl.gpsutility.type.ILoggerStateListener;
import net.benpl.gpsutility.type.INmeaListener;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * GPS Logger is the entity to handle tasks from controller.
 * It talks with external real GPS Logger via bound serial port, and notifies controller on task execution success/failure/progress/...
 */
abstract public class GpsLogger implements INmeaListener {

    protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    protected static final int STATE_IDLE = 1001;
    protected static final int STATE_SERIALPORT_OPENING = 1002;
    protected static final int STATE_SERIALPORT_OPENED = 1003;
    protected static final int STATE_HANDSHAKED = 1004;

    /**
     * Logger state can be IDLE, SERIALPORT_OPENING, SERIALPORT_READY, READY, and other logger specific states
     */
    protected int loggerState;

    protected final String loggerName;

    // Bound serial port to talk with external GPS Logger
    protected SPort sPort;
    // Serial port properties
    protected int serialPortBaudRateIdx = 4; // 38400
    protected int serialPortDataBitsIdx = 4; // 8 bits
    protected int serialPortParityIdx = 0; // None
    protected int serialPortStopBitsIdx = 0; // 1 bit
    protected int serialPortFlowCtrlIdx = 2; // None

    protected LoggerThread loggerThread;
    protected SendJob sendJob;
    protected LoggerTask loggerTask;
    protected ILoggerStateListener loggerStateListener;

    /**
     * Constructor of GPS Logger entity.
     *
     * @param loggerName            Name of this logger.
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
     */
    public GpsLogger(String loggerName, int serialPortBaudRateIdx, int serialPortDataBitsIdx, int serialPortParityIdx, int serialPortStopBitsIdx, int serialPortFlowCtrlIdx) {
        this.loggerName = loggerName;
        // Save SerialPort properties
        this.serialPortBaudRateIdx = serialPortBaudRateIdx;
        this.serialPortDataBitsIdx = serialPortDataBitsIdx;
        this.serialPortParityIdx = serialPortParityIdx;
        this.serialPortStopBitsIdx = serialPortStopBitsIdx;
        this.serialPortFlowCtrlIdx = serialPortFlowCtrlIdx;

        this.sPort = null;
        this.sendJob = null;
        this.loggerTask = null;
        this.loggerStateListener = null;
        this.loggerThread = null;
        this.loggerState = STATE_IDLE;
    }

    /**
     * Reset logger state and all variables.
     */
    final protected void resetLogger() {
        this.preResetLogger();

        if (this.loggerStateListener != null) {
            ILoggerStateListener listener = this.loggerStateListener;
            Platform.runLater(listener::loggerIdle);
            this.loggerStateListener = null;
        }

        if (sPort != null) {
            sPort.closePort();
            sPort = null;
        }

        this.sendJob = null;
        this.loggerTask = null;

        // Stop this thread if still running
        if (this.loggerThread != null) {
            this.loggerThread.stopThread();
            this.loggerThread = null;
        }

        this.loggerState = STATE_IDLE;
    }

    /**
     * Method to retrieve the Logger name.
     * You can override this method to return variable name. (e.g. version attached)
     *
     * @return The Logger name.
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Used by {@link PrimaryController#loggerChooser}.
     *
     * @return Pre-configured logger name.
     */
    @Override
    public String toString() {
        return loggerName;
    }

    /**
     * On logger ready, this method is called by {@link PrimaryController} to retrieve logger
     * associated Config/Control Panels and add them to UI as 'Tab' component programmatically.
     *
     * @return The logger associated Config/Control Panels.
     */
    abstract public LinkedHashMap<String, AnchorPane> createLoggerPanel();

    /**
     * Call hook invoked by {@link LoggerTask.Connect#run(GpsLogger)} for pre-condition checking.
     *
     * @return TRUE - {@link LoggerTask.Connect#run(GpsLogger)} can go ahead, FALSE - failed to start this task.
     */
    abstract protected boolean preConnect();

    /**
     * Call hook invoked by {@link LoggerTask.Disconnect#run(GpsLogger)} for pre-condition checking.
     *
     * @return TRUE - {@link LoggerTask.Disconnect#run(GpsLogger)} should continue to invoke {@link #postDisconnect()},
     * FALSE - disconnect started, keep waiting.
     */
    abstract protected boolean preDisconnect();

    /**
     * Call hook invoked by {@link LoggerTask.Disconnect#run(GpsLogger)} and somewhere else of multi-steps DisconnectLogger procedure.
     */
    protected void postDisconnect() {
        // Task level
        if (loggerTask instanceof LoggerTask.Disconnect) {
            Logging.infoln("%s...success", loggerTask.name);
            LoggerTask task = loggerTask;
            Platform.runLater(task::onSuccess);
            loggerTask = null;
        }

        // Stop LoggerThread
        loggerThread.stopThread();
    }

    protected void stopLoggerThread() {
        loggerThread.stopThread();
    }

    /**
     * Upload logger data operation performed by user.
     *
     * @param filePath    Folder the logger data will be uploaded to.
     * @param exportTypes The export file formats.
     */
    abstract protected void uploadTrack(String filePath, List<ExportType> exportTypes);

    @SuppressWarnings("unchecked")
    final public void execLoggerTask(LoggerTask loggerTask) {
        if (this.loggerTask != null) {
            Logging.errorln("LoggerTask [%s] is being executed.", this.loggerTask.name);
            return;
        }

        if (!loggerTask.preRun(this)) return;

        Logging.infoln("\n%s...start", loggerTask.name);

        this.loggerTask = loggerTask;
        this.loggerTask.onStart();
        this.loggerTask.run(this);
    }

    /**
     * NMEA string is received from serial port.
     * Wrap it into RecvJob and enqueue to the job queue.
     *
     * @param nmea NMEA string received from serial port.
     */
    @Override
    final public void recvNmea(String nmea) {
        if (loggerThread != null) {
            loggerThread.enqueueRecvJob(new RecvJob(this, RecvJob.RECV_JOB_NMEA_DATA, nmea));
        }
    }

    /**
     * Determine if SendJob queue is empty.
     *
     * @return TRUE - empty, FALSE - not empty
     */
    protected boolean isSendJobQueueEmpty() {
        return loggerThread.isEgressQueueEmpty();
    }

    /**
     * Cancel all pending SendJobs in {@link LoggerThread#egressQueue}.
     */
    protected void cancelSendJobs() {
        if (loggerThread != null) {
            loggerThread.cancelSendJobs();
        }
    }

    /**
     * Get SerialPort BaudRate of this logger.
     *
     * @return The SerialPort BaudRate index of
     * {@link net.benpl.gpsutility.serialport.SPortProperty#serialPortBaudRateList}.
     */
    final public int getSerialPortBaudRateIdx() {
        return serialPortBaudRateIdx;
    }

    /**
     * Get SerialPort DataBits of this logger.
     *
     * @return The SerialPort DataBits index of
     * {@link net.benpl.gpsutility.serialport.SPortProperty#serialPortDataBitsList}.
     */
    final public int getSerialPortDataBitsIdx() {
        return serialPortDataBitsIdx;
    }

    /**
     * Get SerialPort Parity of this logger.
     *
     * @return The SerialPort Parity index of
     * {@link net.benpl.gpsutility.serialport.SPortProperty#serialPortParityList}.
     */
    final public int getSerialPortParityIdx() {
        return serialPortParityIdx;
    }

    /**
     * Get SerialPort StopBits of this logger.
     *
     * @return The SerialPort StopBits index of
     * {@link net.benpl.gpsutility.serialport.SPortProperty#serialPortStopBitsList}.
     */
    final public int getSerialPortStopBitsIdx() {
        return serialPortStopBitsIdx;
    }

    /**
     * Get SerialPort BaudRate of this logger.
     *
     * @return The SerialPort FlowCtrl index of
     * {@link net.benpl.gpsutility.serialport.SPortProperty#serialPortFlowCtrlList}.
     */
    final public int getSerialPortFlowCtrlIdx() {
        return serialPortFlowCtrlIdx;
    }

    /**
     * Call hook on SerialPort ready.
     */
    abstract protected void serialPortReady();

    /**
     * Call hook to reset logger state.
     */
    abstract protected void preResetLogger();

    /**
     * Call hook prior to NMEA command sending out.
     *
     * @param nmeaCmd  NMEA command to be sent out. (DataField only)
     * @param nmeaResp NMEA response expected. (DataField only)
     */
    abstract protected void preSendJob(String nmeaCmd, String nmeaResp);

    /**
     * Dispatch NMEA to relevant NMEA handler..
     *
     * @param segs NMEA data field split by ','.
     * @return TRUE - handled successfully, FALSE - otherwise
     */
    abstract protected boolean dispatchNmea(String[] segs);

}
