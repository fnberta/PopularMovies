/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.berta.fabio.popularmovies;

import android.content.Context;
import android.content.res.Resources;
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

    public static int getScreenWidth(Resources res) {
        DisplayMetrics metrics = res.getDisplayMetrics();
        return metrics.widthPixels;
    }
}
