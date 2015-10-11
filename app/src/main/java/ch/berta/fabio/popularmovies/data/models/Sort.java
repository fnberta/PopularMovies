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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents an option how to sort a movie poster images list or grid.
 */
public class Sort {

    @StringDef({SORT_POPULARITY, SORT_RELEASE_DATE, SORT_RATING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortOption {}
    public static final String SORT_POPULARITY = "popularity.desc";
    public static final String SORT_RATING = "vote_average.desc";
    public static final String SORT_RELEASE_DATE = "release_date.desc";
    private String mOption;
    private String mReadableValue;

    public Sort(@SortOption String option, String readableValue) {
        mOption = option;
        mReadableValue = readableValue;
    }

    public String getOption() {
        return mOption;
    }

    public void setOption(@SortOption String option) {
        mOption = option;
    }

    public String getReadableValue() {
        return mReadableValue;
    }

    public void setReadableValue(String readableValue) {
        mReadableValue = readableValue;
    }


}
