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

package ch.berta.fabio.popularmovies.presentation.grid.fav;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.BuildConfig;
import ch.berta.fabio.popularmovies.PopularMovies;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.databinding.FragmentMovieGridFavBinding;
import ch.berta.fabio.popularmovies.domain.models.Sort;
import ch.berta.fabio.popularmovies.presentation.details.MovieDetailsActivity;
import ch.berta.fabio.popularmovies.presentation.details.fav.MovieDetailsFavFragment;
import ch.berta.fabio.popularmovies.presentation.grid.MovieGridBaseFragment;
import ch.berta.fabio.popularmovies.presentation.grid.PosterGridItemDecoration;
import ch.berta.fabio.popularmovies.presentation.grid.di.DaggerMovieGridComponent;
import ch.berta.fabio.popularmovies.presentation.grid.di.MovieGridViewModelModule;
import ch.berta.fabio.popularmovies.presentation.grid.onl.MovieGridOnlFragment;

/**
 * Displays a grid of movie poster images.
 */
public class MovieGridFavFragment extends MovieGridBaseFragment<MovieGridViewModelFav, MovieGridBaseFragment.ActivityListener>
        implements MovieGridViewModelFav.ViewInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_MOVIE_SELECTED_ROW_ID = BuildConfig.APPLICATION_ID + ".intents.MOVIE_SELECTED_ROW_ID";
    private static final int FAV_MOVIES_LOADER = 0;
    @Inject
    MovieRepository movieRepo;
    private MoviesFavRecyclerAdapter recyclerAdapter;
    private FragmentMovieGridFavBinding binding;

    public MovieGridFavFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerMovieGridComponent.builder()
                .applicationComponent(PopularMovies.getAppComponent(getActivity()))
                .movieGridViewModelModule(new MovieGridViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMovieGridFavBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    protected void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        binding.rvGrid.setLayoutManager(layoutManager);
        binding.rvGrid.setHasFixedSize(true);
        binding.rvGrid.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        recyclerAdapter = new MoviesFavRecyclerAdapter(null, viewModel, movieRepo,
                getLayoutWidth(), spanCount);
        binding.rvGrid.setAdapter(recyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FAV_MOVIES_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.attachView(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return movieRepo.getFavMoviesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        recyclerAdapter.swapCursor(data);
        viewModel.setMoviesAvailable(recyclerAdapter.getItemCount() > 0);
        viewModel.setMoviesLoaded(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerAdapter.swapCursor(null);
        viewModel.setMoviesAvailable(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        viewModel.detachView();
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvGrid;
    }

    @Override
    public void launchDetailsScreen(int moviePosition, @NonNull View posterSharedElement) {
        if (!useTwoPane) {
            final Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            final long rowId = recyclerAdapter.getItemId(moviePosition);
            intent.putExtra(INTENT_MOVIE_SELECTED_ROW_ID, rowId);
            startDetailsActivity(intent, posterSharedElement);
        } else {
            final int dbId = recyclerAdapter.getMovieDbIdForPosition(moviePosition);
            if (!viewModel.isMovieSelected(dbId)) {
                final long rowId = recyclerAdapter.getItemId(moviePosition);
                showDetailsFavFragment(rowId);
            }
        }
    }

    private void showDetailsFavFragment(long rowId) {
        final MovieDetailsFavFragment fragment = MovieDetailsFavFragment.newInstance(rowId);
        replaceDetailsFragment(fragment);
    }

    @Override
    public void showOnlineMovies(@NonNull Sort sortSelected) {
        MovieGridOnlFragment fragment = MovieGridOnlFragment.newInstance(sortSelected);
        replaceFragment(fragment);
    }
}