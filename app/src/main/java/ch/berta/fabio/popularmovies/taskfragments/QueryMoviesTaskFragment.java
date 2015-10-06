package ch.berta.fabio.popularmovies.taskfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.List;

import ch.berta.fabio.popularmovies.data.MovieDbClient;
import ch.berta.fabio.popularmovies.data.MovieDbKey;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.MoviesPage;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by fabio on 04.10.15.
 */
public class QueryMoviesTaskFragment extends Fragment {

    private static final String BUNDLE_PAGE = "bundle_page";
    private static final String BUNDLE_SORT = "bundle_sort";
    private TaskInteractionListener mListener;
    private Call<MoviesPage> mLoadMoviePosters;

    public static QueryMoviesTaskFragment newInstance(int page, String sort) {
        QueryMoviesTaskFragment fragment = new QueryMoviesTaskFragment();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_PAGE, page);
        args.putString(BUNDLE_SORT, sort);
        fragment.setArguments(args);

        return fragment;
    }

    public QueryMoviesTaskFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TaskInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle args = getArguments();
        int page = 0;
        String sort = "";
        if (args != null) {
            page = args.getInt(BUNDLE_PAGE);
            sort = args.getString(BUNDLE_SORT);
        }

        if (page > 0 && !TextUtils.isEmpty(sort)) {
            queryMovies(page, sort);
        } else if (mListener != null) {
            mListener.onMovieQueryFailed();
        }
    }

    private void queryMovies(int page, String sort) {
        mLoadMoviePosters = MovieDbClient.getService().loadMoviePosters(page, sort,
                MovieDbKey.MOVIE_DB_KEY);
        mLoadMoviePosters.enqueue(new Callback<MoviesPage>() {
            @Override
            public void onResponse(Response<MoviesPage> response, Retrofit retrofit) {
                MoviesPage page = response.body();
                if (mListener != null) {
                    if (page != null) {
                        mListener.onMoviesQueried(page.getMovies());
                    } else {
                        mListener.onMovieQueryFailed();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mListener != null) {
                    mListener.onMovieQueryFailed();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        mLoadMoviePosters.cancel();
        super.onDestroy();
    }

    public interface TaskInteractionListener {
        void onMoviesQueried(List<Movie> movies);

        void onMovieQueryFailed();
    }
}
