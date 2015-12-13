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

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Provides commonly used utility methods.
 */
public class Utils {

    private static final double POSTER_ASPECT_RATIO = 0.675;
    private static final long COLLAPSE_EXPAND_ANIM_TIME = 300;
    private static final int MAX_LINES_EXPANDED = 500;

    private Utils() {
        // class cannot be instantiated
    }

    /**
     * Returns whether the running Android version is lollipop and higher or an older version.
     *
     * @return whether the running Android version is lollipop and higher or an older version
     */
    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Returns a formatted String using the short date format.
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
     * Returns a formatted String using the long date format.
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
     * Returns the width of the screen.
     *
     * @param res the resources
     * @return the width of the screen
     */
    public static int getScreenWidth(Resources res) {
        DisplayMetrics metrics = res.getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * Returns the correct poster height for a given width and number of columns, respecting the
     * default aspect ratio.
     *
     * @param columns     the number of columns displayed
     * @param layoutWidth the width of the whole layout
     * @return the correct poster height in pixels
     */
    public static int calcPosterHeight(int columns, int layoutWidth) {
        int itemWidth = layoutWidth / columns;
        return (int) (itemWidth / POSTER_ASPECT_RATIO);
    }

    /**
     * Expands or collapses a {@link TextView} by increasing or decreasing its maxLines setting.
     *
     * @param textView the {@link TextView} to expand or collapse
     * @param maxLines the number of lines in the collapsed state
     */
    public static void expandOrCollapseTextView(TextView textView, int maxLines) {
        int value;
        if (textView.getMaxLines() == maxLines) {
            value = MAX_LINES_EXPANDED;
        } else {
            value = maxLines;
        }

        ObjectAnimator anim = ObjectAnimator.ofInt(textView, "maxLines", value);
        anim.setDuration(COLLAPSE_EXPAND_ANIM_TIME);
        anim.start();
    }
}
