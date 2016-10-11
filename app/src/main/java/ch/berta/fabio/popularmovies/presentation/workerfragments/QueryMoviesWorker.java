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
import android.text.TextUtils;

import java.util.List;

import ch.berta.fabio.popularmovies.presentation.common.di.WorkerComponent;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import rx.Observable;

/**
 * Queries TheMoviesDB for movies.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class QueryMoviesWorker extends BaseWorker<List<Movie>, QueryMoviesWorkerListener> {

    public static final String WORKER_TAG = "QUERY_MOVIES_WORKER";
    private static final String LOG_TAG = QueryMoviesWorker.class.getSimpleName();
    private static final String KEY_MOVIE_PAGE = "MOVIE_PAGE";
    private static final String KEY_MOVIE_SORT = "MOVIE_SORT";

    /**
     * Returns a new instance of a {@link QueryMoviesWorker} with a page and sort options
     * as arguments.
     *
     * @param page the page number to be used for the movie query
     * @param sort the sort option to be used for the movie query
     * @return a new instance of a {@link QueryMoviesWorker}
     */
    public static QueryMoviesWorker newInstance(int page, @NonNull String sort) {
        QueryMoviesWorker fragment = new QueryMoviesWorker();

        Bundle args = new Bundle();
        args.putInt(KEY_MOVIE_PAGE, page);
        args.putString(KEY_MOVIE_SORT, sort);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void injectDependencies(@NonNull WorkerComponent workerComponent) {
        workerComponent.inject(this);
    }

    @Nullable
    @Override
    protected Observable<List<Movie>> getObservable(@NonNull Bundle args) {
        int page = args.getInt(KEY_MOVIE_PAGE, 0);
        String sort = args.getString(KEY_MOVIE_SORT, "");
        if (page != 0 && !TextUtils.isEmpty(sort)) {
            return movieRepo.getMoviesOnline(page, sort);
        }

        return null;
    }

    @Override
    protected void onWorkerError() {
        activity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<List<Movie>> observable) {
        activity.setQueryMoviesStream(observable, WORKER_TAG);
    }
}