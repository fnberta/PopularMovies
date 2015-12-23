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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MovieDetails;
import ch.berta.fabio.popularmovies.workerfragments.QueryMovieDetailsWorker;

/**
 * Presents the backdrop image of a selected movie in a collapsing toolbar and hosts a
 * {@link MovieDetailsFragment} that displays other information about the movie.
 */
public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.FragmentInteractionListener,
        QueryMovieDetailsWorker.TaskInteractionListener {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final String DETAILS_FRAGMENT = "details_fragment";
    private FloatingActionButton mFab;
    private MovieDetailsBaseFragment mMovieDetailsFragment;
    private ImageView mImageViewBackdrop;
    private CollapsingToolbarLayout mCollapseToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // enter transition will start when movie poster is loaded
        supportPostponeEnterTransition();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(null);
        }

        mCollapseToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mImageViewBackdrop = (ImageView) findViewById(R.id.iv_toolbar_details_backdrop);

        mFab = (FloatingActionButton) findViewById(R.id.fab_details_favorite);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieDetailsFragment.toggleFavorite();

            }
        });

        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            final long rowId = getIntent().getLongExtra(
                    MovieGridFavFragment.INTENT_MOVIE_SELECTED_ROW_ID, RecyclerView.NO_ID);
            if (rowId != RecyclerView.NO_ID) {
                mMovieDetailsFragment = MovieDetailsFavFragment.newInstance(rowId);
            } else {
                final Movie movie = getIntent().getParcelableExtra(MovieGridFragment.INTENT_MOVIE_SELECTED);
                mMovieDetailsFragment = MovieDetailsFragment.newInstance(movie);
            }

            fragmentManager.beginTransaction()
                    .add(R.id.container, mMovieDetailsFragment,
                            DETAILS_FRAGMENT)
                    .commit();
        } else {
            mMovieDetailsFragment = (MovieDetailsBaseFragment)
                    fragmentManager.getFragment(savedInstanceState, DETAILS_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, DETAILS_FRAGMENT, mMovieDetailsFragment);
    }

    @Override
    public void setOnePaneHeader(String title, String backdrop) {
        mCollapseToolbar.setTitle(title);
        setBackdrop(backdrop);
    }

    private void setBackdrop(String backdrop) {
        if (!TextUtils.isEmpty(backdrop)) {
            String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_BACKDROP_SIZE + backdrop;
            Glide.with(this)
                    .load(imagePath)
                    .into(mImageViewBackdrop);
        }
    }

    @Override
    public void toggleFabImage(boolean isFavoured) {
        mFab.setImageResource(isFavoured ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_outline_white_24dp);
    }

    @Override
    public void onMovieDetailsOnlineLoaded(@NonNull MovieDetails movieDetails) {
        mMovieDetailsFragment.onMovieDetailsOnlineLoaded(movieDetails);
    }

    @Override
    public void onMovieDetailsOnlineLoadFailed() {
        mMovieDetailsFragment.onMovieDetailsOnlineLoadFailed();
    }

    @Override
    public void showFab() {
        // do nothing, only relevant for two pane view
    }

    @Override
    public void hideDetailsFragment() {
        // do nothing, only relevant for two pane view
    }
}
