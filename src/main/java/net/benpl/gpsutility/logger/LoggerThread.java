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
 * Working thread of Logger entity.
 * <p>
 * It is created when SerialPort is ready, and will be destroyed when SerialPort is closed. This thread monitors on
 * {@link #ingressQueue} for RecvJob task scheduling in FIFO manner, and {@link #egressQueue} for SendJob task scheduling
 * in same manner.
 */
public final class LoggerThread extends Thread {

    private final GpsLogger logger;

    private final LinkedList<RecvJob> ingressQueue = new LinkedList<>();
    private final LinkedList<SendJob> egressQueue = new LinkedList<>();

    private boolean running = false;

    public LoggerThread(@NotNull GpsLogger logger) {
        super(String.format("Thread-[%s]", logger.loggerName));
        this.logger = logger;
    }

    /**
     * Determine whether this thread is running.
     *
     * @return TRUE: running, FALSE: not running.
     */
    boolean isRunning() {
        return running;
    }

    /**
     * Determine if SendJob queue is empty.
     *
     * @return TRUE - empty, FALSE - not empty
     */
    boolean isEgressQueueEmpty() {
        return egressQueue.size() == 0;
    }

    /**
     * Cancel all pending SendJobs in {@link #egressQueue}.
     */
    void cancelSendJobs() {
        synchronized (this) {
            egressQueue.clear();
        }
    }

    /**
     * Enqueue SendJobs to this thread and get it notified.
     *
     * @param jobs The jobs to be executed.
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
     * Enqueue RecvJobs to this thread and get it notified.
     *
     * @param events The jobs to be executed.
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
     * Mark thread as InActive and get it notified.
     */
    void stopThread() {
        if (running) {
            Logging.infoln("Stopping thread [%s]...", logger.loggerName);

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
        Logging.infoln("Thread [%s]...started", logger.loggerName);

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
                    // Execute next job only if current job has been answered, or response is not necessary.
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

        logger.loggerThread = null;
        logger.resetLogger();

        Logging.infoln("Thread [%s]...stopped", logger.loggerName);
        Logging.infoln("...disconnected.");
    }

}
