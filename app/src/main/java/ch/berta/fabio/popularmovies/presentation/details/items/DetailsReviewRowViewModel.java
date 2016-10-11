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

package ch.berta.fabio.popularmovies.presentation.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.domain.models.Review;
import ch.berta.fabio.popularmovies.utils.Utils;

/**
 * Provides a view model for the movie review rows.
 * <p/>
 * Extends {@link Observable}.
 */
public class DetailsReviewRowViewModel extends BaseObservable {

    private final int mContentMaxLines;
    private String mReviewAuthor;
    private String mReviewContent;
    private boolean mReviewLastPosition;

    /**
     * Constructs a new {@link DetailsInfoRowViewModel}.
     *
     * @param review             the review to use
     * @param reviewLastPosition whether the review is the last in the list, used to decide whether
     *                           to show a divider or not
     * @param contentMaxLines    the max lines of review content to show
     */
    public DetailsReviewRowViewModel(@NonNull Review review, boolean reviewLastPosition,
                                     int contentMaxLines) {
        setReviewAuthor(review.getAuthor());
        setReviewContent(review.getContent());
        setReviewLastPosition(reviewLastPosition);
        mContentMaxLines = contentMaxLines;
    }

    @Bindable
    public String getReviewAuthor() {
        return mReviewAuthor;
    }

    public void setReviewAuthor(String reviewAuthor) {
        mReviewAuthor = reviewAuthor;
        notifyPropertyChanged(BR.reviewAuthor);
    }

    @Bindable
    public String getReviewContent() {
        return mReviewContent;
    }

    public void setReviewContent(String reviewContent) {
        mReviewContent = reviewContent;
        notifyPropertyChanged(BR.reviewContent);
    }

    @Bindable
    public boolean isReviewLastPosition() {
        return mReviewLastPosition;
    }

    public void setReviewLastPosition(boolean reviewLastPosition) {
        mReviewLastPosition = reviewLastPosition;
        notifyPropertyChanged(BR.reviewLastPosition);
    }

    public void onContentClick(View view) {
        Utils.expandOrCollapseTextView((TextView) view, mContentMaxLines);
    }
}
