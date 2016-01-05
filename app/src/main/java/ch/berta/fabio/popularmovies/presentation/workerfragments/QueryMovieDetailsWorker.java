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

import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import ch.berta.fabio.popularmovies.domain.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepositoryImpl;
import rx.Subscriber;

/**
 * Queries TheMoviesDB for movie details.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class QueryMovieDetailsWorker extends BaseWorker<QueryMovieDetailsWorkerListener> {

    public static final String WORKER_TAG = "WORKER_TAG";
    private static final String LOG_TAG = QueryMovieDetailsWorker.class.getSimpleName();
    private static final String BUNDLE_MOVIE_DB_ID = "BUNDLE_MOVIE_DB_ID";
    private final MovieRepository mMovieRepo = new MovieRepositoryImpl();

    public static QueryMovieDetailsWorker newInstance(int movieDbId) {
        QueryMovieDetailsWorker fragment = new QueryMovieDetailsWorker();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_MOVIE_DB_ID, movieDbId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void onWorkerError() {
        if (mActivity != null) {
            mActivity.onMovieDetailsOnlineLoadFailed();
        }
    }

    @Override
    protected void startWork(@NonNull Bundle args) {
        final int movieDbId = args.getInt(BUNDLE_MOVIE_DB_ID, -1);
        if (movieDbId == -1) {
            onWorkerError();
            return;
        }

        mSubscriptions.add(mMovieRepo.getMovieDetailsOnline(getActivity(), movieDbId)
                .subscribe(new Subscriber<MovieDetails>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onWorkerError();
                    }

                    @Override
                    public void onNext(MovieDetails movieDetails) {
                        if (mActivity != null) {
                            mActivity.onMovieDetailsOnlineLoaded(movieDetails);
                        }
                    }
                })
        );
    }
}
