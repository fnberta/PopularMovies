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
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides a view model for the movie details info row.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public class DetailsInfoRowViewModel extends BaseObservable {

    private String mMoviePosterPath;
    private String mMoviePlot;
    private Date mMovieDate;
    private double mMovieRating;
    private final int mPlotMaxLines;
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
