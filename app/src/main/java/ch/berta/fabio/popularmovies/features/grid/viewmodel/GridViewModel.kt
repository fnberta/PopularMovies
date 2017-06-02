package ch.berta.fabio.popularmovies.features.grid.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import ch.berta.fabio.popularmovies.NavigationTarget
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.component.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

class GridViewModel(
        sharedPrefs: SharedPrefs,
        movieStorage: MovieStorage,
        sortOptions: List<Sort>
) : ViewModel() {

    val state: LiveData<GridState>
    val navigation: LiveData<NavigationTarget>

    val viewEvents = GridUiEvents()
    val activityResults: PublishRelay<ActivityResult> = PublishRelay.create()

    init {
        val sources = GridSources(viewEvents, activityResults, sharedPrefs, movieStorage)
        val sinks = main(sources, sortOptions)

        state = LiveDataReactiveStreams.fromPublisher(sinks
                .ofType(GridSink.State::class.java)
                .map { it.state }
                .toFlowable(BackpressureStrategy.LATEST)
        )
        navigation = LiveDataReactiveStreams.fromPublisher(sinks
                .ofType(GridSink.Navigation::class.java)
                .map { it.target }
                .toFlowable(BackpressureStrategy.LATEST)
        )

        initWriteEffectsSubscriptions(sinks)
    }

    private fun initWriteEffectsSubscriptions(sinks: Observable<GridSink>) {
        sinks
                .ofType(GridSink.SharedPrefsWrite::class.java)
                .subscribe()
    }
}