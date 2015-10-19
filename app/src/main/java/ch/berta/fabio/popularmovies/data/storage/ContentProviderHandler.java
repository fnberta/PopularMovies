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

package ch.berta.fabio.popularmovies.data.storage;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.net.Uri;

import java.lang.ref.WeakReference;

import ch.berta.fabio.popularmovies.ui.MovieDetailsActivity;

/**
 * Created by fabio on 19.10.15.
 */
public class ContentProviderHandler extends AsyncQueryHandler {

    private WeakReference<HandlerInteractionListener> mListener;

    public ContentProviderHandler(ContentResolver cr, HandlerInteractionListener listener) {
        super(cr);

        mListener = new WeakReference<>(listener);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);

        final HandlerInteractionListener listener = mListener.get();
        if (listener != null) {
            listener.onInsertComplete(token, cookie, uri);
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);

        final HandlerInteractionListener listener = mListener.get();
        if (listener != null) {
            listener.onDeleteComplete(token, cookie, result);
        }
    }

    public interface HandlerInteractionListener {
        void onInsertComplete(int token, Object cookie, Uri uri);

        void onDeleteComplete(int token, Object cookie, int result);
    }
}
