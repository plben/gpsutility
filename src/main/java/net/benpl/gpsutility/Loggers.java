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

package net.benpl.gpsutility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.benpl.gpsutility.logger.GpsLogger;
import net.benpl.gpsutility.logger.PrimaryController;

/**
 * Maintain the supported logger list.
 */
public class Loggers {

    /**
     * Maintain the list of all supported loggers.
     * To make your new introduced logger selectable in ComboBox {@link PrimaryController#loggerChooser},
     * you need to append it to this list once ready.
     */
    public static final ObservableList<GpsLogger> all = FXCollections.observableArrayList(
            new net.benpl.gpsutility.logger.holux_m241.GpsLogger(),
            new net.benpl.gpsutility.logger.holux_m1200.GpsLogger(),
            new net.benpl.gpsutility.logger.holux_gr245.GpsLogger(),
            new net.benpl.gpsutility.logger.debugger.GpsLogger()
    );

}
