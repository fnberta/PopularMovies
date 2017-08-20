/*
 * Copyright (c) 2017 Fabio Berta
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

package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.GetMovieDetailsResult
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.dtos.YOUTUBE_BASE_URL
import ch.berta.fabio.popularmovies.features.details.vdos.rows.*
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.formatLong
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber

typealias DetailsStateReducer = (DetailsState) -> DetailsState

data class DetailsWithFromFav(val movieDetails: MovieDetails, val fromFav: Boolean)

fun model(
        initialState: DetailsState,
        actions: Observable<DetailsAction>,
        movieStorage: MovieStorage
): Observable<DetailsState> {
    val movieSelections = actions
            .ofType(DetailsAction.MovieSelected::class.java)
            .map { it.selectedMovie }
            .share()

    val movieDetails = movieSelections
            .switchMap { movieStorage.getMovieDetails(it.id, it.fromFav) }
            .share()

    val favClicksWithDetails = actions
            .ofType(DetailsAction.FavClick::class.java)
            .withLatestFrom(movieSelections, movieDetails.ofType(GetMovieDetailsResult.Success::class.java),
                    { _, selectedMovie, result -> DetailsWithFromFav(result.movieDetails, selectedMovie.fromFav) })
            .share()

    val clearTransient = actions
            .ofType(DetailsAction.TransientClear::class.java)
            .map { clearTransientReducer() }

    val sortSelections = actions
            .ofType(DetailsAction.SortSelection::class.java)
            .map { sortSelectionReducer() }

    val movieInfo = movieSelections.map(::movieInfoReducer)
    val movieDetailsInfo = movieDetails.map(::movieDetailsReducer)

    val favAddMovie = favClicksWithDetails
            .filter { !it.movieDetails.isFav }
            .switchMap { movieStorage.saveMovieAsFav(it.movieDetails) }
            .map(::favAddMovieReducer)
    val favDeleteMovie = favClicksWithDetails
            .filter { it.movieDetails.isFav }
            .switchMap { details ->
                movieStorage.deleteMovieFromFav(details.movieDetails.id).map { Pair(it, details.fromFav) }
            }
            .map { (result, fromFav) -> favDeleteMovieReducer(result, fromFav) }
    val favUpdateMovie = actions
            .ofType(DetailsAction.UpdateSwipe::class.java)
            .withLatestFrom(movieSelections, { _, (id) -> id })
            .switchMap {
                movieStorage.updateFavMovie(it)
                        .map(::favUpdateMovieReducer)
                        .startWith(updateSwipeReducer())
            }

    val videoClicks = actions
            .ofType(DetailsAction.VideoClick::class.java)
            .map { "$YOUTUBE_BASE_URL${it.videoViewModel.key}" }
            .map(::videoClicksReducer)

    val reducers = listOf(clearTransient, sortSelections, movieInfo, movieDetailsInfo, favAddMovie, favDeleteMovie,
            favUpdateMovie, videoClicks)
    return Observable.merge(reducers)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
            .distinctUntilChanged()
}

private fun clearTransientReducer(): DetailsStateReducer = {
    it.copy(message = null, selectedVideoUrl = null, movieDeletedFromFavScreen = false)
}

private fun sortSelectionReducer(): DetailsStateReducer = {
    DetailsState()
}

private fun movieInfoReducer(selectedMovie: SelectedMovie): DetailsStateReducer = {
    val details = listOf(
            DetailsInfoRowViewData(
                    selectedMovie.poster,
                    selectedMovie.releaseDate,
                    selectedMovie.voteAverage,
                    selectedMovie.overview
            ),
            DetailsLoadingRowViewData()
    )
    it.copy(
            updateEnabled = selectedMovie.fromFav,
            title = selectedMovie.title,
            backdrop = selectedMovie.backdrop,
            details = details
    )
}

private fun movieDetailsReducer(result: GetMovieDetailsResult): DetailsStateReducer = {
    when (result) {
        is GetMovieDetailsResult.Failure -> it.copy(
                details = it.details.minus(it.details.last()),
                message = R.string.snackbar_movie_load_reviews_videos_failed
        )
        is GetMovieDetailsResult.Success -> {
            val movieDetails = result.movieDetails
            val details = listOf<DetailsRowViewData>(DetailsInfoRowViewData(
                    movieDetails.poster,
                    movieDetails.releaseDate.formatLong(),
                    movieDetails.voteAverage,
                    movieDetails.overview
            )).let {
                if (movieDetails.videos.isNotEmpty()) {
                    it
                            .plus(DetailsHeaderRowViewData(R.string.header_trailers))
                            .plus(movieDetails.videos.map {
                                DetailsVideoRowViewData(it.key, it.name, it.site, it.size)
                            })
                } else it
            }.let {
                if (movieDetails.reviews.isNotEmpty()) {
                    it
                            .plus(DetailsHeaderRowViewData(R.string.header_reviews))
                            .plus(movieDetails.reviews.map {
                                DetailsReviewRowViewData(it.author, it.content)
                            })
                } else it
            }
            it.copy(
                    title = movieDetails.title,
                    backdrop = movieDetails.backdrop,
                    details = details,
                    favoured = movieDetails.isFav
            )
        }
    }
}

private fun updateSwipeReducer(): DetailsStateReducer = { it.copy(updating = true) }

private fun favAddMovieReducer(result: LocalDbWriteResult.SaveAsFav): DetailsStateReducer = {
    val message = if (result.successful) R.string.snackbar_movie_added_to_favorites else R.string.snackbar_movie_insert_failed
    it.copy(message = message)
}

private fun favDeleteMovieReducer(result: LocalDbWriteResult.DeleteFromFav, fromFav: Boolean): DetailsStateReducer = {
    if (result.successful && fromFav) {
        DetailsState(movieDeletedFromFavScreen = true, message = R.string.snackbar_movie_removed_from_favorites)
    } else {
        val message = if (result.successful) R.string.snackbar_movie_removed_from_favorites else R.string.snackbar_movie_delete_failed
        it.copy(message = message)
    }
}

private fun favUpdateMovieReducer(result: LocalDbWriteResult.UpdateFav): DetailsStateReducer = {
    val message = if (result.successful) R.string.snackbar_movie_updated else R.string.snackbar_movie_update_failed
    it.copy(updating = false, message = message)
}

private fun videoClicksReducer(videoUrl: String): DetailsStateReducer = {
    it.copy(selectedVideoUrl = videoUrl)
}
