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

package ch.berta.fabio.popularmovies.presentation.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieDetailsOnlBinding;
import ch.berta.fabio.popularmovies.di.components.DaggerMovieDetailsComponent;
import ch.berta.fabio.popularmovies.di.modules.MovieDetailsViewModelModule;
import ch.berta.fabio.popularmovies.di.modules.MovieRepositoryModule;
import ch.berta.fabio.popularmovies.domain.models.Movie;
import ch.berta.fabio.popularmovies.presentation.viewmodels.MovieDetailsViewModelOnl;
import ch.berta.fabio.popularmovies.presentation.workerfragments.QueryMovieDetailsWorker;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;

/**
 * Displays detail information about a movie, including poster image, release date, rating, an
 * overview of the plot, reviews and trailers. Uses info from the passed {@link Movie} object and
 * downloads additional information from TheMovieDB.
 * <p/>
 * Queries the local content provider to check if the movie is favoured and displays the according
 * drawable in the FAB.
 */
public class MovieDetailsOnlFragment extends MovieDetailsBaseFragment<MovieDetailsViewModelOnl>
        implements MovieDetailsViewModelOnl.ViewInteractionListener {

    private static final String LOG_TAG = MovieDetailsOnlFragment.class.getSimpleName();
    private static final String KEY_MOVIE = "MOVIE";
    private static final int LOADER_IS_FAV = 0;
    private FragmentMovieDetailsOnlBinding mBinding;

    public MovieDetailsOnlFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsOnlFragment newInstance(@NonNull Movie movie) {
        MovieDetailsOnlFragment fragment = new MovieDetailsOnlFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Movie movie = getArguments().getParcelable(KEY_MOVIE);
        DaggerMovieDetailsComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(getActivity()))
                .movieRepositoryModule(new MovieRepositoryModule())
                .movieDetailsViewModelModule(new MovieDetailsViewModelModule(savedInstanceState,
                        movie, mUseTwoPane))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMovieDetailsOnlBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvDetails;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_IS_FAV, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.attachView(this);
        mViewModel.loadMovieDetails();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mViewModel.onMenuInflation();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mMovieRepo.getIsFavLoader(getActivity(), mViewModel.getMovieDbId());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() != LOADER_IS_FAV) {
            // id does not match, return
            return;
        }

        if (data.moveToFirst()) {
            mViewModel.setMovieFavoured(true);
            mViewModel.setMovieRowId(mMovieRepo.getRowIdFromIsFavCursor(data));
        } else {
            mViewModel.setMovieFavoured(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.detachView();
    }

    @Override
    public void loadQueryMovieDetailsWorker(int movieDbId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment worker = WorkerUtils.findWorker(fragmentManager, QueryMovieDetailsWorker.WORKER_TAG);

        if (worker == null) {
            worker = QueryMovieDetailsWorker.newInstance(movieDbId);
            fragmentManager.beginTransaction()
                    .add(worker, QueryMovieDetailsWorker.WORKER_TAG)
                    .commit();
        }
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(LOADER_IS_FAV, null, this);
    }
}