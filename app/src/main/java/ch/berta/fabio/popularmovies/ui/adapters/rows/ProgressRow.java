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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ch.berta.fabio.popularmovies.R;

/**
 * Provides a simple {@link RecyclerView} row that displays a loading progress bar.
 * <p/>
 * Subclass of {@link RecyclerView.ViewHolder}.
 */
public class ProgressRow extends RecyclerView.ViewHolder {

    public static final int VIEW_RESOURCE = R.layout.row_progress;

    /**
     * Constructs a news {@link ProgressRow}
     *
     * @param view the inflated view
     */
    public ProgressRow(@NonNull View view) {
        super(view);
    }
}
