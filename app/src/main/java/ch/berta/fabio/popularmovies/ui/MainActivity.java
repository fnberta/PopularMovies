package ch.berta.fabio.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.taskfragments.QueryMoviesTaskFragment;
import ch.berta.fabio.popularmovies.ui.dialogs.SortMoviesDialogFragment;

public class MainActivity extends AppCompatActivity implements
        MainFragment.FragmentInteractionListener,
        QueryMoviesTaskFragment.TaskInteractionListener,
        SortMoviesDialogFragment.DialogInteractionListener {

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
