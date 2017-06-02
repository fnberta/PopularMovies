package ch.berta.fabio.popularmovies.features.details.vdos

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.BR

class DetailsViewData : BaseObservable() {
    @get:Bindable var refreshing: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshing)
        }

    @get:Bindable
    var refreshEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshEnabled)
        }
}