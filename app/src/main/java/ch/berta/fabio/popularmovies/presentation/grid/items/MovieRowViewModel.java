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

package ch.berta.fabio.popularmovies.presentation.grid.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.presentation.common.rows.MovieRow;

/**
 * Provides a view model for the movie row.
 * <p/>
 * extends {@link Observable}.
 */
public class MovieRowViewModel extends BaseObservable {

    private String mMovieTitle;
    private Date mMovieReleaseDate;
    private String mMoviePosterPath;
    private int mMoviePosterHeight;

    /**
     * Constructs a new {@link MovieRow} form a {@link Movie} object.
     *
     * @param movie             the movie object to use
     * @param moviePosterHeight the height of the poster image
     */
    public MovieRowViewModel(@NonNull Movie movie, int moviePosterHeight) {
        this(movie.getTitle(), movie.getReleaseDate(), movie.getPosterPath(), moviePosterHeight);
    }

    /**
     * Constructs a new {@link MovieRowViewModel} from individual strings.
     *
     * @param movieTitle        the movie title to use
     * @param movieReleaseDate  the movie release date to use
     * @param moviePosterPath   the movie poster path to use
     * @param moviePosterHeight the height of the poster image
     */
    public MovieRowViewModel(@NonNull String movieTitle, @NonNull Date movieReleaseDate,
                             @NonNull String moviePosterPath, int moviePosterHeight) {
        setMovieInfo(movieTitle, movieReleaseDate, moviePosterPath);
        setMoviePosterHeight(moviePosterHeight);
    }

    @BindingAdapter({"moviePosterHeight"})
    public static void setMoviePosterHeight(View view, int moviePosterHeight) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = moviePosterHeight;
        view.setLayoutParams(layoutParams);
    }

    /**
     * Sets the info of the movie (title, release date and poster path) to the according fields.
     *
     * @param movie the movie to set
     */
    public void setMovieInfo(@NonNull Movie movie) {
        setMovieInfo(movie.getTitle(), movie.getReleaseDate(), movie.getPosterPath());
    }

    /**
     * Sets the movie title, release date and poster path.
     *
     * @param movieTitle       the movie title to set
     * @param movieReleaseDate the movie relesae date to set
     * @param moviePosterPath  the movie poster path to set
     */
    public void setMovieInfo(@NonNull String movieTitle, @NonNull Date movieReleaseDate,
                             @NonNull String moviePosterPath) {
        setMovieTitle(movieTitle);
        setMovieReleaseDate(movieReleaseDate);
        setMoviePosterPath(moviePosterPath);
    }

    @Bindable
    public String getMovieTitle() {
        return mMovieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        mMovieTitle = movieTitle;
        notifyPropertyChanged(BR.movieTitle);
    }

    @Bindable
    public Date getMovieReleaseDate() {
        return mMovieReleaseDate;
    }

    public void setMovieReleaseDate(Date movieReleaseDate) {
        mMovieReleaseDate = movieReleaseDate;
        notifyPropertyChanged(BR.movieReleaseDate);
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
    public int getMoviePosterHeight() {
        return mMoviePosterHeight;
    }

    public void setMoviePosterHeight(int moviePosterHeight) {
        mMoviePosterHeight = moviePosterHeight;
        notifyPropertyChanged(BR.moviePosterHeight);
    }
}
