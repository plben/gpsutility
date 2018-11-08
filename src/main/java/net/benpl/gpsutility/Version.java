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

/**
 * Application version maintenance.
 */
class Version {

    /**
     * Initial version
     * =================================================================
     * GPX 1.1 full compliant.
     * <p>
     * Loggers in list:
     * - Holux M-241
     * The orange one has been verified.
     * The new model (white) was not tested yet.
     * - Holux M-1200 (Not tested yet)
     * - Holux GR-245 (Not tested yet)
     */
    private static final String v1_0 = "V1.0";

    /**
     * - USB_MODE for Holux GR-245
     * - KML 2.2 compliant (with Google extension)
     * - Minor bug fix, and optimization
     */
    private static final String v1_1 = "V1.1";

    /**
     * Application current version
     */
    static final String current = v1_1;
}
