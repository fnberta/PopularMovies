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

package ch.berta.fabio.popularmovies.ui.adapters.rows;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ch.berta.fabio.popularmovies.R;
import ch.berta.fabio.popularmovies.data.models.Movie;
import ch.berta.fabio.popularmovies.ui.adapters.listeners.MovieInteractionListener;

/**
 * Provides a {@link RecyclerView} row that displays a movie with its poster, title and release
 * date.
 */
public class MovieRow extends RecyclerView.ViewHolder {

    private ImageView mImageViewPoster;
    private TextView mTextViewTitle;
    private TextView mTextViewDate;

    public MovieRow(final View itemView, int itemHeight,
                    final MovieInteractionListener listener) {
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
