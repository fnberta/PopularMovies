package ch.berta.fabio.popularmovies.features.details.view

import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.PopularMovies
import ch.berta.fabio.popularmovies.databinding.FragmentMovieDetailsFavBinding
import ch.berta.fabio.popularmovies.extensions.bindToStartStop
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsSinks
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.di.DaggerDetailsFavComponent
import ch.berta.fabio.popularmovies.features.details.di.DetailsFavLoaderModule
import ch.berta.fabio.popularmovies.features.details.viewmodels.DetailsFavViewModel

const val KEY_ROW_ID = "KEY_ROW_ID"

class DetailsFavFragment : BaseFragment<DetailsFavActivityListener>(),
                           LoaderManager.LoaderCallbacks<Cursor>,
                           PosterLoadListener {

    private val viewModel = DetailsFavViewModel()
    private val component by lazy {
        val movieRowId = arguments.getLong(KEY_ROW_ID)
        DaggerDetailsFavComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(activity))
                .detailsFavLoaderModule(DetailsFavLoaderModule(context, movieRowId))
                .build()
    }
    private val recyclerAdapter by lazy {
        DetailsRecyclerAdapter(activityListener.videoClicks, this)
    }
    private lateinit var binding: FragmentMovieDetailsFavBinding

    companion object {
        fun newInstance(movieRowId: Long): DetailsFavFragment {
            val args = Bundle().apply { putLong(KEY_ROW_ID, movieRowId) }
            return DetailsFavFragment().apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMovieDetailsFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvDetails.setHasFixedSize(true)
        binding.rvDetails.layoutManager = LinearLayoutManager(activity)
        binding.rvDetails.adapter = recyclerAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loaderManager.initLoader<Cursor>(0, null, this)
        subscribeToSinks(activityListener.sinks)
    }

    private fun subscribeToSinks(sinks: DetailsSinks) {
        sinks.state
                .bindToStartStop(lifecycleHandler)
                .subscribe { render(it) }
    }

    private fun render(state: DetailsState) {
        recyclerAdapter.swapData(state.details)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        val result = if (data.moveToFirst()) Maybe.Some(data) else Maybe.None
        activityListener.detailsFav.call(result)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> =
            component.detailsFavLoader

    override fun onLoaderReset(loader: Loader<Cursor>?) =
            activityListener.detailsFav.call(Maybe.None)

    override fun onPosterLoaded() {
        ActivityCompat.startPostponedEnterTransition(activity)
    }
}