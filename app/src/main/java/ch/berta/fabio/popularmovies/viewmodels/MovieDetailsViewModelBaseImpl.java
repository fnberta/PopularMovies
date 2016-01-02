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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;

/**
 * Provides an abstract base class that implements {@link MovieDetailsViewModel}.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public abstract class MovieDetailsViewModelBaseImpl<T extends MovieDetailsViewModel.ViewInteractionListener>
        extends BaseObservable
        implements MovieDetailsViewModel<T> {

    T mView;
    Movie mMovie;
    int mReviewsCount;
    int mVideosCount;
    long mMovieRowId;
    final boolean mUseTwoPane;

    /**
     * Constructs a new {@link MovieDetailsViewModelBaseImpl}.
     *
     * @param useTwoPane whether the view is using two panes or not
     */
    public MovieDetailsViewModelBaseImpl(boolean useTwoPane) {
        mUseTwoPane = useTwoPane;
    }

    protected MovieDetailsViewModelBaseImpl(Parcel in) {
        mMovie = in.readParcelable(Movie.class.getClassLoader());
        mReviewsCount = in.readInt();
        mVideosCount = in.readInt();
        mMovieRowId = in.readLong();
        mUseTwoPane = in.readByte() != 0;
    }

    @Override
    public void attachView(T view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public Movie getMovie() {
        return mMovie;
    }

    @Override
    public void setMovie(@NonNull Movie movie) {
        mMovie = movie;
        setReviewsAndVideosCount();
    }

    final void setReviewsAndVideosCount() {
        mReviewsCount = mMovie.getReviews().size();
        mVideosCount = mMovie.getVideos().size();
    }

    @Override
    @Bindable
    public String getMovieTitle() {
        return mMovie != null ? mMovie.getTitle() : "";
    }

    @Override
    @Bindable
    public String getMovieBackdropPath() {
        return mMovie != null ? mMovie.getBackdropPath() : "";
    }

    @Override
    public void setMovieFavoured(boolean isFavoured) {
        mMovie.setIsFavoured(isFavoured);
        notifyPropertyChanged(BR.movieFavoured);
    }

    @Override
    public long getMovieRowId() {
        return mMovieRowId;
    }

    @Override
    public void setMovieRowId(long rowId) {
        mMovieRowId = rowId;
    }

    @Override
    public void onMovieInserted() {
        mView.showSnackbar(R.string.snackbar_added_to_favorites, null);
    }

    @Override
    @CallSuper
    public void onMovieDeleted() {
        if (mUseTwoPane) {
            mView.showSnackbar(R.string.snackbar_removed_from_favorites, null);
        } else {
            onMovieDeletedOnePane();
        }
    }

    protected abstract void onMovieDeletedOnePane();

    @Override
    @CallSuper
    public void onMovieUpdated() {
        // empty default implementation
    }

    @Override
    public void onLocalOperationFailed() {
        mView.showSnackbar(R.string.snackbar_local_operation_failed, null);
    }

    @Override
    public boolean hasMovieVideos() {
        return mMovie != null && !mMovie.getVideos().isEmpty();
    }

    @Override
    public Video getMovieVideoAtPosition(int position) {
        // position - video header and review header if there are reviews - reviews if there are any - info and two pane header if present
        return mMovie.getVideos().get(position - getNumberOfHeaderRows() - mReviewsCount - adjustPosForTwoPane(1));
    }

    @Override
    public Review getMovieReviewAtPosition(int position) {
        // position - review header - info and two pane header if present
        return mMovie.getReviews().get(position - 1 - adjustPosForTwoPane(1));
    }

    final int getNumberOfHeaderRows() {
        int headerRows = 2;
        if (mReviewsCount == 0) {
            headerRows--;
        }
        if (mVideosCount == 0) {
            headerRows--;
        }
        return headerRows;
    }

    final int adjustPosForTwoPane(int position) {
        return mUseTwoPane ? position + 1 : position;
    }

    @Override
    public boolean isMovieReviewLastPosition(@NonNull Review review) {
        return mMovie.getReviews().indexOf(review) == mReviewsCount - 1;
    }

    @Override
    public int getHeaderTitle(int position) {
        if (position != adjustPosForTwoPane(1)) {
            return R.string.header_trailers;
        } else if (mReviewsCount == 0) {
            return R.string.header_trailers;
        } else {
            return R.string.header_reviews;
        }
    }

    @Override
    public int getItemCount() {
        if (mMovie == null) {
            return 0;
        }

        final int count = 1 + mReviewsCount + mVideosCount + getNumberOfHeaderRows();
        return adjustPosForTwoPane(count);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if (mUseTwoPane) {
                return TYPE_TWO_PANE_HEADER;
            } else {
                return TYPE_INFO;
            }
        }

        if (position == 1) {
            if (mUseTwoPane) {
                return TYPE_INFO;
            } else {
                return TYPE_HEADER;
            }
        }

        if (position == 2 && mUseTwoPane) {
            return TYPE_HEADER;
        }

        if (mReviewsCount > 0) {
            final int firstReviewPos = adjustPosForTwoPane(2);
            if (position >= firstReviewPos && position < firstReviewPos + mReviewsCount) {
                return TYPE_REVIEW;
            }

            if (position == firstReviewPos + mReviewsCount) {
                return TYPE_HEADER;
            }
        }

        return TYPE_VIDEO;
    }

    @Override
    public void onFabClick(View view) {
        if (mMovie.isFavoured()) {
            setMovieFavoured(false);
            mView.deleteMovieLocal();
        } else {
            setMovieFavoured(true);
            mView.insertMovieLocal();
        }
    }

    @Override
    public void onPosterLoaded() {
        mView.startPostponedEnterTransition();
    }

    @Override
    public void onVideoRowItemClick(int position) {
        Video video = getMovieVideoAtPosition(position);
        if (video.siteIsYouTube()) {
            mView.startVideoActivity(Uri.parse(video.getYoutubeUrl()));
        }
    }

    final void setYoutubeShareUrl() {
        if (hasMovieVideos()) {
            final Video firstVideo = mMovie.getVideos().get(0);
            final String url = firstVideo.getYoutubeUrl();
            mView.setYoutubeShareUrl(url);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @CallSuper
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mMovie, 0);
        dest.writeInt(mReviewsCount);
        dest.writeInt(mVideosCount);
        dest.writeLong(mMovieRowId);
        dest.writeByte(mUseTwoPane ? (byte) 1 : (byte) 0);
    }
}
