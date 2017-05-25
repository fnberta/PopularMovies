package ch.berta.fabio.popularmovies.features.details.view.viewholders

import android.support.v7.widget.RecyclerView
import ch.berta.fabio.popularmovies.databinding.RowDetailsTwoPaneHeaderBinding
import ch.berta.fabio.popularmovies.features.base.BaseBindingViewHolder

/**
 * Provides a [RecyclerView] row that displays the title and backdrop image of a movie.
 * Only used in two pane setups, e.g. on tables.
 *
 */
class TwoPaneHeaderViewHolder(
        binding: RowDetailsTwoPaneHeaderBinding
) : BaseBindingViewHolder<RowDetailsTwoPaneHeaderBinding>(binding)

