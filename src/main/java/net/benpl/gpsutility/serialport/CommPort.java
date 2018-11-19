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
import net.benpl.gpsutility.logger.NmeaListener;
import net.benpl.gpsutility.misc.Logging;

import java.nio.charset.StandardCharsets;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;

/**
 * CommPort is the wrapper of {@link com.fazecast.jSerialComm.SerialPort}.
 */
public final class CommPort implements SerialPortDataListener {
    /**
     * Pre-defined tag for end of NMEA package.
     */
    private final static String END_OF_PACKAGE = "\r\n";

    /**
     * Name of this wrapped serial port.
     */
    private final String name;
    /**
     * The wrapped serial port.
     */
    private final SerialPort serialPort;
    /**
     * Listener on NMEA sentence.
     */
    private NmeaListener nmeaListener = null;

    /**
     * Byte buffer to receive data from serial port.
     */
    private final byte[] byteBuff = new byte[6144];
    /**
     * String buffer to store data converted from byteBuff.
     */
    private String strBuff = "";

    /**
     * Constructor.
     *
     * @param name       Name of this wrapped serial port.
     * @param serialPort The wrapped serial port.
     */
    public CommPort(String name, SerialPort serialPort) {
        this.name = name;
        this.serialPort = serialPort;
    }

    public String getName() {
        return name;
    }

    /**
     * Get name of this wrapped serial port.
     *
     * @return Name of this wrapped serial port.
     */
    public SerialPort getPort() {
        return serialPort;
    }

    /**
     * Display name of wrapped serial port by {@link net.benpl.gpsutility.logger.PrimaryController#commPorts}.
     *
     * @return Name of this wrapped serial port.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Set parameters for this serial port.
     *
     * @param commBaudRate Serial port baud rate.
     * @param commDataBits Serial port data bits.
     * @param commParity   Serial port parity.
     * @param commStopBits Serial port stop bits.
     * @param commFlowCtrl Serial port flow control.
     */
    public void setParameters(int commBaudRate, int commDataBits, int commParity, int commStopBits, int commFlowCtrl) {
        serialPort.setComPortParameters(commBaudRate, commDataBits, commStopBits, commParity);
        serialPort.setFlowControl(commFlowCtrl);
        // Note that write timeouts (2000 milliseconds) are only available on Windows operating systems. This value is ignored on all other systems.
        serialPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 0, 2000);
    }

    /**
     * Set listener on receiving NMEA sentence.
     *
     * @param nmeaListener Listener on NMEA sentence.
     */
    public void setNmeaListener(NmeaListener nmeaListener) {
        this.nmeaListener = nmeaListener;
    }

    /**
     * Override to return desired event {@link SerialPort#LISTENING_EVENT_DATA_AVAILABLE} for which the {@link #serialEvent(SerialPortEvent)}
     * callback should be triggered.
     * <p>
     * Valid event constants are:
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
                    int i;
                    // Read data out from serial port
                    int recvLen = serialPort.readBytes(byteBuff, byteBuff.length);
                    // Discard the data if no listener attached.
                    if (nmeaListener == null) break;
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
                    // Send string segment to listener one by one.
                    for (i = 0; i < (segs.length - 1); i++) {
                        nmeaListener.recvNmea(segs[i]);
                    }
                    // Verify the last string segment if it is ended with '\r\n'
                    i = segs.length - 1;
                    if (strRecv.endsWith(END_OF_PACKAGE)) {
                        // If the last segment is also ended with '\r\n', send it out.
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
     * Method to send NMEA sentence (string) on the serial port.
     * The sentence will be appended with '\r\n', converted to byte array with US_ASCII charset, and write to serial port.
     *
     * @param nmea The NMEA (string) to be sent.
     * @return TRUE - data sent successfully, FALSE - otherwise.
     */
    public boolean sendData(String nmea) {
        byte[] buff = (nmea + END_OF_PACKAGE).getBytes(StandardCharsets.US_ASCII);
        int sent = serialPort.writeBytes(buff, buff.length);
        return (sent == buff.length);
    }

    /**
     * Method to open serial port for communication.
     *
     * @return TRUE - opened successfully, FALSE - otherwise.
     */
    public boolean openPort() {
        Logging.info("Open serial port [%s]...", name);

        strBuff = "";

        if (serialPort.isOpen()) {
            Logging.infoln("success");
            serialPort.addDataListener(CommPort.this);
            return true;
        } else if (serialPort.openPort()) {
            Logging.infoln("success");
            serialPort.addDataListener(CommPort.this);
            return true;
        } else {
            Logging.infoln("failed");
            return false;
        }
    }

    /**
     * Method to close serial port.
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
}
