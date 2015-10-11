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

package ch.berta.fabio.popularmovies.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.Utils;
import ch.berta.fabio.popularmovies.data.models.Movie;

/**
 * Provides the adapter for a movie poster images grid.
 */
public class MoviesRecyclerAdapter extends RecyclerView.Adapter {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_PROGRESS = 1;
    private static final int VIEW_RESOURCE_ITEM = R.layout.row_movie;
    private static final int VIEW_RESOURCE_PROGRESS = R.layout.row_progress;
    private static final double POSTER_ASPECT_RATIO = 0.675;
    private static final String LOG_TAG = MoviesRecyclerAdapter.class.getSimpleName();
    private List<Movie> mMovies;
    private Fragment mLifecycleFragment;
    private AdapterInteractionListener mListener;
    private int mItemHeight;

    public MoviesRecyclerAdapter(Context context, List<Movie> movies, Fragment fragment,
                                 AdapterInteractionListener listener) {
        mMovies = movies;
        mLifecycleFragment = fragment;
        mListener = listener;

        calcPosterHeight(context);
    }

    private void calcPosterHeight(Context context) {
        int columns = context.getResources().getInteger(R.integer.span_count);
        int screenWidth = Utils.getScreenWidth(context);
        int itemWidth = screenWidth / columns;
        mItemHeight = (int) (itemWidth / POSTER_ASPECT_RATIO);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_ITEM,
                        parent, false);
                return new MovieRow(view, mItemHeight, mListener);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_PROGRESS,
                        parent, false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_ITEM:
                MovieRow movieRow = (MovieRow) holder;
                Movie movie = mMovies.get(position);

                movieRow.setMovie(movie.getTitle(), movie.getReleaseDateFormatted(false),
                        movie.getPosterPath(), mLifecycleFragment);
                break;
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMovies.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    /**
     * Returns the position of the last movie in the adapter.
     *
     * @return the position of the last movie
     */
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    /**
     * Clears all movies from the adapter and sets new ones.
     *
     * @param movies the movies to be set
     */
    public void setMovies(List<Movie> movies) {
        mMovies.clear();

        if (!movies.isEmpty()) {
            mMovies.addAll(movies);
        }

        notifyDataSetChanged();
    }

    /**
     * Adds movies to the adapter.
     *
     * @param movies the movies to be added
     */
    public void addMovies(List<Movie> movies) {
        mMovies.addAll(movies);
        notifyItemRangeInserted(getItemCount(), movies.size());
    }

    /**
     * Shows a progressbar in the last row as an indicator that more objects are being fetched.
     */
    public void showLoadMoreIndicator() {
        mMovies.add(null);
        notifyItemInserted(getLastPosition());
    }

    /**
     * Hides the progressbar in the last row.
     */
    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mMovies.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Handles the interaction of the adapter.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a movie.
         *
         * @param position   the adapter position of the clicked movie
         * @param sharedView the view to be used for a shared element transition
         */
        void onMovieRowItemClick(int position, View sharedView);
    }

    private static class MovieRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewPoster;
        private TextView mTextViewTitle;
        private TextView mTextViewDate;

        public MovieRow(final View itemView, int itemHeight,
                        final AdapterInteractionListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMovieRowItemClick(getAdapterPosition(), mImageViewPoster);
                }
            });

            setPosterHeight(itemView, itemHeight);
            mImageViewPoster = (ImageView) itemView.findViewById(R.id.iv_poster);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tv_date);
        }

        private void setPosterHeight(View itemView, int itemHeight) {
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = itemHeight;
            itemView.setLayoutParams(layoutParams);
        }

        /**
         * Sets all information of a movie.
         *
         * @param title    the title of the movie
         * @param date     the release date of the movie
         * @param poster   the relative url of the movie poster
         * @param fragment the fragment whose lifecycle the image loading should follow
         */
        public void setMovie(String title, String date, String poster, Fragment fragment) {
            if (!TextUtils.isEmpty(poster)) {
                mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                String imageUrl = Movie.IMAGE_BASE_URL + Movie.IMAGE_POSTER_SIZE + poster;
                Glide.with(fragment)
                        .load(imageUrl)
                        .crossFade()
                        .into(mImageViewPoster);
            } else {
                mImageViewPoster.setScaleType(ImageView.ScaleType.CENTER);
                mImageViewPoster.setImageResource(R.drawable.ic_movie_white_72dp);
            }
            mTextViewTitle.setText(title);
            mTextViewDate.setText(date);
        }
    }

    private static class ProgressRow extends RecyclerView.ViewHolder {

        public ProgressRow(View view) {
            super(view);
        }
    }
}
