package ch.berta.fabio.popularmovies.features.grid.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridBinding
import ch.berta.fabio.popularmovies.features.base.BaseFragment
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.component.GridState
import ch.berta.fabio.popularmovies.features.grid.vdos.GridViewData
import ch.berta.fabio.popularmovies.features.grid.viewmodel.GridViewModel
import ch.berta.fabio.popularmovies.calcPosterHeight
import com.mugen.Mugen
import com.mugen.MugenCallbacks

class GridFragment : BaseFragment<BaseFragment.ActivityListener>() {

    private val viewModel: GridViewModel by lazy { ViewModelProviders.of(activity).get(GridViewModel::class.java) }
    private val viewData = GridViewData()
    private val recyclerAdapter: GridRecyclerAdapter by lazy {
        GridRecyclerAdapter(calcPosterHeight(resources), viewModel)
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
                if (viewData.refreshEnabled) viewModel.uiEvents.loadMore.accept(Unit)
            }

            override fun isLoading(): Boolean = viewData.loading || viewData.refreshing || viewData.loadingMore

            override fun hasLoadedAllItems(): Boolean = false
        }).start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.srlGrid.setOnRefreshListener { viewModel.uiEvents.refreshSwipes.accept(Unit) }
        viewModel.state.observe(this, Observer<GridState> {
            it?.let { render(it) }
        })
    }

    private fun render(state: GridState) {
        viewData.refreshEnabled = state.sort.option != SortOption.SORT_FAVORITE
        viewData.empty = state.empty
        viewData.loading = state.loading
        viewData.refreshing = state.refreshing
        viewData.loadingMore = state.loadingMore
        recyclerAdapter.swapData(state.movies)

        if (state.snackbar.show) {
            Snackbar.make(binding.rvGrid, state.snackbar.message, Snackbar.LENGTH_LONG).show()
            viewModel.uiEvents.snackbarShown.accept(Unit)
        }
    }
}
