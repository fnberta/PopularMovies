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

package ch.berta.fabio.popularmovies.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.berta.fabio.popularmovies.BuildConfig;
import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.repositories.MovieRepository;
import ch.berta.fabio.popularmovies.ui.adapters.MoviesFavRecyclerAdapter;
import ch.berta.fabio.popularmovies.ui.adapters.decorators.PosterGridItemDecoration;

/**
 * Displays a grid of movie poster images.
 */
public class MovieGridFavFragment extends MovieGridBaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_MOVIE_SELECTED_ROW_ID = BuildConfig.APPLICATION_ID + ".intents.MOVIE_SELECTED_ROW_ID";
    private static final int FAV_MOVIES_LOADER = 0;
    private MoviesFavRecyclerAdapter mRecyclerAdapter;
    private MovieRepository mMovieRepo;

    public MovieGridFavFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieRepo = new MovieRepository();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_grid_fav, container, false);
    }

    protected void setupRecyclerView() {
        final int spanCount = getResources().getInteger(R.integer.span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new PosterGridItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_padding)));
        mRecyclerAdapter = new MoviesFavRecyclerAdapter(null, mViewEmpty, getLayoutWidth(),
                spanCount, mMovieRepo, this, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FAV_MOVIES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mMovieRepo.getFavMoviesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
        toggleMainVisibility(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);
    }

    @Override
    protected Intent setDetailsIntentExtras(Intent intent, int position) {
        final long rowId = mRecyclerAdapter.getItemId(position);
        intent.putExtra(INTENT_MOVIE_SELECTED_ROW_ID, rowId);
        return intent;
    }

    @Nullable
    @Override
    protected MovieDetailsBaseFragment getDetailsFragment(int position) {
        final int dbId = mRecyclerAdapter.getMovieDbIdForPosition(position);
        if (mMovieDbIdSelected == dbId) {
            return null;
        }

        mMovieDbIdSelected = dbId;
        final long rowId = mRecyclerAdapter.getItemId(position);
        return MovieDetailsFavFragment.newInstance(rowId);
    }
}
