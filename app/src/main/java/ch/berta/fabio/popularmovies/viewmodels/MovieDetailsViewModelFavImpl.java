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

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.SnackbarAction;

/**
 * Provides an implementation of the {@link MovieDetailsViewModelFav} interface.
 * <p/>
 * Subclass of {@link MovieDetailsViewModelBaseImpl}.
 */
public class MovieDetailsViewModelFavImpl extends
        MovieDetailsViewModelBaseImpl<MovieDetailsViewModelFav.ViewInteractionListener> implements
        MovieDetailsViewModelFav {

    public static final Creator<MovieDetailsViewModelFavImpl> CREATOR = new Creator<MovieDetailsViewModelFavImpl>() {
        public MovieDetailsViewModelFavImpl createFromParcel(Parcel source) {
            return new MovieDetailsViewModelFavImpl(source);
        }

        public MovieDetailsViewModelFavImpl[] newArray(int size) {
            return new MovieDetailsViewModelFavImpl[size];
        }
    };
    private boolean mRefreshing;

    /**
     * Constructs a new {@link MovieDetailsViewModelFavImpl}.
     *
     * @param rowId      the row id of the movie
     * @param useTwoPane whether the view uses two panes or not
     */
    public MovieDetailsViewModelFavImpl(long rowId, boolean useTwoPane) {
        super(useTwoPane);

        mMovieRowId = rowId;
    }

    protected MovieDetailsViewModelFavImpl(Parcel in) {
        super(in);
        mRefreshing = in.readByte() != 0;
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
            mView.showSnackbar(R.string.snackbar_movie_no_data, null);
        }
    }

    private void onMovieUpdateFailed() {
        setRefreshing(false);
        mView.showSnackbar(R.string.snackbar_movie_update_failed, new SnackbarAction(R.string.snackbar_retry) {
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
            mView.hideDetailsScreen();
        }
    }

    @Override
    protected void onMovieDeletedOnePane() {
        mView.finishScreen();
    }

    @Override
    public void onMovieDetailsUpdated() {
        mView.removeUpdateMovieDetailsWorker();
        mView.restartLoader();
    }

    @Override
    public void onMovieDetailsUpdateFailed() {
        mView.removeUpdateMovieDetailsWorker();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(mRefreshing ? (byte) 1 : (byte) 0);
    }
}
