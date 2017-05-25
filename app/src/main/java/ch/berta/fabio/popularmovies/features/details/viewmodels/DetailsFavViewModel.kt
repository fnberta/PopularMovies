package ch.berta.fabio.popularmovies.features.details.viewmodels

import android.databinding.BaseObservable
import android.databinding.Bindable
import ch.berta.fabio.popularmovies.BR

class DetailsFavViewModel : BaseObservable() {
    @get:Bindable var refreshing: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshing)
        }
}