package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable

data class DetailsVideoRowViewModel(
        @get:Bindable val key: Int,
        @get:Bindable val name: String,
        @get:Bindable val site: String,
        @get:Bindable val size: Int
) : BaseObservable()