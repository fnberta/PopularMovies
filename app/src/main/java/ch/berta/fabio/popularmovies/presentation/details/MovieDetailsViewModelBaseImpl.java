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

package ch.berta.fabio.popularmovies.presentation.details;

import android.content.ContentProviderResult;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.models.Review;
import ch.berta.fabio.popularmovies.domain.models.Video;
import ch.berta.fabio.popularmovies.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides an abstract base class that implements {@link MovieDetailsViewModel}.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public abstract class MovieDetailsViewModelBaseImpl<T extends MovieDetailsViewModel.ViewInteractionListener>
        extends ViewModelBaseImpl<T>
        implements MovieDetailsViewModel<T> {

    private static final String STATE_REVIEWS_COUNT = "STATE_REVIEWS_COUNT";
    private static final String STATE_VIDEOS_COUNT = "STATE_VIDEOS_COUNT";
    private static final String STATE_MOVIE_ROW_ID = "STATE_MOVIE_ROW_ID";
    protected final boolean useTwoPane;
    @Inject
    protected MovieRepository movieRepo;
    protected Movie movie;
    protected int reviewsCount;
    protected int videosCount;
    protected long movieRowId;

    /**
     * Constructs a new {@link MovieDetailsViewModelBaseImpl}.
     *
     * @param savedState      the saved state of the view model
     * @param movieRepository the movie repository for local inserts and deletes
     * @param useTwoPane      whether the view is using two panes or not
     */
    public MovieDetailsViewModelBaseImpl(@Nullable Bundle savedState,
                                         @NonNull MovieRepository movieRepository, boolean useTwoPane) {
        movieRepo = movieRepository;
        this.useTwoPane = useTwoPane;

        if (savedState != null) {
            reviewsCount = savedState.getInt(STATE_REVIEWS_COUNT);
            videosCount = savedState.getInt(STATE_VIDEOS_COUNT);
            movieRowId = savedState.getLong(STATE_MOVIE_ROW_ID);
        }
    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        outState.putInt(STATE_REVIEWS_COUNT, reviewsCount);
        outState.putInt(STATE_VIDEOS_COUNT, videosCount);
        outState.putLong(STATE_MOVIE_ROW_ID, movieRowId);
    }

    @Override
    public Movie getMovie() {
        return movie;
    }

    @Override
    public void setMovie(@NonNull Movie movie) {
        this.movie = movie;
        setReviewsAndVideosCount();
    }

    protected final void setReviewsAndVideosCount() {
        reviewsCount = movie.getReviews().size();
        videosCount = movie.getVideos().size();
    }

    @Override
    @Bindable
    public String getMovieTitle() {
        return movie != null ? movie.getTitle() : "";
    }

    @Override
    @Bindable
    public String getMovieBackdropPath() {
        return movie != null ? movie.getBackdropPath() : "";
    }

    @Override
    public void setMovieFavoured(boolean isFavoured) {
        movie.setIsFavoured(isFavoured);
        notifyPropertyChanged(BR.movieFavoured);
    }

    @Override
    public long getMovieRowId() {
        return movieRowId;
    }

    @Override
    public void setMovieRowId(long rowId) {
        movieRowId = rowId;
    }

    protected abstract void onMovieDeletedOnePane();

    @Override
    public boolean hasMovieVideos() {
        return movie != null && !movie.getVideos().isEmpty();
    }

    @Override
    public Video getMovieVideoAtPosition(int position) {
        // position - video header and review header if there are reviews - reviews if there are any - info and two pane header if present
        return movie.getVideos().get(position - getNumberOfHeaderRows() - reviewsCount - adjustPosForTwoPane(1));
    }

    @Override
    public Review getMovieReviewAtPosition(int position) {
        // position - review header - info and two pane header if present
        return movie.getReviews().get(position - 1 - adjustPosForTwoPane(1));
    }

    protected final int getNumberOfHeaderRows() {
        int headerRows = 2;
        if (reviewsCount == 0) {
            headerRows--;
        }
        if (videosCount == 0) {
            headerRows--;
        }
        return headerRows;
    }

    protected final int adjustPosForTwoPane(int position) {
        return useTwoPane ? position + 1 : position;
    }

    @Override
    public boolean isMovieReviewLastPosition(@NonNull Review review) {
        return movie.getReviews().indexOf(review) == reviewsCount - 1;
    }

    @Override
    public int getHeaderTitle(int position) {
        if (position != adjustPosForTwoPane(1)) {
            return R.string.header_trailers;
        } else if (reviewsCount == 0) {
            return R.string.header_trailers;
        } else {
            return R.string.header_reviews;
        }
    }

    @Override
    public int getItemCount() {
        if (movie == null) {
            return 0;
        }

        final int count = 1 + reviewsCount + videosCount + getNumberOfHeaderRows();
        return adjustPosForTwoPane(count);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if (useTwoPane) {
                return TYPE_TWO_PANE_HEADER;
            } else {
                return TYPE_INFO;
            }
        }

        if (position == 1) {
            if (useTwoPane) {
                return TYPE_INFO;
            } else {
                return TYPE_HEADER;
            }
        }

        if (position == 2 && useTwoPane) {
            return TYPE_HEADER;
        }

        if (reviewsCount > 0) {
            final int firstReviewPos = adjustPosForTwoPane(2);
            if (position >= firstReviewPos && position < firstReviewPos + reviewsCount) {
                return TYPE_REVIEW;
            }

            if (position == firstReviewPos + reviewsCount) {
                return TYPE_HEADER;
            }
        }

        return TYPE_VIDEO;
    }

    @Override
    public void onFabClick(View view) {
        if (movie.isFavoured()) {
            setMovieFavoured(false);
            final Observable<Integer> observable = movieRepo.deleteMovieLocal(movieRowId);
            subscriptions.add(observable.subscribe(new Subscriber<Integer>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    MovieDetailsViewModelBaseImpl.this.view.showMessage(R.string.snackbar_movie_delete_failed, null);
                }

                @Override
                public void onNext(Integer integer) {
                    onMovieDeleted();
                }
            }));
        } else {
            setMovieFavoured(true);
            final Observable<ContentProviderResult[]> observable = movieRepo.insertMovieLocal(movie);
            subscriptions.add(observable.subscribe(new Subscriber<ContentProviderResult[]>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            MovieDetailsViewModelBaseImpl.this.view.showMessage(R.string.snackbar_movie_insert_failed, null);
                        }

                        @Override
                        public void onNext(ContentProviderResult[] contentProviderResults) {
                            MovieDetailsViewModelBaseImpl.this.view.showMessage(R.string.snackbar_movie_added_to_favorites, null);
                        }
                    })
            );
        }
    }

    @CallSuper
    protected void onMovieDeleted() {
        if (useTwoPane) {
            view.showMessage(R.string.snackbar_movie_removed_from_favorites, null);
        } else {
            onMovieDeletedOnePane();
        }
    }

    @Override
    public void onPosterLoaded() {
        view.startPostponedEnterTransition();
    }

    @Override
    public void onVideoRowItemClick(int position) {
        Video video = getMovieVideoAtPosition(position);
        if (video.siteIsYouTube()) {
            view.startVideoActivity(Uri.parse(video.getYoutubeUrl()));
        }
    }

    protected final void setYoutubeShareUrl() {
        if (hasMovieVideos()) {
            final Video firstVideo = movie.getVideos().get(0);
            final String url = firstVideo.getYoutubeUrl();
            view.setYoutubeShareUrl(url);
        }
    }
}
