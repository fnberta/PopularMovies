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

package ch.berta.fabio.popularmovies.presentation.grid;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Draws the padding around the movie grid items.
 * <p>
 * Adjusts the padding depending on the position of an item.
 * </p>
 */
public class PosterGridItemDecoration extends RecyclerView.ItemDecoration {

    private final int itemPadding;

    public PosterGridItemDecoration(int itemPadding) {
        super();

        this.itemPadding = itemPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        int spanCount = getSpanCount(parent);
        int column = position % spanCount;

        outRect.left = column == 0 ? itemPadding : itemPadding / 2;
        outRect.right = column + 1 == spanCount ? itemPadding : itemPadding / 2;
        if (position < spanCount) {
            outRect.top = itemPadding;
        }
        outRect.bottom = itemPadding;
    }


    private int getSpanCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof GridLayoutManager)) {
            throw new RuntimeException("Only supports GridLayoutManager!");
        }

        return ((GridLayoutManager) layoutManager).getSpanCount();
    }
}
