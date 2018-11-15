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
 * SendJob is the task for outgoing NMEA command packing, writing data to serial port, and starting no response timer
 * for the sent NMEA command if necessary.
 * <p>
 * To avoid conflict between threads, the task is not executed immediately, but enqueued to a FIFO queue {@link LoggerThread#egressQueue}.
 * {@link LoggerThread} is responsible for schedule these tasks execution sequentially.
 */
public class SendJob implements Runnable {

    /**
     * Which Logger entity this job is to be executed.
     */
    private final GpsLogger logger;
    /**
     * Description of this job
     */
    final String desc;
    /**
     * NMEA command to be sent out. (Data field only)
     */
    private final String nmeaCmd;
    /**
     * NMEA response to be expected. (Data field only)
     */
    private final String nmeaResp;
    private final boolean lastJob;
    /**
     * Expiry value of NoResp timer.
     */
    private long expiry = 2000;

    private Timer noRespTimer = null;

    public boolean isLastJob() {
        return lastJob;
    }

    /**
     * The job to encapsulate NMEA command and send it out on SerialPort.
     *
     * @param logger   Logger to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  The NMEA command to send out.
     * @param nmeaResp The expected NMEA response.
     */
    public SendJob(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp) {
        this.logger = logger;
        this.desc = desc;
        this.nmeaCmd = nmeaCmd;
        this.nmeaResp = nmeaResp;
        this.lastJob = false;
    }

    /**
     * The job to encapsulate NMEA command and send it out on SerialPort.
     *
     * @param logger   Logger to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  The NMEA command to send out.
     * @param nmeaResp The expected NMEA response.
     * @param lastJob  Is this the last job.
     */
    public SendJob(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp, boolean lastJob) {
        this.logger = logger;
        this.desc = desc;
        this.nmeaCmd = nmeaCmd;
        this.nmeaResp = nmeaResp;
        this.lastJob = lastJob;
    }

    /**
     * The job to encapsulate NMEA command and send it out on SerialPort.
     * It is for long time NMEA command, like format/erase logger. You can adjust the expiry value of NoResp timer to
     * adapt some special NMEA commands.
     *
     * @param logger   Logger to handle this job.
     * @param desc     Brief description of this job.
     * @param nmeaCmd  The NMEA command to send out.
     * @param nmeaResp The expected NMEA response.
     * @param lastJob  Is this the last job.
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
     * Job executed by logger thread {@link LoggerThread}.
     * Encapsulate NMEA package and send it out to SerialPort. If nmeaResp is not empty, a NoResp TimerTask will be scheduled for it.
     */
    @Override
    public void run() {
        // Execute call hook
        logger.preSendJob(nmeaCmd, nmeaResp);

        if (Utils.isNotEmpty(desc)) Logging.debugln("\n%s...start", desc);

        // Encapsulate NMEA package
        String nmea = String.format("$%s*%02X", nmeaCmd, Utils.getCheckSum(nmeaCmd));

        Logging.debug("<== %s...", nmea);

        // Send NMEA package
        if (logger.sPort.sendData(nmea)) {
            Logging.debugln("success");

            if (Utils.isNotEmpty(nmeaResp)) {
                // This SendJob is done. But expect to see response.
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
                // Task unrelated SendJob. Nothing to do.
                if (this instanceof NonTask) {
                    return;
                }

                // Task related SendJob. Need to determine if task is done.
                if (lastJob && logger.loggerTask != null) {
                    logger.loggerTask.postRun0(LoggerTask.CAUSE.SUCCESS);
                    logger.loggerTask = null;
                }
            }
        } else {
            Logging.debugln("fail\n");
            // Send DATA fail is CRITICAL!!!
            // It means connection broken, logger turn off, ...

            if (Utils.isNotEmpty(desc)) Logging.errorln("%s...failed", desc);

            // Stop attached task if exist.
            // Otherwise, stop logger entity silently.
            if (logger.loggerTask != null) {
                logger.loggerTask.postRun0(LoggerTask.CAUSE.SEND_DATA_FAIL);
                logger.loggerTask = null;
            } else {
                logger.stopLogger();
            }
        }
    }

    /**
     * Check if NMEA is the expected response.
     *
     * @param nmea The NMEA package passed from {@link RecvJob}. (DataField only)
     */
    boolean isRespExpected(String nmea) {
        return nmea.startsWith(nmeaResp);
    }

    /**
     * Start a new timer to monitor response from external logger.
     */
    private void startNoRespTimer() {
        // Start NoResp TimerTask for this job.
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

                // Stop associated task if exist.
                // Otherwise stop logger entity silently.
                if (logger.loggerTask != null) {
                    logger.loggerTask.postRun0(LoggerTask.CAUSE.NO_RESP);
                    logger.loggerTask = null;
                } else {
                    logger.stopLogger();
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

    public static class NonTask extends SendJob {

        public NonTask(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp) {
            super(logger, desc, nmeaCmd, nmeaResp);
        }

        public NonTask(GpsLogger logger, String desc, String nmeaCmd, String nmeaResp, long expiry) {
            super(logger, desc, nmeaCmd, nmeaResp, false, expiry);
        }
    }
}
