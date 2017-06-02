package ch.berta.fabio.popularmovies.features.details.view.viewholders

import android.support.annotation.IntegerRes
import android.widget.TextView
import ch.berta.fabio.popularmovies.databinding.RowDetailsInfoBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder
import ch.berta.fabio.popularmovies.expandOrCollapse

/**
 * Provides a [RecyclerView] row that displays basic information about a movie.
 *
 */
class InfoViewHolder(
        binding: RowDetailsInfoBinding,
        @IntegerRes maxLinesCollapsed: Int
) : BaseBindingViewHolder<RowDetailsInfoBinding>(binding) {

    init {
        binding.tvDetailsPlot.setOnClickListener { (it as TextView).expandOrCollapse(maxLinesCollapsed) }
    }
}