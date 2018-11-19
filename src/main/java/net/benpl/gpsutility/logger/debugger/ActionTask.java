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

import net.benpl.gpsutility.logger.ActionListener;
import net.benpl.gpsutility.logger.StateListener;
import net.benpl.gpsutility.serialport.CommPort;
import net.benpl.gpsutility.serialport.CommProperty;

/**
 * Debugger implementation of {@link net.benpl.gpsutility.logger.ActionTask}
 */
abstract public class ActionTask extends net.benpl.gpsutility.logger.ActionTask<GpsLogger> {
    /**
     * Constructor of task.
     *
     * @param name           Name of this task.
     * @param gpsLogger      Logger entity to execute this task.
     * @param actionListener Listener on task execution.
     */
    public ActionTask(String name, GpsLogger gpsLogger, ActionListener actionListener) {
        super(name, gpsLogger, actionListener);
    }

    /**
     * Connect task - Connect to external GPS Data Logger.
     */
    public static class Connect extends net.benpl.gpsutility.logger.ActionTask.Connect<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger       The logger entity to execute this task.
         * @param actionListener  Listener on task execution.
         * @param commPort        Serial port to talk with this logger.
         * @param commBaudRateIdx Index of {@link CommProperty#commBaudRateList}
         * @param commDataBitsIdx Index of {@link CommProperty#commDataBitsList}
         * @param commParityIdx   Index of {@link CommProperty#commParityList}
         * @param commStopBitsIdx Index of {@link CommProperty#commStopBitsList}
         * @param commFlowCtrlIdx Index of {@link CommProperty#commFlowCtrlList}
         * @param stateListener   Listener on state changed of logger entity..
         */
        public Connect(GpsLogger gpsLogger, ActionListener actionListener, CommPort commPort, int commBaudRateIdx, int commDataBitsIdx, int commParityIdx, int commStopBitsIdx, int commFlowCtrlIdx, StateListener stateListener) {
            super(gpsLogger, actionListener, commPort, commBaudRateIdx, commDataBitsIdx, commParityIdx, commStopBitsIdx, commFlowCtrlIdx, stateListener);
        }

        /**
         * Callback to listen on logger state changed.
         *
         * @param state The new state.
         */
        @Override
        protected void stateChanged(int state) {
            if (state == GpsLogger.STATE_SERIALPORT_OPENED) {
                postExec(CAUSE.SUCCESS);
            }
        }
    }

    /**
     * Disconnect task - Disconnect from external GPS Data Logger.
     */
    public static class Disconnect extends net.benpl.gpsutility.logger.ActionTask.Disconnect<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         */
        public Disconnect(GpsLogger gpsLogger, ActionListener actionListener) {
            super(gpsLogger, actionListener);
        }

        /**
         * Task execution body.
         *
         * @return TRUE - task started and done, {@link #postExec(CAUSE)} should be invoked with SUCCESS cause to close this task at once;
         * FALSE - task started, waiting for response.
         */
        @Override
        protected boolean run() {
            return true;
        }
    }

    /**
     * DebugNmea task - Send NMEA sentence to external GPS Data Logger for debug purpose.
     */
    public static class DebugNmea extends net.benpl.gpsutility.logger.ActionTask.DebugNmea<GpsLogger> {
        /**
         * Constructor.
         *
         * @param gpsLogger      The logger entity to execute this task.
         * @param actionListener Listener on task execution.
         * @param nmea           The NMEA command to be sent for debug purpose.
         */
        public DebugNmea(GpsLogger gpsLogger, ActionListener actionListener, String nmea) {
            super(gpsLogger, actionListener, nmea);
        }
    }
}
