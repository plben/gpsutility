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

package net.benpl.gpsutility.logger.holux_m1200;

import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.logger.ActionListener;
import net.benpl.gpsutility.logger.StateListener;
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;

import java.util.LinkedHashMap;

/**
 * Holux M-1200 implementation of {@link net.benpl.gpsutility.logger.GpsLogger}.
 */
public final class GpsLogger extends net.benpl.gpsutility.logger.GpsLogger {

//    static final int STATE_USB_MODE = 2001;

    /**
     * Logger ID should be returned by GPS Data Logger within 'PHLX852'.
     */
    static final String LOGGER_ID = "M1200";

    //    // The configuration loaded from GPS Data Logger (M-241 specific)
//    // =====================================================================
//    String hwVer;
//    String fwVer;
//    String userName;
    // The configuration loaded from GPS Data Logger (MTK common)
    // =====================================================================
    int spiStatus; // SPI Status. 1: Ready, 2: Busy, 3: Full
    long fmtReg; // log Format Register. Refer to MTK datasheet
    int rcdBy; // Record By (Second/Distance/Speed)
    int bySec; // log by time (0.1 second)
    int byDist; // log by distance (0.1 meter)
    int bySpeed; // log by speed (km/h)
    int rcdMethod; // Record method. 1: Overlap, 2: Stop on FULL
    // Log Status
    // Bit [1]: auto-log start bit; 0 - stopped, 1 - started
    // Bit [2]: log method bit: 0 - overlap, 1 - stop on full
    // Bit [8]: log function enabled bit; 0 - receiver, 1 - logger
    // Bit [9]: log function disabled bit
    // Bit [10]: logger need format bit
    // Bit [11]: logger full bit
    int loggerStatus;
    long rcdAddr; // Record next write address.
    String flashId; // Flash ID.
    long rcdRcnt; // Total record count
    byte[] failSector; // 0: broken, 1: valid
    int mtkVersion; // MTK hardware version.

    // Temporary variables used during Upload Track operation
    // =====================================================================
    int totalBlocks; // How many blocks (1 KB for each) log data
    byte[] logData;
    int readAddr;

//    /**
//     * The associated Config Panel
//     */
//    private AnchorPane configPane;
//    /**
//     * The controller of associated FX Config Panel
//     */
//    Controller configPaneController;
//
//    private Timer usbModeTimer = null;

    public GpsLogger() {
        // 38400/8bits/No Parity/1bit/No Flow Control
        super("Holux M-1200", 4, 4, 0, 0, 2);

//        this.hwVer = "";
//        this.fwVer = "";
//        this.configPane = null;
//        this.configPaneController = null;
    }

    /**
     * Return LogParser on log data uploaded from GPS Data Logger.
     *
     * @return The LogParser.
     */
    @Override
    protected LogParser getParser() {
        return new LogParser(logData);
    }

    /**
     * Call hook to reset logger state.
     */
    @Override
    protected void preResetLogger() {
//        hwVer = "";
//        fwVer = "";
//        configPaneController = null;
//        configPane = null;
//
//        // Cancel any pending USB_MODE TimerTask
//        cancelUsbModeTimer();
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
    @Override
    protected void performConnect(ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener loggerStateListener) {
        execActionTask(new ActionTask.Connect(this, actionListener, commPort, commBaudRateIdx, commDataBitsIdx, commParityIdx, commStopBitsIdx, commFlowCtrlIdx, loggerStateListener));
    }

    /**
     * Method to create and return logger specific Config/Control panels.
     *
     * @return The created panels.
     */
    @Override
    public LinkedHashMap<String, AnchorPane> createLoggerPanel() {
        LinkedHashMap<String, AnchorPane> panels = new LinkedHashMap<>();
//        try {
//            // Create logger associated Config/Control Pane
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HoluxM241.fxml"));
//            configPane = loader.load();
//            configPaneController = loader.getController();
//            configPaneController.setGpsLogger(this);
//
//            configPaneController.setRcdMethod(rcdMethod);
//            configPaneController.setRecordBy(rcdBy);
//            configPaneController.setRecordBySec(bySec / 10);
//            configPaneController.setRecordByDist(byDist / 10);
//            configPaneController.setUserName(userName);
//
//            panels.put(name, configPane);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Logging.errorln("Failed to load logger's config panel");
//        }
        return panels;
    }

    /**
     * Perform Disconnect action.
     *
     * @param actionListener Listener on action performed.
     */
    @Override
    protected void performDisconnect(ActionListener actionListener) {
        execActionTask(new ActionTask.Disconnect(this, actionListener));
    }

    /**
     * Perform DebugNmea action.
     *
     * @param actionListener Listener on action performed.
     * @param nmea           NMEA sentence to be sent to GPS Data Logger.
     */
    @Override
    protected void performDebugNmea(ActionListener actionListener, String nmea) {
        execActionTask(new ActionTask.DebugNmea(this, actionListener, nmea));
    }

    /**
     * Perform UploadTrack action.
     *
     * @param actionListener Listener on action performed.
     */
    @Override
    protected void performUploadTrack(ActionListener actionListener) {
        execActionTask(new ActionTask.UploadTrack(this, actionListener));
    }

//    /**
//     * Perform SaveConfig action.
//     *
//     * @param actionListener Listener on task execution.
//     * @param rcdMethod      Config - record method
//     * @param rcdBy          Config - record by
//     * @param bySec          Config - value of record by seconds
//     * @param byDist         Config - value of record by distance
//     */
//    final void performSaveConfig(ActionListener actionListener, int rcdMethod, int rcdBy, int bySec, int byDist) {
//        execActionTask(new ActionTask.SaveConfig(this, actionListener, rcdMethod, rcdBy, bySec, byDist));
//    }
//
//    /**
//     * Perform ModUserName action.
//     *
//     * @param actionListener Listener on task execution.
//     * @param userName       The new user name.
//     */
//    final void performModifyUserName(ActionListener actionListener, String userName) {
//        execActionTask(new ActionTask.ModUserName(this, actionListener, userName));
//    }

    /**
     * Method to cleanup resources created during task UploadTrack.
     */
    void postUploadTrack() {
        logData = null;
    }

//    /**
//     * Method to start UsbMode timer, to keep UsbMode alive.
//     */
//    void startUsbModeTimer() {
//        if (usbModeTimer != null) {
//            usbModeTimer.cancel();
//        }
//
//        usbModeTimer = new Timer("Timer-UsbMode");
//        usbModeTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (state == STATE_USB_MODE) {
//                    loggerThread.enqueueSendJob(
//                            new SendJob.NonTask(GpsLogger.this, null, "HOLUX241,6", null) // HeartBeat with HOLUX M-241, to keep USB_MODE alive (TODO: What is for GR245???)
//                    );
//                } else {
//                    usbModeTimer.cancel();
//                    usbModeTimer = null;
//                }
//            }
//        }, 6000, 6000);
//    }
//
//    /**
//     * Method to cancel pending TimerTask.
//     */
//    void cancelUsbModeTimer() {
//        if (usbModeTimer != null) {
//            usbModeTimer.cancel();
//            usbModeTimer = null;
//        }
//    }
}
