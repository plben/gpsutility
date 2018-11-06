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

    private static final double EQUATORIAL_RADIUS = 6378137.0;

    /**
     * Calculate spherical distance between two coordinates.
     *
     * @param lat1 Latitude of coordinate 1.
     * @param lon1 Longitude of coordinate 1.
     * @param lat2 Latitude of coordinate 2.
     * @param lon2 Longitude of coordinate 2.
     * @return Distance between these 2 coordinates.
     */
    public static double sphericalDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * EQUATORIAL_RADIUS;
    }

    /**
     * Byte array compare with limited length.
     *
     * @param buff1   Byte array 1.
     * @param offset1 Offset of byte array 1.
     * @param buff2   Byte array 2.
     * @param offset2 Offset of byte array 2.
     * @param len     How many bytes to be compared.
     * @return TRUE - if equals, FALSE - otherwise.
     */
    public static boolean equals(byte[] buff1, int offset1, byte[] buff2, int offset2, int len) {
        for (int i = 0; i < len; i++) {
            if (buff1[offset1 + i] != buff2[offset2 + i]) return false;
        }
        return true;
    }

    /**
     * XOR checksum of string.
     *
     * @param str The string to be calculated.
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
     * @param offset Offset on byte array.
     * @param len    Total bytes to be checked.
     * @return Checksum
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
     * @return Byte array.
     */
    public static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * Convert byte array to Hex string.
     *
     * @param buff   The source byte array.
     * @param offset Offset from byte array.
     * @param len    Total bytes to Hex string.
     * @return The converted Hex string.
     */
    public static String byteArrayToHexString(byte[] buff, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(Integer.toString(buff[offset + i], 16));
        }
        return sb.toString();
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
     * Read variable length bytes from array and convert it to Integer value.
     *
     * @param buff   Byte array to be read.
     * @param offset Read offset on byte array.
     * @param len    Total bytes to be read. (should not larger than 4)
     * @return The Integer value.
     */
    public static Integer leReadInt(byte[] buff, int offset, int len) {
        byte[] swap = {0, 0, 0, 0};

        for (int i = 0; i < len; i++) {
            swap[swap.length - 1 - i] = buff[offset + i];
        }

        Integer value
                = ((swap[0] << 24) & 0xFF000000)
                | ((swap[1] << 16) & 0x00FF0000)
                | ((swap[2] << 8) & 0x0000FF00)
                | (swap[3] & 0x000000FF);

        return value;
    }

    /**
     * Read variable length bytes from array and convert it to Double value.
     *
     * @param buff   Byte array to be read.
     * @param offset Read offset on byte array.
     * @param len    Total bytes to be read. (should not larger than 4)
     * @return The Double value.
     */
    public static Double leReadFloatAsDouble(byte[] buff, int offset, int len) {
        byte[] swap = {0, 0, 0, 0};

        for (int i = 0; i < len; i++) {
            swap[swap.length - 1 - i] = buff[offset + len - 1 - i];
        }

        return (double) ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    /**
     * Read variable length bytes from array and convert it to Double value.
     *
     * @param buff   Byte array to be read.
     * @param offset Read offset on byte array.
     * @param len    Total bytes to be read. (should not larger than 8)
     * @return The Double value.
     */
    public static Double leReadDouble(byte[] buff, int offset, int len) {
        byte[] swap = {0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < len; i++) {
            swap[swap.length - 1 - i] = buff[offset + len - 1 - i];
        }

        return ByteBuffer.wrap(swap).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    /**
     * Verify if a string is not empty.
     *
     * @param str String to be verified.
     * @return TRUE - string is not empty, FALSE - otherwise.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

    /**
     * Verify if a string is empty.
     *
     * @param str String to be verified.
     * @return TRUE - string is empty, FALSE - otherwise.
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

}
