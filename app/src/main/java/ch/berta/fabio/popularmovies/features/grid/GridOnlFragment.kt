package ch.berta.fabio.popularmovies.features.grid

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridOnlBinding
import ch.berta.fabio.popularmovies.extensions.bindTo
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.PosterGridItemDecoration
import ch.berta.fabio.popularmovies.features.grid.component.GridViewState
import ch.berta.fabio.popularmovies.features.grid.di.GridComponent
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlViewModel
import com.jakewharton.rxbinding.support.v4.widget.refreshes
import rx.Observable
import timber.log.Timber
import javax.inject.Inject

const val KEY_SORT_VALUE = "KEY_SORT_VALUE"
const val KEY_LOADER_SORT = "KEY_LOADER_SORT"
const val KEY_LOADER_PAGE = "KEY_LOADER_PAGE"

class GridOnlFragment : BaseFragment<GridOnlActivityListener>(),
                        LoaderManager.LoaderCallbacks<Observable<List<Movie>>> {

    @Inject
    lateinit var movieRepo: MovieRepository
    private val viewModel = GridOnlViewModel()
    private val recyclerAdapter: GridOnlRecyclerAdapter by lazy {
        GridOnlRecyclerAdapter()
    }
    lateinit private var binding: FragmentMovieGridOnlBinding

    companion object {
        fun newInstance(sortValue: String): GridOnlFragment {
            val args = Bundle()
            args.putString(KEY_SORT_VALUE, sortValue)
            val fragment = GridOnlFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieGridOnlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeRelays()
    }

    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.span_count)
        val layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = recyclerAdapter.getItemViewType(position)
                return if (viewType == R.layout.row_progress) spanCount else 1
            }
        }
        binding.rvGrid.layoutManager = layoutManager
        binding.rvGrid.setHasFixedSize(true)
        val itemPadding = resources.getDimensionPixelSize(R.dimen.grid_padding)
        binding.rvGrid.addItemDecoration(PosterGridItemDecoration(itemPadding))
        binding.rvGrid.adapter = recyclerAdapter
    }

    private fun subscribeRelays() {
        binding.srlGrid.refreshes().subscribe(activityListener.refreshSwipes)
        recyclerAdapter.movieClicks.subscribe(activityListener.movieClicks)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        injectDependencies(activityListener.component)
        binding.viewModel = viewModel

        val args = getLoaderArgs(arguments.getString(KEY_SORT_VALUE))
        loaderManager.initLoader<Observable<List<Movie>>>(0, args, this)

        subscribeToState(activityListener.state)
    }

    private fun getLoaderArgs(sort: String, page: Int = 1): Bundle {
        val bundle = Bundle()
        bundle.putString(KEY_LOADER_SORT, sort)
        bundle.putInt(KEY_LOADER_PAGE, page)
        return bundle
    }

    private fun injectDependencies(component: GridComponent) {
        component.inject(this)
    }

    private fun subscribeToState(state: Observable<GridViewState>) {
        state.bindTo(lifecycleHandler.lifecycle).subscribe { render(it) }
    }

    private fun render(state: GridViewState) {
        viewModel.loading = state.loading
        viewModel.refreshing = state.refreshing

        recyclerAdapter.swapData(state.moviesOnl)
        if (state.loadNewSort || state.refreshMoviesOnl) {
            val args = getLoaderArgs(state.sortOptions[state.sortSelectedPos].value)
            loaderManager.restartLoader(0, args, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Observable<List<Movie>>> {
        val loader = activityListener.component.moviesOnlLoader
        loader.sort = args?.getString(KEY_LOADER_SORT) ?: arguments.getString(KEY_SORT_VALUE)
        loader.page = args?.getInt(KEY_LOADER_PAGE) ?: 1
        return loader
    }

    override fun onLoadFinished(
            loader: Loader<Observable<List<Movie>>>,
            data: Observable<List<Movie>>
    ) {
        data.subscribe(activityListener.moviesOnl)
    }

    override fun onLoaderReset(loader: Loader<Observable<List<Movie>>>) {
        activityListener.moviesOnl.call(emptyList<Movie>())
    }
}
