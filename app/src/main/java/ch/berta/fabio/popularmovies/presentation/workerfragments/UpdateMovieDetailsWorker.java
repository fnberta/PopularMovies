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

import android.content.ContentProviderResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.berta.fabio.popularmovies.presentation.common.di.WorkerComponent;
import ch.berta.fabio.popularmovies.domain.models.MovieDetails;
import rx.Observable;
import rx.functions.Func1;

/**
 * Queries TheMoviesDB for movie details and updates the corresponding movie in the local content
 * provider.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class UpdateMovieDetailsWorker extends BaseWorker<ContentProviderResult[], UpdateMovieDetailsWorkerListener> {

    public static final String WORKER_TAG = "UPDATE_MOVIE_DETAILS_WORKER";
    private static final String LOG_TAG = UpdateMovieDetailsWorker.class.getSimpleName();
    private static final String KEY_MOVIE_DB_ID = "MOVIE_DB_ID";
    private static final String KEY_MOVIE_ROW_ID = "MOVIE_ROW_ID";

    /**
     * Returns a new instance of a {@link UpdateMovieDetailsWorker}.
     *
     * @param movieDbId  the TheMovieDB db id of the movie to update
     * @param movieRowId the row id in the local content provider of the movie to update
     * @return a new instance of a {@link UpdateMovieDetailsWorker}
     */
    public static UpdateMovieDetailsWorker newInstance(int movieDbId, long movieRowId) {
        UpdateMovieDetailsWorker fragment = new UpdateMovieDetailsWorker();

        Bundle args = new Bundle();
        args.putInt(KEY_MOVIE_DB_ID, movieDbId);
        args.putLong(KEY_MOVIE_ROW_ID, movieRowId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void injectDependencies(@NonNull WorkerComponent workerComponent) {
        workerComponent.inject(this);
    }

    @Nullable
    @Override
    protected Observable<ContentProviderResult[]> getObservable(@NonNull Bundle args) {
        final int movieDbId = args.getInt(KEY_MOVIE_DB_ID, -1);
        final long movieRowId = args.getLong(KEY_MOVIE_ROW_ID, -1);
        if (movieDbId != -1 && movieRowId != -1) {
            return mMovieRepo.getMovieDetailsOnline(movieDbId)
                    .flatMap(new Func1<MovieDetails, Observable<ContentProviderResult[]>>() {
                        @Override
                        public Observable<ContentProviderResult[]> call(MovieDetails movieDetails) {
                            return mMovieRepo.updateMovieLocal(movieDetails, movieRowId);
                        }
                    });
        }

        return null;
    }

    @Override
    protected void onWorkerError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<ContentProviderResult[]> observable) {
        mActivity.setUpdateMovieDetailsStream(observable, WORKER_TAG);
    }
}
