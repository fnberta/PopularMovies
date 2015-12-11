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

package ch.berta.fabio.popularmovies.data.models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;

/**
 * Represents a movie, queried from TheMovieDB.
 */
public class Movie implements Parcelable {

    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String IMAGE_POSTER_SIZE = "w185";
    public static final String IMAGE_BACKDROP_SIZE = "w780";
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    @SerializedName("backdrop_path")
    private String mBackdropPath;
    @SerializedName("id")
    private int mDbId;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("release_date")
    private Date mReleaseDate;
    @SerializedName("poster_path")
    private String mPosterPath;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("vote_average")
    private double mVoteAverage;
    private boolean mIsFavoured;
    private List<Review> mReviews;
    private List<Video> mVideos;

    public Movie() {
    }

    public Movie(@NonNull List<Video> videos, @NonNull String backdropPath, int dbId,
                 @NonNull String overview, @NonNull Date releaseDate,
                 @NonNull String posterPath, @NonNull String title, double voteAverage,
                 @NonNull List<Review> reviews) {
        mVideos = videos;
        mBackdropPath = backdropPath;
        mDbId = dbId;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mTitle = title;
        mVoteAverage = voteAverage;
        mReviews = reviews;
    }

    protected Movie(Parcel in) {
        mBackdropPath = in.readString();
        mDbId = in.readInt();
        mOverview = in.readString();
        long tmpMReleaseDate = in.readLong();
        mReleaseDate = tmpMReleaseDate == -1 ? null : new Date(tmpMReleaseDate);
        mPosterPath = in.readString();
        mTitle = in.readString();
        mVoteAverage = in.readDouble();
        mIsFavoured = in.readByte() != 0;
        mReviews = in.createTypedArrayList(Review.CREATOR);
        mVideos = in.createTypedArrayList(Video.CREATOR);
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(@NonNull String backdropPath) {
        mBackdropPath = backdropPath;
    }

    public int getDbId() {
        return mDbId;
    }

    public void setDbId(int dbId) {
        mDbId = dbId;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(@NonNull String overview) {
        mOverview = overview;
    }

    public Date getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(@NonNull Date releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(@NonNull String posterPath) {
        mPosterPath = posterPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        mVoteAverage = voteAverage;
    }

    public boolean isFavoured() {
        return mIsFavoured;
    }

    public void setIsFavoured(boolean isFavoured) {
        mIsFavoured = isFavoured;
    }

    public List<Review> getReviews() {
        return mReviews;
    }

    public void setReviews(@NonNull List<Review> reviews) {
        mReviews = reviews;
    }

    public List<Video> getVideos() {
        return mVideos;
    }

    public void setVideos(@NonNull List<Video> videos) {
        mVideos = videos;
    }

    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Movie.COLUMN_DB_ID, mDbId);
        contentValues.put(MovieContract.Movie.COLUMN_TITLE, mTitle);
        contentValues.put(MovieContract.Movie.COLUMN_RELEASE_DATE, getReleaseDateAsLong());
        contentValues.put(MovieContract.Movie.COLUMN_VOTE_AVERAGE, mVoteAverage);
        contentValues.put(MovieContract.Movie.COLUMN_PLOT, mOverview);
        contentValues.put(MovieContract.Movie.COLUMN_POSTER, mPosterPath);
        contentValues.put(MovieContract.Movie.COLUMN_BACKDROP, mBackdropPath);
        return contentValues;
    }

    public String getReleaseDateFormatted(boolean showLong) {
        Date date = getReleaseDate();
        if (date == null) {
            return "";
        }

        return showLong ? Utils.formatDateLong(date) : Utils.formatDateShort(date);
    }

    public long getReleaseDateAsLong() {
        return mReleaseDate.getTime();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBackdropPath);
        dest.writeInt(mDbId);
        dest.writeString(mOverview);
        dest.writeLong(mReleaseDate != null ? mReleaseDate.getTime() : -1);
        dest.writeString(mPosterPath);
        dest.writeString(mTitle);
        dest.writeDouble(mVoteAverage);
        dest.writeByte(mIsFavoured ? (byte) 1 : (byte) 0);
        dest.writeTypedList(mReviews);
        dest.writeTypedList(mVideos);
    }
}
