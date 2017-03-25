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

package ch.berta.fabio.popularmovies.features.grid

import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.widget.ArrayAdapter
import ch.berta.fabio.popularmovies.PopularMovies
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding
import ch.berta.fabio.popularmovies.extensions.addInitialData
import ch.berta.fabio.popularmovies.extensions.bindTo
import ch.berta.fabio.popularmovies.extensions.setSelectionIfNotSelected
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.grid.component.MovieGridIntentions
import ch.berta.fabio.popularmovies.features.grid.component.GridViewState
import ch.berta.fabio.popularmovies.features.grid.component.createInitialState
import ch.berta.fabio.popularmovies.features.grid.component.model
import ch.berta.fabio.popularmovies.features.grid.di.DaggerGridComponent
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import ch.berta.fabio.popularmovies.features.grid.di.GridLoaderModule
import ch.berta.fabio.popularmovies.features.grid.GridFavActivityListener
import ch.berta.fabio.popularmovies.features.grid.GridFavFragment
import ch.berta.fabio.popularmovies.features.grid.GridOnlActivityListener
import ch.berta.fabio.popularmovies.features.grid.GridOnlFragment
import ch.berta.fabio.popularmovies.utils.calcPosterHeight
import com.jakewharton.rxbinding.widget.itemSelections
import com.jakewharton.rxrelay.BehaviorRelay
import rx.Observable
import javax.inject.Inject

const val PERSIST_SORT = "PERSIST_SORT"

/**
 * Provides the main entry point to the app.
 */
class GridActivity : BaseActivity(),
                     GridOnlActivityListener,
                     GridFavActivityListener {

    override val moviesOnl: BehaviorRelay<List<Movie>> = BehaviorRelay.create()
    override val moviesFav: BehaviorRelay<Sequence<Map<String, Any?>>> = BehaviorRelay.create()
    override val movieClicks: BehaviorRelay<Int> = BehaviorRelay.create()
    override val loadMore: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val refreshSwipes: BehaviorRelay<Unit> = BehaviorRelay.create()
    override val component: GridComponent by lazy {
        DaggerGridComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(this))
                .gridLoaderModule(GridLoaderModule(this))
                .build()
    }
    override lateinit var state: Observable<GridViewState>
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private val spinnerAdapter by lazy {
        val adapter = ArrayAdapter<Sort>(this, R.layout.spinner_item_toolbar, mutableListOf())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter
    }
    private val useTwoPane by lazy {
        resources.getBoolean(R.bool.use_two_pane_layout)
    }
    lateinit private var binding: ActivityMovieGridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMovieGridBinding>(this,
                R.layout.activity_movie_grid)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        injectDependencies()
        setupSortSpinner()

        val initialState = createInitialState(savedInstanceState)
        state = setupComponent(initialState)
        subscribeToState(state)

        if (savedInstanceState == null) {
            addInitialFragment(initialState.sortOptions.first())
        }
    }

    private fun injectDependencies() {
        component.inject(this)
    }

    private fun setupSortSpinner() {
        binding.spGridSort.adapter = spinnerAdapter
    }

    private fun addInitialFragment(initialSort: Sort) {
        val fragment = GridOnlFragment.newInstance(initialSort.value)
        supportFragmentManager.beginTransaction()
                .add(R.id.container_main, fragment, fragment.javaClass.canonicalName)
                .commit()
    }

    private fun createInitialState(savedInstanceState: Bundle?): GridViewState {
        val savedState = savedInstanceState?.get(
                GridActivity::class.java.canonicalName) as? GridViewState
        return savedState ?: createInitialState(sharedPrefs.getInt(PERSIST_SORT, 0),
                makeSortOptionDisplay { getString(it) })
    }

    private fun setupComponent(initialState: GridViewState): Observable<GridViewState> {
        val intentions = MovieGridIntentions(
                activityResult,
                activityStarted,
                fragmentCommitted,
                moviesOnl,
                moviesFav,
                binding.spGridSort.itemSelections(),
                movieClicks,
                loadMore,
                refreshSwipes
        )

        return model(initialState, intentions, calcPosterHeight(resources, useTwoPane))
    }

    private fun subscribeToState(state: Observable<GridViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
//        state.saveForConfigChange(lifecycleHandler, name).subscribe()
    }

    private fun render(state: GridViewState) {
        renderSortSelection(state)
    }

    private fun renderSortSelection(state: GridViewState) {
        spinnerAdapter.addInitialData(state.sortOptions)
        binding.spGridSort.setSelectionIfNotSelected(state.sortSelectedPos)
        sharedPrefs.edit().putInt(PERSIST_SORT, state.sortSelectedPos).apply()

        if (state.showOnlGrid) {
            val sortValue = state.sortOptions[state.sortSelectedPos].value
            replaceFragment(GridOnlFragment.newInstance(sortValue))
        }
        if (state.showFavGrid) {
            replaceFragment(GridFavFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val canonicalName = fragment.javaClass.canonicalName
        supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, fragment, canonicalName)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        fragmentCommitted.call(canonicalName)
    }

    private fun startDetailsScreen(state: GridViewState) {
//        if (!useTwoPane) {
//            val intent = Intent(this, MovieDetailsActivity::class.java)
//            intent.putExtra(INTENT_MOVIE_SELECTED, movie)
//            startDetailsActivity(intent, posterSharedElement)
//        } else if (!viewModel.isMovieSelected(movie)) {
//            showDetailsOnlFragment(movie)
//        }
    }

    private fun showDetailsOnlFragment(movie: Movie) {
//        val fragment = MovieDetailsOnlFragment.newInstance(movie)
//        replaceDetailsFragment(fragment)
    }

    private fun hideDetailsFragment() {
//        val detailsFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TWO_PANE_DETAILS)
//        if (detailsFragment != null) {
//            supportFragmentManager.beginTransaction()
//                    .remove(detailsFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
//                    .commit()
//        }
    }
}
