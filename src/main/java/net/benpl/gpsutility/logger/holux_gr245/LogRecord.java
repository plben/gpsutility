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

package net.benpl.gpsutility.logger.holux_gr245;

import net.benpl.gpsutility.type.AbstractLogRecord;

/**
 * Log record to store GPS information of each way point, and how to decode the log data from Holux M-241.
 */
final class LogRecord extends AbstractLogRecord {

    /**
     * The maximum used satellites number. Should not exceed the length of {@link AbstractLogRecord#sats}
     */
    static final int MAX_USED_SATELLITES = 32;

    /**
     * The FormatRegister value when this record occurred.
     */
    final int fieldMask;

    /**
     * Create a log record with field mask (Format register).
     *
     * @param fieldMask The value indicates which fields are available in this record.
     */
    private LogRecord(int fieldMask) {
        this.fieldMask = fieldMask;
    }

    /**
     * Decode one log record from byte buffer.
     *
     * @param fieldMask Field mask indicates which fields are available in this record.
     * @param buff      Source byte buffer.
     * @param offset    Offset on byte buffer.
     * @return The decoded log record on success. Otherwise return NULL.
     */
    static AbstractLogRecord decode(int fieldMask, byte[] buff, int offset) {
        try {
            LogRecord record = new LogRecord(fieldMask);

            int idx = 0;
            int bmask;

            // Decode field by field within the record
            for (int i = 0; i < 32; i++) {
                // Bit mask for field available detection
                bmask = 1 << i;
                if ((bmask & fieldMask) == 0) {
                    continue;
                }

                // Decode this field with associated field decoder.
                // Returned field size (bytes) is added to the buffer index for next round.
                LogRecordField field = LogRecordField.all.get(bmask);
                if (field != null) {
                    idx += field.decode(record, buff, offset + idx);
                }
            }

            return record;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Calculate record size (bytes) base on field mask.
     *
     * @param fieldMask Field mask indicates which fields are available in this record.
     * @return The record size in bytes.
     */
    static int getRecordSize(int fieldMask) {
        int bmask;
        int size = 0;

        // Loop through all fields
        for (int i = 0; i < 32; i++) {
            // Bit mask for field available detection
            bmask = 1 << i;
            if ((bmask & fieldMask) == 0) {
                continue;
            }

            // Only take care the known fields. (supported)
            LogRecordField field = LogRecordField.all.get(bmask);
            if (field == null) continue;

            // Field SID/ELEVATION/AZIMUTH/SNR contains multiple satellites info.
            if (((fieldMask & LogRecordField.FIELD_MASK_SID) != 0)
                    && (bmask == LogRecordField.FIELD_MASK_SID || bmask == LogRecordField.FIELD_MASK_ELEVATION || bmask == LogRecordField.FIELD_MASK_AZIMUTH || bmask == LogRecordField.FIELD_MASK_SNR)) {
                size += field.size * MAX_USED_SATELLITES;
            } else {
                size += field.size;
            }
        }

        // + 1(byte) checksum
        size++;

        return size;
    }

}
