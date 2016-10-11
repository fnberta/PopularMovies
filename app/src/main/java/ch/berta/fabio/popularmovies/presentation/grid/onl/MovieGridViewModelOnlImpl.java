/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.presentation.grid.onl;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.SnackbarAction;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.grid.MovieGridViewModelBaseImpl;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides an implementation of the {@link MovieGridViewModelOnl} interface.
 * <p/>
 * Subclass of {@link MovieGridViewModelBaseImpl}.
 */
public class MovieGridViewModelOnlImpl extends
        MovieGridViewModelBaseImpl<MovieGridViewModelOnl.ViewInteractionListener>
        implements MovieGridViewModelOnl {

    private static final String STATE_MOVIE_PAGE = "STATE_MOVIE_PAGE";
    private static final String STATE_REFRESHING = "STATE_REFRESHING";
    private static final String STATE_LOADING_MORE = "STATE_LOADING_MORE";
    private static final String STATE_LOADING_NEW_SORT = "STATE_LOADING_NEW_SORT";
    private final List<Movie> mMovies;
    private int mMoviePage;
    private boolean mRefreshing;
    private boolean mLoadingMore;
    private boolean mLoadingNewSort;
    private Sort mSortSelected;

    /**
     * Constructs a new {@link MovieGridViewModelOnlImpl}.
     *
     * @param sortSelected the currently selected sort option
     */
    public MovieGridViewModelOnlImpl(@Nullable Bundle savedState, @NonNull Sort sortSelected) {
        super(savedState);

        mSortSelected = sortSelected;
        mMovies = new ArrayList<>();

        if (savedState != null) {
            mMoviePage = savedState.getInt(STATE_MOVIE_PAGE);
            mRefreshing = savedState.getBoolean(STATE_REFRESHING);
            mLoadingMore = savedState.getBoolean(STATE_LOADING_MORE);
            mLoadingNewSort = savedState.getBoolean(STATE_LOADING_NEW_SORT);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putInt(STATE_MOVIE_PAGE, mMoviePage);
        outState.putBoolean(STATE_REFRESHING, mRefreshing);
        outState.putBoolean(STATE_LOADING_MORE, mLoadingMore);
        outState.putBoolean(STATE_LOADING_NEW_SORT, mLoadingNewSort);
    }

    @Override
    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefreshing(true);
                mMoviePage = 1;
                mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
            }
        };
    }

    @Override
    public void loadMovies() {
        final int moviesSize = getItemCount();
        if (moviesSize == 0) {
            mMoviePage = 1;
            mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
        } else {
            if (mLoadingMore) {
                // scroll to last position to show load more indicator
                mView.scrollToPosition(moviesSize - 1);
            }

            if (!mLoadingNewSort) {
                setMoviesLoaded(true);
            }
        }
    }

    @Override
    public void setQueryMoviesStream(@NonNull Observable<List<Movie>> observable, @NonNull final String workerTag) {
        mSubscriptions.add(observable.subscribe(new Subscriber<List<Movie>>() {
            @Override
            public void onCompleted() {
                mView.removeWorker(workerTag);
            }

            @Override
            public void onError(Throwable e) {
                mView.removeWorker(workerTag);
                onMoviesLoadFailed();
            }

            @Override
            public void onNext(List<Movie> movies) {
                if (mMoviePage == 1) {
                    setMovies(movies);
                } else {
                    addMovies(movies);
                }

                mMoviePage++;
            }
        }));
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        super.onWorkerError(workerTag);

        onMoviesLoadFailed();
    }

    private void onMoviesLoadFailed() {
        if (mMoviePage == 1) {
            mLoadingNewSort = false;
            setRefreshing(false);
            setMoviesLoaded(true);

            mView.showMessage(R.string.snackbar_movies_load_failed, new SnackbarAction(R.string.snackbar_retry) {
                @Override
                public void onClick(View v) {
                    setRefreshing(true);
                    mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
                }
            });
        } else {
            hideLoadMoreIndicator();
            mLoadingMore = false;

            mView.showMessage(R.string.snackbar_movies_load_failed, null);
        }
    }

    private void setMovies(@NonNull List<Movie> movies) {
        // finished loading
        mLoadingNewSort = false;
        setRefreshing(false);

        // set movies
        mMovies.clear();
        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }
        mView.notifyMoviesChanged();

        // update view
        mView.scrollToPosition(0);
        setMoviesLoaded(true);
        setMoviesAvailable(!movies.isEmpty());
    }

    private void addMovies(@NonNull List<Movie> movies) {
        // remove load more indicator
        hideLoadMoreIndicator();

        // insert movies
        mMovies.addAll(movies);
        mView.notifyMoviesInserted(getItemCount(), movies.size());

        // indicate load more process finished
        mLoadingMore = false;
    }

    private void hideLoadMoreIndicator() {
        final int position = getItemCount() - 1;
        mMovies.remove(position);
        mView.notifyLoadMoreRemoved(position);
    }

    @Override
    public void onLoadMore() {
        mLoadingMore = true;
        showLoadMoreIndicator();
        mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
    }

    private void showLoadMoreIndicator() {
        mMovies.add(null);
        mView.notifyLoadMoreInserted(getItemCount() - 1);
    }

    @Override
    public boolean isLoading() {
        return mRefreshing || mLoadingMore || mLoadingNewSort;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return mMoviePage >= MOVIE_DB_MAX_PAGE;
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMovies.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public Movie getMovieAtPosition(int position) {
        return mMovies.get(position);
    }

    @Override
    public boolean isMovieSelected(@NonNull Movie movie) {
        final int dbId = movie.getDbId();
        if (dbId == mMovieDbIdSelected) {
            return true;
        }

        mMovieDbIdSelected = dbId;
        return false;
    }

    @Override
    public void onMovieRowItemClick(int position, @NonNull View sharedView) {
        final Movie movie = getMovieAtPosition(position);
        mView.launchDetailsScreen(movie, sharedView);
    }

    @Override
    protected void switchSort(@NonNull Sort sort) {
        if (sort.getOption().equals(Sort.SORT_FAVORITE)) {
            mView.showFavoriteMovies();
            return;
        }

        mSortSelected = sort;

        setMoviesLoaded(false);
        setRefreshing(false);
        mLoadingMore = false;
        mLoadingNewSort = true;
        mMoviePage = 1;

        mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), true);
    }
}
