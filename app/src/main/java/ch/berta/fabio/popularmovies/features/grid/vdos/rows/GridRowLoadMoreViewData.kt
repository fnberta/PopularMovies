package ch.berta.fabio.popularmovies.features.grid.vdos.rows

import ch.berta.fabio.popularmovies.R

data class GridRowLoadMoreViewData(
        override val viewType: Int = R.layout.row_progress
) : GridRowViewData