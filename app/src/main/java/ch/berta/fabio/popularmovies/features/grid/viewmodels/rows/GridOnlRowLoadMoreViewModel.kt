package ch.berta.fabio.popularmovies.features.grid.viewmodels.rows

import ch.berta.fabio.popularmovies.R

data class GridOnlRowLoadMoreViewModel(
        override val viewType: Int = R.layout.row_progress
) : GridOnlRowViewModel