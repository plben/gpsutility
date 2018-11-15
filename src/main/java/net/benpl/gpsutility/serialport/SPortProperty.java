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
 * Serial port available properties using in UI {@link javafx.scene.control.ComboBox}.
 */
public class SPortProperty {

    public static final ObservableList<SPortProperty> serialPortBaudRateList = FXCollections.observableArrayList(
            new SPortProperty("4800", 4800),
            new SPortProperty("9600", 9600),
            new SPortProperty("14400", 14400),
            new SPortProperty("19200", 19200),
            new SPortProperty("38400", 38400),
            new SPortProperty("57600", 57600),
            new SPortProperty("115200", 115200),
            new SPortProperty("230400", 230400),
            new SPortProperty("460800", 460800),
            new SPortProperty("921600", 921600)
    );

    public static final ObservableList<SPortProperty> serialPortDataBitsList = FXCollections.observableArrayList(
            new SPortProperty("4", 4),
            new SPortProperty("5", 5),
            new SPortProperty("6", 6),
            new SPortProperty("7", 7),
            new SPortProperty("8", 8)
    );

    public static final ObservableList<SPortProperty> serialPortParityList = FXCollections.observableArrayList(
            new SPortProperty("None", SerialPort.NO_PARITY),
            new SPortProperty("Odd", SerialPort.ODD_PARITY),
            new SPortProperty("Even", SerialPort.EVEN_PARITY),
            new SPortProperty("Mark", SerialPort.MARK_PARITY),
            new SPortProperty("Space", SerialPort.SPACE_PARITY)
    );

    public static final ObservableList<SPortProperty> serialPortStopBitsList = FXCollections.observableArrayList(
            new SPortProperty("1", SerialPort.ONE_STOP_BIT),
            new SPortProperty("1.5", SerialPort.ONE_POINT_FIVE_STOP_BITS),
            new SPortProperty("2", SerialPort.TWO_STOP_BITS)
    );

    public static final ObservableList<SPortProperty> serialPortFlowCtrlList = FXCollections.observableArrayList(
            new SPortProperty("Xoff/Xon", SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED),
            new SPortProperty("HwCtrl", SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED | SerialPort.FLOW_CONTROL_DSR_ENABLED | SerialPort.FLOW_CONTROL_DTR_ENABLED),
            new SPortProperty("None", SerialPort.FLOW_CONTROL_DISABLED)
    );

    private final String label;
    private final int data;

    private SPortProperty(String label, int data) {
        this.label = label;
        this.data = data;
    }

    public int getData() {
        return data;
    }

    @Override
    public String toString() {
        return label;
    }

}
