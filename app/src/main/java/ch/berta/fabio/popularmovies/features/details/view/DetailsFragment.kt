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
import ch.berta.fabio.popularmovies.features.details.viewmodels.DetailsViewModel


const val KEY_ARGS = "KEY_ARGS"

class DetailsFragment : BaseFragment<BaseFragment.ActivityListener>(),
                        PosterLoadListener {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(DetailsViewModel::class.java) }
    private val recyclerAdapter by lazy { DetailsRecyclerAdapter(viewModel, this) }
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
        viewModel.state.observe(this, Observer<DetailsState> {
            it?.let { render(it) }
        })
    }

    private fun render(state: DetailsState) {
        recyclerAdapter.swapData(state.details)
        binding.srlDetailsFav.isRefreshing = state.updating
    }

    override fun onPosterLoaded() {
        ActivityCompat.startPostponedEnterTransition(activity)
    }
}