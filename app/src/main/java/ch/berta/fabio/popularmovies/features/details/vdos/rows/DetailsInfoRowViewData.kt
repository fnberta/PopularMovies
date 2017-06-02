package ch.berta.fabio.popularmovies.features.details.vdos.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R

data class DetailsInfoRowViewData(
        @get:Bindable val posterPath: String?,
        @get:Bindable val date: String?,
        @get:Bindable val rating: Double?,
        @get:Bindable val plot: String?,
        @get:Bindable val transitionEnabled: Boolean = true,
        override val viewType: Int = R.layout.row_details_info
) : BaseObservable(), DetailsRowViewData