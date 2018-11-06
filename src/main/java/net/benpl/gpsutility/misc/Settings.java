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

import java.util.prefs.Preferences;

/**
 * Application preference mechanism.
 */
public class Settings {

    private static final Preferences prefs = Preferences.userRoot().node(Settings.class.getName());

    private static final String PREF_GPSTRACK_STOREPATH = "pref.GpsTrack.StorePath";

    public static String getUserHomeDir() {
        return System.getProperty("user.home");
    }

    public static String getGpsTrackStorePath() {
        return prefs.get(PREF_GPSTRACK_STOREPATH, System.getProperty("user.home"));
    }

    public static void setGpsTrackStorePath(String path) {
        prefs.put(PREF_GPSTRACK_STOREPATH, path);
    }

}
