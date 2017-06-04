/*
 * Copyright (c) 2017 Fabio Berta
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

package ch.berta.fabio.popularmovies

import android.content.res.Resources
import android.os.Build

private const val POSTER_ASPECT_RATIO = 0.675

/**
 * Returns the correct poster height for a given width and number of columns, respecting the
 * default aspect ratio.

 * @param res resources
 * @param useTwoPane phone or table mode
 * @return the correct poster height in pixels
 */
fun calcPosterHeight(
        res: Resources,
        useTwoPane: Boolean = res.getBoolean(R.bool.use_two_pane_layout),
        spanCount: Int = res.getInteger(R.integer.span_count)
): Int {
    val itemWidth = getLayoutWidth(res, useTwoPane) / spanCount
    return (itemWidth / POSTER_ASPECT_RATIO).toInt()
}

private fun getLayoutWidth(res: Resources, useTwoPane: Boolean): Int = getScreenWidth(res).let {
    if (useTwoPane) it / 100 * res.getInteger(R.integer.two_pane_list_width_percentage)
    else it
}

private fun getScreenWidth(res: Resources): Int = res.displayMetrics.widthPixels
