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

import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;
import net.benpl.gpsutility.type.AbstractLogParser;
import net.benpl.gpsutility.type.AbstractLogRecord;

import javax.xml.bind.DatatypeConverter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Log parser to parse the log data read from Holux M-241.
 */
public class LogParser extends AbstractLogParser {

    private static final int LOG_SECTOR_HEADER_SIZE = 0x200;

    private static final int RECORD_RCR_BY_BUTTON = 0x08;

    /**
     * Record method changed by dynamic setting.
     * 0 - Overlap
     * 1 - Stop on Full
     */
    private static final int DYNAMIC_SETTING_RECORD_METHOD_STOP_ON_FULL = 0x0004;
    /**
     * Logger state changed by dynamic setting.
     * 0 - Record stopped
     * 1 - Record started
     */
    private static final int DYNAMIC_SETTING_LOGGER_STATE_STARTED = 0x0002;

    private int sectorTotal;
    private int sectorRecordTotal;
    private int sectorFormatRegistor;
    private int sectorRecordMethod = 0;
    private int sectorLoggerState;
    private int sectorBySeconds;
    private int sectorByDistance;
    private int sectorBySpeed;
    private int sectorRecordSize; // can be modified on the fly by FormatRegistor from DYNAMIC_SETTING_PATTERN

    /**
     * Parse whole log data.
     */
    @Override
    public void parse() {
        byte[] detected;

        int totalRecordCount = 0;

        // Calculate total sectors.
        sectorTotal = logData.length / 65536 + 1;

        // Parse log data sector by sector
        for (int sectorIdx = 0; sectorIdx < sectorTotal; sectorIdx++) {
            int sectorRecordCount = 0;

            // Sector header portion (0x200 bytes of total)
            //=======================================================================
            if (!handleSectorHeader(sectorIdx)) continue;

            // Sector data portion (variable length)
            //=======================================================================
            // Skip whole sector header, start at data portion
            int offset = sectorIdx * 65536 + LOG_SECTOR_HEADER_SIZE;

            while ((offset < logData.length) && (sectorRecordCount < sectorRecordTotal)) {
                if (handleDynamicSetting(offset)) {
                    offset += DYNAMIC_SETTING_PATTERN_SIZE;
                } else if ((detected = detectWatermark(offset)) != null) {
                    // Watermark detected
                    Logging.debugln("Logger watermark [%s] detected", new String(detected));
                    offset += detected.length;
                } else if (handleEndOfSector(offset)) {
                    // EndOfSector detected
                    Logging.debugln("END_OF_SECTOR detected");
                    break;
                } else if (handleRecordData(offset)) {
                    // Increament record counters
                    sectorRecordCount++;
                    totalRecordCount++;

                    // Jump over current record
                    offset += sectorRecordSize;
                } else {
                    Logging.errorln("Don't know how to handle. Skip this sector and jump to next one");
                    break;
                }
            }
        }

        // If current segment is not empty, store it into list.
        if (!track.isEmpty()) {
            // Put it into list
            trackList.add(track);
        }

        if (trackList.size() > 0) {
            Logging.infoln("Total %d records", totalRecordCount);
        }
    }

    /**
     * Handle sector header portion.
     *
     * @param sectorIdx The sector index for prompt only.
     * @return TRUE - if handled successfully, FALSE - otherwise.
     */
    private boolean handleSectorHeader(int sectorIdx) {
        int offset = 0;

        Logging.infoln("\nSector #%d", sectorIdx);
        Logging.infoln("=========================================================");

        // Total records of this sector
        sectorRecordTotal = Utils.leReadInt(logData, offset, 2);
        offset += 2;
        if (sectorRecordTotal == 0x0000FFFF) {
            // 0xFFFF means this sector still has space to record data
            Logging.infoln("Sector #%d not full", sectorIdx);
        } else {
            Logging.infoln("Sector #%d with %d records", sectorIdx, sectorRecordTotal);
        }

        // Format register
        sectorFormatRegistor = Utils.leReadInt(logData, offset, 4);
        offset += 4;
        //sectorFormatRegistor &= 0x7FFFFFFF; // Clear Holux-specific 'low precision' bit
        Logging.infoln("Initial format register: 0x%08X", sectorFormatRegistor);

        // Initialize log record size (bytes) with Format Register
        sectorRecordSize = LogRecord.getRecordSize(sectorFormatRegistor);
        Logging.infoln("-> Record size %d bytes", sectorRecordSize);

        // Logger mode (log policy)
        sectorLoggerState = Utils.leReadInt(logData, offset, 2);
        offset += 2;
        Logging.infoln("Initial logger mode: 0x%04X", sectorIdx, sectorLoggerState);
        if (sectorIdx < (sectorTotal - 1)) {
            // TODO: why???
            if (sectorLoggerState != 0x0104 && sectorLoggerState != 0x0106) {
                Logging.errorln("-> Invalid initial logger mode, - ignore this sector");
                return false;
            }
        }

        // Config of Auto-Log
        sectorBySeconds = Utils.leReadInt(logData, offset, 4);
        offset += 4;
        Logging.infoln("Initial auto-log perid: %.01f (seconds)", (double) sectorBySeconds / 10);

        sectorByDistance = Utils.leReadInt(logData, offset, 4);
        offset += 4;
        Logging.infoln("Initial auto-log distance: %.01f (meters)", (double) sectorByDistance / 10);

        sectorBySpeed = Utils.leReadInt(logData, offset, 4);
        offset += 4;
        Logging.infoln("Initial auto-log speed: %.01f (km/h)", (double) sectorBySpeed / 10);

        return true;
    }

    /**
     * Handle dynamic setting on the fly.
     *
     * @param offset Offset of {@link #logData}
     * @return TRUE - if dynamic setting found and handled successfully, FALSE - otherwise.
     */
    private boolean handleDynamicSetting(int offset) {
        byte[] detected;

        if ((detected = detectDynamicSetting(offset)) == null) {
            return false;
        } else {
            // Dynamic Setting detected
            Logging.debugln("Dynamic setting [%s] detected", DatatypeConverter.printHexBinary(detected));

            DynamicSetting handler = dynamicSettings.get(detected[0] & 0x00FF);
            if (handler == null) {
                Logging.errorln("Unknown dynamic setting ID 0x%02X", detected[0]);
            } else {
                handler.handle(detected);
            }
            return true;
        }
    }

    /**
     * Determine End of sector in case sector is not full.
     *
     * @param offset Offset of {@link #logData}
     * @return TRUE - means End of this sector, FALSE - otherwise.
     */
    private boolean handleEndOfSector(int offset) {
        // If this sector is not full, need to detect the END_OF_SECTOR manually.
        return (sectorRecordTotal == 0x0000FFFF) && (isEndOfSector(offset, sectorRecordSize));
    }

    /**
     * Handle as record data.
     *
     * @param offset Offset of {@link #logData}
     * @return TRUE - if record decoded successfully, FALSE - otherwise.
     */
    private boolean handleRecordData(int offset) {
        // Checksum validation
        int chk = Utils.getCheckSum(logData, offset, sectorRecordSize);
        if (chk != 0) {
            Logging.errorln("Checksum fail on [%s] - END_OF_SECTOR???", Utils.byteArrayToHexString(logData, offset, sectorRecordSize));
            return false;
        }

        // Decode 1 record from byte buffer
        AbstractLogRecord record = LogRecord.decode(sectorFormatRegistor, logData, offset);
        if (record == null) {
            Logging.errorln("Skip this sector due to decoding failure!");
            return false;
        }

        // Put record into track
        track.add(record);

        // If recorded by button, this record is also a POI
        if (record.getRcr() != null && (record.getRcr() & RECORD_RCR_BY_BUTTON) != 0) {
            poiList.add(record);
        }

        return true;
    }

    /**
     * Handlers of all dynamic settings.
     */
    private final Map<Integer, DynamicSetting> dynamicSettings = Stream.of(
            new AbstractMap.SimpleEntry<>(2, (DynamicSetting) (byte[] buff) -> {
                // Format register
                int setting = Utils.leReadInt(buff, 1, 4);

                if (setting != sectorFormatRegistor) {
                    Logging.infoln("Format register updated to: 0x%08X", setting);

                    // Re-calculate log record size (bytes) with Format Register
                    sectorRecordSize = LogRecord.getRecordSize(setting);
                    Logging.infoln("-> New record size: %d (bytes)", sectorRecordSize);

                    sectorFormatRegistor = setting;
                }
            }),
            new AbstractMap.SimpleEntry<>(3, (DynamicSetting) (byte[] buff) -> {
                // Update 0.1 seconds of auto-log by second
                int setting = Utils.leReadInt(buff, 1, 4);
                if (setting != sectorBySeconds) {
                    Logging.infoln("Auto-log perid updated to: %.01f (seconds)", (double) setting / 10);
                    sectorBySeconds = setting;
                }
            }),
            new AbstractMap.SimpleEntry<>(4, (DynamicSetting) (byte[] buff) -> {
                // Update 0.1 meters of auto-log by distance
                int setting = Utils.leReadInt(buff, 1, 4);
                if (setting != sectorByDistance) {
                    Logging.infoln("Auto-log distance updated to: %.01f (meters)", (double) setting / 10);
                    sectorByDistance = setting;
                }
            }),
            new AbstractMap.SimpleEntry<>(5, (DynamicSetting) (byte[] buff) -> {
                // Update 0.1 km/h of auto-log by speed
                int setting = Utils.leReadInt(buff, 1, 4);
                if (setting != sectorBySpeed) {
                    Logging.infoln("Auto-log speed updated to: %.01f (km/h)", (double) setting / 10);
                    sectorBySpeed = setting;
                }
            }),
            new AbstractMap.SimpleEntry<>(6, (DynamicSetting) (byte[] buff) -> {
                // Update record method. Overlap or Stop on Full
                int setting = Utils.leReadInt(buff, 1, 2);
                if (setting != sectorRecordMethod) {
                    Logging.infoln("Record method updated to: %s", (setting & DYNAMIC_SETTING_RECORD_METHOD_STOP_ON_FULL) == 0 ? "Overlap" : "Stop on Full");
                    sectorRecordMethod = setting;
                }
            }),
            new AbstractMap.SimpleEntry<>(7, (DynamicSetting) (byte[] buff) -> {
                // Update logger mode. START or STOP
                int setting = Utils.leReadInt(buff, 1, 2);
                if (setting != sectorLoggerState) {
                    Logging.infoln("Logger state updated to: %s", (setting & DYNAMIC_SETTING_LOGGER_STATE_STARTED) == 0 ? "Stopped" : "Started");

                    // If logger is now stopped and current segment is not empty,
                    // save the segment to list and re-create a new one.
                    if ((setting & DYNAMIC_SETTING_LOGGER_STATE_STARTED) == 0 && !track.isEmpty()) {
                        // Put log segment into list
                        trackList.add(track);
                        // Re-create a new segment
                        track = new LinkedList<>();
                    }

                    sectorLoggerState = setting;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private interface DynamicSetting {

        void handle(byte[] buff);
    }

    /**
     * Determine if Dynamic Setting.
     *
     * @param offset Offset of {@link #logData}
     * @return The dynamic setting field if found, otherwise NULL.
     */
    private byte[] detectDynamicSetting(int offset) {
        if (!Utils.equals(logData, offset, DYNAMIC_SETTING_PATTERN_PREFIX, 0, DYNAMIC_SETTING_PATTERN_PREFIX.length))
            return null;
        if (!Utils.equals(logData, offset + DYNAMIC_SETTING_PATTERN_PREFIX.length + DYNAMIC_SETTING_PATTERN_DATA_SIZE, DYNAMIC_SETTING_PATTERN_SUFFIX, 0, DYNAMIC_SETTING_PATTERN_SUFFIX.length))
            return null;

        return Arrays.copyOfRange(logData,
                offset + DYNAMIC_SETTING_PATTERN_PREFIX.length,
                offset + DYNAMIC_SETTING_PATTERN_PREFIX.length + DYNAMIC_SETTING_PATTERN_DATA_SIZE);
    }

    /**
     * Determine if the End of sector.
     *
     * @param offset Offset of {@link #logData}
     * @param len    How many bytes to be compared. (record size usually)
     * @return TRUE - End, FALSE - Not end yet.
     */
    private boolean isEndOfSector(int offset, int len) {
        for (int i = 0; i < len; i++) {
            if (logData[offset + i] != (byte) 0xFF) {
                return false;
            }
        }
        return true;
    }

    /**
     * Detect watermark on log data.
     *
     * @param offset Offset of {@link #logData}
     * @return The watermark ID byte array if found. Otherwise NULL.
     */
    private byte[] detectWatermark(int offset) {
        int idx;
        if (Utils.equals(logData, offset, WATERMARK_TAG1, 0, WATERMARK_TAG1.length)) {
            idx = offset + WATERMARK_TAG1.length;

            if (Utils.equals(logData, idx, WATERMARK_TAG2, 0, WATERMARK_TAG2.length)) {
                idx = offset + WATERMARK_TAG1.length + WATERMARK_TAG2.length;
                while (logData[idx] == ' ') idx++;
                return Arrays.copyOfRange(logData, offset, idx);
            }

            if (Utils.equals(logData, idx, WATERMARK_TAG3, 0, WATERMARK_TAG3.length)) {
                idx = offset + WATERMARK_TAG1.length + WATERMARK_TAG3.length;
                while (logData[idx] == ' ') idx++;
                return Arrays.copyOfRange(logData, offset, idx);
            }
        }

        return null;
    }

    // DYNAMIC_SETTING_PATTERN
    private static final byte[] DYNAMIC_SETTING_PATTERN_PREFIX = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
    private static final byte[] DYNAMIC_SETTING_PATTERN_SUFFIX = {(byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB};
    private static final int DYNAMIC_SETTING_PATTERN_DATA_SIZE = 5;
    private static final int DYNAMIC_SETTING_PATTERN_SIZE = DYNAMIC_SETTING_PATTERN_PREFIX.length + DYNAMIC_SETTING_PATTERN_DATA_SIZE + DYNAMIC_SETTING_PATTERN_SUFFIX.length;

    // m241
    private static final byte[] WATERMARK_TAG1 = {'H', 'O', 'L', 'U', 'X', 'G', 'R', '2', '4', '1'};
    private static final byte[] WATERMARK_TAG2 = {'L', 'O', 'G', 'G', 'E', 'R'};
    private static final byte[] WATERMARK_TAG3 = {'W', 'A', 'Y', 'P', 'N', 'T'};

}
