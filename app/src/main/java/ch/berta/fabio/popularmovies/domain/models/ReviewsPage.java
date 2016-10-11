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
    private int page;
    @SerializedName("results")
    private List<Review> reviews = new ArrayList<>();
    @SerializedName("total_pages")
    private int totalPages;
    @SerializedName("total_results")
    private int totalResults;

    public ReviewsPage() {
    }

    protected ReviewsPage(Parcel in) {
        page = in.readInt();
        reviews = in.createTypedArrayList(Review.CREATOR);
        totalPages = in.readInt();
        totalResults = in.readInt();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(page);
        dest.writeTypedList(reviews);
        dest.writeInt(totalPages);
        dest.writeInt(totalResults);
    }
}
