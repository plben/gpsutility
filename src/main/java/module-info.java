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

module net.benpl.gpsutility {
    // Runtime modules
    requires java.xml;
    requires java.prefs;
    // Serial port
    requires com.fazecast.jSerialComm;
    // Jaxb API & Implementation
    requires java.xml.bind;
    requires com.sun.xml.bind;
    // Annotation @PostConstruct
    requires java.annotation;
    // JetBrains annotations
    requires org.jetbrains.annotations;
    // JavaFX
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // By default, a type in a module is not accessible to other modules unless it’s a public type and you export its package.
    // Apply to public type.
    exports net.benpl.gpsutility to javafx.fxml, javafx.graphics;
    exports net.benpl.gpsutility.logger to javafx.base;
    // Apply to reflection.
    opens net.benpl.gpsutility.logger to javafx.fxml;
    opens net.benpl.gpsutility.logger.holux_m241 to javafx.fxml;
    opens net.benpl.gpsutility.gpx to java.xml.bind;
    opens net.benpl.gpsutility.kml to java.xml.bind;
}
