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

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import net.benpl.gpsutility.Loggers;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Settings;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller of {@link fxml/PrimaryWindow.fxml}
 */
public class PrimaryController implements Controller, StateListener {

    /**
     * File name formatter for exporting log data to external files.
     */
    protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**
     * Maintain an available serial port list for referred by {@link #loggerChooser}.
     */
    private ObservableList<CommPort> commPorts = FXCollections.observableArrayList();

    /**
     * The logger in Active.
     */
    private GpsLogger activeLogger = null;

    /**
     * 'Copy' menu item of log window.
     */
    private MenuItem logCopy;

    /**
     * Timer of serial port monitoring.
     */
    private Timer serialPortMonitoringTimer;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ComboBox<GpsLogger> loggerChooser;

    @FXML
    private void loggerChooserActionPerformed(ActionEvent event) {
        GpsLogger logger = loggerChooser.getValue();

        baudRateChooser.setValue(CommProperty.commBaudRateList.get(logger.commBaudRateIdx));
        parityChooser.setValue(CommProperty.commParityList.get(logger.commParityIdx));
        dataBitsChooser.setValue(CommProperty.commDataBitsList.get(logger.commDataBitsIdx));
        stopBitsChooser.setValue(CommProperty.commStopBitsList.get(logger.commStopBitsIdx));
        flowCtrlChooser.setValue(CommProperty.commFlowCtrlList.get(logger.commFlowCtrlIdx));
    }

    @FXML
    private ComboBox<CommPort> sPortChooser;

    @FXML
    private void sPortChooserActionPerformed(ActionEvent event) {
        logWindow.setText("Logger - Disconnected.");

        if (sPortChooser.getValue() != null) {
            if (sPortChooser.getValue().getPort() == null) {
                baudRateChooser.setDisable(true);
                parityChooser.setDisable(true);
                dataBitsChooser.setDisable(true);
                stopBitsChooser.setDisable(true);
                flowCtrlChooser.setDisable(true);
                connectBtn.setDisable(true);
            } else {
                baudRateChooser.setDisable(false);
                parityChooser.setDisable(false);
                dataBitsChooser.setDisable(false);
                stopBitsChooser.setDisable(false);
                flowCtrlChooser.setDisable(false);
                connectBtn.setDisable(false);
            }
        }
    }

    @FXML
    private ComboBox<CommProperty> baudRateChooser;

    @FXML
    private ComboBox<CommProperty> dataBitsChooser;

    @FXML
    private ComboBox<CommProperty> flowCtrlChooser;

    @FXML
    private ComboBox<CommProperty> parityChooser;

    @FXML
    private ComboBox<CommProperty> stopBitsChooser;

    @FXML
    private TextField nmeaInput;

    @FXML
    private Button sendNmeaBtn;

    @FXML
    private void sendNmeaBtnActionPerformed(ActionEvent event) {
        String text = nmeaInput.getText();
        if (Utils.isEmpty(text)) return;

        activeLogger.performDebugNmea(new ActionListener() {
            @Override
            public void onStart() {
                priorExecution();
            }

            @Override
            public void onSuccess() {
                postExecution();
            }

            /**
             * For critical failure, will got notified by {@link LoggerThread} via {@link StateListener#stateChanged(int)}.
             * So no action is necessary here.
             */
            @Override
            public void onFail(ActionTask.CAUSE cause) {
                postExecution();
            }
        }, text);
    }

    @FXML
    private TextField uploadPath;

    @FXML
    private void uploadPathOnClicked(MouseEvent event) {
        String str = uploadPath.getText();
        if (Utils.isEmpty(str)) return;

        File filePath = new File(str);

        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle("Upload log data to...");

        if (filePath.isDirectory()) {
            chooser.setInitialDirectory(filePath);
        } else {
            chooser.setInitialDirectory(filePath.getParentFile());
        }

        File selected = chooser.showDialog(null);
        if (selected != null) {
            if (selected.exists() && selected.canWrite()) {
                uploadPath.setText(selected.getAbsolutePath());
                Settings.setGpsTrackStorePath(selected.getAbsolutePath());
            }
        }
    }

    @FXML
    private CheckBox gpxExport;

    @FXML
    private void gpxExportActionPerformed(ActionEvent event) {
        boolean enable = gpxExport.isSelected() || kmlExport.isSelected();
        uploadPath.setDisable(!enable);
        uploadTrackBtn.setDisable(!enable);
    }

    @FXML
    private CheckBox kmlExport;

    @FXML
    private void kmlExportActionPerformed(ActionEvent event) {
        boolean enable = gpxExport.isSelected() || kmlExport.isSelected();
        uploadPath.setDisable(!enable);
        uploadTrackBtn.setDisable(!enable);
    }

    @FXML
    private Button uploadTrackBtn;

    @FXML
    private void uploadTrackBtnActionPerformed(ActionEvent event) {
        uploadProgress.setProgress(0);

        List<LogParser.ExportType> exportTypes = new ArrayList<>();
        if (gpxExport.isSelected()) exportTypes.add(LogParser.ExportType.GPX);
        if (kmlExport.isSelected()) exportTypes.add(LogParser.ExportType.KML);

        activeLogger.performUploadTrack(new ActionListener.UploadTrack() {
            @Override
            public void onProgress(double progress) {
                // Update progress
                uploadProgress.setProgress(progress);
            }

            @Override
            public void onStart() {
                priorExecution();
            }

            @Override
            public void onSuccess() {
                try {
                    // Parse the log
                    LogParser logParser = activeLogger.getParser();
                    Logging.infoln("\nParsing log data...");
                    logParser.parse();
                    Logging.infoln("Parse log data...success");

                    // Export to external file one by one
                    Date now = new Date();
                    String exportPath = uploadPath.getText();
                    String filename = sdf.format(now);
                    String exported;
                    for (LogParser.ExportType exportType : exportTypes) {
                        switch (exportType) {
                            case GPX:
                                exported = logParser.toGpx(new File(exportPath, filename + ".pgx"), now);
                                Logging.infoln("Log data exported to: %s", exported);
                                break;

                            case KML:
                                exported = logParser.toKml(new File(exportPath, filename + ".kml"), now);
                                Logging.infoln("Log data exported to: %s", exported);
                                break;

                            default:
                                break;
                        }
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                    Logging.infoln("Parse log data...failed");
                }

                postExecution();
            }

            /**
             * For critical failure, will got notified by {@link LoggerThread} via {@link StateListener#stateChanged(int)}.
             * So no action is necessary here.
             */
            @Override
            public void onFail(ActionTask.CAUSE cause) {
                Logging.errorln("Upload track data ... fail");
                postExecution();
            }
        });
    }

    @FXML
    private ProgressBar uploadProgress;

    @FXML
    private Button connectBtn;

    @FXML
    private void connectBtnActionPerformed(ActionEvent event) {
        if (activeLogger == null) {
            logTextArea.setText("");

            // Pickup selected Logger and start it
            GpsLogger logger = loggerChooser.getValue();

            logger.performConnect(
                    new ActionListener() {
                        @Override
                        public void onStart() {
                            // Initial components state
                            logWindow.setText("Logger - Connecting...");

                            // Disable logger, serial port, serial port properties selection
                            loggerChooser.setDisable(true);
                            sPortChooser.setDisable(true);
                            baudRateChooser.setDisable(true);
                            dataBitsChooser.setDisable(true);
                            parityChooser.setDisable(true);
                            stopBitsChooser.setDisable(true);
                            flowCtrlChooser.setDisable(true);
                            connectBtn.setDisable(true);
                        }

                        @Override
                        public void onSuccess() {
                            // START task executed ... success
                            activeLogger = logger;
                            // Install Logger tab(s)
                            activeLogger.createLoggerPanel().forEach((s, anchorPane) -> {
                                Tab tab = new Tab(s);
                                tab.setContent(anchorPane);
                                tabPane.getTabs().add(tab);
                            });

                            // Initialize 'Connected' state components
                            nmeaInput.setDisable(false);
                            sendNmeaBtn.setDisable(false);
                            uploadPath.setDisable(false);
                            uploadTrackBtn.setDisable(false);
                            gpxExport.setDisable(false);
                            kmlExport.setDisable(false);
                            connectBtn.setDisable(false);
                            connectBtn.setText("Disconnect");

                            logWindow.setText(String.format("Logger - Connected.【%s】", activeLogger.getName()));
                        }

                        @Override
                        public void onFail(ActionTask.CAUSE cause) {
                            // START task executed ... failed
                            resetToDefault();
                            logWindow.setText("Logger - Disconnected.");
                        }
                    },
                    sPortChooser.getValue(),
                    baudRateChooser.getSelectionModel().getSelectedIndex(),
                    dataBitsChooser.getSelectionModel().getSelectedIndex(),
                    parityChooser.getSelectionModel().getSelectedIndex(),
                    stopBitsChooser.getSelectionModel().getSelectedIndex(),
                    flowCtrlChooser.getSelectionModel().getSelectedIndex(),
                    this
            );
        } else {
            logWindow.setText("Logger - Disconnecting...");
            activeLogger.performDisconnect(new ActionListener() {
                @Override
                public void onStart() {
                    // Nothing to do
                }

                @Override
                public void onSuccess() {
                    resetToDefault();
                }

                /**
                 * For critical failure, will got notified by {@link LoggerThread} via {@link StateListener#stateChanged(int)}.
                 * So no action is necessary here.
                 */
                @Override
                public void onFail(ActionTask.CAUSE cause) {
                    Logging.errorln("Disconnect logger [%s] ... fail", activeLogger.name);
                    resetToDefault();
                }
            });
        }
    }

    @FXML
    private TitledPane logWindow;

    @FXML
    private TextArea logTextArea;

    @FXML
    private RadioButton errLogLevel;

    @FXML
    private RadioButton infoLogLevel;

    @FXML
    private RadioButton debugLogLevel;

    /**
     * Method to initialize variables and status of this FX page.
     */
    @Override
    public void initialize() {
        // Initial log level
        Logging.setLevel(debugLogLevel.isSelected() ? Logging.DEBUG : (infoLogLevel.isSelected() ? Logging.INFO : Logging.ERROR));
        // Redirect all log and console output to TextArea
        Logging.redirectTo(logTextArea);

        // Initialize serial port properties ComboBoxes
        baudRateChooser.setItems(CommProperty.commBaudRateList);
        parityChooser.setItems(CommProperty.commParityList);
        dataBitsChooser.setItems(CommProperty.commDataBitsList);
        stopBitsChooser.setItems(CommProperty.commStopBitsList);
        flowCtrlChooser.setItems(CommProperty.commFlowCtrlList);

        // Load supported GPS loggers
        loggerChooser.setItems(Loggers.all);
        loggerChooser.setValue(Loggers.all.get(0));
        loggerChooser.fireEvent(new ActionEvent()); // Fire an action event to trigger refreshing all serial port properties

        // Load available serial ports
        sPortChooser.setItems(commPorts);
        refreshSerialPort();

        // Initialize GPS track upload file/path
        File filePath = new File(Settings.getGpsTrackStorePath());
        if (filePath.exists()) {
            if (filePath.canWrite()) {
                uploadPath.setText(filePath.getAbsolutePath());
            } else {
                uploadPath.setText(Settings.getUserHomeDir());
            }
        } else {
            uploadPath.setText(Settings.getUserHomeDir());
        }

        // Initialize GPS track upload progress bar
        uploadProgress.setProgress(0);

        // Group log levels Error/Info/Debug
        ToggleGroup logLevelToggle = new ToggleGroup();
        errLogLevel.setToggleGroup(logLevelToggle);
        infoLogLevel.setToggleGroup(logLevelToggle);
        debugLogLevel.setToggleGroup(logLevelToggle);
        // listen on changes in selected toggle
        logLevelToggle.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal == errLogLevel) {
                Logging.setLevel(Logging.ERROR);
            } else if (newVal == infoLogLevel) {
                Logging.setLevel(Logging.INFO);
            } else {
                Logging.setLevel(Logging.DEBUG);
            }
        });

        // Log TextArea popup menu
        MenuItem menuItem1 = new MenuItem("Select All");
        menuItem1.setOnAction(actionEvent -> logTextArea.selectAll());
        logCopy = new MenuItem("Copy");
        logCopy.setOnAction(actionEvent -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(logTextArea.getSelectedText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        MenuItem menuItem3 = new SeparatorMenuItem();
        MenuItem menuItem4 = new MenuItem("Clear");
        menuItem4.setOnAction(actionEvent -> logTextArea.setText(""));
        ContextMenu menu = new ContextMenu(menuItem1, logCopy, menuItem3, menuItem4);
        logTextArea.setContextMenu(menu);
        // Rebuild the popup menu each time got triggered
        logTextArea.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.isPopupTrigger()) {
                logCopy.setDisable(Utils.isEmpty(logTextArea.getSelectedText()));
            }
        });

        // Schedule TimerTask to refresh Serial Port
        serialPortMonitoringTimer = new Timer("Timer-SerialPortMonitoring");
        serialPortMonitoringTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (activeLogger == null) {
                    refreshSerialPort();
                }
            }
        }, 5000, 5000);

        // FIX: Text looks blurred in TextArea
        // The issue is centered around a bug introduced in JavaFX 8 which causes some blurriness of the content displayed
        // within a ScrollPane when said ScrollPane has decimal value constraints, the bug has to do with the cached image
        // of the content so turning off caching works. TextAreas make use of ScrollPanes.
        Platform.runLater(() -> {
            logTextArea.setCache(false);
            ScrollPane scrollPane = (ScrollPane) logTextArea.getChildrenUnmodifiable().get(0);
            scrollPane.setCache(false);
            for (Node node : scrollPane.getChildrenUnmodifiable()) {
                node.setCache(false);
            }
        });
    }

    /**
     * Method to perform close action when user click on the 'X' button.
     */
    public void onClose() {
        if (serialPortMonitoringTimer != null) {
            serialPortMonitoringTimer.cancel();
            serialPortMonitoringTimer = null;
        }

        if (activeLogger == null) {
            Platform.exit();
        } else {
            // Stop the active logger
            // then exit this application.
            activeLogger.performDisconnect(new ActionListener() {
                @Override
                public void onStart() {
                    // Nothing to do
                }

                @Override
                public void onSuccess() {
                    // Exit application, normal case
                    Platform.exit();
                }

                @Override
                public void onFail(ActionTask.CAUSE cause) {
                    // Exit application, exception case
                    System.exit(0);
                }
            });
        }
    }

    /**
     * Method called by serialPortMonitoringTimer periodically to update serial port combobox if necessary.
     */
    private void refreshSerialPort() {
        ObservableList<CommPort> ports = FXCollections.observableArrayList();

        // Retrieve serial port list from system
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        if (serialPorts != null && serialPorts.length > 0) {
            // Sort the array by name
            Arrays.sort(serialPorts, (SerialPort sp1, SerialPort sp2) -> (sp1.getSystemPortName().compareTo(sp2.getSystemPortName())));
            // Build CommPort list with available SerialPort
            for (SerialPort serialPort : serialPorts) {
                CommPort commPort = new CommPort(serialPort.getSystemPortName() + " (" + serialPort.getPortDescription() + ")", serialPort);
                ports.add(commPort);
            }
        }

        // If no available serial port, insert 'No available Serial Port' into CommPort list
        if (ports.size() == 0) {
            ports.add(new CommPort("No available Serial Port", null));
        }

        // Test if data model of serial port combobox need to be updated
        if (ports.size() == commPorts.size()) {
            // Same serial port number, need to compare one by one
            for (int i = 0; i < ports.size(); i++) {
                if (!ports.get(i).getName().equals(commPorts.get(i).getName())) {
                    Platform.runLater(() -> {
                        commPorts.setAll(ports);
                        sPortChooser.getSelectionModel().select(0);
                    });
                    return;
                }
            }
        } else {
            // Different serial port number, update combobox directly
            Platform.runLater(() -> {
                commPorts.setAll(ports);
                sPortChooser.getSelectionModel().select(0);
            });
        }
    }

    /**
     * Callback on logger entity state changed.
     *
     * @param state The new state.
     */
    @Override
    public void stateChanged(int state) {
        if (state == GpsLogger.STATE_IDLE) {
            // If logger entity has been transit to IDLE, reset the FX page to default state.
            resetToDefault();
        }
    }

    /**
     * Method to reset this FX page to default state.
     */
    private void resetToDefault() {
        // Remove Logger tab(s)
        ObservableList<Tab> tabs = tabPane.getTabs();
        tabs.remove(1, tabs.size());

        // Reset components state
        // Disable upload track
        uploadProgress.setProgress(0);
        uploadPath.setDisable(true);
        gpxExport.setDisable(true);
        kmlExport.setDisable(true);
        uploadTrackBtn.setDisable(true);
        // Disable NMEA debug
        nmeaInput.setDisable(true);
        sendNmeaBtn.setDisable(true);
        // Enable logger, serial port, serial port properties selection
        loggerChooser.setDisable(false);
        sPortChooser.setDisable(false);
        baudRateChooser.setDisable(false);
        dataBitsChooser.setDisable(false);
        parityChooser.setDisable(false);
        stopBitsChooser.setDisable(false);
        flowCtrlChooser.setDisable(false);
        // Update button to 'Connect'
        connectBtn.setDisable(false);
        connectBtn.setText("Connect");

        activeLogger = null;
        logWindow.setText("Logger - Disconnected.");
    }

    /**
     * Method to disable relevant components prior to action performed.
     */
    private void priorExecution() {
        connectBtn.setDisable(true);
        uploadPath.setDisable(true);
        uploadTrackBtn.setDisable(true);
        nmeaInput.setDisable(true);
        sendNmeaBtn.setDisable(true);
        gpxExport.setDisable(true);
        kmlExport.setDisable(true);
        tabPane.getTabs().forEach(tab -> {
            if (tab.getContent() != anchorPane) tab.setDisable(true);
        });
    }

    /**
     * Method to enable relevant components post action performed.
     */
    private void postExecution() {
        connectBtn.setDisable(false);
        uploadPath.setDisable(false);
        uploadTrackBtn.setDisable(false);
        nmeaInput.setDisable(false);
        sendNmeaBtn.setDisable(false);
        gpxExport.setDisable(false);
        kmlExport.setDisable(false);
        tabPane.getTabs().forEach(tab -> tab.setDisable(false));
    }
}
