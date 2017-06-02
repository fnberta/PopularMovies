package ch.berta.fabio.popularmovies.features.common.vdos

import android.databinding.Bindable
import android.databinding.Observable

interface LoadingEmptyViewData : Observable {
    @get:Bindable var loading: Boolean
    @get:Bindable var empty: Boolean
}