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

package ch.berta.fabio.popularmovies.presentation.workerfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.berta.fabio.popularmovies.presentation.common.di.WorkerComponent;
import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import rx.Observable;

/**
 * Queries TheMoviesDB for movie details.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class QueryMovieDetailsWorker extends BaseWorker<MovieDetails, QueryMovieDetailsWorkerListener> {

    public static final String WORKER_TAG = "QUERY_MOVIE_DETAILS_WORKER";
    private static final String LOG_TAG = QueryMovieDetailsWorker.class.getSimpleName();
    private static final String KEY_MOVIE_DB_ID = "MOVIE_DB_ID";

    /**
     * Returns a new instance of a {@link QueryMovieDetailsWorker}.
     *
     * @param movieDbId the TheMovieDB db id of the movie to query details for.
     * @return a new instance of a {@link QueryMovieDetailsWorker}
     */
    public static QueryMovieDetailsWorker newInstance(int movieDbId) {
        QueryMovieDetailsWorker fragment = new QueryMovieDetailsWorker();

        Bundle args = new Bundle();
        args.putInt(KEY_MOVIE_DB_ID, movieDbId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void injectDependencies(@NonNull WorkerComponent workerComponent) {
        workerComponent.inject(this);
    }

    @Nullable
    @Override
    protected Observable<MovieDetails> getObservable(@NonNull Bundle args) {
        final int movieDbId = args.getInt(KEY_MOVIE_DB_ID, -1);
        if (movieDbId != -1) {
            return movieRepo.getMovieDetailsOnline(movieDbId);
        }

        return null;
    }

    @Override
    protected void onWorkerError() {
        activity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<MovieDetails> observable) {
        activity.setQueryMovieDetailsStream(observable, WORKER_TAG);
    }
}
