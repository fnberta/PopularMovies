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

package ch.berta.fabio.popularmovies.presentation.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.Date;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsInteractionListener;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides a view model for the movie details info row.
 * <p/>
 * Extends {@link Observable}.
 */
public class DetailsInfoRowViewModel extends BaseObservable {

    private final int mPlotMaxLines;
    private String mMoviePosterPath;
    private String mMoviePlot;
    private Date mMovieDate;
    private double mMovieRating;
    private boolean mTransitionEnabled;

    /**
     * Constructs a new {@link DetailsInfoRowViewModel}.
     *
     * @param movie        the movie to use
     * @param plotMaxLines the maximum lines of plot info to show
     */
    public DetailsInfoRowViewModel(@NonNull Movie movie, int plotMaxLines) {
        setMoviePosterPath(movie.getPosterPath());
        setMoviePlot(movie.getOverview());
        setMovieDate(movie.getReleaseDate());
        setMovieRating(movie.getVoteAverage());
        setTransitionEnabled(true);
        mPlotMaxLines = plotMaxLines;
    }

    @BindingAdapter({"imageUrl", "fallback", "loadedListener"})
    public static void loadMoviePosterWithCallback(ImageView view, String imageUrl,
                                                   Drawable fallback,
                                                   final MovieDetailsInteractionListener listener) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .asBitmap()
                .error(fallback)
                .into(new BitmapImageViewTarget(view) {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                        listener.onPosterLoaded();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);

                        listener.onPosterLoaded();
                    }
                });
    }

    @Bindable
    public String getMoviePosterPath() {
        return mMoviePosterPath;
    }

    public void setMoviePosterPath(String moviePosterPath) {
        mMoviePosterPath = moviePosterPath;
        notifyPropertyChanged(BR.moviePosterPath);
    }

    @Bindable
    public String getMoviePlot() {
        return mMoviePlot;
    }

    public void setMoviePlot(String moviePlot) {
        mMoviePlot = moviePlot;
        notifyPropertyChanged(BR.moviePlot);
    }

    @Bindable
    public Date getMovieDate() {
        return mMovieDate;
    }

    public void setMovieDate(Date movieDate) {
        mMovieDate = movieDate;
        notifyPropertyChanged(BR.movieDate);
    }

    @Bindable
    public double getMovieRating() {
        return mMovieRating;
    }

    public void setMovieRating(double movieRating) {
        mMovieRating = movieRating;
        notifyPropertyChanged(BR.movieRating);
    }

    @Bindable
    public boolean isTransitionEnabled() {
        return mTransitionEnabled;
    }

    public void setTransitionEnabled(boolean transitionEnabled) {
        mTransitionEnabled = transitionEnabled;
        notifyPropertyChanged(BR.transitionEnabled);
    }

    public void onPlotClick(View view) {
        Utils.expandOrCollapseTextView((TextView) view, mPlotMaxLines);
    }
}
