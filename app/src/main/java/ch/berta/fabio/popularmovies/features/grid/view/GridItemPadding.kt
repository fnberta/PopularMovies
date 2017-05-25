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

package ch.berta.fabio.popularmovies.features.grid.view

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Draws the padding around the movie grid items.
 * Adjusts the padding depending on the position of an item.
 */
class GridItemPadding(private val itemPadding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State?
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val spanCount = getSpanCount(parent)
        val column = position % spanCount

        outRect.left = if (column == 0) itemPadding else itemPadding / 2
        outRect.right = if (column + 1 == spanCount) itemPadding else itemPadding / 2
        if (position < spanCount) {
            outRect.top = itemPadding
        }
        outRect.bottom = itemPadding
    }

    private fun getSpanCount(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager as? GridLayoutManager
                ?: throw RuntimeException("Only supports GridLayoutManager!")

        return layoutManager.spanCount
    }
}
