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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Serial port available parameters list in UI {@link javafx.scene.control.ComboBox}.
 */
public class CommProperty {
    /**
     * Pre-defined serial port baud rate list.
     */
    public static final ObservableList<CommProperty> commBaudRateList = FXCollections.observableArrayList(
            new CommProperty("4800", 4800),
            new CommProperty("9600", 9600),
            new CommProperty("14400", 14400),
            new CommProperty("19200", 19200),
            new CommProperty("38400", 38400),
            new CommProperty("57600", 57600),
            new CommProperty("115200", 115200),
            new CommProperty("230400", 230400),
            new CommProperty("460800", 460800),
            new CommProperty("921600", 921600)
    );

    /**
     * Pre-defined serial port data bits list.
     */
    public static final ObservableList<CommProperty> commDataBitsList = FXCollections.observableArrayList(
            new CommProperty("4", 4),
            new CommProperty("5", 5),
            new CommProperty("6", 6),
            new CommProperty("7", 7),
            new CommProperty("8", 8)
    );

    /**
     * Pre-defined serial port parity list.
     */
    public static final ObservableList<CommProperty> commParityList = FXCollections.observableArrayList(
            new CommProperty("None", SerialPort.NO_PARITY),
            new CommProperty("Odd", SerialPort.ODD_PARITY),
            new CommProperty("Even", SerialPort.EVEN_PARITY),
            new CommProperty("Mark", SerialPort.MARK_PARITY),
            new CommProperty("Space", SerialPort.SPACE_PARITY)
    );

    /**
     * Pre-defined serial port stop bits list.
     */
    public static final ObservableList<CommProperty> commStopBitsList = FXCollections.observableArrayList(
            new CommProperty("1", SerialPort.ONE_STOP_BIT),
            new CommProperty("1.5", SerialPort.ONE_POINT_FIVE_STOP_BITS),
            new CommProperty("2", SerialPort.TWO_STOP_BITS)
    );

    /**
     * Pre-defined serial port flow control list.
     */
    public static final ObservableList<CommProperty> commFlowCtrlList = FXCollections.observableArrayList(
            new CommProperty("Xoff/Xon", SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED),
            new CommProperty("HwCtrl", SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED | SerialPort.FLOW_CONTROL_DSR_ENABLED | SerialPort.FLOW_CONTROL_DTR_ENABLED),
            new CommProperty("None", SerialPort.FLOW_CONTROL_DISABLED)
    );

    /**
     * The prompt of this parameter. to display in UI {@link javafx.scene.control.ComboBox}.
     */
    private final String label;
    /**
     * The data of this parameter.
     */
    private final int data;

    /**
     * Constructor.
     *
     * @param label The prompt of this parameter.
     * @param data  The data of this parameter.
     */
    private CommProperty(String label, int data) {
        this.label = label;
        this.data = data;
    }

    /**
     * Get data of this parameter.
     *
     * @return The data of this parameter.
     */
    public int getData() {
        return data;
    }

    /**
     * Display prompt of this parameter in UI {@link javafx.scene.control.ComboBox}.
     *
     * @return The prompt of this parameter.
     */
    @Override
    public String toString() {
        return label;
    }
}
