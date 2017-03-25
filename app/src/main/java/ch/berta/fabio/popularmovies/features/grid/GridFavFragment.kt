package ch.berta.fabio.popularmovies.features.grid

import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridFavBinding
import ch.berta.fabio.popularmovies.extensions.bindTo
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.PosterGridItemDecoration
import ch.berta.fabio.popularmovies.features.grid.component.GridViewState
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridFavViewModel
import ch.berta.fabio.popularmovies.utils.calcPosterHeight
import org.jetbrains.anko.db.asMapSequence
import rx.Observable
import timber.log.Timber
import javax.inject.Inject

class GridFavFragment : BaseFragment<GridFavActivityListener>(),
                        LoaderManager.LoaderCallbacks<Cursor> {

    @Inject
    lateinit var movieRepo: MovieRepository
    private val viewModel = GridFavViewModel()
    private val useTwoPane by lazy {
        resources.getBoolean(R.bool.use_two_pane_layout)
    }
    private val recyclerAdapter by lazy {
        val spanCount = resources.getInteger(R.integer.span_count)
        val posterHeight = calcPosterHeight(resources, useTwoPane, spanCount)
        GridFavRecyclerAdapter(emptySequence(), posterHeight)
    }
    lateinit private var binding: FragmentMovieGridFavBinding

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieGridFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.span_count)
        val layoutManager = GridLayoutManager(activity, spanCount)
        binding.rvGrid.layoutManager = layoutManager
        binding.rvGrid.setHasFixedSize(true)
        val itemPadding = resources.getDimensionPixelSize(R.dimen.grid_padding)
        binding.rvGrid.addItemDecoration(PosterGridItemDecoration(itemPadding))
        binding.rvGrid.adapter = recyclerAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        injectDependencies(activityListener.component)
        binding.viewModel = viewModel
        loaderManager.initLoader<Cursor>(0, null, this)
        subscribeToState(activityListener.state)
    }

    private fun injectDependencies(component: GridComponent) {
        component.inject(this)
    }

    private fun subscribeToState(state: Observable<GridViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: GridViewState) {
        viewModel.loading = state.loading
        viewModel.empty = state.moviesFav.count() == 0
        recyclerAdapter.swapData(state.moviesFav)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return activityListener.component.moviesFavLoader
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        val sequence = data?.asMapSequence() ?: emptySequence()
        activityListener.moviesFav.call(sequence)
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        activityListener.moviesFav.call(emptySequence())
    }
}
