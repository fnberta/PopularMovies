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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a movie, queried from TheMovieDB.
 */
public class MovieDetails implements Parcelable {

    public static final Parcelable.Creator<MovieDetails> CREATOR = new Parcelable.Creator<MovieDetails>() {
        public MovieDetails createFromParcel(Parcel source) {
            return new MovieDetails(source);
        }

        public MovieDetails[] newArray(int size) {
            return new MovieDetails[size];
        }
    };
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
        mGenres = new ArrayList<>();
        in.readList(mGenres, List.class.getClassLoader());
        mDbId = in.readInt();
        mReviewsPage = in.readParcelable(ReviewsPage.class.getClassLoader());
        mVideosPage = in.readParcelable(VideosPage.class.getClassLoader());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(mGenres);
        dest.writeInt(mDbId);
        dest.writeParcelable(mReviewsPage, 0);
        dest.writeParcelable(mVideosPage, 0);
    }
}
