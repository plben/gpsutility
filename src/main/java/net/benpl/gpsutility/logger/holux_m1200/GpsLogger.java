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

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.export.ExportBuilder;
import net.benpl.gpsutility.export.ExportType;
import net.benpl.gpsutility.logger.PrimaryController;
import net.benpl.gpsutility.logger.SendJob;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.type.NmeaHandler;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holux M-1200 GPS Logger entity inherited from {@link net.benpl.gpsutility.logger.GpsLogger}.
 */
public final class GpsLogger extends net.benpl.gpsutility.logger.GpsLogger {

    // FIXME: Need M-1200 specific NMEA commands
//    private static final int STATE_USB_MODE = 2001;

    /**
     * Logger ID should be returned by external Logger within 'PHLX852'.
     */
    // FIXME: Don't know the logger ID of M-1200
    private static final String LOGGER_ID = "GR1200";

    // FIXME: Need M-1200 specific NMEA commands
//    // The configuration loaded from external Logger (M-241 specific)
//    // =====================================================================
//    private String hwVer;
//    private String fwVer;
//    private String userName;
    // The configuration loaded from external Logger (MTK common)
    // =====================================================================
    private int spiStatus; // SPI Status. 1: Ready, 2: Busy, 3: Full
    private long fmtReg; // log Format Register. Refer to MTK datasheet
    // FIXME: Need M-1200 specific NMEA commands
//    private int rcdBy; // Record By (Second/Distance/Speed)
    private int bySec; // log by time (0.1 second)
    private int byDist; // log by distance (0.1 meter)
    private int bySpeed; // log by speed (km/h)
    private int rcdMethod; // Record method. 1: Overlap, 2: Stop on FULL
    // Log Status
    // Bit [1]: auto-log start bit; 0 - stopped, 1 - started
    // Bit [2]: log method bit: 0 - overlap, 1 - stop on full
    // Bit [8]: log function enabled bit; 0 - receiver, 1 - logger
    // Bit [9]: log function disabled bit
    // Bit [10]: logger need format bit
    // Bit [11]: logger full bit
    private int loggerStatus;
    private long rcdAddr; // Record next write address.
    private String flashId; // Flash ID.
    private long rcdRcnt; // Total record count
    private byte[] failSector; // 0: broken, 1: valid
    private int mtkVersion; // MTK hardware version.

    // Temporary variables used during Upload Track operation
    // =====================================================================
    private int totalBlocks; // How many blocks (1 KB for each) log data
    private byte[] logData;
    private int readAddr;
    private String uploadFilePath;
    private List<ExportType> exportTypes;

    // FIXME: Need M-1200 specific NMEA commands
//    /**
//     * The associated Config Panel
//     */
//    private AnchorPane configPane;
//    /**
//     * The controller of associated Config Panel
//     */
//    private Controller configPaneController;
//
//    private final USBMode usbMode = new USBMode();

    public GpsLogger() {
        // 38400/8bits/No Parity/1bit/No Flow Control
        super("Holux M-1200", 4, 4, 0, 0, 2);

        // FIXME: Need M-1200 specific NMEA commands
//        this.hwVer = "";
//        this.fwVer = "";
//        this.configPane = null;
//        this.configPaneController = null;
    }

    @Override
    public String getLoggerName() {
        // FIXME: Need M-1200 specific NMEA commands
//        if (loggerState == STATE_USB_MODE) return String.format("%s V%s_%s", loggerName, hwVer, fwVer);
//        else return loggerName;
        return loggerName;
    }

    /**
     * On logger ready, this method is called by {@link PrimaryController} to retrieve logger
     * associated Config/Control Panels and add them to UI as 'Tab' component programmatically.
     *
     * @return The logger associated Config/Control Panels.
     */
    @Override
    public LinkedHashMap<String, AnchorPane> createLoggerPanel() {
        LinkedHashMap<String, AnchorPane> panels = new LinkedHashMap<>();
        // FIXME: Need M-1200 specific NMEA commands
//        try {
//            // Create logger associated Config/Control Pane
//            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/HoluxM241.fxml"));
//            configPane = loader.load();
//            configPaneController = loader.getController();
//            configPaneController.setGpsLogger(GpsLogger.this);
//
//            configPaneController.setRcdMethod(rcdMethod);
//            configPaneController.setRecordBy(rcdBy);
//            configPaneController.setRecordBySec(bySec / 10);
//            configPaneController.setRecordByDist(byDist / 10);
//            configPaneController.setUserName(userName);
//
//            panels.put(loggerName, configPane);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Logging.errorln("Failed to load logger's config panel");
//        }
        return panels;
    }

    /**
     * Call hook on SerialPort ready.
     */
    @Override
    protected void serialPortReady() {
        if (loggerTask instanceof LoggerTask.Connect) {
            // Once SerialPort is ready, launch a batch of NMEA commands.
            loggerThread.enqueueSendJob(
                    new SendJob(this, "Handshake", "PHLX810", "PHLX852"), // Query logger for module ID
                    // FIXME: Need M-1200 specific NMEA commands
//                    new SendJob(this, "Switch to USB-Mode", "HOLUX241,1", "HOLUX001,1"), // Transit HoluxM241 into USB_MODE; Stop logging if started (TODO: Is it PHLX826->PHLX859 for GR245???)
                    new SendJob(this, "Query SPI status", "PMTK182,2,1", "PMTK182,3,1"), // Query HoluxM241 for SPI status
                    new SendJob(this, "Query FmtReg", "PMTK182,2,2", "PMTK182,3,2"), // Query HoluxM1200 for log format register
                    new SendJob(this, "Query BySec", "PMTK182,2,3", "PMTK182,3,3"), // Query HoluxM1200 for interval (in 0.1 second) of BySEC
                    new SendJob(this, "Query ByDist", "PMTK182,2,4", "PMTK182,3,4"), // Query HoluxM1200 for interval (in 0.1 meter) of ByDistance
                    new SendJob(this, "Query BySpeed", "PMTK182,2,5", "PMTK182,3,5"), // Query HoluxM1200 for interval (in 0.1 km/h) of BySpeed
                    new SendJob(this, "Query RcdMethod", "PMTK182,2,6", "PMTK182,3,6"), // Query HoluxM1200 for record method. (Overlap or StopOnFull)
                    new SendJob(this, "Query LoggerState", "PMTK182,2,7", "PMTK182,3,7"), // Query HoluxM1200 for log status. (Start/Stop)
                    new SendJob(this, "Query RcdAddr", "PMTK182,2,8", "PMTK182,3,8"), // Query HoluxM1200 for record address. (next write)
                    new SendJob(this, "Query flashID", "PMTK182,2,9", "PMTK182,3,9"), // Query HoluxM1200 for flash ID
                    new SendJob(this, "Query RcdTotal", "PMTK182,2,10", "PMTK182,3,10"), // Query HoluxM1200 for total records
                    new SendJob(this, "Query FailSectors", "PMTK182,2,11", "PMTK182,3,11"), // Query HoluxM1200 for FailSector in flash
                    new SendJob(this, "Query MtkVersion", "PMTK182,2,12", "PMTK182,3,12") // Query HoluxM1200 for MTK hardware version
                    // FIXME: Need M-1200 specific NMEA commands
//                    new SendJob(this, "Query FwVer", "HOLUX241,3", "HOLUX001,3"), // Query HoluxM241 for firmware version
//                    new SendJob(this, "Query HwVer", "HOLUX241,7", "HOLUX001,7"), // Query HoluxM241 for hardware version
//                    new SendJob(this, "Query UserName", "HOLUX241,5", "HOLUX001,5"), // Query HoluxM241 for user name
//                    new SendJob(this, "Query RcdBy", "HOLUX241,8", "HOLUX001,8") // Query HoluxM241 for record method. (0: BySec, 1: ByDist)
            );
        }
    }

    /**
     * Call hook to reset logger state.
     */
    @Override
    protected void preResetLogger() {
        // FIXME: Need M-1200 specific NMEA commands
//        hwVer = "";
//        fwVer = "";
//        configPaneController = null;
//        configPane = null;
//
//        // Cancel any pending USB_MODE TimerTask
//        usbMode.exit();
    }

    /**
     * Call hook before NMEA command sending out.
     *
     * @param nmeaCmd  NMEA command to be sent out. (DataField only)
     * @param nmeaResp NMEA response expected. (DataField only)
     */
    @Override
    protected void preSendJob(String nmeaCmd, String nmeaResp) {

    }

    /**
     * Dispatch NMEA to relevant NMEA handler..
     *
     * @param segs NMEA data field split by ','.
     * @return TRUE - handled successfully, FALSE - otherwise
     */
    @Override
    protected boolean dispatchNmea(String[] segs) {
        NmeaHandler<GpsLogger> handler = NMEA_ROOT.get(segs[0]);
        if (handler != null) return handler.handle(this, segs, 1);
        return true;
    }

    /**
     * The NMEA handlers of ROOT level.
     */
    private final Map<String, NmeaHandler<GpsLogger>> NMEA_ROOT = Stream.of(
            new AbstractMap.SimpleEntry<>("PHLX852", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // Validate DataField segment count
                    if (segs.length != 2) {
                        NmeaHandler.error(segs, "segment count invalid");
                        return false;
                    }

                    // Validate logger ID
                    if (!LOGGER_ID.equals(segs[1])) {
                        Logging.errorln("Invalid logger ID: [%s]", segs[1]);
                        return false;
                    }

                    // Transit logger state to HANDSHAKED
                    loggerState = STATE_HANDSHAKED;
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("HOLUX001", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    if (segs.length < 2) {
                        NmeaHandler.error(segs, "segment count invalid");
                        return false;
                    }

                    NmeaHandler<GpsLogger> handler = NMEA_HOLUX001.get(segs[idx]);
                    if (handler != null) return handler.handle(logger, segs, idx + 1);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("PMTK182", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    NmeaHandler<GpsLogger> handler = NMEA_PMTK182.get(segs[idx]);
                    if (handler != null) return handler.handle(logger, segs, idx + 1);
                    return true;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * The sub NMEA handlers under 'HOLUX001'.
     */
    private final Map<String, NmeaHandler<GpsLogger>> NMEA_HOLUX001 = Stream.of(
            new AbstractMap.SimpleEntry<>("1", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
//                    // Transit SM to USB_MODE state
//                    loggerState = STATE_USB_MODE;
//                    // Schedule TimerTask to keep USB_MODE alive.
//                    usbMode.keepAlive();
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("2", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // Transit SM to SERIALPORT_READY state
                    loggerState = STATE_SERIALPORT_OPENED;

                    if (loggerTask instanceof LoggerTask.Disconnect) {
                        postDisconnect();
                    }

                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("3", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
//                    // Firmware version info
//                    fwVer = String.format("%.02f", Float.parseFloat(segs[idx]) / 100);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("4", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
                    // Change User Name done
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("5", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
//                    // User name
//                    userName = segs[idx];
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("7", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
//                    // Hardware version
//                    hwVer = segs[idx];
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("8", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
//                    // Record By
//                    rcdBy = Integer.parseInt(segs[idx]);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("9", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // FIXME: Need M-1200 specific NMEA commands
                    // Save config done
                    return true;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * The sub NMEA handlers under 'PMTK182'.
     */
    private final Map<String, NmeaHandler<GpsLogger>> NMEA_PMTK182 = Stream.of(
            new AbstractMap.SimpleEntry<>("3", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    // Return Log Status
                    // Validate DataField segment count
                    if (segs.length < 3) {
                        NmeaHandler.error(segs, "segment count invalid");
                        return false;
                    }

                    NmeaHandler<GpsLogger> handler = NMEA_PMTK182_3.get(segs[idx]);
                    if (handler != null) {
                        return handler.handle(logger, segs, idx + 1);
                    }
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("8", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    if (loggerTask instanceof LoggerTask.UploadTrack) {
                        Platform.runLater(() -> ((LoggerTask.UploadTrack) loggerTask).onProgress(((double) readAddr / 1024.0 + 1.0) / (double) totalBlocks));
                    }

                    byte[] logSeg = Utils.hexStringToByteArray(segs[3]);
                    logData = Utils.concatByteArray(logData, logSeg);

                    if (loggerTask instanceof LoggerTask.UploadTrack) {
                        readAddr += 0x400;
                        if (readAddr < rcdAddr) {
                            loggerThread.enqueueSendJob(
                                    new SendJob(logger, null, String.format("PMTK182,7,%08X,00000400", readAddr), String.format("PMTK182,8,%08X", readAddr)) // Read log of 1KB size
                            );
                        } else {
                            Logging.infoln("Read data from [%s]...success", loggerName);

                            try {
                                // Parse the log
                                Logging.infoln("Parsing log data...");
                                ExportBuilder<LogParser> exportBuilder = new ExportBuilder<>(logData, LogParser.class);
                                Logging.infoln("Parse log data...success");

                                // Export to external file one by one
                                String filePath;
                                Date now = new Date();
                                for (ExportType exportType : exportTypes) {
                                    switch (exportType) {
                                        case GPX:
                                            filePath = exportBuilder.toGpx(new File(uploadFilePath, sdf.format(now) + ".pgx"));
                                            Logging.infoln("Log data exported to: %s", filePath);
                                            break;

                                        case KML:
                                            filePath = exportBuilder.toKml(new File(uploadFilePath, sdf.format(now) + ".kml"));
                                            Logging.infoln("Log data exported to: %s", filePath);
                                            break;

                                        default:
                                            break;
                                    }
                                }
                            } catch (JAXBException e) {
                                e.printStackTrace();
                                Logging.infoln("Parse log data...failed");
                            } catch (IllegalAccessException | InstantiationException e) {
                                e.printStackTrace();
                                Logging.infoln("Invalid log parser.");
                            } catch (NoSuchMethodException | InvocationTargetException e) {
                                e.printStackTrace();
                                Logging.infoln("Initialize parser...failed");
                            }

                            // Do not forget to release resources
                            logData = null;
                        }
                    }
                    return true;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * The sub NMEA handlers under 'PMTK182,3'.
     */
    private final Map<String, NmeaHandler<GpsLogger>> NMEA_PMTK182_3 = Stream.of(
            new AbstractMap.SimpleEntry<>("1", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    spiStatus = Integer.parseInt(segs[idx]);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("2", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    fmtReg = Long.parseLong(segs[idx], 16);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("3", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    bySec = Integer.parseInt(segs[idx]);
                    // FIXME: Need M-1200 specific NMEA commands
//                    if (configPaneController != null) configPaneController.setRecordBySec(bySec);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("4", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    byDist = Integer.parseInt(segs[idx]);
                    // FIXME: Need M-1200 specific NMEA commands
//                    if (configPaneController != null) configPaneController.setRecordByDist(byDist);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("5", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    bySpeed = Integer.parseInt(segs[idx]);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("6", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    rcdMethod = Integer.parseInt(segs[idx]);
                    // FIXME: Need M-1200 specific NMEA commands
//                    if (configPaneController != null) configPaneController.setRcdMethod(rcdMethod);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("7", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    loggerStatus = Integer.parseInt(segs[idx]);
                    // TODO: need to take care
                    // Bit [10]: logger need format bit
                    // Bit [11]: logger full bit
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("8", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    rcdAddr = Long.parseLong(segs[idx], 16);

                    if (loggerTask instanceof LoggerTask.UploadTrack) {
                        totalBlocks = (int) (rcdAddr / 0x400) + 1;
                        readAddr = 0;
                        logData = null;
                        loggerThread.enqueueSendJob(
                                new SendJob(logger, null, String.format("PMTK182,7,%08X,00000400", readAddr), String.format("PMTK182,8,%08X", readAddr)) // Read log of 1KB size
                        );
                    }
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("9", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    flashId = segs[idx];
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("10", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    rcdRcnt = Long.parseLong(segs[idx], 16);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("11", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    failSector = Utils.hexStringToByteArray(segs[idx]);
                    return true;
                }
            }),
            new AbstractMap.SimpleEntry<>("12", new NmeaHandler<GpsLogger>() {
                @Override
                public boolean handle(GpsLogger logger, String[] segs, int idx) {
                    mtkVersion = Integer.parseInt(segs[idx]);
                    return true;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    @Override
    protected boolean preConnect() {
        // Validate SM
        if (this.loggerState == STATE_IDLE) {
            return true;
        } else {
            Logging.errorln("ConnectLogger: invalid state %d", this.loggerState);
            return false;
        }
    }

    @Override
    protected boolean preDisconnect() {
        // FIXME: Need M-1200 specific NMEA commands
//        if (loggerState == STATE_USB_MODE) {
//            // If in USB_MODE, transit HOLUX M-241 out of it.
//            loggerThread.enqueueSendJob(
//                    new SendJob(this, "Exit USB-Mode", "HOLUX241,2", "HOLUX001,2") // Transit HoluxM241 out from USB_MODE (TODO: Is it PHLX827->PHLX860 for GR245???)
//            );
//            return false;
//        } else {
//            return true;
//        }
        return true;
    }

    @Override
    protected void uploadTrack(String filePath, List<ExportType> exportTypes) {
        Logging.infoln("Reading data from [%s]...", loggerName);

        this.uploadFilePath = filePath;
        this.exportTypes = exportTypes;

        LinkedList<SendJob> jobs = new LinkedList<>();

        if ((loggerStatus & 0x0002) != 0) {
            // If auto-log is started, stop it
            jobs.add(new SendJob(this, "Stop logging", "PMTK182,5", "PMTK001,182,5,3")); // Stop logging
            jobs.add(new SendJob(this, "Query LoggerState", "PMTK182,2,7", "PMTK182,3,7")); // Query HoluxM1200 for log status.
        }
        jobs.add(new SendJob(this, null, "PMTK182,2,8", "PMTK182,3,8")); // Query HoluxM1200 for record address. (next write)

        // Send these NMEA commands to HOLUX M-1200
        loggerThread.enqueueSendJob(jobs.toArray(new SendJob[0]));
    }

    // FIXME: Need M-1200 specific NMEA commands
//    /**
//     * Perform Save Logger Config action.
//     *
//     * @param rcdMethod Value of record method.
//     * @param rcdBy     Value of record by.
//     * @param bySec     Value of record by seconds
//     * @param byDist    Value of record by distance
//     */
//    void saveConfig(int rcdMethod, int rcdBy, int bySec, int byDist) {
//        SendJob[] jobs = new SendJob[3];
//
//        jobs[0] = new SendJob(this, "Save RcdMethod", "PMTK182,1,6," + rcdMethod, "PMTK001,182,1,3"); // Change record method. (1: BySec, 2: ByDist)
//
//        if (rcdBy == 0) {
//            jobs[1] = new SendJob(this, "Save BySec", "PMTK182,1,3," + bySec * 10, "PMTK001,182,1,3"); // Change value of record by second
//        } else {
//            jobs[1] = new SendJob(this, "Save ByDist", "PMTK182,1,4," + byDist * 10, "PMTK001,182,1,3"); // Change value of record by distance
//        }
//
//        jobs[2] = new SendJob(this, "Save RcdBy", "HOLUX241,9," + rcdBy, "HOLUX001,9"); // Change record by. (0: BySec, 1: ByDist)
//
//        // Send these NMEA commands to HOLUX M-241
//        loggerThread.enqueueSendJob(jobs);
//    }

    // FIXME: Need M-1200 specific NMEA commands
//    /**
//     * Perform Modify UserName action.
//     *
//     * @param userName User name to be updated
//     */
//    void modUserName(String userName) {
//        // Send the NMEA command to HOLUX M-241
//        loggerThread.enqueueSendJob(
//                new SendJob(this, null, "HOLUX241,4," + userName, "HOLUX001,4") // Change logger user name
//        );
//    }

    // FIXME: Need M-1200 specific NMEA commands
//    class USBMode {
//
//        private Timer timer = null;
//
//        /**
//         * Keep USB_MODE alive by sending 'HOLUX241,6' repeatedly.
//         */
//        void keepAlive() {
//            if (timer != null) {
//                timer.cancel();
//            }
//
//            timer = new Timer("Timer-UsbMode");
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    loggerThread.enqueueSendJob(
//                            new SendJob.NonTask(GpsLogger.this, null, "HOLUX241,6", null) // HeartBeat with HOLUX M-241, to keep USB_MODE alive (TODO: What is for GR245???)
//                    );
//                }
//            }, 6000, 6000);
//        }
//
//        /**
//         * Cancel pending TimerTask.
//         */
//        void exit() {
//            if (timer != null) {
//                timer.cancel();
//                timer = null;
//            }
//        }
//    }
}
