package ch.berta.fabio.popularmovies.features.grid.vdos

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.BR
import ch.berta.fabio.popularmovies.features.common.vdos.LoadingEmptyViewData

class GridViewData : BaseObservable(), LoadingEmptyViewData {
    @get:Bindable
    override var loading: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.loading)
        }

    @get:Bindable
    override var empty: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.empty)
        }

    @get:Bindable
    var refreshEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshEnabled)
        }

    @get:Bindable
    var refreshing: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshing)
        }

    @get:Bindable
    var loadingMore: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.loadingMore)
        }
}