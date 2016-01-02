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
 * Represents a result page of reviews obtained from TheMovieDb.
 */
public class ReviewsPage implements Parcelable {

    public static final Parcelable.Creator<ReviewsPage> CREATOR = new Parcelable.Creator<ReviewsPage>() {
        public ReviewsPage createFromParcel(Parcel source) {
            return new ReviewsPage(source);
        }

        public ReviewsPage[] newArray(int size) {
            return new ReviewsPage[size];
        }
    };
    @SerializedName("page")
    private int mPage;
    @SerializedName("results")
    private List<Review> mReviews = new ArrayList<>();
    @SerializedName("totalPages")
    private int mTotalPages;
    @SerializedName("totalResults")
    private int mTotalResults;

    public ReviewsPage() {
    }

    protected ReviewsPage(Parcel in) {
        mPage = in.readInt();
        mReviews = in.createTypedArrayList(Review.CREATOR);
        mTotalPages = in.readInt();
        mTotalResults = in.readInt();
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public List<Review> getReviews() {
        return mReviews;
    }

    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getTotalResults() {
        return mTotalResults;
    }

    public void setTotalResults(int totalResults) {
        mTotalResults = totalResults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPage);
        dest.writeTypedList(mReviews);
        dest.writeInt(mTotalPages);
        dest.writeInt(mTotalResults);
    }
}
