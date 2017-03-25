package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable

data class DetailsReviewRowViewModel(
        @get:Bindable val author: String,
        @get:Bindable val content: String,
        @get:Bindable val lastPosition: Boolean
) : BaseObservable()