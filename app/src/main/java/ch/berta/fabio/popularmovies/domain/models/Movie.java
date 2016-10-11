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
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a movie, queried from TheMovieDB.
 */
public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("id")
    private int dbId;
    private String overview;
    @SerializedName("release_date")
    private Date releaseDate;
    @SerializedName("poster_path")
    private String posterPath;
    private String title;
    @SerializedName("vote_average")
    private double voteAverage;
    private boolean favoured;
    private List<Review> reviews;
    private List<Video> videos;
    private boolean reviewsAndVideosSet;

    public Movie() {
    }

    public Movie(@NonNull List<Video> videos, @NonNull String backdropPath, int dbId,
                 @NonNull String overview, @NonNull Date releaseDate,
                 @NonNull String posterPath, @NonNull String title, double voteAverage,
                 @NonNull List<Review> reviews, boolean favoured) {
        this.videos = videos;
        this.backdropPath = backdropPath;
        this.dbId = dbId;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.title = title;
        this.voteAverage = voteAverage;
        this.reviews = reviews;
        this.favoured = favoured;
        reviewsAndVideosSet = true;
    }

    protected Movie(Parcel in) {
        backdropPath = in.readString();
        dbId = in.readInt();
        overview = in.readString();
        long tmpMReleaseDate = in.readLong();
        releaseDate = tmpMReleaseDate == -1 ? null : new Date(tmpMReleaseDate);
        posterPath = in.readString();
        title = in.readString();
        voteAverage = in.readDouble();
        favoured = in.readByte() != 0;
        reviews = in.createTypedArrayList(Review.CREATOR);
        videos = in.createTypedArrayList(Video.CREATOR);
        reviewsAndVideosSet = in.readByte() != 0;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(@NonNull String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(@NonNull String overview) {
        this.overview = overview;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(@NonNull Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(@NonNull String posterPath) {
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

    public boolean isFavoured() {
        return favoured;
    }

    public void setIsFavoured(boolean isFavoured) {
        favoured = isFavoured;
    }

    @NonNull
    public List<Review> getReviews() {
        return reviews != null ? reviews : Collections.<Review>emptyList();
    }

    public void setReviews(@NonNull List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    public List<Video> getVideos() {
        return videos != null ? videos : Collections.<Video>emptyList();
    }

    public void setVideos(@NonNull List<Video> videos) {
        this.videos = videos;
    }

    public boolean areReviewsAndVideosSet() {
        return reviewsAndVideosSet;
    }

    public void setReviewsAndVideosSet(boolean reviewsAndVideosSet) {
        this.reviewsAndVideosSet = reviewsAndVideosSet;
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
        dest.writeInt(dbId);
        dest.writeString(overview);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : -1);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeDouble(voteAverage);
        dest.writeByte(favoured ? (byte) 1 : (byte) 0);
        dest.writeTypedList(reviews);
        dest.writeTypedList(videos);
        dest.writeByte(reviewsAndVideosSet ? (byte) 1 : (byte) 0);
    }
}
