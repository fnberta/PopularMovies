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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment;
import ch.berta.fabio.popularmovies.ui.dialogs.SortMoviesDialogFragment;

/**
 * Provides the main entry point to the app and hosts a {@link MainFragment}.
 */
public class MainActivity extends AppCompatActivity implements
        QueryMoviesTaskFragment.TaskInteractionListener,
        SortMoviesDialogFragment.DialogInteractionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onMoviesQueried(List<Movie> movies) {
        mMainFragment.onMoviesQueried(movies);
    }

    @Override
    public void onMovieQueryFailed() {
        mMainFragment.onMovieQueryFailed();
    }

    @Override
    public void setSortOption(int optionIndex) {
        mMainFragment.onSortOptionSelected(optionIndex);
    }
}
