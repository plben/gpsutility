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

package net.benpl.gpsutility.misc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Miscellaneous utilities.
 */
public class Utils {

    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final double EARTH_EQUATORIAL_RADIUS = 6378137.0; // a
    private static final double EARTH_POLE_RADIUS = 6356752.3;       // b
    private static final double EARTH_RADIUS = 6371008.8;            // r = 1/3 * (2a + b)

    /**
     * Calculate spherical distance between two coordinates.
     * <p>
     * Formula:
     * a = sin²(Δφ/2) + cos φ1 * cos φ2 * sin²(Δλ/2)
     * c = 2 ⋅ atan2( √a, √(1−a) )
     * d = R ⋅ c
     * (φ is latitude, λ is longitude, R is earth’s radius)
     *
     * @param lat1 Latitude of origin coordinate.
     * @param lon1 Longitude of origin coordinate.
     * @param lat2 Latitude of destination coordinate.
     * @param lon2 Longitude of destination coordinate.
     * @return Spherical distance between two coordinates.
     */
    public static double sphericalDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = square(Math.sin(dLat / 2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * square(Math.sin(dLon / 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * EARTH_RADIUS;
    }

    /**
     * Replacement of {@link Math#pow(double, double)}.
     *
     * @param value Value to be squared.
     * @return Squared value.
     */
    private static double square(double value) {
        return value * value;
    }

    /**
     * Compare two byte arrays with particular length.
     *
     * @param array1  Byte array 1.
     * @param offset1 Offset of byte array 1.
     * @param array2  Byte array 2.
     * @param offset2 Offset of byte array 2.
     * @param length  Bytes to be compared.
     * @return TRUE - if equals, FALSE - otherwise.
     */
    public static boolean compareByteArray(byte[] array1, int offset1, byte[] array2, int offset2, int length) {
        for (int i = 0; i < length; i++) {
            if (array1[offset1 + i] != array2[offset2 + i]) return false;
        }
        return true;
    }

    /**
     * XOR checksum of string.
     *
     * @param str String to be calculated.
     * @return The XOR checksum.
     */
    public static int getCheckSum(String str) {
        int chk = 0;
        for (int i = 0; i < str.length(); i++) {
            chk ^= str.charAt(i);
        }
        return chk & 0x00FF;
    }

    /**
     * XOR checksum of byte array.
     *
     * @param buff   Byte array to check.
     * @param offset Offset of byte array.
     * @param len    Bytes to be calculated.
     * @return The XOR checksum
     */
    public static int getCheckSum(byte[] buff, int offset, int len) {
        int chk = 0;
        for (int i = 0; i < len; i++) {
            chk ^= buff[offset + i];
        }
        return chk & 0x00FF;
    }

    /**
     * Convert Hex string to byte array.
     *
     * @param str Hex string to be converted.
     * @return Converted byte array; or NULL in case of failure.
     */
    public static byte[] toByteArray(String str) {
        int length = str.length();
        if ((length % 2) != 0) {
            Logging.errorln("Invalid string length: %d", length);
            return null;
        }

        byte[] buff = new byte[length / 2];
        for (int i = 0, h, l; i < length; i += 2) {
            h = charToDigit(str.charAt(i));
            if (h == -1) return null;

            l = charToDigit(str.charAt(i + 1));
            if (l == -1) return null;

            buff[i / 2] = (byte) ((h << 4) + l);
        }
        return buff;
    }

    /**
     * Convert character to digit.
     *
     * @param ch Character value.
     * @return Converted digit; or -1 in case of failure.
     */
    private static int charToDigit(char ch) {
        if (ch >= '0' && ch <= '9') return (ch - '0');
        else if (ch >= 'A' && ch <= 'F') return (ch - 'A' + 10);
        else if (ch >= 'a' && ch <= 'f') return (ch - 'a' + 10);
        else {
            Logging.errorln("Invalid character: [%c]", ch);
            return -1;
        }
    }

    /**
     * Convert byte array to Hex string.
     *
     * @param buff   Byte array to be converted.
     * @param offset Offset of byte array.
     * @param length Bytes to be converted.
     * @return Converted Hex string.
     */
    public static String toHexString(byte[] buff, int offset, int length) {
        char[] hexBuff = new char[length * 2];
        for (int i = 0, v; i < length; i++) {
            v = buff[offset + i] & 0xFF;
            hexBuff[i * 2] = HEX_ARRAY[v / 16];
            hexBuff[i * 2 + 1] = HEX_ARRAY[v % 16];
        }
        return new String(hexBuff);
    }

    /**
     * Concatenate two byte arrays into one.
     *
     * @param a The byte array a.
     * @param b The byte array b.
     * @return The byte array = a + b.
     */
    public static byte[] concatByteArray(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return null;
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            byte[] c = new byte[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
            return c;
        }
    }

    /**
     * Read bytes from buffer (little endian) and convert to Integer value.
     *
     * @param buff   Byte buffer.
     * @param offset Offset on byte buffer.
     * @param length Bytes to be read. (should not bigger than 4)
     * @return Converted Integer value.
     */
    public static Integer leReadInt(byte[] buff, int offset, int length) {
        byte[] swap = {0, 0, 0, 0};
        System.arraycopy(buff, offset, swap, 0, length);
        return ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Read bytes from buffer (little endian) and convert to Long value.
     *
     * @param buff   Byte buffer.
     * @param offset Offset on byte buffer.
     * @param length Bytes to be read. (should not bigger than 8)
     * @return Converted Long value.
     */
    public static Long leReadLong(byte[] buff, int offset, int length) {
        byte[] swap = {0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(buff, offset, swap, 0, length);
        return ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    /**
     * Read bytes from buffer (little endian), convert to Float value, and return as Double.
     *
     * @param buff   Byte buffer.
     * @param offset Offset on byte buffer.
     * @param length Bytes to read. (should not bigger than 4)
     * @return Converted Double value.
     */
    public static Double leReadFloatAsDouble(byte[] buff, int offset, int length) {
        byte[] swap = {0, 0, 0, 0};
        int skip = swap.length - length;
        System.arraycopy(buff, offset, swap, skip, length);
        return (double) ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    /**
     * Read bytes from buffer (little endian) and convert to Double value.
     *
     * @param buff   Byte buffer.
     * @param offset Offset on byte buffer.
     * @param length Bytes to read. (should not bigger than 8)
     * @return Converted Double value.
     */
    public static Double leReadDouble(byte[] buff, int offset, int length) {
        byte[] swap = {0, 0, 0, 0, 0, 0, 0, 0};
        int skip = swap.length - length;
        System.arraycopy(buff, offset, swap, skip, length);
        return ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    /**
     * Test if a string is not empty.
     *
     * @param str String to be tested.
     * @return TRUE - string is not empty, FALSE - otherwise.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

    /**
     * Test if a string is empty.
     *
     * @param str String to be tested.
     * @return TRUE - string is empty, FALSE - otherwise.
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

}
