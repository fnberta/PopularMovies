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
    private String mBackdropPath;
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
    @SerializedName("genres")
    private List<Genre> mGenres = new ArrayList<>();
    @SerializedName("id")
    private int mDbId;
    @SerializedName("reviews")
    private ReviewsPage mReviewsPage;
    @SerializedName("videos")
    private VideosPage mVideosPage;

    public MovieDetails() {
    }

    protected MovieDetails(Parcel in) {
        mBackdropPath = in.readString();
        mOverview = in.readString();
        long tmpMReleaseDate = in.readLong();
        mReleaseDate = tmpMReleaseDate == -1 ? null : new Date(tmpMReleaseDate);
        mPosterPath = in.readString();
        mTitle = in.readString();
        mVoteAverage = in.readDouble();
        mGenres = in.createTypedArrayList(Genre.CREATOR);
        mDbId = in.readInt();
        mReviewsPage = in.readParcelable(ReviewsPage.class.getClassLoader());
        mVideosPage = in.readParcelable(VideosPage.class.getClassLoader());
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
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

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
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

    public List<Genre> getGenres() {
        return mGenres;
    }

    public void setGenres(List<Genre> genres) {
        mGenres = genres;
    }

    public int getDbId() {
        return mDbId;
    }

    public void setDbId(int dbId) {
        mDbId = dbId;
    }

    public ReviewsPage getReviewsPage() {
        return mReviewsPage;
    }

    public void setReviewsPage(ReviewsPage reviewsPage) {
        mReviewsPage = reviewsPage;
    }

    public VideosPage getVideosPage() {
        return mVideosPage;
    }

    public void setVideosPage(VideosPage videosPage) {
        mVideosPage = videosPage;
    }

    /**
     * Returns a {@link ContentValues} object with the movie's data.
     *
     * @return a {@link ContentValues} object with the movie's data
     */
    public ContentValues getContentValuesEntry() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.Movie.COLUMN_DB_ID, mDbId);
        contentValues.put(MovieContract.Movie.COLUMN_TITLE, mTitle);
        contentValues.put(MovieContract.Movie.COLUMN_RELEASE_DATE, mReleaseDate.getTime());
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
        dest.writeString(mBackdropPath);
        dest.writeString(mOverview);
        dest.writeLong(mReleaseDate != null ? mReleaseDate.getTime() : -1);
        dest.writeString(mPosterPath);
        dest.writeString(mTitle);
        dest.writeDouble(mVoteAverage);
        dest.writeTypedList(mGenres);
        dest.writeInt(mDbId);
        dest.writeParcelable(mReviewsPage, 0);
        dest.writeParcelable(mVideosPage, 0);
    }
}
