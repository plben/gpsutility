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

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import net.benpl.gpsutility.export.ExportType;
import net.benpl.gpsutility.misc.Logging;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Debugger entity inherited from {@link net.benpl.gpsutility.logger.GpsLogger}.
 */
public final class GpsLogger extends net.benpl.gpsutility.logger.GpsLogger {

    public GpsLogger() {
        // 38400/8bits/No Parity/1bit/No Flow Control
        super("Debugger", 4, 4, 0, 0, 2);
    }

    @Override
    public LinkedHashMap<String, AnchorPane> createLoggerPanel() {
        // No addition Panel for Debugger
        return new LinkedHashMap<String, AnchorPane>() {{
        }};
    }

    @Override
    protected boolean preConnect() {
        return true;
    }

    @Override
    protected boolean preDisconnect() {
        return true;
    }

    @Override
    protected void uploadTrack(String filePath, List<ExportType> exportTypes) {
        // nothing to do
    }

    @Override
    protected void serialPortReady() {
        Logging.infoln("%s...success", loggerTask.getName());
        // Notify caller the success.
        Platform.runLater(() -> {
            loggerTask.onSuccess();
            loggerTask = null;
        });
    }

    @Override
    protected void preResetLogger() {
        // nothing to do
    }

    @Override
    protected void preSendJob(String nmeaCmd, String nmeaResp) {
        // nothing to do
    }

    @Override
    protected boolean dispatchNmea(String[] segs) {
        // Yes, got it and handled
        return true;
    }

}
