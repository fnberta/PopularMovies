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

package ch.berta.fabio.popularmovies.presentation.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.StringRes;

import ch.berta.fabio.popularmovies.BR;
import ch.berta.fabio.popularmovies.presentation.common.rows.HeaderRow;
import ch.berta.fabio.popularmovies.presentation.common.viewmodels.items.HeaderRowViewModel;

/**
 * Provides an implementation of the {@link HeaderRowViewModel} interface.
 * <p/>
 * Subclass of {@link BaseObservable}.
 */
public class DetailsHeaderRowViewModel extends BaseObservable implements HeaderRowViewModel {

    @StringRes
    private int mHeader;

    /**
     * Constructs a new {@link HeaderRow}.
     *
     * @param header the header to display
     */
    public DetailsHeaderRowViewModel(@StringRes int header) {
        setHeader(header);
    }

    @Override
    @StringRes
    @Bindable
    public int getHeader() {
        return mHeader;
    }

    @Override
    public void setHeader(@StringRes int header) {
        mHeader = header;
        notifyPropertyChanged(BR.header);
    }
}
