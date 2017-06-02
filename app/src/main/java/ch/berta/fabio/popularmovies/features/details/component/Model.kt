package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.GetMovieDetailsResult
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.features.common.SnackbarMessage
import ch.berta.fabio.popularmovies.features.details.vdo.rows.*
import ch.berta.fabio.popularmovies.features.details.view.DetailsArgs
import ch.berta.fabio.popularmovies.formatLong
import io.reactivex.Observable

data class DetailsState(
        val updating: Boolean = false,
        val title: String = "",
        val backdropPath: String = "",
        val favoured: Boolean = false,
        val details: List<DetailsRowViewData> = emptyList(),
        val snackbar: SnackbarMessage = SnackbarMessage(false)
)

typealias DetailsStateReducer = (DetailsState) -> DetailsState

fun model(
        actions: Observable<DetailsAction>,
        getMovieDetails: Observable<GetMovieDetailsResult>,
        localDbWriteResults: Observable<LocalDbWriteResult>,
        detailsArgs: DetailsArgs
): Observable<DetailsState> {
    val movieDetails = getMovieDetails
            .map(::movieDetailsReducer)

    val updateSwipes = actions
            .ofType(DetailsAction.UpdateSwipe::class.java)
            .map { updateSwipeReducer() }

    val favSave = localDbWriteResults
            .ofType(LocalDbWriteResult.SaveAsFav::class.java)
            .map(::saveAsFavReducer)
    val favDelete = localDbWriteResults
            .ofType(LocalDbWriteResult.DeleteFromFav::class.java)
            .filter { !detailsArgs.fromFavList }
            .map(::deleteFromFavReducer)
    val updateFav = localDbWriteResults
            .ofType(LocalDbWriteResult.UpdateFav::class.java)
            .map(::updateFavReducer)

    val initialState = DetailsState()
    val reducers = listOf(movieDetails, updateSwipes, favSave, favDelete, updateFav)
    return Observable.merge(reducers)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
}

private fun movieDetailsReducer(result: GetMovieDetailsResult): DetailsStateReducer = {
    when (result) {
        is GetMovieDetailsResult.Failure -> it.copy(snackbar = SnackbarMessage(false,
                R.string.snackbar_movie_load_reviews_videos_failed))
        is GetMovieDetailsResult.Success -> {
            val movieDetails = result.movieDetails
            val details = listOf<DetailsRowViewData>(
                    DetailsInfoRowViewData(
                            movieDetails.poster,
                            movieDetails.releaseDate.formatLong(),
                            movieDetails.voteAverage,
                            movieDetails.overview
                    )
            ).let {
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
                    backdropPath = movieDetails.backdrop,
                    details = details,
                    favoured = movieDetails.isFav

            )
        }
    }
}

private fun updateSwipeReducer(): DetailsStateReducer = { it.copy(updating = true) }

private fun saveAsFavReducer(result: LocalDbWriteResult.SaveAsFav): DetailsStateReducer = {
    val snackbar = SnackbarMessage(true,
            if (result.successful) R.string.snackbar_movie_added_to_favorites
            else R.string.snackbar_movie_insert_failed)
    it.copy(snackbar = snackbar)
}

private fun deleteFromFavReducer(result: LocalDbWriteResult.DeleteFromFav): DetailsStateReducer = {
    val snackbar = SnackbarMessage(true,
            if (result.successful) R.string.snackbar_movie_removed_from_favorites
            else R.string.snackbar_movie_delete_failed)
    it.copy(snackbar = snackbar)
}

private fun updateFavReducer(result: LocalDbWriteResult.UpdateFav): DetailsStateReducer = {
    val snackbar = SnackbarMessage(true,
            if (result.successful) R.string.snackbar_movie_updated
            else R.string.snackbar_movie_update_failed)
    it.copy(updating = false, snackbar = snackbar)
}
