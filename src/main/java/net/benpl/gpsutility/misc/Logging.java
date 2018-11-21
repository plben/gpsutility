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
    private static LogOutputStream los = null;

    /**
     * Redirect internal log and all system console output to target object.
     *
     * @param target The target object log will be redirected to.
     */
    public static void redirectTo(Object target) {
        if (target instanceof TextArea) {
            // Save original console print stream
            origOut = System.out;

            // Create log output stream upon this target object
            los = new LogOutputStream<>((TextArea) target) {
                /**
                 * Method to print design intended message.
                 *
                 * @param text Design intended message.
                 */
                @Override
                public void print(String text) {
                    printText(text);
                }

                /**
                 * Method to redirect message from {@link System#out}.
                 *
                 * @param arg0 Message from {@link System#out}.
                 */
                @Override
                public void write(int arg0) {
                    printText(Character.toString((char) arg0));
                }

                /**
                 * Method to print message in log window.
                 *
                 * @param text Message to be printed in log window.
                 */
                private void printText(String text) {
                    // Append message to log window
                    this.target.appendText(text);
                    // Scroll log window to bottom
                    this.target.setScrollTop(Double.MAX_VALUE);
                }
            };

            // Create print stream upon this new log output stream
            PrintStream printStream = new PrintStream(los);

            // Redirect system console to this new print stream
            System.setOut(printStream);
        } else {
            System.out.println("Redirect log to " + target.getClass().getCanonicalName() + " is not supported!");
        }
    }

    /**
     * Restore system console to original print stream.
     */
    public static void restore() {
        if (origOut == null) {
            return;
        }

        System.setOut(origOut);
        los = null;
    }

    /**
     * Set logging level for this application. All log messages below this level will be ignored silently. (ERROR > INFO > DEBUG)
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
     * Method to print ERROR message.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void error(String format, Object... args) {
        print(String.format(format, args));
    }

    /**
     * Method to print ERROR message. (appended with '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void errorln(String format, Object... args) {
        print(String.format(format, args) + "\n");
    }

    /**
     * Method to print INFO message.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void info(String format, Object... args) {
        if (INFO >= level) {
            print(String.format(format, args));
        }
    }

    /**
     * Method to print INFO message. (appended with '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void infoln(String format, Object... args) {
        if (INFO >= level) {
            print(String.format(format, args) + "\n");
        }
    }

    /**
     * Method to print DEBUG message.
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void debug(String format, Object... args) {
        if (DEBUG >= level) {
            print(String.format(format, args));
        }
    }

    /**
     * Method to print DEBUG message. (appended with '\n')
     *
     * @param format A format string. Refer to {@link java.lang.String#format}.
     * @param args   Arguments referenced by the format specifiers in the format string. Refer to {@link java.lang.String#format}.
     */
    public static void debugln(String format, Object... args) {
        if (DEBUG >= level) {
            print(String.format(format, args) + "\n");
        }
    }

    /**
     * Method to print message in log window.
     *
     * @param text The message to be printed.
     */
    private static void print(String text) {
        if (los == null) {
            System.out.print(text);
        } else {
            if (Platform.isFxApplicationThread()) {
                los.print(text);
            } else {
                Platform.runLater(() -> los.print(text));
            }
        }
    }

    /**
     * The wrapper of {@link java.io.OutputStream}
     *
     * @param <T> Class type of target object.
     */
    abstract static private class LogOutputStream<T> extends OutputStream {

        final T target;

        private LogOutputStream(T target) {
            this.target = target;
        }

        abstract void print(String text);
    }
}
