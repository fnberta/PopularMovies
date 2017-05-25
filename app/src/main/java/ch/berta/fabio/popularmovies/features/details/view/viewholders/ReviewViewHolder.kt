package ch.berta.fabio.popularmovies.features.details.view.viewholders

import android.support.annotation.IntegerRes
import android.widget.TextView
import ch.berta.fabio.popularmovies.databinding.RowDetailsReviewBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder
import ch.berta.fabio.popularmovies.utils.expandOrCollapseTextView

/**
 * Provides a [RecyclerView] row that displays reviews about a movie.
 *
 */
class ReviewViewHolder(
        binding: RowDetailsReviewBinding,
        @IntegerRes maxLines: Int
) : BaseBindingViewHolder<RowDetailsReviewBinding>(binding) {

    init {
        binding.tvDetailsReviewContent.setOnClickListener {
            expandOrCollapseTextView(it as TextView, maxLines)
        }
    }
}