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

package ch.berta.fabio.popularmovies.presentation.details.fav;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieDetailsFavBinding;
import ch.berta.fabio.popularmovies.databinding.RowDetailsInfoBinding;
import ch.berta.fabio.popularmovies.presentation.details.di.DaggerMovieDetailsComponent;
import ch.berta.fabio.popularmovies.presentation.details.di.MovieDetailsViewModelModule;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsBaseFragment;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsRecyclerAdapter.InfoRow;
import ch.berta.fabio.popularmovies.presentation.workerfragments.UpdateMovieDetailsWorker;
import ch.berta.fabio.popularmovies.utils.Utils;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;

/**
 * Displays detail information about a movie, including poster image, release date, rating, an
 * overview of the plot, reviews and videos (e.g. trailers). Queries data from the local content
 * provider.
 */
public class MovieDetailsFavFragment extends MovieDetailsBaseFragment<MovieDetailsViewModelFav>
        implements MovieDetailsViewModelFav.ViewInteractionListener {

    public static final int RESULT_UNFAVOURED = 2;
    private static final String KEY_MOVIE_ROW_ID = "MOVIE_ROW_ID";
    private static final int LOADER_FAV = 0;
    private static final String LOG_TAG = MovieDetailsFavFragment.class.getSimpleName();
    private FragmentMovieDetailsFavBinding mBinding;

    public MovieDetailsFavFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFavFragment newInstance(long movieRowId) {
        MovieDetailsFavFragment fragment = new MovieDetailsFavFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_MOVIE_ROW_ID, movieRowId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long movieRowId = getArguments().getLong(KEY_MOVIE_ROW_ID);
        DaggerMovieDetailsComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(getActivity()))
                .movieDetailsViewModelModule(new MovieDetailsViewModelModule(savedInstanceState,
                        movieRowId, mUseTwoPane))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMovieDetailsFavBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvDetails;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_FAV, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.attachView(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mMovieRepo.getFavMovieDetailsLoader(getActivity(), mViewModel.getMovieRowId());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() != LOADER_FAV || mViewModel.isDataSetAndNotReloading()) {
            // data is already set or loader id does not match, return
            return;
        }

        if (data.moveToFirst()) {
            final Movie movie = mMovieRepo.getMovieFromFavMovieDetailsCursor(data);
            mViewModel.setMovie(movie);
            getActivity().invalidateOptionsMenu();
        } else {
            mViewModel.onMovieDataEmpty();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.detachView();
    }

    @Override
    public void loadUpdateMovieDetailsWorker(int movieDbId, long movieRowId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, UpdateMovieDetailsWorker.WORKER_TAG);

        if (worker == null) {
            worker = UpdateMovieDetailsWorker.newInstance(movieDbId, movieRowId);
            fragmentManager.beginTransaction()
                    .add(worker, UpdateMovieDetailsWorker.WORKER_TAG)
                    .commit();
        }
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(LOADER_FAV, null, this);
    }

    @Override
    public void hideDetailsView() {
        mActivity.hideDetailsFragment();
    }

    @Override
    public void finishScreen() {
        removeSharedElement();
        final FragmentActivity activity = getActivity();
        activity.setResult(RESULT_UNFAVOURED);
        ActivityCompat.finishAfterTransition(activity);
    }

    /**
     * Disables shared element transition, it would break the recycler view item change animation.
     */
    private void removeSharedElement() {
        if (!mUseTwoPane && Utils.isRunningLollipopAndHigher()) {
            // info row will always be the first position in one pane mode, hence 0
            InfoRow infoRow = (InfoRow) mRecyclerView.findViewHolderForAdapterPosition(0);
            final RowDetailsInfoBinding binding = infoRow.getBinding();
            binding.getViewModel().setTransitionEnabled(false);
            binding.executePendingBindings();
        }
    }
}
