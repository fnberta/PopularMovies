package ch.berta.fabio.popularmovies.features.grid.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.grid.Sort

class GridViewModelFactory(
        val sharedPrefs: SharedPrefs,
        val movieStorage: MovieStorage,
        val sortOptions: List<Sort>
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GridViewModel(sharedPrefs, movieStorage, sortOptions) as T
    }
}