package ch.berta.fabio.popularmovies.features.details.vdo.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R

data class DetailsTwoPaneHeaderViewData(
        @get:Bindable val title: String,
        @get:Bindable val backdropPath: String,
        override val viewType: Int = R.layout.row_details_two_pane_header
) : BaseObservable(), DetailsRowViewData