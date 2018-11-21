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

package net.benpl.gpsutility.logger;

import net.benpl.gpsutility.misc.Logging;
import net.benpl.gpsutility.misc.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * SendJob is the wrapper of outgoing NMEA sentence, to be executed by working thread of logger entity.
 */
public class SendJob implements Runnable {
    /**
     * The logger entity to execute this job.
     */
    private final GpsLogger logger;
    /**
     * Description of this job
     */
    final String desc;
    /**
     * NMEA sentence to be sent out. (Data field only)
     */
    private final String nmeaCmd;
    /**
     * NMEA sentence to be expected. (Data field only)
     */
    private final String nmeaResp;
    /**
     * Whether this job is the last one.
     */
    private final boolean lastJob;
    /**
     * Expiry value of NoResp timer.
     */
    private final long expiry;
    /**
     * NoResp timer.
     */
    private Timer noRespTimer = null;

    /**
     * Constructor.
     *
     * @param logger   Logger entity to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  NMEA sentence to be send out.
     * @param nmeaResp NMEA sentence to be expected.
     */
    public SendJob(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp) {
        this(logger, desc, nmeaCmd, nmeaResp, false);
    }

    /**
     * Constructor.
     *
     * @param logger   Logger entity to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  NMEA sentence to be send out.
     * @param nmeaResp NMEA sentence to be expected.
     * @param lastJob  Last job indicator.
     */
    public SendJob(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp, boolean lastJob) {
        this(logger, desc, nmeaCmd, nmeaResp, lastJob, 2000);
    }

    /**
     * Constructor.
     *
     * @param logger   Logger entity to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  NMEA sentence to be send out.
     * @param nmeaResp NMEA sentence to be expected.
     * @param lastJob  Last job indicator.
     * @param expiry   Expiry value of NoResp timer.
     */
    public SendJob(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp, boolean lastJob, long expiry) {
        this.logger = logger;
        this.desc = desc;
        this.nmeaCmd = nmeaCmd;
        this.nmeaResp = nmeaResp;
        this.lastJob = lastJob;
        this.expiry = expiry;
    }

    /**
     * Job body
     */
    @Override
    public void run() {
        if (Utils.isNotEmpty(desc)) Logging.debugln("\n%s...start", desc);

        // Encapsulate NMEA package
        String nmea = String.format("$%s*%02X", nmeaCmd, Utils.getCheckSum(nmeaCmd));

        Logging.debug("<== %s...", nmea);

        // Send NMEA package
        if (logger.commPort.sendData(nmea)) {
            Logging.debugln("success");

            if (Utils.isNotEmpty(nmeaResp)) {
                // This SendJob is done. But want to check response.
                // Save this job for later response checking;
                logger.sendJob = this;
                // Start a NoResp timer for this job.
                startNoRespTimer();
            } else {
                // This SendJob is done. No interested for response checking.
                logger.sendJob = null;

                // SendJob level
                if (Utils.isNotEmpty(desc)) Logging.debugln("%s...success", desc);

                // Task level
                if (this instanceof NonTask) {
                    // Task unrelated SendJob. Nothing to do.
                } else {
                    // Task related SendJob. Need to determine if task is done.
                    if (lastJob && logger.actionTask != null) {
                        logger.actionTask.postExec(ActionTask.CAUSE.SUCCESS);
                        logger.actionTask = null;
                    }
                }
            }
        } else {
            Logging.debugln("fail\n");
            // Send DATA fail is CRITICAL!!!
            // It means connection broken, logger turn off, ...

            if (Utils.isNotEmpty(desc)) Logging.errorln("%s...failed", desc);

            if (logger.actionTask != null) {
                // Stop attached task if exist.
                logger.actionTask.postExec(ActionTask.CAUSE.SEND_DATA_FAIL);
                logger.actionTask = null;
            } else {
                // Otherwise, stop logger entity silently.
                logger.loggerThread.stopThread();
            }
        }
    }

    /**
     * Test if this job is the last one.
     *
     * @return TRUE - the last one; FALSE - not the last one.
     */
    boolean isLastJob() {
        return lastJob;
    }

    /**
     * Test if received NMEA sentence is the expected response.
     *
     * @param nmea The NMEA sentence received from serial port.
     * @return TRUE - the expected response; FALSE - not expected.
     */
    final boolean isRespExpected(String nmea) {
        return Utils.isNotEmpty(nmeaResp) && nmea.startsWith(nmeaResp);
    }

    /**
     * Method to handle received NMEA sentence. (It is tested by {@link #isRespExpected(String)} as expected)
     *
     * @param nmea The NMEA sentence received from serial port.
     * @return TRUE - handled correctly; FALSE - failed to handle.
     */
    final boolean handleResp(String nmea) {
        if (nmea.length() == nmeaResp.length()) {
            return handle(null);
        } else if (nmea.length() > nmeaResp.length() + 1) {
            if (nmea.charAt(nmeaResp.length()) == ',') {
                return handle(nmea.substring(nmeaResp.length() + 1));
            }
        }

        return false;
    }

    /**
     * The handler body to handle received NMEA sentence. (to be override)
     *
     * @param nmea The NMEA sentence received from serial port.
     * @return TRUE - handled correctly; FALSE - failed to handle.
     */
    public boolean handle(String nmea) {
        return true;
    }

    /**
     * Start NoResp timer for this job.
     */
    private void startNoRespTimer() {
        noRespTimer = new Timer("Timer-NoResponse");
        noRespTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Logging.errorln("[%s] ...no response!", nmeaCmd);

                // Cancel this SendJob
                logger.sendJob.cancelNoRespTimer();
                logger.sendJob = null;

                // Cancel all pending SendJobs
                logger.cancelAllSendJobs();

                if (logger.actionTask != null) {
                    // Stop associated task if exist.
                    logger.actionTask.postExec(ActionTask.CAUSE.NO_RESP);
                    logger.actionTask = null;
                } else {
                    // Otherwise stop logger entity silently.
                    logger.loggerThread.stopThread();
                }
            }
        }, expiry);
    }

    /**
     * Cancel pending NoResp TimerTask.
     */
    void cancelNoRespTimer() {
        if (noRespTimer != null) {
            noRespTimer.cancel();
            noRespTimer = null;
        }
    }

    /**
     * Another style SendJob which is none task related.
     */
    public static class NonTask extends SendJob {
        public NonTask(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp) {
            super(logger, desc, nmeaCmd, nmeaResp);
        }
    }
}
