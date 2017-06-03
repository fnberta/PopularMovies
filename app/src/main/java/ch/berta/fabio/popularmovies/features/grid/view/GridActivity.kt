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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ch.berta.fabio.popularmovies.PopularMovies
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.bindTo
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.SharedPrefs
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding
import ch.berta.fabio.popularmovies.di.ApplicationComponent
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.SortOption.*
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelFactory
import ch.berta.fabio.popularmovies.navigateTo
import javax.inject.Inject

/**
 * Provides the main entry point to the app.
 */
class GridActivity : BaseActivity(), BaseFragment.ActivityListener {

    @Inject
    lateinit var sharedPrefs: SharedPrefs
    @Inject
    lateinit var movieStorage: MovieStorage
    private val component: ApplicationComponent by lazy { PopularMovies.getAppComponent(this) }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMovieGridBinding>(this, R.layout.activity_movie_grid)
    }
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
    private val viewModel by lazy {
        val factory = GridViewModelFactory(sharedPrefs, movieStorage, sortOptions)
        ViewModelProviders.of(this, factory).get(GridViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
        initViewModel()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        setupSortSpinner()

        if (savedInstanceState == null) {
            addFragment()
        }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer<GridState> {
            it?.let { render(it) }
        })
        viewModel.navigation
                .bindTo(lifecycle)
                .subscribe { navigateTo(this, it) }
    }

    private fun render(state: GridState) {
        binding.spGridSort.setSelection(sortOptions.indexOf(state.sort))
    }

    private fun setupSortSpinner() {
        binding.spGridSort.adapter = spinnerAdapter
        binding.spGridSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    viewModel.uiEvents.sortSelections.accept(position)
        }
    }

    private fun addFragment() {
        val fragment = GridFragment()
        supportFragmentManager.beginTransaction()
                .add(R.id.container_main, fragment, fragment.javaClass.canonicalName)
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.activityResults.accept(ActivityResult(requestCode, resultCode, data))
    }
}
