/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.floens.chan.utils;

import android.util.Log;

import org.floens.chan.ChanBuild;

public class Logger {
    private static final String TAG = "Clover";
    private static final String TAG_SPACER = " | ";

    public static boolean debugEnabled() {
        return ChanBuild.DEVELOPER_MODE;
    }

    public static void v(String tag, String message) {
        if (debugEnabled()) {
            Log.v(TAG + TAG_SPACER + tag, message);
        }
    }

    public static void v(String tag, String message, Throwable throwable) {
        if (debugEnabled()) {
            Log.v(TAG + TAG_SPACER + tag, message, throwable);
        }
    }

    public static void v(String tag, String format, Object... args) {
        if (debugEnabled()) {
            Log.v(TAG + TAG_SPACER + tag, String.format(format, args));
        }
    }

    public static void d(String tag, String message) {
        if (debugEnabled()) {
            Log.d(TAG + TAG_SPACER + tag, message);
        }
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (debugEnabled()) {
            Log.d(TAG + TAG_SPACER + tag, message, throwable);
        }
    }

    public static void d(String tag, String format, Object... args) {
        if (debugEnabled()) {
            Log.d(TAG + TAG_SPACER + tag, String.format(format, args));
        }
    }

    public static void i(String tag, String message) {
        Log.i(TAG + TAG_SPACER + tag, message);
    }

    public static void i(String tag, String message, Throwable throwable) {
        Log.i(TAG + TAG_SPACER + tag, message, throwable);
    }

    public static void i(String tag, String format, Object... args) {
        Log.i(TAG + TAG_SPACER + tag, String.format(format, args));
    }

    public static void w(String tag, String message) {
        Log.w(TAG + TAG_SPACER + tag, message);
    }

    public static void w(String tag, String message, Throwable throwable) {
        Log.w(TAG + TAG_SPACER + tag, message, throwable);
    }

    public static void w(String tag, String format, Object... args) {
        Log.w(TAG + TAG_SPACER + tag, String.format(format, args));
    }

    public static void e(String tag, String message) {
        Log.e(TAG + TAG_SPACER + tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + TAG_SPACER + tag, message, throwable);
    }

    public static void e(String tag, String format, Object... args) {
        Log.e(TAG + TAG_SPACER + tag, String.format(format, args));
    }

    public static void wtf(String tag, String message) {
        Log.wtf(TAG + TAG_SPACER + tag, message);
    }

    public static void wtf(String tag, String message, Throwable throwable) {
        Log.wtf(TAG + TAG_SPACER + tag, message, throwable);
    }

    public static void wtf(String tag, String format, Object... args) {
        Log.wtf(TAG + TAG_SPACER + tag, String.format(format, args));
    }

    public static void test(String message) {
        if (debugEnabled()) {
            Log.i(TAG + TAG_SPACER + "test", message);
        }
    }

    public static void test(String message, Throwable throwable) {
        if (debugEnabled()) {
            Log.i(TAG + TAG_SPACER + "test", message, throwable);
        }
    }

    public static void test(String format, Object... args) {
        if (debugEnabled()) {
            Log.i(TAG + TAG_SPACER + "test", String.format(format, args));
        }
    }
}
