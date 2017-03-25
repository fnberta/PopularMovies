package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable

data class DetailsFavViewModel(
        @get:Bindable val refreshing: Boolean
) : BaseObservable()