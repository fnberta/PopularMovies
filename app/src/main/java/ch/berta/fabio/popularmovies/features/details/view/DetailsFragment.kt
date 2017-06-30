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
import android.os.Bundle
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
import ch.berta.fabio.popularmovies.features.details.viewmodel.DetailsViewModel


const val KEY_ARGS = "KEY_ARGS"

class DetailsFragment : BaseFragment<BaseFragment.ActivityListener>(),
                        PosterLoadListener {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(DetailsViewModel::class.java) }
    private val viewData = DetailsViewData()
    private val recyclerAdapter by lazy { DetailsRecyclerAdapter(viewModel.uiEvents.videoClicks, this) }
    private lateinit var binding: FragmentMovieDetailsBinding

    companion object {
        fun newInstance(detailsArgs: DetailsArgs): DetailsFragment {
            val args = Bundle().apply { putParcelable(KEY_ARGS, detailsArgs) }
            return DetailsFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

        binding.srlDetailsFav.setOnRefreshListener { viewModel.uiEvents.updateSwipes.accept(Unit) }
        viewData.refreshEnabled = arguments.getParcelable<DetailsArgs>(KEY_ARGS).fromFavList
        viewModel.state.observe(this, Observer<DetailsState> {
            it?.let { render(it) }
        })
    }

    private fun render(state: DetailsState) {
        recyclerAdapter.swapData(state.details)
        viewData.refreshing = state.updating
    }

    override fun onPosterLoaded() {
        ActivityCompat.startPostponedEnterTransition(activity)
    }
}