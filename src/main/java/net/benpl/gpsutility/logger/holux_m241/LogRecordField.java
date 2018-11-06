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

import net.benpl.gpsutility.misc.Utils;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holux M-241 log record field's decoder.
 */
abstract public class LogRecordField {

    // The name of this field
    public final String name;
    // The size (bytes) of this field
    final int size;

    LogRecordField(String name, int size) {
        this.name = name;
        this.size = size;
    }

    /**
     * Decode this field on byte buffer.
     *
     * @param record The log record object to store fields info. It also carries the Field Mask info.
     * @param buff   The source byte buffer.
     * @param offset Offset on byte buffer for this field.
     * @return How many bytes have been consumed after successfully decoded.
     */
    abstract int decode(LogRecord record, byte[] buff, int offset);

    // Bit mask for each field.
    private static final int FIELD_MASK_UTC = 0x00000001;
    private static final int FIELD_MASK_VALID = 0x00000002;
    private static final int FIELD_MASK_LATITUDE = 0x00000004;
    private static final int FIELD_MASK_LONGITUDE = 0x00000008;
    private static final int FIELD_MASK_HEIGHT = 0x00000010;
    private static final int FIELD_MASK_SPEED = 0x00000020;
    private static final int FIELD_MASK_HEADING = 0x00000040;
    private static final int FIELD_MASK_DSTA = 0x00000080;
    private static final int FIELD_MASK_DAGE = 0x00000100;
    private static final int FIELD_MASK_PDOP = 0x00000200;
    private static final int FIELD_MASK_HDOP = 0x00000400;
    private static final int FIELD_MASK_VDOP = 0x00000800;
    private static final int FIELD_MASK_NSAT = 0x00001000;
    static final int FIELD_MASK_SID = 0x00002000;
    static final int FIELD_MASK_ELEVATION = 0x00004000;
    static final int FIELD_MASK_AZIMUTH = 0x00008000;
    static final int FIELD_MASK_SNR = 0x00010000;
    private static final int FIELD_MASK_RCR = 0x00020000;
    private static final int FIELD_MASK_MILLISECOND = 0x00040000;
    private static final int FIELD_MASK_DISTANCE = 0x00080000;

    /**
     * All field decoders.
     */
    static final Map<Integer, LogRecordField> all = Stream.of(
            new AbstractMap.SimpleEntry<>(FIELD_MASK_UTC, new LogRecordField("UTC", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setUtc(new Date(Utils.leReadInt(buff, offset, size) * 1000L));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_VALID, new LogRecordField("VALID", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setValid(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_LATITUDE, new LogRecordField("LATITUDE,N/S", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setLatitude(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_LONGITUDE, new LogRecordField("LONGITUDE,E/W", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setLongitude(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_HEIGHT, new LogRecordField("HEIGHT", 3) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setHeight(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_SPEED, new LogRecordField("SPEED", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setSpeed(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_HEADING, new LogRecordField("HEADING", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setHeading(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_DSTA, new LogRecordField("DSTA", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setDsta(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_DAGE, new LogRecordField("DAGE", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setDage(Utils.leReadFloatAsDouble(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_PDOP, new LogRecordField("PDOP", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setPdop(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_HDOP, new LogRecordField("HDOP", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setHdop(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_VDOP, new LogRecordField("VDOP", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setVdop(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_NSAT, new LogRecordField("NSAT (USED/VIEW)", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    // BIT[7:0] Number of satellites in view
                    record.setNsatInView(buff[offset] & 0x00FF);
                    // BIT[15:8] Number of satellites in use
                    record.setNsatInUsed(buff[offset + 1] & 0x00FF);
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_SID, new LogRecordField("SID", 4) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    // SID->BIT[23:16]􀃎 Number of satellites in view (Duplicated with NSAT???)
                    // (The first SID contains this info)
                    int count = Utils.leReadInt(buff, offset + 2, 2);
                    if (count > LogRecord.MAX_USED_SATELLITES) {
                        record.setSatCount(LogRecord.MAX_USED_SATELLITES);  // this can't happen ? or...
                    } else {
                        record.setSatCount(count);
                    }

                    int fieldSize;
                    int idx = offset;

                    for (int i = 0; i < count; i++) {
                        LogRecord.SatInfo sat = new LogRecord.SatInfo();

                        // SID->BIT[7:0]􀃎 ID of satellite in view
                        sat.id = buff[idx];
                        // SID->BIT[8]􀃎 SAT in use
                        sat.used = (buff[idx + 1] & 0x01) != 0;
                        idx += size;

                        // ELEVATION
                        if ((record.fieldMask & FIELD_MASK_ELEVATION) != 0) {
                            fieldSize = all.get(FIELD_MASK_ELEVATION).size;
                            sat.elevation = Utils.leReadInt(buff, idx, fieldSize);
                            idx += fieldSize;
                        }

                        // AZIMUTH
                        if ((record.fieldMask & FIELD_MASK_AZIMUTH) != 0) {
                            fieldSize = all.get(FIELD_MASK_AZIMUTH).size;
                            sat.azimut = Utils.leReadInt(buff, idx, fieldSize);
                            idx += fieldSize;
                        }

                        // SNR
                        if ((record.fieldMask & FIELD_MASK_SNR) != 0) {
                            fieldSize = all.get(FIELD_MASK_SNR).size;
                            sat.snr = Utils.leReadInt(buff, idx, fieldSize);
                            idx += fieldSize;
                        }

                        record.getSats()[i] = sat;
                    }

                    return idx;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_ELEVATION, new LogRecordField("ELEVATION", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    // Already handled in SID
                    return 0;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_AZIMUTH, new LogRecordField("AZIMUTH", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    // Already handled in SID
                    return 0;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_SNR, new LogRecordField("SNR", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    // Already handled in SID
                    return 0;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_RCR, new LogRecordField("RCR", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setRcr(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_MILLISECOND, new LogRecordField("MILLISECOND", 2) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setMilliseconds(Utils.leReadInt(buff, offset, size));
                    return size;
                }
            }),
            new AbstractMap.SimpleEntry<>(FIELD_MASK_DISTANCE, new LogRecordField("DISTANCE", 8) {
                @Override
                int decode(LogRecord record, byte[] buff, int offset) {
                    record.setDistance(Utils.leReadDouble(buff, offset, size));
                    return size;
                }
            })
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

}
