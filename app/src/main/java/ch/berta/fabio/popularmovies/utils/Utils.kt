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

package ch.berta.fabio.popularmovies.utils

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.os.Build
import android.widget.TextView
import ch.berta.fabio.popularmovies.R
import java.text.DateFormat
import java.util.*

private const val POSTER_ASPECT_RATIO = 0.675
private const val COLLAPSE_EXPAND_ANIM_TIME: Long = 300
private const val MAX_LINES_EXPANDED = 500

/**
 * Returns whether the running Android version is lollipop and higher or an older version.
 *
 * @return whether the running Android version is lollipop and higher or an older version
 */
fun isRunningLollipopAndHigher(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}

/**
 * Returns a formatted String using the short date format.
 *
 * @param date the date to be formatted
 * @return a string with the formatted date
 */
fun formatDateShort(date: Date): String {
    val dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    return dateFormatter.format(date)
}

/**
 * Returns a formatted String using the long date format.

 * @param date the date to be formatted
 * @return a string with the formatted date
 */
fun formatDateLong(date: Date): String {
    val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
    return dateFormatter.format(date)
}

/**
 * Returns the correct poster height for a given width and number of columns, respecting the
 * default aspect ratio.

 * @param res resources
 * @param useTwoPane phone or table mode
 * @return the correct poster height in pixels
 */
fun calcPosterHeight(res: Resources,
                     useTwoPane: Boolean,
                     spanCount: Int = res.getInteger(R.integer.span_count)
): Int {
    val itemWidth = getLayoutWidth(res, useTwoPane) / spanCount
    return (itemWidth / POSTER_ASPECT_RATIO).toInt()
}

private fun getLayoutWidth(res: Resources, useTwoPane: Boolean): Int {
    val screenWidth = getScreenWidth(res)
    return if (useTwoPane) {
        screenWidth / 100 * res.getInteger(R.integer.two_pane_list_width_percentage)
    } else screenWidth
}

private fun getScreenWidth(res: Resources): Int {
    return res.displayMetrics.widthPixels
}

/**
 * Expands or collapses a [TextView] by increasing or decreasing its maxLines setting.

 * @param textView the [TextView] to expand or collapse
 * @param maxLines the number of lines in the collapsed state
 */
fun expandOrCollapseTextView(textView: TextView, maxLines: Int) {
    val value = if (textView.maxLines == maxLines) MAX_LINES_EXPANDED else maxLines
    val anim = ObjectAnimator.ofInt(textView, "maxLines", value)
    anim.duration = COLLAPSE_EXPAND_ANIM_TIME
    anim.start()
}
