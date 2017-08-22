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

package ch.berta.fabio.popularmovies.features.details.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.databinding.FragmentMovieDetailsBinding
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.vdos.DetailsViewData
import ch.berta.fabio.popularmovies.features.details.view.viewholders.InfoViewHolder
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModel
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModelOnePane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModelTwoPane
import ch.berta.fabio.popularmovies.features.grid.viewmodel.MoviesState


class DetailsFragment : BaseFragment<BaseFragment.ActivityListener>(),
                        PosterLoadListener {

    private val useTwoPane by lazy { resources.getBoolean(R.bool.use_two_pane_layout) }
    private val viewModel by lazy {
        if (useTwoPane) {
            ViewModelProviders.of(activity).get(GridViewModelTwoPane::class.java) as DetailsViewModel
        } else {
            ViewModelProviders.of(activity).get(DetailsViewModelOnePane::class.java) as DetailsViewModel
        }
    }
    private val viewData = DetailsViewData()
    private val recyclerAdapter by lazy { DetailsRecyclerAdapter(viewModel.videoClicks, this) }
    private lateinit var binding: FragmentMovieDetailsBinding

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        binding.viewData = viewData
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvDetails.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        binding.rvDetails.layoutManager = layoutManager
        val itemDecoration = ReviewsItemDecoration(context, R.layout.row_details_review)
        binding.rvDetails.addItemDecoration(itemDecoration)
        binding.rvDetails.adapter = recyclerAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.srlDetailsFav.setOnRefreshListener { viewModel.updateSwipes.accept(Unit) }
        viewModel.state.observe(this, Observer<MoviesState> {
            if (it is MoviesState.Details) {
                render(it.value)
            }
        })
    }

    private fun render(state: DetailsState) {
        with(state) {
            if (movieDeletedFromFavScreen && !useTwoPane) {
                removeSharedElement()
                activity.setResult(RS_DELETED_FROM_FAV)
                ActivityCompat.finishAfterTransition(activity)
                return
            }

            viewData.refreshEnabled = updateEnabled
            viewData.refreshing = updating
            recyclerAdapter.swapData(details)

            if (selectedVideoUrl != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedVideoUrl))
                startActivity(intent)
                viewModel.transientClears.accept(Unit)
            }

            if (message != null) {
                Snackbar.make(binding.srlDetailsFav, message, Snackbar.LENGTH_LONG).show()
                viewModel.transientClears.accept(Unit)
            }
        }
    }

    private fun removeSharedElement() {
        // info row will always be the first position in one pane mode, hence 0
        val infoRow = binding.rvDetails.findViewHolderForAdapterPosition(0) as InfoViewHolder
        infoRow.binding.viewData.transitionEnabled = false
        infoRow.binding.executePendingBindings()
    }

    override fun onPosterLoaded() {
        ActivityCompat.startPostponedEnterTransition(activity)
    }
}