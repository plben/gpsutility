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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * Working thread of logger entity.
 */
public final class LoggerThread extends Thread {
    /**
     * The logger entity this working thread serve for.
     */
    private final GpsLogger logger;
    /**
     * For NMEA sentences received from serial port.
     */
    private final LinkedList<RecvJob> ingressQueue = new LinkedList<>();
    /**
     * For NMEA sentences to be sent out to serial port.
     */
    private final LinkedList<SendJob> egressQueue = new LinkedList<>();
    /**
     * To be tested if this thread is running.
     */
    boolean running = false;

    /**
     * Constructor
     *
     * @param logger The logger entity this working thread serve for.
     */
    public LoggerThread(@NotNull GpsLogger logger) {
        super(String.format("Thread-[%s]", logger.name));
        this.logger = logger;
    }

    /**
     * Method to cancel all pending SendJobs in working thread.
     */
    void cancelSendJobs() {
        synchronized (this) {
            egressQueue.clear();
        }
    }

    /**
     * Method to enqueue SendJobs to working thread and get it notified.
     *
     * @param jobs Jobs to be executed.
     */
    public void enqueueSendJob(@NotNull SendJob... jobs) {
        synchronized (this) {
            for (SendJob job : jobs) {
                egressQueue.addLast(job);
            }
            notify();
        }
    }

    /**
     * Method to enqueue RecvJobs to working thread and get it notified.
     *
     * @param events Jobs to be executed.
     */
    void enqueueRecvJob(@NotNull RecvJob... events) {
        synchronized (this) {
            for (RecvJob event : events) {
                ingressQueue.addLast(event);
            }
            notify();
        }
    }

    /**
     * Method to stop this working thread.
     */
    void stopThread() {
        if (running) {
            Logging.infoln("Stopping thread [%s]...", logger.name);

            synchronized (this) {
                running = false;
                notify();
            }
        }
    }

    /**
     * Thread body.
     */
    @Override
    public void run() {
        Logging.infoln("Thread [%s]...started", logger.name);

        logger.sendJob = null;
        ingressQueue.clear();
        egressQueue.clear();
        running = true;

        while (running) {
            synchronized (this) {
                try {
                    // Wait for notification
                    wait();

                    // Handle all incoming RecvJobs
                    while (running && ingressQueue.size() > 0) {
                        RecvJob job = ingressQueue.removeFirst();
                        job.run();

                        if (!job.success) {
                            // In case of failed to handle incoming NMEA, all pending SendJobs are meaningless.
                            // So simply clear the SendJob queue.
                            egressQueue.clear();
                        }
                    }

                    // Handle outgoing SendJobs
                    while (running && logger.sendJob == null && egressQueue.size() > 0) {
                        // Take one job from EgressQueue and execute it.
                        egressQueue.removeFirst().run();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        }

        // Clear all pending jobs
        ingressQueue.clear();
        egressQueue.clear();

        Logging.infoln("Thread [%s]...stopped", logger.name);

        logger.loggerThread = null;
        logger.resetLogger();
    }

}
