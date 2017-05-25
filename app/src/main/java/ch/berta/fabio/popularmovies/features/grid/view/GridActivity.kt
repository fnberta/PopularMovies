/*
 * Copyright (c) 2015 Fabio Berta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.berta.fabio.popularmovies.features.grid.view

import android.content.SharedPreferences
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.ArrayAdapter
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.PopularMovies
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding
import ch.berta.fabio.popularmovies.effects.*
import ch.berta.fabio.popularmovies.extensions.bindToDestroy
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption.*
import ch.berta.fabio.popularmovies.features.grid.component.*
import ch.berta.fabio.popularmovies.features.grid.di.DaggerGridComponent
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import ch.berta.fabio.popularmovies.features.grid.di.GridLoaderModule
import ch.berta.fabio.popularmovies.utils.calcPosterHeight
import com.jakewharton.rxbinding.widget.itemSelections
import com.jakewharton.rxrelay.BehaviorRelay
import javax.inject.Inject

/**
 * Provides the main entry point to the app.
 */
class GridActivity : BaseActivity(),
                     GridOnlActivityListener,
                     GridFavActivityListener {

    override val moviesOnl: BehaviorRelay<List<Movie>> = BehaviorRelay.create()
    override val moviesFav: BehaviorRelay<Maybe<Cursor>> = BehaviorRelay.create()
    override val movieClicks: BehaviorRelay<SelectedMovie> = BehaviorRelay.create()
    override val loadMore: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val refreshSwipes: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val component: GridComponent by lazy {
        DaggerGridComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(this))
                .gridLoaderModule(GridLoaderModule(this))
                .build()
    }
    override lateinit var sinks: GridSinks
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private val sortOptions by lazy {
        listOf(
                Sort(SORT_POPULARITY, "popularity.desc", getString(R.string.sort_popularity)),
                Sort(SORT_RATING, "vote_average.desc", getString(R.string.sort_rating)),
                Sort(SORT_RELEASE_DATE, "release_date.desc", getString(R.string.sort_release_date)),
                Sort(SORT_FAVORITE, "favorite", getString(R.string.sort_favorite))
        )
    }
    private val spinnerAdapter by lazy {
        ArrayAdapter<Sort>(this, R.layout.spinner_item_toolbar, sortOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
    private val useTwoPane by lazy { resources.getBoolean(R.bool.use_two_pane_layout) }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMovieGridBinding>(this, R.layout.activity_movie_grid)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        val initialState = savedInstanceState?.get(GridActivity::class.java.canonicalName)
                as? GridState ?: getNewInitialState()

        binding.spGridSort.adapter = spinnerAdapter
        binding.spGridSort.setSelection(sortOptions.indexOf(initialState.sort))

        sinks = setupComponent(initialState)
        subscribeToSinks(sinks)

        if (savedInstanceState == null) {
            addInitialFragment(initialState.sort)
        }
    }

    private fun getNewInitialState(): GridState {
        val sortPos = readFrom(sharedPrefs, SharedPrefsReadTarget(KEY_SORT_POS, 0))
        return createInitialState(sortOptions[sortPos])
    }

    private fun addInitialFragment(initialSort: Sort) {
        val fragment: Fragment = when (initialSort.option) {
            SORT_FAVORITE -> GridFavFragment()
            else -> GridOnlFragment.newInstance(initialSort.value)
        }
        supportFragmentManager.beginTransaction()
                .add(R.id.container_main, fragment, fragment.javaClass.canonicalName)
                .commit()
    }

    private fun setupComponent(initialState: GridState): GridSinks {
        val viewEvents = GridViewEvents(
                binding.spGridSort.itemSelections(),
                movieClicks,
                loadMore,
                refreshSwipes
        )
        val dataLoadEvents = GridDataLoadEvents(
                moviesOnl,
                moviesFav
        )

        val sources = GridSources(viewEvents, frameworkEvents, dataLoadEvents)
        return main(initialState, sources, sortOptions, calcPosterHeight(resources, useTwoPane))
    }

    private fun subscribeToSinks(sinks: GridSinks) {
        sinks.state
                .bindToDestroy(lifecycleHandler)
                .subscribe { render(it) }
//        state.saveForConfigChange(lifecycleHandler, name).subscribe()
        sinks.navigation
                .bindToDestroy(lifecycleHandler)
                .subscribe { navigateTo(this, it) }
        sinks.sharedPrefs
                .bindToDestroy(lifecycleHandler)
                .subscribe { persistTo(sharedPrefs, it) }
    }

    private fun render(state: GridState) {
        binding.spGridSort.setSelection(sortOptions.indexOf(state.sort))
    }
}
