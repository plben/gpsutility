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

package net.benpl.gpsutility.logger.debugger;

import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.logger.ActionListener;
import net.benpl.gpsutility.logger.LogParser;
import net.benpl.gpsutility.logger.StateListener;
import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.serialport.CommPort;

import java.util.LinkedHashMap;

/**
 * Debugger implementation of {@link net.benpl.gpsutility.logger.GpsLogger}.
 */
public final class GpsLogger extends net.benpl.gpsutility.logger.GpsLogger {

    public GpsLogger() {
        // 38400/8bits/No Parity/1bit/No Flow Control
        super("Debugger", 4, 4, 0, 0, 2);
    }

    @Override
    public LinkedHashMap<String, AnchorPane> createLoggerPanel() {
        // No addition Panel for Debugger
        return new LinkedHashMap<>() {{
        }};
    }

    @Override
    protected void preResetLogger() {
        // nothing to do
    }

    @Override
    protected void performConnect(ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener loggerStateListener) {
        execActionTask(new ActionTask.Connect(this, actionListener, commPort, commBaudRateIdx, commDataBitsIdx, commParityIdx, commStopBitsIdx, commFlowCtrlIdx, loggerStateListener));
    }

    @Override
    protected void performDisconnect(ActionListener actionListener) {
        execActionTask(new ActionTask.Disconnect(this, actionListener));
    }

    @Override
    protected void performDebugNmea(ActionListener actionListener, String nmea) {
        execActionTask(new ActionTask.DebugNmea(this, actionListener, nmea));
    }

    @Override
    protected void performUploadTrack(ActionListener actionListener) {
        Logging.errorln("UploadTrack is not supported.");
    }

    @Override
    protected LogParser getParser() {
        return null;
    }

}
