package ch.berta.fabio.popularmovies.features.details.viewmodels.rows

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.R

data class DetailsReviewRowViewModel(
        @get:Bindable val author: String,
        @get:Bindable val content: String,
        override val viewType: Int = R.layout.row_details_review
) : BaseObservable(), DetailsRowViewModel