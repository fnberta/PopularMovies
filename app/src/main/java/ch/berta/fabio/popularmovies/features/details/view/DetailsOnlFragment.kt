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
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.databinding.FragmentMovieDetailsOnlBinding
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.details.component.DetailsSinks
import ch.berta.fabio.popularmovies.features.details.component.DetailsState
import ch.berta.fabio.popularmovies.features.details.di.DaggerDetailsOnlComponent
import ch.berta.fabio.popularmovies.features.details.di.DetailsOnlLoaderModule
import rx.Observable


const val KEY_DB_ID = "KEY_DB_ID"
const val LOADER_ONL_DETAILS = 2
const val LOADER_ONL_IS_FAV = 3

class DetailsOnlFragment : BaseFragment<DetailsOnlActivityListener>(),
                           PosterLoadListener {

    private val component by lazy {
        val movieDbId = arguments.getInt(KEY_DB_ID)
        DaggerDetailsOnlComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(activity))
                .detailsOnlLoaderModule(DetailsOnlLoaderModule(context, movieDbId))
                .build()
    }
    private val recyclerAdapter by lazy {
        DetailsRecyclerAdapter(activityListener.videoClicks, this)
    }
    private lateinit var binding: FragmentMovieDetailsOnlBinding

    companion object {
        fun newInstance(movieDbId: Int): DetailsOnlFragment {
            val args = Bundle().apply { putInt(KEY_DB_ID, movieDbId) }
            return DetailsOnlFragment().apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMovieDetailsOnlBinding.inflate(inflater, container, false)
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
        val itemDecoration = FavReviewsItemDecoration(context, R.layout.row_details_review)
        binding.rvDetails.addItemDecoration(itemDecoration)
        binding.rvDetails.adapter = recyclerAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initLoaders()
        subscribeToSinks(activityListener.sinks)
    }

    private fun initLoaders() {
        loaderManager.initLoader<Observable<MovieDetails>>(LOADER_ONL_DETAILS, null,
                object : LoaderManager.LoaderCallbacks<Observable<MovieDetails>> {
                    override fun onCreateLoader(id: Int,
                                                args: Bundle?
                    ): Loader<Observable<MovieDetails>> {
                        return component.detailsOnlLoader
                    }

                    override fun onLoadFinished(
                            loader: Loader<Observable<MovieDetails>>,
                            data: Observable<MovieDetails>
                    ) {
                        data.subscribe(activityListener.detailsOnl)
                    }

                    override fun onLoaderReset(loader: Loader<Observable<MovieDetails>>) {
                        // do nothing
                    }
                })
        loaderManager.initLoader<Cursor>(LOADER_ONL_IS_FAV, null,
                object : LoaderManager.LoaderCallbacks<Cursor> {
                    override fun onCreateLoader(id: Int,
                                                args: Bundle?
                    ): Loader<Cursor> {
                        return component.detailsOnlIdLoader
                    }

                    override fun onLoadFinished(
                            loader: Loader<Cursor>,
                            data: Cursor
                    ) {
                        val result = if (data.moveToFirst()) Maybe.Some(data) else Maybe.None
                        activityListener.detailsOnlId.call(result)
                    }

                    override fun onLoaderReset(loader: Loader<Cursor>) {
                        // do nothing
                    }
                })
    }

    private fun subscribeToSinks(sinks: DetailsSinks) {
        sinks.state.subscribe { render(it) }
    }

    private fun render(state: DetailsState) {
        recyclerAdapter.swapData(state.details)
    }

    override fun onPosterLoaded() {
        ActivityCompat.startPostponedEnterTransition(activity)
    }

}