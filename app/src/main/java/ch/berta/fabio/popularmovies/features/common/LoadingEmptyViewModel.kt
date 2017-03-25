package ch.berta.fabio.popularmovies.features.common

import android.databinding.Bindable
import android.databinding.Observable

interface LoadingEmptyViewModel : Observable {
    @get:Bindable var loading: Boolean
    @get:Bindable var empty: Boolean
}