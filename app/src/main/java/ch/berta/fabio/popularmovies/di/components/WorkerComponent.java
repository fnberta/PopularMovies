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

package ch.berta.fabio.popularmovies.di.components;

import ch.berta.fabio.popularmovies.di.modules.MovieRepositoryModule;
import ch.berta.fabio.popularmovies.di.scopes.PerFragment;
import ch.berta.fabio.popularmovies.domain.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMovieDetailsWorker;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMoviesWorker;
import ch.berta.fabio.popularmovies.presentation.workerfragments.UpdateMovieDetailsWorker;
import dagger.Component;

/**
 * Defines the dependency injection component for the headless worker fragments.
 */
@PerFragment
@Component(modules = {MovieRepositoryModule.class},
        dependencies = {ApplicationComponent.class})
public interface WorkerComponent {

    void inject(QueryMoviesWorker queryMoviesWorker);

    void inject(QueryMovieDetailsWorker queryMovieDetailsWorker);

    void inject(UpdateMovieDetailsWorker updateMovieDetailsWorker);

    MovieRepository getMovieRepository();
}
