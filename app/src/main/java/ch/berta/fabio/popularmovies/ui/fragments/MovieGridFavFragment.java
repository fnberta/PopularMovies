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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.BuildConfig;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepositoryImpl;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridFavBinding;
import ch.berta.fabio.popularmovies.ui.activities.MovieDetailsActivity;
import ch.berta.fabio.popularmovies.ui.adapters.MoviesFavRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.decorators.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModel;
import ch.berta.fabio.popularmovies.viewmodels.MovieGridViewModelFav;

/**
 * Displays a grid of movie poster images.
 */
public class MovieGridFavFragment extends MovieGridBaseFragment implements
        MovieGridViewModelFav.ViewInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_MOVIE_SELECTED_ROW_ID = BuildConfig.APPLICATION_ID + ".intents.MOVIE_SELECTED_ROW_ID";
    private static final int FAV_MOVIES_LOADER = 0;
    private MoviesFavRecyclerAdapter mRecyclerAdapter;
    private MovieRepository mMovieRepo;
    private FragmentMovieGridFavBinding mBinding;
    private MovieGridViewModelFav mViewModel;
    private FragmentInteractionListener mActivity;

    public MovieGridFavFragment() {
        // required empty constructor
    }

    public static MovieGridFavFragment newInstance(@NonNull MovieGridViewModel viewModel) {
        MovieGridFavFragment fragment = new MovieGridFavFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_VIEW_MODEL, viewModel);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
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

        mMovieRepo = new MovieRepositoryImpl();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMovieGridFavBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    protected void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mBinding.rvGrid.setLayoutManager(layoutManager);
        mBinding.rvGrid.setHasFixedSize(true);
        mBinding.rvGrid.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new MoviesFavRecyclerAdapter(null, mViewModel, mMovieRepo,
                getLayoutWidth(), spanCount);
        mBinding.rvGrid.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FAV_MOVIES_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.attachView(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mMovieRepo.getFavMoviesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
        mViewModel.setMoviesAvailable(mRecyclerAdapter.getItemCount() > 0);
        mViewModel.setMoviesLoaded(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);
        mViewModel.setMoviesAvailable(false);
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
    public void launchDetailsScreen(int moviePosition, @NonNull View posterSharedElement) {
        if (!mUseTwoPane) {
            final Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            final long rowId = mRecyclerAdapter.getItemId(moviePosition);
            intent.putExtra(INTENT_MOVIE_SELECTED_ROW_ID, rowId);
            startDetailsActivity(intent, posterSharedElement);
        } else {
            final int dbId = mRecyclerAdapter.getMovieDbIdForPosition(moviePosition);
            if (!mViewModel.isMovieSelected(dbId)) {
                final long rowId = mRecyclerAdapter.getItemId(moviePosition);
                mActivity.showDetailsFavFragment(rowId);
            }
        }
    }

    /**
     * Defines the interaction with the hosting activity
     */
    public interface FragmentInteractionListener {
        /**
         * Shows the details screen for a favoured movie.
         *
         * @param rowId the row id of the movie
         */
        void showDetailsFavFragment(long rowId);
    }
}
