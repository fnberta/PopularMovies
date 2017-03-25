package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable

data class DetailsInfoRowViewModel(
        @get:Bindable val transitionEnabled: Boolean,
        @get:Bindable val posterPath: String,
        @get:Bindable val date: String,
        @get:Bindable val rating: Double,
        @get:Bindable val plot: String
) : BaseObservable()