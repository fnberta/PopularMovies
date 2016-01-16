/*
 * Copyright (c) 2015 Fabio Berta
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

package ch.berta.fabio.popularmovies.presentation.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;

import ch.berta.fabio.popularmovies.BuildConfig;
import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridOnlBinding;
import ch.berta.fabio.popularmovies.di.components.DaggerMovieGridComponent;
import ch.berta.fabio.popularmovies.di.modules.MovieGridViewModelModule;
import ch.berta.fabio.popularmovies.di.modules.MovieRepositoryModule;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.ui.activities.MovieDetailsActivity;
import ch.berta.fabio.popularmovies.presentation.ui.adapters.MoviesOnlRecyclerAdapter;
import ch.berta.fabio.popularmovies.presentation.ui.adapters.decorators.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMoviesWorker;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;

/**
 * Displays a grid of movie poster images.
 */
public class MovieGridOnlFragment extends MovieGridBaseFragment<MovieGridViewModelOnl,
        MovieGridBaseFragment.ActivityListener>
        implements MovieGridViewModelOnl.ViewInteractionListener {

    public static final String INTENT_MOVIE_SELECTED = BuildConfig.APPLICATION_ID + ".intents.MOVIE_SELECTED";
    private static final String LOG_TAG = MovieGridOnlFragment.class.getSimpleName();
    private static final String KEY_SORT_SELECTED = "SORT_SELECTED";
    private MoviesOnlRecyclerAdapter mRecyclerAdapter;
    private FragmentMovieGridOnlBinding mBinding;

    public MovieGridOnlFragment() {
        // required empty constructor
    }

    public static MovieGridOnlFragment newInstance(@NonNull Sort sortSelected) {
        MovieGridOnlFragment fragment = new MovieGridOnlFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_SORT_SELECTED, sortSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Sort sortSelected = getArguments().getParcelable(KEY_SORT_SELECTED);
        DaggerMovieGridComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(getActivity()))
                .movieRepositoryModule(new MovieRepositoryModule())
                .movieGridViewModelModule(new MovieGridViewModelModule(savedInstanceState, sortSelected))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMovieGridOnlBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (mViewModel.isRefreshing()) {
            // work around bug in SwipeRefreshLayout that prevents changing refresh state before it
            // is laid out, TODO: remove once bug is fixed
            mBinding.srlGrid.post(new Runnable() {
                @Override
                public void run() {
                    mBinding.srlGrid.setRefreshing(true);
                }
            });
        }
    }

    @Override
    protected void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mRecyclerAdapter.getItemViewType(position);
                return viewType == MovieGridViewModelOnl.TYPE_PROGRESS ? spanCount : 1;
            }
        });
        mBinding.rvGrid.setLayoutManager(layoutManager);
        mBinding.rvGrid.setHasFixedSize(true);
        mBinding.rvGrid.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new MoviesOnlRecyclerAdapter(mViewModel, getLayoutWidth(), spanCount);
        mBinding.rvGrid.setAdapter(mRecyclerAdapter);
        Mugen.with(mBinding.rvGrid, mViewModel).start();
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.attachView(this);
        mViewModel.loadMovies();
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.detachView();
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.rvGrid;
    }

    @Override
    public void loadQueryMoviesWorker(int moviePage, @NonNull String sortOption,
                                      boolean forceNewQuery) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, QueryMoviesWorker.WORKER_TAG);

        if (worker == null) {
            worker = QueryMoviesWorker.newInstance(moviePage, sortOption);
            fragmentManager.beginTransaction()
                    .add(worker, QueryMoviesWorker.WORKER_TAG)
                    .commit();
        } else if (forceNewQuery) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(worker);
            worker = QueryMoviesWorker.newInstance(moviePage, sortOption);
            transaction.add(worker, QueryMoviesWorker.WORKER_TAG).commit();
        }
    }

    @Override
    public void scrollToPosition(int position) {
        mBinding.rvGrid.scrollToPosition(position);
    }

    @Override
    public void notifyMoviesChanged() {
        mRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyMoviesInserted(int positionStart, int itemCount) {
        mRecyclerAdapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void notifyLoadMoreInserted(int position) {
        mRecyclerAdapter.notifyItemInserted(position);
    }

    @Override
    public void notifyLoadMoreRemoved(int position) {
        mRecyclerAdapter.notifyItemRemoved(position);
    }

    @Override
    public void launchDetailsScreen(@NonNull Movie movie, @NonNull View posterSharedElement) {
        if (!mUseTwoPane) {
            final Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra(INTENT_MOVIE_SELECTED, movie);
            startDetailsActivity(intent, posterSharedElement);
        } else if (!mViewModel.isMovieSelected(movie)) {
            showDetailsOnlFragment(movie);
        }
    }

    private void showDetailsOnlFragment(@NonNull Movie movie) {
        final MovieDetailsOnlFragment fragment = MovieDetailsOnlFragment.newInstance(movie);
        replaceDetailsFragment(fragment);
    }

    @Override
    public void showFavoriteMovies() {
        MovieGridFavFragment fragment = new MovieGridFavFragment();
        replaceFragment(fragment);
    }
}
