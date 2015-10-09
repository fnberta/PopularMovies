package ch.berta.fabio.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private static final String DETAILS_FRAGMENT = "details_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        supportPostponeEnterTransition();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Movie movie = getIntent().getParcelableExtra(MainFragment.INTENT_MOVIE_SELECTED);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(movie.getOriginalTitle());
        }

        ImageView ivBackdrop = (ImageView) findViewById(R.id.iv_details_backdrop);
        String backdrop = movie.getBackdropPath();
        if (!TextUtils.isEmpty(backdrop)) {
            String imagePath = Movie.IMAGE_BASE_URL + Movie.IMAGE_BACKDROP_SIZE + backdrop;
            Glide.with(this)
                    .load(imagePath)
                    .into(ivBackdrop);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, MovieDetailsFragment.newInstance(movie), DETAILS_FRAGMENT)
                    .commit();
        }
    }
}
