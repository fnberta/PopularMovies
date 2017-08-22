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
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.calcPosterHeight
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridBinding
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.view.DetailsActivity
import ch.berta.fabio.popularmovies.features.details.view.RQ_DETAILS
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.vdos.GridViewData
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModel
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelOnePane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelTwoPane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.MoviesState
import com.mugen.Mugen
import com.mugen.MugenCallbacks

class GridFragment : BaseFragment<BaseFragment.ActivityListener>() {

    private val useTwoPane by lazy { resources.getBoolean(R.bool.use_two_pane_layout) }
    private val viewModel: GridViewModel by lazy {
        if (useTwoPane) {
            ViewModelProviders.of(activity).get(GridViewModelTwoPane::class.java) as GridViewModel
        } else {
            ViewModelProviders.of(activity).get(GridViewModelOnePane::class.java) as GridViewModel
        }
    }
    private val viewData = GridViewData()
    private val recyclerAdapter: GridRecyclerAdapter by lazy {
        GridRecyclerAdapter(calcPosterHeight(resources), viewModel.movieSelections)
    }
    lateinit private var binding: FragmentMovieGridBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieGridBinding.inflate(inflater, container, false)
        binding.viewData = viewData
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.srlGrid.setOnRefreshListener { viewModel.refreshSwipes.accept(Unit) }
    }

    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(ch.berta.fabio.popularmovies.R.integer.span_count)
        val layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (recyclerAdapter.getItemViewType(position) == R.layout.row_progress) spanCount
                    else 1
        }
        binding.rvGrid.layoutManager = layoutManager
        binding.rvGrid.setHasFixedSize(true)
        val itemPadding = resources.getDimensionPixelSize(R.dimen.grid_padding)
        binding.rvGrid.addItemDecoration(GridItemPadding(itemPadding))
        binding.rvGrid.adapter = recyclerAdapter
        Mugen.with(binding.rvGrid, object : MugenCallbacks {
            override fun onLoadMore() {
                if (viewData.refreshEnabled) viewModel.loadMore.accept(Unit)
            }

            override fun isLoading(): Boolean = viewData.loading || viewData.refreshing || viewData.loadingMore

            override fun hasLoadedAllItems(): Boolean = false
        }).start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.state.observe(this, Observer<MoviesState> {
            if (it is MoviesState.Grid) {
                render(it.value)
            }
        })
    }

    private fun render(state: GridState) {
        with(state) {
            if (selectedMovie != null && !useTwoPane) {
                val options = getString(R.string.shared_transition_details_poster).let {
                    ViewCompat.setTransitionName(selectedMovie.posterView, it)
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, selectedMovie.posterView, it)
                }
                DetailsActivity.startWithExtras(activity, selectedMovie, RQ_DETAILS, options.toBundle())
                viewModel.transientClears.accept(Unit)
                return
            }

            viewData.refreshEnabled = sort.option != SortOption.SORT_FAVORITE
            viewData.empty = empty
            viewData.loading = loading
            viewData.refreshing = refreshing
            viewData.loadingMore = loadingMore
            recyclerAdapter.swapData(movies, diffTransition)

            if (message != null) {
                Snackbar.make(binding.srlGrid, message, Snackbar.LENGTH_LONG).show()
                viewModel.transientClears.accept(Unit)
            }
        }
    }
}
