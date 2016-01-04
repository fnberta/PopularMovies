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

package ch.berta.fabio.popularmovies.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

import java.util.Date;

import ch.berta.fabio.popularmovies.data.models.Movie;

/**
 * Defines a view model for the movie row.
 * <p/>
 * extends {@link Observable}.
 */
public interface MovieRowViewModel extends Observable {
    /**
     * Sets the info of the movie (title, release date and poster path) to the according fields.
     *
     * @param movie the movie to set
     */
    void setMovieInfo(@NonNull Movie movie);

    /**
     * Sets the movie title, release date and poster path.
     *
     * @param movieTitle       the movie title to set
     * @param movieReleaseDate the movie relesae date to set
     * @param moviePosterPath  the movie poster path to set
     */
    void setMovieInfo(@NonNull String movieTitle, @NonNull Date movieReleaseDate,
                      @NonNull String moviePosterPath);

    @Bindable
    String getMovieTitle();

    void setMovieTitle(String movieTitle);

    @Bindable
    Date getMovieReleaseDate();

    void setMovieReleaseDate(Date movieReleaseDate);

    @Bindable
    String getMoviePosterPath();

    void setMoviePosterPath(String moviePosterPath);

    @Bindable
    int getMoviePosterHeight();

    void setMoviePosterHeight(int moviePosterHeight);
}
