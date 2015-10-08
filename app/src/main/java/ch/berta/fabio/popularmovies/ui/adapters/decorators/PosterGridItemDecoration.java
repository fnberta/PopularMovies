package ch.berta.fabio.popularmovies.ui.adapters.decorators;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by fabio on 04.10.15.
 */
public class PosterGridItemDecoration extends RecyclerView.ItemDecoration {

    private int mItemPadding;

    public PosterGridItemDecoration(int itemPadding) {
        super();

        mItemPadding = itemPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        int spanCount = getSpanCount(parent);
        int column = position % spanCount;

        outRect.left = column == 0 ? mItemPadding : mItemPadding / 2;
        outRect.right = column + 1 == spanCount ? mItemPadding : mItemPadding / 2;
        if (position < spanCount) {
            outRect.top = mItemPadding;
        }
        outRect.bottom = mItemPadding;
    }


    private int getSpanCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof GridLayoutManager)) {
            throw new RuntimeException("Only supports GridLayoutManager!");
        }

        return ((GridLayoutManager) layoutManager).getSpanCount();
    }
}
