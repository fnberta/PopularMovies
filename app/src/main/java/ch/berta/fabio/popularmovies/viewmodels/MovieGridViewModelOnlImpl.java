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

package ch.berta.fabio.popularmovies.viewmodels;

import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.SnackbarAction;
import ch.berta.fabio.popularmovies.data.models.Sort;

/**
 * Provides an implementation of the {@link MovieGridViewModelOnl} interface.
 * <p/>
 * Subclass of {@link MovieGridViewModelBaseImpl}.
 */
public class MovieGridViewModelOnlImpl extends
        MovieGridViewModelBaseImpl<MovieGridViewModelOnl.ViewInteractionListener> implements
        MovieGridViewModelOnl {

    public static final Creator<MovieGridViewModelOnlImpl> CREATOR = new Creator<MovieGridViewModelOnlImpl>() {
        public MovieGridViewModelOnlImpl createFromParcel(Parcel source) {
            return new MovieGridViewModelOnlImpl(source);
        }

        public MovieGridViewModelOnlImpl[] newArray(int size) {
            return new MovieGridViewModelOnlImpl[size];
        }
    };
    private final List<Movie> mMovies;
    private int mMoviePage;
    private boolean mRefreshing;
    private boolean mLoadingMore;
    private boolean mLoadingNewSort;
    private Sort mSortSelected;
    private int mMovieDbIdSelected;

    /**
     * Constructs a new {@link MovieGridViewModelOnlImpl}.
     *
     * @param sortSelected the currently selected sort option
     */
    public MovieGridViewModelOnlImpl(@NonNull Sort sortSelected) {
        mSortSelected = sortSelected;
        mMovies = new ArrayList<>();
    }

    protected MovieGridViewModelOnlImpl(Parcel in) {
        super(in);

        mMovies = in.createTypedArrayList(Movie.CREATOR);
        mMoviePage = in.readInt();
        mRefreshing = in.readByte() != 0;
        mLoadingMore = in.readByte() != 0;
        mLoadingNewSort = in.readByte() != 0;
        mSortSelected = in.readParcelable(Sort.class.getClassLoader());
        mMovieDbIdSelected = in.readInt();
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
                mMoviePage = 1;
                mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
            }
        };
    }

    @Override
    public void onSortOptionSelected(@NonNull Sort sortSelected) {
        mSortSelected = sortSelected;

        setMoviesLoaded(false);
        setRefreshing(false);
        mLoadingMore = false;
        mLoadingNewSort = true;
        mMoviePage = 1;

        mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), true);
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
    public void onMoviesOnlineLoaded(@NonNull List<Movie> movies) {
        mView.removeQueryMoviesWorker();

        if (mMoviePage == 1) {
            mLoadingNewSort = false;
            setRefreshing(false);

            setMovies(movies);
            mView.scrollToPosition(0);

            setMoviesLoaded(true);
            setMoviesAvailable(!movies.isEmpty());
        } else {
            hideLoadMoreIndicator();
            addMovies(movies);
            mLoadingMore = false;
        }

        mMoviePage++;
    }

    private void setMovies(@NonNull List<Movie> movies) {
        mMovies.clear();

        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }

        mView.notifyMoviesChanged();
    }

    private void hideLoadMoreIndicator() {
        final int position = getItemCount() - 1;
        mMovies.remove(position);
        mView.notifyLoadMoreRemoved(position);
    }

    private void addMovies(@NonNull List<Movie> movies) {
        mMovies.addAll(movies);
        mView.notifyMoviesInserted(getItemCount(), movies.size());
    }

    @Override
    public void onMoviesOnlineLoadFailed() {
        mView.removeQueryMoviesWorker();

        if (mMoviePage == 1) {
            mLoadingNewSort = false;
            setRefreshing(false);
            setMoviesLoaded(true);

            mView.showSnackbar(R.string.error_connection, new SnackbarAction(R.string.snackbar_retry) {
                @Override
                public void onClick(View v) {
                    setRefreshing(true);
                    mView.loadQueryMoviesWorker(mMoviePage, mSortSelected.getOption(), false);
                }
            });
        } else {
            hideLoadMoreIndicator();
            mLoadingMore = false;

            mView.showSnackbar(R.string.error_connection, null);
        }
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeTypedList(mMovies);
        dest.writeInt(mMoviePage);
        dest.writeByte(mRefreshing ? (byte) 1 : (byte) 0);
        dest.writeByte(mLoadingMore ? (byte) 1 : (byte) 0);
        dest.writeByte(mLoadingNewSort ? (byte) 1 : (byte) 0);
        dest.writeParcelable(mSortSelected, 0);
        dest.writeInt(mMovieDbIdSelected);
    }
}
