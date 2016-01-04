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

package ch.berta.fabio.popularmovies.workerfragments;

import android.content.ContentProviderResult;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepositoryImpl;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Queries TheMoviesDB for movie details and updates the corresponding movie in the local content
 * provider.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class UpdateMovieDetailsWorker extends BaseWorker<UpdateMovieDetailsWorkerListener> {

    public static final String WORKER_TAG = "WORKER_TAG";
    private static final String LOG_TAG = UpdateMovieDetailsWorker.class.getSimpleName();
    private static final String BUNDLE_MOVIE_DB_ID = "BUNDLE_MOVIE_DB_ID";
    private static final String BUNDLE_MOVIE_ROW_ID = "BUNDLE_MOVIE_ROW_ID";
    private final MovieRepository mMovieRepo = new MovieRepositoryImpl();

    public static UpdateMovieDetailsWorker newInstance(int movieDbId, long movieRowId) {
        UpdateMovieDetailsWorker fragment = new UpdateMovieDetailsWorker();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_MOVIE_DB_ID, movieDbId);
        args.putLong(BUNDLE_MOVIE_ROW_ID, movieRowId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void onWorkerError() {
        if (mActivity != null) {
            mActivity.onMovieDetailsUpdateFailed();
        }
    }

    @Override
    protected void startWork(@NonNull Bundle args) {
        final int movieDbId = args.getInt(BUNDLE_MOVIE_DB_ID, -1);
        final long movieRowId = args.getLong(BUNDLE_MOVIE_ROW_ID, -1);
        if (movieDbId == -1 || movieRowId == -1) {
            onWorkerError();
            return;
        }

        final Context context = getContext();
        mSubscriptions.add(mMovieRepo.getMovieDetailsOnline(context, movieDbId)
                .flatMap(new Func1<MovieDetails, Observable<ContentProviderResult[]>>() {
                    @Override
                    public Observable<ContentProviderResult[]> call(MovieDetails movieDetails) {
                        return mMovieRepo.updateMovieLocal(context, movieDetails, movieRowId);
                    }
                })
                .subscribe(new Subscriber<ContentProviderResult[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onWorkerError();
                    }

                    @Override
                    public void onNext(ContentProviderResult[] contentProviderResults) {
                        if (mActivity != null) {
                            mActivity.onMovieDetailsUpdated();
                        }
                    }
                })
        );
    }
}
