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

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.data.models.Review;
import ch.berta.fabio.popularmovies.data.models.Video;
import ch.berta.fabio.popularmovies.data.storage.MovieContract;
import ch.berta.fabio.popularmovies.ui.adapters.MovieDetailsRecyclerAdapter;

/**
 * Provides a base class for the display of detail information about a movie, including poster
 * image, release date, rating, an overview of the plot, reviews and trailers.
 */
public abstract class MovieDetailsBaseFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieDetailsRecyclerAdapter.AdapterInteractionListener {

    private static final String LOG_TAG = MovieDetailsBaseFragment.class.getSimpleName();
    private static final int TOKEN_DELETE = 0;
    FragmentInteractionListener mListener;
    long mMovieRowId;
    boolean mUseTwoPane;
    Movie mMovie;
    MovieDetailsRecyclerAdapter mRecyclerAdapter;
    RecyclerView mRecyclerView;
    private InsertMovieTask mInsertMovieTask;
    private QueryHandler mDeleteMovieHandler;
    private Intent mShareYoutubeIntent;
    private boolean mHasVideos = false;

    public MovieDetailsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mUseTwoPane = getResources().getBoolean(R.bool.use_two_pane_layout);

        mShareYoutubeIntent = new Intent(Intent.ACTION_SEND);
        mShareYoutubeIntent.setType("text/plain");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_details);
        mRecyclerAdapter = getRecyclerAdapter();
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        if (mUseTwoPane) {
            mListener.showFab();
        }
    }

    @NonNull
    protected abstract MovieDetailsRecyclerAdapter getRecyclerAdapter();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_movie_details, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_details_action_share);
        shareItem.setVisible(mHasVideos);
    }

    final void setShareYoutubeIntent() {
        final List<Video> videos = mMovie.getVideos();
        if (videos.isEmpty()) {
            mHasVideos = false;
        } else {
            mHasVideos = true;
            final Video firstVideo = videos.get(0);
            final String url = Video.YOUTUBE_BASE_URL + firstVideo.getKey();
            mShareYoutubeIntent.putExtra(Intent.EXTRA_TEXT, url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_details_action_share:
                shareYoutubeUrl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareYoutubeUrl() {
        startActivity(Intent.createChooser(mShareYoutubeIntent, getString(R.string.action_share)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    final void setFavoured(boolean isFavoured) {
        mMovie.setIsFavoured(isFavoured);
        mListener.toggleFabImage(isFavoured);
    }

    /**
     * Adds or removes a movie from the local content provider, depending on whether the user set
     * the movie as favoured or not.
     */
    public void toggleFavorite() {
        boolean newlyFavoured;
        if (!mMovie.isFavoured()) {
            newlyFavoured = true;
            mInsertMovieTask = new InsertMovieTask();
            mInsertMovieTask.execute(mMovie);
        } else {
            newlyFavoured = false;
            mDeleteMovieHandler = new QueryHandler(getActivity().getContentResolver());
            mDeleteMovieHandler.startDelete(
                    TOKEN_DELETE,
                    null,
                    MovieContract.Movie.buildMovieUri(mMovieRowId),
                    null,
                    null
            );
        }

        setFavoured(newlyFavoured);
    }

    private void onMovieInserted() {
        Snackbar.make(mRecyclerView, getString(R.string.snackbar_added_to_favorites), Snackbar.LENGTH_LONG).show();
    }

    private void onMovieDeleted() {
        if (mUseTwoPane) {
            Snackbar.make(mRecyclerView, getString(R.string.snackbar_removed_from_favorites), Snackbar.LENGTH_LONG).show();
            mListener.hideDetailsFragment();
        } else {
            onMovieDeletedOnePane();
        }
    }

    protected abstract void onMovieDeletedOnePane();

    @Override
    public void onVideoRowItemClick(int position) {
        Video video = mRecyclerAdapter.getVideoAtPosition(position);
        final Uri uri = Uri.parse(Video.YOUTUBE_BASE_URL + video.getKey());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onPosterLoaded() {
        startPostponedEnterTransition();
    }

    final void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mInsertMovieTask != null) {
            mInsertMovieTask.cancel(true);
        }

        if (mDeleteMovieHandler != null) {
            mDeleteMovieHandler.cancelOperation(TOKEN_DELETE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface FragmentInteractionListener {
        /**
         * Toggles the drawable shown in the FAB, either an empty or filled heart depending on
         * whether the movie is favoured or not.
         *
         * @param isFavoured whether the movies is favoured or not
         */
        void toggleFabImage(boolean isFavoured);

        /**
         * Shows the FAB.
         */
        void showFab();

        /**
         * Sets the title and backdrop image of the header shown in {@link AppBarLayout} of the
         * hosting activity.
         *
         * @param title    the title to show
         * @param backdrop the backdrop image to show
         */
        void setOnePaneHeader(String title, String backdrop);

        /**
         * Hides the details fragment in a two pane layout (on tablets).
         */
        void hideDetailsFragment();
    }

    /**
     * Handles content provider batch insert operation on a background thread. To avoid a leak, the
     * process needs to be canceled in the activity's or fragment's onPause() method.
     */
    private class InsertMovieTask extends AsyncTask<Movie, Integer, ContentProviderResult[]> {

        @Override
        protected ContentProviderResult[] doInBackground(Movie... params) {
            final Movie movie = params[0];

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation
                            .newInsert(MovieContract.Movie.CONTENT_URI)
                            .withValues(movie.getContentValuesEntry())
                            .build()
            );

            List<Review> reviews = movie.getReviews();
            if (!reviews.isEmpty()) {
                for (Review review : reviews) {
                    ops.add(ContentProviderOperation
                                    .newInsert(MovieContract.Review.CONTENT_URI)
                                    .withValueBackReference(MovieContract.Review.COLUMN_MOVIE_ID, 0)
                                    .withValues(review.getContentValuesEntry())
                                    .build()
                    );
                }
            }

            List<Video> videos = movie.getVideos();
            if (!videos.isEmpty()) {
                for (Video video : videos) {
                    // only add youtube videos
                    if (video.getSite().equals("YouTube")) {
                        ops.add(ContentProviderOperation
                                        .newInsert(MovieContract.Video.CONTENT_URI)
                                        .withValueBackReference(MovieContract.Video.COLUMN_MOVIE_ID, 0)
                                        .withValues(video.getContentValuesEntry())
                                        .build()
                        );
                    }
                }
            }

            try {
                return getActivity().getContentResolver().applyBatch(MovieContract.CONTENT_AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ContentProviderResult[] contentProviderResults) {
            super.onPostExecute(contentProviderResults);

            if (contentProviderResults != null) {
                onMovieInserted();
            }
        }
    }

    /**
     * Handles content provider delete operation on a background thread. To avoid a leak, the
     * process needs to be canceled in the activity's or fragment's onPause() method.
     */
    @SuppressWarnings("HandlerLeak")
    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);

            onMovieDeleted();
        }
    }
}
