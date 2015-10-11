/*
 * Copyright (c) 2015 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Provides commonly used utility methods.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    /**
     * Returns a formatted String using the short date format
     *
     * @param date the date to be formatted
     * @return a string with the formatted date
     */
    public static String formatDateShort(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.getDefault());

        return dateFormatter.format(date);
    }

    /**
     * Returns a formatted String using the long date format
     *
     * @param date the date to be formatted
     * @return a string with the formatted date
     */
    public static String formatDateLong(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG,
                Locale.getDefault());

        return dateFormatter.format(date);
    }

    /**
     * Returns a basic snackbar
     *
     * @param view    the view to whose layout the snackbar attaches itself
     * @param message the message to be shown in the snackbar
     * @return a basic snackbar
     */
    public static Snackbar getBasicSnackbar(View view, String message) {
        return getBasicSnackbar(view, message, Snackbar.LENGTH_LONG);
    }

    /**
     * Returns a basic snackbar
     *
     * @param view     the view to whose layout the snackbar attaches itself
     * @param message  the message to be shown in the snackbar
     * @param duration the duration the snackbar should be shown on screen
     * @return a basic snackbar
     */
    public static Snackbar getBasicSnackbar(View view, String message, int duration) {
        return Snackbar.make(view, message, duration);
    }

    /**
     * Returns the width of the screen
     *
     * @param context the {@link Context} to used to get the resources
     * @return the width of the screen
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }
}
