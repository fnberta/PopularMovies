package ch.berta.fabio.popularmovies.data.models;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 04.10.15.
 */
public class Sort {

    @StringDef({SORT_POPULARITY, SORT_RELEASE_DATE, SORT_RATING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SortOption {}
    public static final String SORT_POPULARITY = "popularity.desc";
    public static final String SORT_RELEASE_DATE = "release_date.desc";
    public static final String SORT_RATING = "vote_average.desc";

    private String mOption;
    private String mReadableValue;

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

    public Sort(@SortOption String option, String readableValue) {
        mOption = option;
        mReadableValue = readableValue;
    }
}
