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
import android.text.TextUtils;

import java.util.List;

import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.domain.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepositoryImpl;
import rx.Subscriber;

/**
 * Queries TheMoviesDB for movies.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class QueryMoviesWorker extends BaseWorker<QueryMoviesWorkerListener> {

    public static final String WORKER_TAG = "WORKER_TAG";
    private static final String LOG_TAG = QueryMoviesWorker.class.getSimpleName();
    private static final String BUNDLE_PAGE = "BUNDLE_PAGE";
    private static final String BUNDLE_SORT = "BUNDLE_SORT";
    private final MovieRepository mMovieRepo = new MovieRepositoryImpl();

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
        args.putInt(BUNDLE_PAGE, page);
        args.putString(BUNDLE_SORT, sort);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void onWorkerError() {
        if (mActivity != null) {
            mActivity.onMoviesOnlineLoadFailed();
        }
    }

    @Override
    protected void startWork(@NonNull Bundle args) {
        int page = args.getInt(BUNDLE_PAGE, 0);
        String sort = args.getString(BUNDLE_SORT, "");
        if (page == 0 || TextUtils.isEmpty(sort)) {
            onWorkerError();
            return;
        }

        mSubscriptions.add(mMovieRepo.getMoviesOnline(getActivity(), page, sort)
                .subscribe(new Subscriber<List<Movie>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onWorkerError();
                    }

                    @Override
                    public void onNext(List<Movie> movies) {
                        if (mActivity != null) {
                            mActivity.onMoviesOnlineLoaded(movies);
                        }
                    }
                })
        );
    }
}
