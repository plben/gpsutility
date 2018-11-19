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

/**
 * Listener on ActionTask execution.
 * All callback methods are invoked with {@link javafx.application.Platform#runLater(Runnable)}.
 */
public interface ActionListener {

    /**
     * Callback before task execution to update UI components state.
     */
    void onStart();

    /**
     * Callback on task execution success.
     */
    void onSuccess();

    /**
     * Callback on task execution failure.
     *
     * @param cause The failure cause.
     */
    void onFail(ActionTask.CAUSE cause);

    /**
     * UploadTrack task specific ActionListener.
     */
    interface UploadTrack extends ActionListener {
        /**
         * Callback on upload track progress update.
         *
         * @param progress The upload progress.
         */
        void onProgress(double progress);
    }
}
