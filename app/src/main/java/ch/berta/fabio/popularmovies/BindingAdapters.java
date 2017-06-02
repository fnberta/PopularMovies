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

package ch.berta.fabio.popularmovies;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import ch.berta.fabio.popularmovies.features.details.view.PosterLoadListener;

/**
 * Contains generic binding adapters.
 */
public class BindingAdapters {

    private BindingAdapters() {
        // class cannot be instantiated
    }

    @BindingAdapter({"colorScheme"})
    public static void setColorScheme(SwipeRefreshLayout view, int[] colorScheme) {
        view.setColorSchemeColors(colorScheme);
    }

    @BindingAdapter({"backdropUrl"})
    public static void loadBackdrop(ImageView view, String backdropUrl) {
        Glide.with(view.getContext())
                .load(backdropUrl)
                .into(view);
    }

    @BindingAdapter({"imageUrl", "fallback"})
    public static void loadMovieImage(ImageView view, String imageUrl, Drawable fallback) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .error(fallback)
                .crossFade()
                .into(view);
    }

    @BindingAdapter({"poster", "listener", "fallback"})
    public static void loadPosterWithListener(ImageView view,
                                              String imageUrl,
                                              final PosterLoadListener listener,
                                              Drawable fallback) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .error(fallback)
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        listener.onPosterLoaded();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        listener.onPosterLoaded();
                        return false;
                    }
                })
                .into(view);
    }
}
