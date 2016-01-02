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

package ch.berta.fabio.popularmovies.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.ui.adapters.rows.HeaderRow;

/**
 * Provides a view model for a generic header row.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public class HeaderRowViewModel extends BaseObservable {

    private String mHeader;

    /**
     * Constructs a new {@link HeaderRow}.
     *
     * @param header the header to display
     */
    public HeaderRowViewModel(@NonNull String header) {
        setHeader(header);
    }

    @Bindable
    public String getHeader() {
        return mHeader;
    }

    public void setHeader(@NonNull String header) {
        mHeader = header;
        notifyPropertyChanged(BR.header);
    }
}
