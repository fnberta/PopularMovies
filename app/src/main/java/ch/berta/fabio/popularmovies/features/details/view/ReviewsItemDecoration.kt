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

package ch.berta.fabio.popularmovies.features.details.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View

class ReviewsItemDecoration(
        context: Context,
        val dividerViewType: Int
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable
    private val bounds = Rect()

    init {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        divider = a.getDrawable(0)
        a.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }

        drawDivider(c, parent)
    }

    @SuppressLint("NewApi")
    private fun drawDivider(canvas: Canvas, parent: RecyclerView) {
        canvas.save()

        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        (0..parent.childCount - 2)
                .map { parent.getChildAt(it) }
                .filter { parent.getChildViewHolder(it).itemViewType == dividerViewType }
                .forEach {
                    parent.getDecoratedBoundsWithMargins(it, bounds)
                    val bottom = bounds.bottom + Math.round(ViewCompat.getTranslationY(it))
                    val top = bottom - divider.intrinsicHeight
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(canvas)
                }

        canvas.restore()
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State?
    ) = outRect.set(0, 0, 0, divider.intrinsicHeight)
}