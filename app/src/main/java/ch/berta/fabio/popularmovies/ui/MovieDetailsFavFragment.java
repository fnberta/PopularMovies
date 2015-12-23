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

package ch.berta.fabio.popularmovies.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.WorkerUtils;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.ui.adapters.MovieDetailsRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.MovieDetailsRecyclerAdapter.InfoRow;

/**
 * Displays detail information about a movie, including poster image, release date, rating, an
 * overview of the plot, reviews and videos (e.g. trailers). Queries data from the local content
 * provider.
 */
public class MovieDetailsFavFragment extends MovieDetailsBaseFragment {

    public static final int RESULT_UNFAVOURED = 2;
    private static final int LOADER_FAV = 0;
    private static final String KEY_MOVIE_ROW_ID = "KEY_MOVIE_ROW_ID";
    private static final String LOG_TAG = MovieDetailsFavFragment.class.getSimpleName();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MovieDetailsFavFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of a {@link MovieDetailsFavFragment}.
     *
     * @param movieRowId the row id of the movie whose details should be displayed
     * @return a new instance of a {@link MovieDetailsFavFragment}
     */
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

        Bundle args = getArguments();
        if (args != null) {
            mMovieRowId = args.getLong(KEY_MOVIE_ROW_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_details_fav, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_details_fav);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.red_500, R.color.red_700, R.color.amber_A400);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMovieDetailsWithWorker();
            }
        });
    }

    @Override
    public void onMovieDetailsOnlineLoadFailed() {
        WorkerUtils.removeWorker(getFragmentManager(), QUERY_MOVIE_DETAILS_WORKER);

        onMovieUpdateFailed();
    }

    private void onMovieUpdateFailed() {
        mSwipeRefreshLayout.setRefreshing(false);

        Snackbar.make(mRecyclerView, R.string.snackbar_movie_update_failed, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchMovieDetailsWithWorker();
                    }
                })
                .show();
    }

    @Override
    public void onMovieDetailsOnlineLoaded(MovieDetails movieDetails) {
        WorkerUtils.removeWorker(getFragmentManager(), QUERY_MOVIE_DETAILS_WORKER);

        mMovieRepo.updateMovieLocal(getActivity(), movieDetails, mMovieRowId, this);
    }

    @Override
    public void onMovieUpdated() {
        super.onMovieUpdated();

        getLoaderManager().restartLoader(LOADER_FAV, null, this);
    }

    @NonNull
    @Override
    protected MovieDetailsRecyclerAdapter getRecyclerAdapter() {
        return new MovieDetailsRecyclerAdapter(getActivity(), mUseTwoPane, this, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_FAV, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mMovieRepo.getFavMovieDetailsLoader(getActivity(), mMovieRowId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() != LOADER_FAV || mMovie != null && !mSwipeRefreshLayout.isRefreshing()) {
            // data is already set or loader id does not match, return
            return;
        }

        if (data.moveToFirst()) {
            mMovie = mMovieRepo.getMovieFromFavMovieDetailsCursor(data);
            setFavoured(true);

            if (!mUseTwoPane) {
                mListener.setOnePaneHeader(mMovie.getTitle(), mMovie.getBackdropPath());
            }
            mRecyclerAdapter.setMovie(mMovie);

            setShareYoutubeIntent();
            getActivity().invalidateOptionsMenu();

            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else if (mSwipeRefreshLayout.isRefreshing()) {
            onMovieUpdateFailed();
        } else {
            startPostponedEnterTransition();
            Snackbar.make(mRecyclerView, R.string.snackbar_no_movie_data, Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onMovieDeleted() {
        super.onMovieDeleted();

        if (mUseTwoPane) {
            mListener.hideDetailsFragment();
        }
    }

    @Override
    protected void onMovieDeletedOnePane() {
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
            infoRow.removeSharedElement();
        }
    }
}
