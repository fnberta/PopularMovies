package ch.berta.fabio.popularmovies.features.grid.view

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.Movie
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridOnlBinding
import ch.berta.fabio.popularmovies.effects.KEY_LOADER_ARGS
import ch.berta.fabio.popularmovies.effects.LoaderTarget
import ch.berta.fabio.popularmovies.extensions.bindToStartStop
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.component.GridSinks
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.viewmodels.GridOnlViewModel
import com.jakewharton.rxbinding.support.v4.widget.refreshes
import com.mugen.Mugen
import com.mugen.MugenCallbacks
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import rx.Observable

@PaperParcel
data class LoadOnlMoviesArgs(
        val sort: String,
        val page: Int = 1
) : PaperParcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelLoadOnlMoviesArgs.CREATOR
    }
}

const val LOADER_ONL_MOVIES = 1

class GridOnlFragment : BaseFragment<GridOnlActivityListener>(),
                        LoaderManager.LoaderCallbacks<Observable<List<Movie>>> {

    private val viewModel = GridOnlViewModel()
    private val recyclerAdapter: GridOnlRecyclerAdapter by lazy {
        GridOnlRecyclerAdapter(activityListener.movieClicks)
    }
    lateinit private var binding: FragmentMovieGridOnlBinding

    companion object {
        fun newInstance(sortValue: String): GridOnlFragment {
            val args = Bundle().apply {
                putParcelable(KEY_LOADER_ARGS, LoadOnlMoviesArgs(sortValue))
            }
            return GridOnlFragment().apply { arguments = args }
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
        val spanCount = resources.getInteger(ch.berta.fabio.popularmovies.R.integer.span_count)
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
        binding.rvGrid.addItemDecoration(GridItemPadding(itemPadding))
        binding.rvGrid.adapter = recyclerAdapter
        Mugen.with(binding.rvGrid, object : MugenCallbacks {
            override fun onLoadMore() = activityListener.loadMore.call(Unit)

            override fun isLoading(): Boolean = viewModel.loading || viewModel.refreshing
                    || viewModel.loadingMore

            override fun hasLoadedAllItems(): Boolean = false
        }).start()
    }

    private fun subscribeRelays() {
        binding.srlGrid.refreshes().subscribe(activityListener.refreshSwipes)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityListener.component.inject(this)
        binding.viewModel = viewModel
        loaderManager.initLoader<Observable<List<Movie>>>(LOADER_ONL_MOVIES, arguments, this)
        subscribeToSinks(activityListener.sinks)
    }

    private fun subscribeToSinks(sinks: GridSinks) {
        sinks.state
                .bindToStartStop(lifecycleHandler)
                .subscribe { render(it) }
        sinks.loader
                .bindToStartStop(lifecycleHandler)
                .subscribe { loadData(it) }
    }

    private fun render(state: GridState) {
        viewModel.loading = state.loading
        viewModel.refreshing = state.refreshing
        viewModel.loadingMore = state.loadingMore
        recyclerAdapter.swapData(state.moviesOnl)
    }

    private fun loadData(target: LoaderTarget) {
        val args = Bundle().apply { putParcelable(KEY_LOADER_ARGS, target.args) }
        loaderManager.restartLoader(target.key, args, this)
    }

    override fun onCreateLoader(
            id: Int,
            args: Bundle?
    ): Loader<Observable<List<Movie>>> = activityListener.component.moviesOnlLoader.apply {
        val loaderArgs = args?.get(KEY_LOADER_ARGS) as? LoadOnlMoviesArgs
        loaderArgs?.let {
            sort = it.sort
            page = it.page
        }
    }

    override fun onLoadFinished(
            loader: Loader<Observable<List<Movie>>>,
            data: Observable<List<Movie>>
    ) {
        data.subscribe(activityListener.moviesOnl)
    }

    override fun onLoaderReset(loader: Loader<Observable<List<Movie>>>) =
            activityListener.moviesOnl.call(emptyList<Movie>())
}
