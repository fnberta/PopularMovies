/*
 * Copyright (c) 2016 Fabio Berta
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

package ch.berta.fabio.popularmovies.presentation.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import ch.berta.fabio.popularmovies.domain.models.SnackbarAction;
import ch.berta.fabio.popularmovies.presentation.common.viewmodels.ViewModel;
import ch.berta.fabio.popularmovies.utils.WorkerUtils;

/**
 * Provides an abstract base class for {@link Fragment} to extend from.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseFragment<T extends ViewModel>
        extends Fragment
        implements ViewModel.ViewInteractionListener {

    @Inject
    protected T mViewModel;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mViewModel.saveState(outState);
    }

    @Override
    public void showMessage(@StringRes int text, @Nullable SnackbarAction action) {
        Snackbar snackbar = Snackbar.make(getSnackbarView(), text, Snackbar.LENGTH_LONG);
        if (action != null) {
            snackbar.setAction(action.getActionText(), action);
        }
        snackbar.show();
    }

    protected abstract View getSnackbarView();

    @Override
    public void removeWorker(@NonNull String workerTag) {
        WorkerUtils.removeWorker(getFragmentManager(), workerTag);
    }
}
