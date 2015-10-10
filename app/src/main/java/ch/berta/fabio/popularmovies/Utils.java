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
 * Created by fabio on 04.10.15.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    public static String formatDateShort(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.getDefault());

        return dateFormatter.format(date);
    }

    public static String formatDateLong(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG,
                Locale.getDefault());

        return dateFormatter.format(date);
    }

    public static Snackbar getBasicSnackbar(View view, String message) {
        return getBasicSnackbar(view, message, Snackbar.LENGTH_LONG);
    }

    public static Snackbar getBasicSnackbar(View view, String message, int duration) {
        return Snackbar.make(view, message, duration);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }
}
