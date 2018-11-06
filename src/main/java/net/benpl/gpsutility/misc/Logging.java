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

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Application logging mechanism.
 */
public class Logging {

    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int ERROR = 3;

    private static int level = INFO;

    private static PrintStream origOut = null;
    private static PrintStream origErr = null;
    private static LogOutputStream los = null;

    /**
     * Redirect internal log and all system console output to target object.
     *
     * @param target The target object log will be redirected to.
     */
    public static void redirectTo(Object target) {
        if (target instanceof TextArea) {
            // Save original system console output stream
            origOut = System.out;
            origErr = System.err;

            // Create output stream to this target
            los = new LogOutputStream<TextArea>((TextArea) target) {
                /**
                 * This method is for design intended logging. In other word, it
                 * is called by user code programmatically.
                 *
                 * @param text The text message to be printed in log window.
                 */
                @Override
                public void print(String text) {
                    // Append output to JTextArea
                    this.target.appendText(text);
                    // scrolls the text area to the end of data
                    this.target.setScrollTop(Double.MAX_VALUE);
                }

                /**
                 * The method is for Exception logging only. It's to capture and
                 * redirect any unknown message to this target.
                 *
                 * @param arg0 The Exception message redirected from console.
                 * @throws IOException In case of any Exception happened in this
                 * method.
                 */
                @Override
                public void write(int arg0) throws IOException {
                    // redirects data to the text area
                    this.target.appendText(Character.toString((char) arg0));
                    // scrolls the text area to the end of data
                    this.target.setScrollTop(Double.MAX_VALUE);
                }
            };

            // Create print stream on the output stream
            PrintStream printStream = new PrintStream(los);

            // Redirect system console output to this new print stream
            System.setOut(printStream);
            System.setErr(printStream);
        } else {
            System.out.println("Redirect log to " + target.getClass().getCanonicalName() + " is not supported!");
        }
    }

    /**
     * Restore system console output to original state.
     */
    public static void restore() {
        if (origOut == null || origErr == null) {
            return;
        }

        System.setOut(origOut);
        System.setErr(origErr);
        los = null;
    }

    /**
     * Set logging level for this application. All log messages below this level
     * will be ignored silently. (ERROR > INFO > DEBUG)
     *
     * @param level The log level.
     */
    public static void setLevel(int level) {
        if (level == ERROR || level == INFO || level == DEBUG) {
            Logging.level = level;
        } else {
            errorln("Invalid log level: %d", level);
        }
    }

    /**
     * Called by user to print message as ERROR level.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void error(String format, Object... args) {
        print(String.format(format, args));
    }

    /**
     * Called by user to print message as ERROR level. (followed by '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void errorln(String format, Object... args) {
        print(String.format(format, args) + "\n");
    }

    /**
     * Called by user to print message as INFO level.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void info(String format, Object... args) {
        if (INFO >= level) {
            print(String.format(format, args));
        }
    }

    /**
     * Called by user to print message as INFO level. (followed by '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void infoln(String format, Object... args) {
        if (INFO >= level) {
            print(String.format(format, args) + "\n");
        }
    }

    /**
     * Called by user to print message as DEBUG level.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void debug(String format, Object... args) {
        if (DEBUG >= level) {
            print(String.format(format, args));
        }
    }

    /**
     * Called by user to print message as DEBUG level. (followed by '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string. Refer to {@link java.lang.String#format}.
     */
    public static void debugln(String format, Object... args) {
        if (DEBUG >= level) {
            print(String.format(format, args) + "\n");
        }
    }

    /**
     * Internal implementation to print the message.
     *
     * @param text Log message to be printed.
     */
    private static void print(String text) {
        if (los == null) {
            System.out.println(text);
        } else {
            if (Platform.isFxApplicationThread()) {
                los.print(text);
            } else {
                Platform.runLater(() -> {
                    los.print(text);
                });
            }
        }
    }

    /**
     * The wrapper of {@link java.io.OutputStream}
     *
     * @param <T> The target type of this output stream.
     */
    abstract static private class LogOutputStream<T> extends OutputStream {

        final T target;

        private LogOutputStream(T target) {
            this.target = target;
        }

        abstract void print(String text);
    }
}
