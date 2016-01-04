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

package ch.berta.fabio.popularmovies.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
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
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.ui.adapters.listeners.MovieDetailsInteractionListener;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides an implementation of the {@link DetailsInfoRowViewModel} interface.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public class DetailsInfoRowViewModelImpl extends BaseObservable implements DetailsInfoRowViewModel {

    private final int mPlotMaxLines;
    private String mMoviePosterPath;
    private String mMoviePlot;
    private Date mMovieDate;
    private double mMovieRating;
    private boolean mTransitionEnabled;

    /**
     * Constructs a new {@link DetailsInfoRowViewModelImpl}.
     *
     * @param movie        the movie to use
     * @param plotMaxLines the maximum lines of plot info to show
     */
    public DetailsInfoRowViewModelImpl(@NonNull Movie movie, int plotMaxLines) {
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

    @Override
    @Bindable
    public String getMoviePosterPath() {
        return mMoviePosterPath;
    }

    @Override
    public void setMoviePosterPath(String moviePosterPath) {
        mMoviePosterPath = moviePosterPath;
        notifyPropertyChanged(BR.moviePosterPath);
    }

    @Override
    @Bindable
    public String getMoviePlot() {
        return mMoviePlot;
    }

    @Override
    public void setMoviePlot(String moviePlot) {
        mMoviePlot = moviePlot;
        notifyPropertyChanged(BR.moviePlot);
    }

    @Override
    @Bindable
    public Date getMovieDate() {
        return mMovieDate;
    }

    @Override
    public void setMovieDate(Date movieDate) {
        mMovieDate = movieDate;
        notifyPropertyChanged(BR.movieDate);
    }

    @Override
    @Bindable
    public double getMovieRating() {
        return mMovieRating;
    }

    @Override
    public void setMovieRating(double movieRating) {
        mMovieRating = movieRating;
        notifyPropertyChanged(BR.movieRating);
    }

    @Override
    @Bindable
    public boolean isTransitionEnabled() {
        return mTransitionEnabled;
    }

    @Override
    public void setTransitionEnabled(boolean transitionEnabled) {
        mTransitionEnabled = transitionEnabled;
        notifyPropertyChanged(BR.transitionEnabled);
    }

    @Override
    public void onPlotClick(View view) {
        Utils.expandOrCollapseTextView((TextView) view, mPlotMaxLines);
    }
}
