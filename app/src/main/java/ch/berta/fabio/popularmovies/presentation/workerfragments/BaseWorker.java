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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.presentation.common.di.DaggerWorkerComponent;
import ch.berta.fabio.popularmovies.presentation.common.di.WorkerComponent;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Provides an abstract base class for worker fragments that are retained across configuration
 * changes in order to to async tasks.
 */
public abstract class BaseWorker<T, S extends BaseWorkerListener> extends Fragment {

    private static final String LOG_TAG = BaseWorker.class.getSimpleName();
    private final PublishSubject<T> subject = PublishSubject.create();
    S activity;
    @Inject
    MovieRepository movieRepo;
    private Subscription subscription;

    public BaseWorker() {
        // required empty constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (S) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement WorkerInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        final Bundle args = getArguments();
        if (args == null) {
            onWorkerError();
            return;
        }

        final WorkerComponent repoComp = DaggerWorkerComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(getActivity()))
                .build();
        injectDependencies(repoComp);

        final Observable<T> observable = getObservable(args);
        if (observable != null) {
            subscription = observable.subscribe(subject);
        } else {
            onWorkerError();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setStream(subject.asObservable());
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    protected abstract void injectDependencies(@NonNull WorkerComponent workerComponent);

    @Nullable
    protected abstract Observable<T> getObservable(@NonNull Bundle args);

    protected abstract void onWorkerError();

    protected abstract void setStream(@NonNull Observable<T> observable);
}
