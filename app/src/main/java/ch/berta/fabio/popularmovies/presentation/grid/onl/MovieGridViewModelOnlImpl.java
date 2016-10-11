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
    private final List<Movie> movies;
    private int moviePage;
    private boolean refreshing;
    private boolean loadingMore;
    private boolean loadingNewSort;
    private Sort sortSelected;

    /**
     * Constructs a new {@link MovieGridViewModelOnlImpl}.
     *
     * @param sortSelected the currently selected sort option
     */
    public MovieGridViewModelOnlImpl(@Nullable Bundle savedState, @NonNull Sort sortSelected) {
        super(savedState);

        this.sortSelected = sortSelected;
        movies = new ArrayList<>();

        if (savedState != null) {
            moviePage = savedState.getInt(STATE_MOVIE_PAGE);
            refreshing = savedState.getBoolean(STATE_REFRESHING);
            loadingMore = savedState.getBoolean(STATE_LOADING_MORE);
            loadingNewSort = savedState.getBoolean(STATE_LOADING_NEW_SORT);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putInt(STATE_MOVIE_PAGE, moviePage);
        outState.putBoolean(STATE_REFRESHING, refreshing);
        outState.putBoolean(STATE_LOADING_MORE, loadingMore);
        outState.putBoolean(STATE_LOADING_NEW_SORT, loadingNewSort);
    }

    @Override
    @Bindable
    public boolean isRefreshing() {
        return refreshing;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefreshing(true);
                moviePage = 1;
                view.loadQueryMoviesWorker(moviePage, sortSelected.getOption(), false);
            }
        };
    }

    @Override
    public void loadMovies() {
        final int moviesSize = getItemCount();
        if (moviesSize == 0) {
            moviePage = 1;
            view.loadQueryMoviesWorker(moviePage, sortSelected.getOption(), false);
        } else {
            if (loadingMore) {
                // scroll to last position to show load more indicator
                view.scrollToPosition(moviesSize - 1);
            }

            if (!loadingNewSort) {
                setMoviesLoaded(true);
            }
        }
    }

    @Override
    public void setQueryMoviesStream(@NonNull Observable<List<Movie>> observable, @NonNull final String workerTag) {
        subscriptions.add(observable.subscribe(new Subscriber<List<Movie>>() {
            @Override
            public void onCompleted() {
                view.removeWorker(workerTag);
            }

            @Override
            public void onError(Throwable e) {
                view.removeWorker(workerTag);
                onMoviesLoadFailed();
            }

            @Override
            public void onNext(List<Movie> movies) {
                if (moviePage == 1) {
                    setMovies(movies);
                } else {
                    addMovies(movies);
                }

                moviePage++;
            }
        }));
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        super.onWorkerError(workerTag);

        onMoviesLoadFailed();
    }

    private void onMoviesLoadFailed() {
        if (moviePage == 1) {
            loadingNewSort = false;
            setRefreshing(false);
            setMoviesLoaded(true);

            view.showMessage(R.string.snackbar_movies_load_failed, new SnackbarAction(R.string.snackbar_retry) {
                @Override
                public void onClick(View v) {
                    setRefreshing(true);
                    view.loadQueryMoviesWorker(moviePage, sortSelected.getOption(), false);
                }
            });
        } else {
            hideLoadMoreIndicator();
            loadingMore = false;

            view.showMessage(R.string.snackbar_movies_load_failed, null);
        }
    }

    private void setMovies(@NonNull List<Movie> movies) {
        // finished loading
        loadingNewSort = false;
        setRefreshing(false);

        // set movies
        this.movies.clear();
        if (!movies.isEmpty()) {
            this.movies.addAll(movies);
        }
        view.notifyMoviesChanged();

        // update view
        view.scrollToPosition(0);
        setMoviesLoaded(true);
        setMoviesAvailable(!movies.isEmpty());
    }

    private void addMovies(@NonNull List<Movie> movies) {
        // remove load more indicator
        hideLoadMoreIndicator();

        // insert movies
        this.movies.addAll(movies);
        view.notifyMoviesInserted(getItemCount(), movies.size());

        // indicate load more process finished
        loadingMore = false;
    }

    private void hideLoadMoreIndicator() {
        final int position = getItemCount() - 1;
        movies.remove(position);
        view.notifyLoadMoreRemoved(position);
    }

    @Override
    public void onLoadMore() {
        loadingMore = true;
        showLoadMoreIndicator();
        view.loadQueryMoviesWorker(moviePage, sortSelected.getOption(), false);
    }

    private void showLoadMoreIndicator() {
        movies.add(null);
        view.notifyLoadMoreInserted(getItemCount() - 1);
    }

    @Override
    public boolean isLoading() {
        return refreshing || loadingMore || loadingNewSort;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return moviePage >= MOVIE_DB_MAX_PAGE;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (movies.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public Movie getMovieAtPosition(int position) {
        return movies.get(position);
    }

    @Override
    public boolean isMovieSelected(@NonNull Movie movie) {
        final int dbId = movie.getDbId();
        if (dbId == movieDbIdSelected) {
            return true;
        }

        movieDbIdSelected = dbId;
        return false;
    }

    @Override
    public void onMovieRowItemClick(int position, @NonNull View sharedView) {
        final Movie movie = getMovieAtPosition(position);
        view.launchDetailsScreen(movie, sharedView);
    }

    @Override
    protected void switchSort(@NonNull Sort sort) {
        if (sort.getOption().equals(Sort.SORT_FAVORITE)) {
            view.showFavoriteMovies();
            return;
        }

        sortSelected = sort;

        setMoviesLoaded(false);
        setRefreshing(false);
        loadingMore = false;
        loadingNewSort = true;
        moviePage = 1;

        view.loadQueryMoviesWorker(moviePage, sortSelected.getOption(), true);
    }
}
