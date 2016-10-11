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
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents an option how to sort a movie poster images list or grid.
 */
public class Sort implements Parcelable {

    public static final String SORT_POPULARITY = "popularity.desc";
    public static final String SORT_RATING = "vote_average.desc";
    public static final String SORT_RELEASE_DATE = "release_date.desc";
    public static final String SORT_FAVORITE = "favorite";
    public static final Parcelable.Creator<Sort> CREATOR = new Parcelable.Creator<Sort>() {
        @Override
        public Sort createFromParcel(Parcel source) {
            return new Sort(source);
        }

        @Override
        public Sort[] newArray(int size) {
            return new Sort[size];
        }
    };
    private String option;
    private String readableValue;

    public Sort(@SortOption String option, String readableValue) {
        this.option = option;
        this.readableValue = readableValue;
    }

    protected Sort(Parcel in) {
        option = in.readString();
        readableValue = in.readString();
    }

    public String getOption() {
        return option;
    }

    public void setOption(@SortOption String option) {
        this.option = option;
    }

    public String getReadableValue() {
        return readableValue;
    }

    public void setReadableValue(String readableValue) {
        this.readableValue = readableValue;
    }

    @Override
    public String toString() {
        return readableValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(option);
        dest.writeString(readableValue);
    }

    @StringDef({SORT_POPULARITY, SORT_RELEASE_DATE, SORT_RATING, SORT_FAVORITE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortOption {
    }
}
