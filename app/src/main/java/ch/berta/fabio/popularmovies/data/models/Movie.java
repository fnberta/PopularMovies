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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
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

    @SerializedName("adult")
    private boolean mAdult;
    @SerializedName("backdrop_path")
    private String mBackdropPath;
    @SerializedName("genre_ids")
    private List<Integer> mGenreIds = new ArrayList<>();
    @SerializedName("id")
    private int mId;
    @SerializedName("original_language")
    private String mOriginalLanguage;
    @SerializedName("original_title")
    private String mOriginalTitle;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("release_date")
    private Date mReleaseDate;
    @SerializedName("poster_path")
    private String mPosterPath;
    @SerializedName("popularity")
    private double mPopularity;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("video")
    private boolean mVideo;
    @SerializedName("vote_average")
    private double mVoteAverage;
    @SerializedName("vote_count")
    private int mVoteCount;
    private boolean mIsFavoured;

    public Movie() {
    }

    public Movie(String backdropPath, int id, String overview, Date releaseDate, String posterPath,
                 String title, double voteAverage, boolean isFavoured) {
        mBackdropPath = backdropPath;
        mId = id;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mTitle = title;
        mVoteAverage = voteAverage;
        mIsFavoured = isFavoured;
    }

    protected Movie(Parcel in) {
        mAdult = in.readByte() != 0;
        mBackdropPath = in.readString();
        mGenreIds = new ArrayList<>();
        in.readList(mGenreIds, List.class.getClassLoader());
        mId = in.readInt();
        mOriginalLanguage = in.readString();
        mOriginalTitle = in.readString();
        mOverview = in.readString();
        long tmpMReleaseDate = in.readLong();
        mReleaseDate = tmpMReleaseDate == -1 ? null : new Date(tmpMReleaseDate);
        mPosterPath = in.readString();
        mPopularity = in.readDouble();
        mTitle = in.readString();
        mVideo = in.readByte() != 0;
        mVoteAverage = in.readDouble();
        mVoteCount = in.readInt();
        mIsFavoured = in.readByte() != 0;
    }

    public boolean isAdult() {
        return mAdult;
    }

    public void setAdult(boolean adult) {
        mAdult = adult;
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
    }

    public List<Integer> getGenreIds() {
        return mGenreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        mGenreIds = genreIds;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getOriginalLanguage() {
        return mOriginalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        mOriginalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        mOriginalTitle = originalTitle;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public Date getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        mReleaseDate = releaseDate;
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

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public double getPopularity() {
        return mPopularity;
    }

    public void setPopularity(double popularity) {
        mPopularity = popularity;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isVideo() {
        return mVideo;
    }

    public void setVideo(boolean video) {
        mVideo = video;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        mVoteAverage = voteAverage;
    }

    public int getVoteCount() {
        return mVoteCount;
    }

    public void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    public boolean isFavoured() {
        return mIsFavoured;
    }

    public void setIsFavoured(boolean isFavoured) {
        mIsFavoured = isFavoured;
    }

    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Movie.COLUMN_DB_ID, mId);
        contentValues.put(MovieContract.Movie.COLUMN_TITLE, mTitle);
        contentValues.put(MovieContract.Movie.COLUMN_RELEASE_DATE, getReleaseDateAsLong());
        contentValues.put(MovieContract.Movie.COLUMN_VOTE_AVERAGE, mVoteAverage);
        contentValues.put(MovieContract.Movie.COLUMN_PLOT, mOverview);
        contentValues.put(MovieContract.Movie.COLUMN_POSTER, mPosterPath);
        contentValues.put(MovieContract.Movie.COLUMN_BACKDROP, mBackdropPath);
        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mAdult ? (byte) 1 : (byte) 0);
        dest.writeString(mBackdropPath);
        dest.writeList(mGenreIds);
        dest.writeInt(mId);
        dest.writeString(mOriginalLanguage);
        dest.writeString(mOriginalTitle);
        dest.writeString(mOverview);
        dest.writeLong(mReleaseDate != null ? mReleaseDate.getTime() : -1);
        dest.writeString(mPosterPath);
        dest.writeDouble(mPopularity);
        dest.writeString(mTitle);
        dest.writeByte(mVideo ? (byte) 1 : (byte) 0);
        dest.writeDouble(mVoteAverage);
        dest.writeInt(mVoteCount);
        dest.writeByte(mIsFavoured ? (byte) 1 : (byte) 0);
    }
}
