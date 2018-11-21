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

/**
 * Holux M-241 implementation of {@link net.benpl.gpsutility.logger.LogParser}.
 */
public class LogParser extends net.benpl.gpsutility.logger.LogParserHolux {

    private static final byte[] watermarkHead = new byte[]{'H', 'O', 'L', 'U', 'X', 'G', 'R', '2', '4', '1'};
    private static final byte[][] watermarkTails = new byte[][]{{'L', 'O', 'G', 'G', 'E', 'R'}, {'W', 'A', 'Y', 'P', 'N', 'T'}};

    /**
     * Constructor.
     *
     * @param logData Log data read from serial port.
     */
    public LogParser(byte[] logData) {
        super(logData);
    }

    /**
     * Calculate record size (bytes) base on field mask.
     *
     * @param fieldMask Field mask indicates which fields are available in this record.
     * @return Record size in bytes.
     */
    @Override
    protected int getRecordSize(int fieldMask) {
        return LogRecord.getRecordSize(fieldMask);
    }

    /**
     * Decode one log record from byte buffer.
     *
     * @param fieldMask Field mask indicates which fields are available in this record.
     * @param buff      Source byte buffer.
     * @param offset    Offset on byte buffer.
     * @return The decoded log record on success. Otherwise return NULL.
     */
    @Override
    protected LogRecord decodeRecord(int fieldMask, byte[] buff, int offset) {
        return LogRecord.decode(fieldMask, buff, offset);
    }

    /**
     * Get watermark head.
     *
     * @return The watermark head.
     */
    @Override
    protected byte[] getWatermarkHead() {
        return watermarkHead;
    }

    /**
     * Get watermark tails.
     *
     * @return The watermark tails.
     */
    @Override
    protected byte[][] getWatermarkTails() {
        return watermarkTails;
    }
}
