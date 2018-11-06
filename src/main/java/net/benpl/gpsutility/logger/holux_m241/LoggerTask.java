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

package net.benpl.gpsutility.logger.holux_m241;

/**
 * Inherited from {@link net.benpl.gpsutility.logger.LoggerTask} for MTK specific implementation.
 */
abstract public class LoggerTask extends net.benpl.gpsutility.logger.LoggerTask<GpsLogger> {
    /**
     * Constructor of task.
     *
     * @param name The name of this task.
     */
    public LoggerTask(String name) {
        super(name);
    }

    /**
     * SaveConfig Task
     */
    abstract public static class SaveConfig extends LoggerTask {
        private final int rcdMethod;
        private final int rcdBy;
        private final int bySec;
        private final int byDist;

        public SaveConfig(int rcdMethod, int rcdBy, int bySec, int byDist) {
            super("Save Config");
            this.rcdMethod = rcdMethod;
            this.rcdBy = rcdBy;
            this.bySec = bySec;
            this.byDist = byDist;
        }

        @Override
        public void run(GpsLogger gpsLogger) {
            gpsLogger.saveConfig(rcdMethod, rcdBy, bySec, byDist);
        }
    }

    /**
     * ModUserName Task
     */
    abstract public static class ModUserName extends LoggerTask {
        private final String userName;

        public ModUserName(String userName) {
            super("Modify UserName");
            this.userName = userName;
        }

        @Override
        public void run(GpsLogger gpsLogger) {
            gpsLogger.modUserName(userName);
        }
    }
}
