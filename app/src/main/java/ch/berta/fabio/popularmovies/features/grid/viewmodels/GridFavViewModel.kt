package ch.berta.fabio.popularmovies.features.grid.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.BR
import ch.berta.fabio.popularmovies.features.common.LoadingEmptyViewModel
import javax.inject.Inject

class GridFavViewModel @Inject constructor() : BaseObservable(), LoadingEmptyViewModel {
    @get:Bindable override var loading: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.loading)
        }

    @get:Bindable override var empty: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.empty)
        }
}