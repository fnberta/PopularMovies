package ch.berta.fabio.popularmovies.features.details.vdo.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R

data class DetailsVideoRowViewData(
        @get:Bindable val key: String,
        @get:Bindable val name: String,
        @get:Bindable val site: String,
        @get:Bindable val size: Int,
        override val viewType: Int = R.layout.row_details_video
) : BaseObservable(), DetailsRowViewData