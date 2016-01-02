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

package ch.berta.fabio.popularmovies.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;

import ch.berta.fabio.popularmovies.BuildConfig;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.SnackbarAction;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridBinding;
import ch.berta.fabio.popularmovies.ui.activities.MovieDetailsActivity;
import ch.berta.fabio.popularmovies.ui.adapters.MoviesRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.decorators.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModel;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelOnl;
import ch.berta.fabio.popularmovies.workerfragments.QueryMoviesWorker;

/**
 * Displays a grid of movie poster images.
 */
public class MovieGridOnlFragment extends MovieGridBaseFragment implements
        MovieGridViewModelOnl.ViewInteractionListener {

    public static final String INTENT_MOVIE_SELECTED = BuildConfig.APPLICATION_ID + ".intents.MOVIE_SELECTED";
    private static final String LOG_TAG = MovieGridOnlFragment.class.getSimpleName();
    private static final String QUERY_MOVIES_TASK = "query_movies_task";
    private MoviesRecyclerAdapter mRecyclerAdapter;
    private FragmentMovieGridBinding mBinding;
    private MovieGridViewModelOnl mViewModel;
    private FragmentInteractionListener mActivity;

    public MovieGridOnlFragment() {
        // required empty constructor
    }

    public static MovieGridOnlFragment newInstance(@NonNull MovieGridViewModel viewModel) {
        MovieGridOnlFragment fragment = new MovieGridOnlFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_VIEW_MODEL, viewModel);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mViewModel = args.getParcelable(KEY_VIEW_MODEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMovieGridBinding.inflate(inflater, container, false);
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
        mRecyclerAdapter = new MoviesRecyclerAdapter(mViewModel, getLayoutWidth(), spanCount);
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
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void loadQueryMoviesWorker(int moviePage, @NonNull String sortOption,
                                      boolean forceNewQuery) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, QUERY_MOVIES_TASK);

        if (worker == null) {
            worker = QueryMoviesWorker.newInstance(moviePage, sortOption);
            fragmentManager.beginTransaction()
                    .add(worker, QUERY_MOVIES_TASK)
                    .commit();
        } else if (forceNewQuery) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(worker);
            worker = QueryMoviesWorker.newInstance(moviePage, sortOption);
            transaction.add(worker, QUERY_MOVIES_TASK).commit();
        }
    }

    @Override
    public void removeQueryMoviesWorker() {
        WorkerUtils.removeWorker(getFragmentManager(), QUERY_MOVIES_TASK);
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
    public void showSnackbar(@StringRes int text, @Nullable SnackbarAction action) {
        Snackbar snackbar = Snackbar.make(mBinding.rvGrid, text, Snackbar.LENGTH_LONG);
        if (action != null) {
            snackbar.setAction(action.getActionText(), action);
        }
        snackbar.show();
    }

    @Override
    public void launchDetailsScreen(@NonNull Movie movie, @NonNull View posterSharedElement) {
        if (!mUseTwoPane) {
            final Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra(INTENT_MOVIE_SELECTED, movie);
            startDetailsActivity(intent, posterSharedElement);
        } else if (!mViewModel.isMovieSelected(movie)) {
            mActivity.showDetailsOnlFragment(movie);
        }
    }

    /**
     * Defines the interaction with the hosting activity
     */
    public interface FragmentInteractionListener {
        /**
         * Shows the details screen of a movie.
         *
         * @param movie the movie to show the details for
         */
        void showDetailsOnlFragment(@NonNull Movie movie);
    }
}
