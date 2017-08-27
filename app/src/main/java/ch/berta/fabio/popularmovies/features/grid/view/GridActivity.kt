/*
 * Copyright (c) 2017 Fabio Berta
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
import ch.berta.fabio.popularmovies.databinding.ActivityMovieGridBinding
import ch.berta.fabio.popularmovies.features.base.ActivityResult
import ch.berta.fabio.popularmovies.features.base.BaseActivity
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.view.DetailsFragment
import ch.berta.fabio.popularmovies.features.grid.Sort
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.di.DaggerGridComponent
import ch.berta.fabio.popularmovies.features.grid.di.modules.GridViewModelFactoryModule
import ch.berta.fabio.popularmovies.features.grid.makeSortOptions
import ch.berta.fabio.popularmovies.features.grid.vdos.GridHeaderViewData
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelOnePane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelTwoPane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.MoviesState

/**
 * Provides the main entry point to the app.
 */
class GridActivity : BaseActivity(), BaseFragment.ActivityListener {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMovieGridBinding>(this, R.layout.activity_movie_grid)
    }
    private val viewData = GridHeaderViewData()
    private val useTwoPane by lazy { resources.getBoolean(R.bool.use_two_pane_layout) }
    private val sortOptions by lazy { makeSortOptions { getString(it) } }
    private val component by lazy {
        DaggerGridComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(this))
                .gridViewModelFactoryModule(GridViewModelFactoryModule(useTwoPane, sortOptions))
                .build()
    }
    private val viewModel by lazy {
        val factory = component.gridViewModelFactory
        if (useTwoPane) {
            ViewModelProviders.of(this, factory).get(GridViewModelTwoPane::class.java) as GridViewModel
        } else {
            ViewModelProviders.of(this, factory).get(GridViewModelOnePane::class.java) as GridViewModel
        }
    }
    private val spinnerAdapter by lazy {
        ArrayAdapter<Sort>(this, R.layout.spinner_item_toolbar, sortOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewData = viewData
        initViewModel()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        setupSortSpinner()
        if (useTwoPane) {
            setupFab()
        }

        if (savedInstanceState == null) {
            addFragments()
        }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer<MoviesState> {
            when (it) {
                is MoviesState.Grid -> renderGrid(it.value)
                is MoviesState.Details -> renderDetails(it.value)
            }
        })
    }

    private fun renderGrid(state: GridState) {
        binding.spGridSort.setSelection(sortOptions.indexOf(state.sort))
    }

    private fun renderDetails(state: DetailsState) {
        viewData.movieSelected = true
        viewData.favoured = state.favoured
    }

    private fun setupSortSpinner() {
        binding.spGridSort.adapter = spinnerAdapter
        binding.spGridSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    viewModel.sortSelections.accept(position)
        }
    }

    private fun setupFab() {
        binding.gridContent.fabGridFavorite.setOnClickListener {
            (viewModel as GridViewModelTwoPane).favClicks.accept(Unit)
        }
    }

    private fun addFragments() {
        supportFragmentManager.beginTransaction()
                .apply {
                    val gridFragment = GridFragment()
                    add(R.id.container_main, gridFragment, gridFragment.javaClass.canonicalName)
                    if (useTwoPane) {
                        val detailsFragment = DetailsFragment()
                        add(R.id.container_details, detailsFragment, detailsFragment.javaClass.canonicalName)
                    }
                }
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.activityResults.accept(ActivityResult(requestCode, resultCode, data))
    }
}
