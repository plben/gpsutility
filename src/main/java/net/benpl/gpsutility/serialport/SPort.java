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

package net.benpl.gpsutility.serialport;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.type.INmeaListener;

import java.nio.charset.StandardCharsets;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;

/**
 * Wrapper of {@link com.fazecast.jSerialComm.SerialPort}.
 * <p>
 * This module is responsible for associated serial port configuration, open, close, and listen on it. Uplink data received
 * from serial port will be converted to string, segmented by '\r\n', and passed to upper listeners. Downlink string from
 * upper layer will be appended with '\r\n', converted to byte array, and sent out on the serial port.
 */
public final class SPort implements SerialPortDataListener {

    private final static String END_OF_PACKAGE = "\r\n";

    private final String name;
    private final SerialPort serialPort;

    private INmeaListener nmeaListener = null;

    private final byte[] byteBuff = new byte[4096];
    private String strBuff = "";

    public SPort(String name, SerialPort serialPort) {
        this.name = name;
        this.serialPort = serialPort;
    }

    public String getName() {
        return name;
    }

    public SerialPort getPort() {
        return serialPort;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Must be overridden to return one or more desired event constants for which the {@link #serialEvent(SerialPortEvent)}
     * callback should be triggered. Valid event constants are:
     * <p>
     * {@link SerialPort#LISTENING_EVENT_DATA_AVAILABLE}
     * {@link SerialPort#LISTENING_EVENT_DATA_RECEIVED}
     * {@link SerialPort#LISTENING_EVENT_DATA_WRITTEN}
     *
     * @return The event constants that should trigger the {@link #serialEvent(SerialPortEvent)} callback.
     */
    @Override
    public int getListeningEvents() {
        // Listen on data available only.
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    /**
     * Called whenever one of the serial port events specified by the {@link #getListeningEvents()} method occurs.
     *
     * @param event A {@link SerialPortEvent} object containing information and/or data about the serial event that occurred.
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        synchronized (this) {
            int eventType = event.getEventType();

            switch (eventType) {
                case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                    // Read data out from serial port
                    int recvLen = serialPort.readBytes(byteBuff, byteBuff.length);

                    // Discard the data if no listener is attached.
                    if (nmeaListener == null) {
                        break;
                    }

                    // Convert byte array to string, and append to string buffer.
                    String strRecv = strBuff + new String(byteBuff, 0, recvLen, StandardCharsets.US_ASCII);
                    // Split the whole string into segments with END_OF_PACKAGE
                    String[] segs = strRecv.split(END_OF_PACKAGE);

                    // Segmentation failure means no END_OF_PACKAGE found.
                    // Put new string into buffer, and continue listening...
                    if (segs.length == 0) {
                        strBuff = strRecv;
                        break;
                    }

                    int i;

                    // Send string segment to listener one by one.
                    for (i = 0; i < (segs.length - 1); i++) {
                        nmeaListener.recvNmea(segs[i]);
                    }

                    // Send the last string segment to listener.
                    i = segs.length - 1;
                    if (strRecv.endsWith(END_OF_PACKAGE)) {
                        // If the last segment is also completed, send it out.
                        nmeaListener.recvNmea(segs[i]);
                        // And free the string buffer.
                        strBuff = "";
                    } else {
                        // Otherwise, put the last segment into buffer.
                        strBuff = segs[i];
                    }

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Set properties on serial port.
     *
     * @param commBaudRate Serial port baud rate.
     * @param commDataBits Serial port data bits.
     * @param commParity   Serial port parity.
     * @param commStopBits Serial port stop bits.
     * @param commFlowCtrl Serial port flow control.
     */
    public void setProperties(int commBaudRate, int commDataBits, int commParity, int commStopBits, int commFlowCtrl) {
        serialPort.setComPortParameters(commBaudRate, commDataBits, commStopBits, commParity);
        serialPort.setFlowControl(commFlowCtrl);
        // Note that write timeouts (2000 milliseconds) are only available on Windows operating systems. This value is ignored on all other systems.
        serialPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 0, 2000);
    }

    /**
     * Called by logger object to listen on nmea package.
     *
     * @param nmeaListener Listener on nmea package.
     */
    public void setNmeaListener(INmeaListener nmeaListener) {
        this.nmeaListener = nmeaListener;
    }

    /**
     * Send data (string) on the serial port. The string will be appended with '\r\n', converted to byte array with
     * US_ASCII charset, and write to the serial port.
     *
     * @param data The data (string) to be sent.
     * @return TRUE - data sent successfully, FALSE - otherwise.
     */
    public boolean sendData(String data) {
        byte[] buff = (data + "\r\n").getBytes(StandardCharsets.US_ASCII);
        int sent = serialPort.writeBytes(buff, buff.length);
        return (sent == buff.length);
    }

    /**
     * Opens this serial port for reading and writing.
     *
     * @return TRUE - opened successfully, FALSE - otherwise.
     */
    public boolean openPort() {
        Logging.info("Open serial port [%s]...", name);

        strBuff = "";

        if (serialPort.isOpen()) {
            Logging.infoln("success");
            serialPort.addDataListener(SPort.this);
            return true;
        } else if (serialPort.openPort()) {
            Logging.infoln("success");
            serialPort.addDataListener(SPort.this);
            return true;
        } else {
            Logging.infoln("failed");
            return false;
        }
    }

    /**
     * Closes this serial port.
     */
    public void closePort() {
        Logging.info("Close serial port [%s]...", name);

        nmeaListener = null;
        serialPort.removeDataListener();

        if (serialPort.isOpen()) {
            // Close the serial port
            if (serialPort.closePort()) {
                Logging.infoln("success");
            } else {
                Logging.infoln("failed");
            }
        } else {
            Logging.infoln("success");
        }
    }

    /**
     * Returns whether the serial port is currently opened and available for communication.
     *
     * @return Whether the port is opened.
     */
    public boolean isConnected() {
        return serialPort.isOpen();
    }
}
