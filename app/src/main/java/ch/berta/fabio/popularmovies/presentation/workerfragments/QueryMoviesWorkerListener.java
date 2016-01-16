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

package ch.berta.fabio.popularmovies.presentation.workerfragments;

import android.support.annotation.NonNull;

import java.util.List;

import ch.berta.fabio.popularmovies.domain.models.Movie;
import rx.Observable;

/**
 * Defines the actions after a query for movies was attempted.
 */
public interface QueryMoviesWorkerListener extends BaseWorkerListener {

    /**
     * Sets the observable with the movie query.
     *
     * @param observable the observable to set
     * @param workerTag  the tag of the worker fragment
     */
    void setQueryMoviesStream(@NonNull Observable<List<Movie>> observable,
                              @NonNull String workerTag);

}
