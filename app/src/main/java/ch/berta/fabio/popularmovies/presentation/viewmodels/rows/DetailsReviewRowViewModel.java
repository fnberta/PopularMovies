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

package ch.berta.fabio.popularmovies.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.view.View;

/**
 * Defines a view model for the movie review rows.
 * <p/>
 * Extends {@link Observable}.
 */
public interface DetailsReviewRowViewModel extends Observable {

    @Bindable
    String getReviewAuthor();

    void setReviewAuthor(String reviewAuthor);

    @Bindable
    String getReviewContent();

    void setReviewContent(String reviewContent);

    @Bindable
    boolean isReviewLastPosition();

    void setReviewLastPosition(boolean reviewLastPosition);

    void onContentClick(View view);
}
