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

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a movie, queried from TheMovieDB.
 */
public class MovieDetails implements Parcelable {

    public static final Creator<MovieDetails> CREATOR = new Creator<MovieDetails>() {
        public MovieDetails createFromParcel(Parcel source) {
            return new MovieDetails(source);
        }

        public MovieDetails[] newArray(int size) {
            return new MovieDetails[size];
        }
    };
    @SerializedName("backdrop_path")
    private String backdropPath;
    private String overview;
    @SerializedName("release_date")
    private Date releaseDate;
    @SerializedName("poster_path")
    private String posterPath;
    private String title;
    @SerializedName("vote_average")
    private double voteAverage;
    private List<Genre> genres = new ArrayList<>();
    @SerializedName("id")
    private int dbId;
    @SerializedName("reviews")
    private ReviewsPage reviewsPage;
    @SerializedName("videos")
    private VideosPage videosPage;

    public MovieDetails() {
    }

    protected MovieDetails(Parcel in) {
        backdropPath = in.readString();
        overview = in.readString();
        long tmpMReleaseDate = in.readLong();
        releaseDate = tmpMReleaseDate == -1 ? null : new Date(tmpMReleaseDate);
        posterPath = in.readString();
        title = in.readString();
        voteAverage = in.readDouble();
        genres = in.createTypedArrayList(Genre.CREATOR);
        dbId = in.readInt();
        reviewsPage = in.readParcelable(ReviewsPage.class.getClassLoader());
        videosPage = in.readParcelable(VideosPage.class.getClassLoader());
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public ReviewsPage getReviewsPage() {
        return reviewsPage;
    }

    public void setReviewsPage(ReviewsPage reviewsPage) {
        this.reviewsPage = reviewsPage;
    }

    public VideosPage getVideosPage() {
        return videosPage;
    }

    public void setVideosPage(VideosPage videosPage) {
        this.videosPage = videosPage;
    }

    /**
     * Returns a {@link ContentValues} object with the movie's data.
     *
     * @return a {@link ContentValues} object with the movie's data
     */
    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Movie.COLUMN_DB_ID, dbId);
        contentValues.put(MovieContract.Movie.COLUMN_TITLE, title);
        contentValues.put(MovieContract.Movie.COLUMN_RELEASE_DATE, releaseDate.getTime());
        contentValues.put(MovieContract.Movie.COLUMN_VOTE_AVERAGE, voteAverage);
        contentValues.put(MovieContract.Movie.COLUMN_PLOT, overview);
        contentValues.put(MovieContract.Movie.COLUMN_POSTER, posterPath);
        contentValues.put(MovieContract.Movie.COLUMN_BACKDROP, backdropPath);
        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(backdropPath);
        dest.writeString(overview);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : -1);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeDouble(voteAverage);
        dest.writeTypedList(genres);
        dest.writeInt(dbId);
        dest.writeParcelable(reviewsPage, 0);
        dest.writeParcelable(videosPage, 0);
    }
}
