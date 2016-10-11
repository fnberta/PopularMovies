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

package ch.berta.fabio.popularmovies.presentation.viewmodels;

import android.content.ContentProviderResult;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.SnackbarAction;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides an implementation of the {@link MovieDetailsViewModelFav} interface.
 * <p/>
 * Subclass of {@link MovieDetailsViewModelBaseImpl}.
 */
public class MovieDetailsViewModelFavImpl extends
        MovieDetailsViewModelBaseImpl<MovieDetailsViewModelFav.ViewInteractionListener>
        implements MovieDetailsViewModelFav {

    private static final String STATE_REFRESHING = "STATE_REFRESHING";
    private boolean mRefreshing;

    /**
     * Constructs a new {@link MovieDetailsViewModelFavImpl}.
     *
     * @param savedState      the bundle to recover the state from
     * @param movieRepository the movie repository for local inserts and deletes
     * @param rowId           the row id of the movie
     * @param useTwoPane      whether the view uses two panes or not
     */
    public MovieDetailsViewModelFavImpl(@Nullable Bundle savedState,
                                        @NonNull MovieRepository movieRepository, long rowId,
                                        boolean useTwoPane) {
        super(savedState, movieRepository, useTwoPane);

        mMovieRowId = rowId;

        if (savedState != null) {
            mRefreshing = savedState.getBoolean(STATE_REFRESHING);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_REFRESHING, mRefreshing);
    }

    @Override
    public void setMovie(@NonNull Movie movie) {
        super.setMovie(movie);

        notifyPropertyChanged(BR.movieTitle);
        notifyPropertyChanged(BR.movieBackdropPath);

        setYoutubeShareUrl();
        mView.notifyDataChanged();
        if (mRefreshing) {
            setRefreshing(false);
        }
    }

    @Override
    @Bindable
    public boolean isMovieFavoured() {
        return true;
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
    public boolean isDataSetAndNotReloading() {
        return mMovie != null && !mRefreshing;
    }

    @Override
    public void onMovieDataEmpty() {
        if (mRefreshing) {
            onMovieUpdateFailed();
        } else {
            mView.startPostponedEnterTransition();
            mView.showMessage(R.string.snackbar_movie_no_data, null);
        }
    }

    private void onMovieUpdateFailed() {
        setRefreshing(false);
        mView.showMessage(R.string.snackbar_movie_update_failed, new SnackbarAction(R.string.snackbar_retry) {
            @Override
            public void onClick(View v) {
                mView.loadUpdateMovieDetailsWorker(mMovie.getDbId(), mMovieRowId);
            }
        });
    }

    @Override
    public void onMovieDeleted() {
        super.onMovieDeleted();

        if (mUseTwoPane) {
            mView.hideDetailsView();
        }
    }

    @Override
    protected void onMovieDeletedOnePane() {
        mView.finishScreen();
    }

    @Override
    public void setUpdateMovieDetailsStream(@NonNull Observable<ContentProviderResult[]> observable, @NonNull final String workerTag) {
        mSubscriptions.add(observable.subscribe(new Subscriber<ContentProviderResult[]>() {
            @Override
            public void onCompleted() {
                mView.removeWorker(workerTag);
            }

            @Override
            public void onError(Throwable e) {
                mView.removeWorker(workerTag);
                onMovieUpdateFailed();
            }

            @Override
            public void onNext(ContentProviderResult[] contentProviderResults) {
                mView.restartLoader();
            }
        }));
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        super.onWorkerError(workerTag);

        onMovieUpdateFailed();
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefreshing(true);
                mView.loadUpdateMovieDetailsWorker(mMovie.getDbId(), mMovieRowId);
            }
        };
    }
}
