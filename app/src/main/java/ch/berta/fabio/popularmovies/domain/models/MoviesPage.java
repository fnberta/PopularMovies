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

package ch.berta.fabio.popularmovies.domain.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a results page from a /discover/movies query of TheMovieDB.
 */
public class MoviesPage {

    @SerializedName("page")
    private int mPage;
    @SerializedName("results")
    private List<Movie> mMovies = new ArrayList<>();
    @SerializedName("total_pages")
    private int mTotalPages;
    @SerializedName("total_Movies")
    private int mTotalMovies;

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public List<Movie> getMovies() {
        return mMovies;
    }

    public void setMovies(List<Movie> Movies) {
        mMovies = Movies;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getTotalMovies() {
        return mTotalMovies;
    }

    public void setTotalMovies(int totalMovies) {
        mTotalMovies = totalMovies;
    }

}
