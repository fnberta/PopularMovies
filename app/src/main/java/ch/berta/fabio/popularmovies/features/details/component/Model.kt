package ch.berta.fabio.popularmovies.features.details.component

import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.extensions.getDoubleFromColumn
import ch.berta.fabio.popularmovies.extensions.getLongFromColumn
import ch.berta.fabio.popularmovies.extensions.getStringFromColumn
import ch.berta.fabio.popularmovies.features.common.SnackbarMessage
import ch.berta.fabio.popularmovies.features.details.viewmodels.rows.*
import ch.berta.fabio.popularmovies.utils.formatDateLong
import org.jetbrains.anko.db.asMapSequence
import rx.Observable
import java.util.*

data class DetailsState(
        val title: String,
        val backdropPath: String?,
        val favoured: Boolean,
        val details: List<DetailsRowViewModel>,
        val snackbar: SnackbarMessage
)

typealias DetailsStateReducer = (DetailsState) -> DetailsState

data class VideosReviews(
        val videos: MutableList<DetailsVideoRowViewModel> = mutableListOf(),
        val reviews: MutableList<DetailsReviewRowViewModel> = mutableListOf()
)

fun createInitialState(): DetailsState =
        DetailsState("", "", false, emptyList(), SnackbarMessage(false))

fun model(
        initialState: DetailsState,
        actions: Observable<DetailsAction>
): Observable<DetailsState> {
    val detailsOnl = Observable.combineLatest(
            actions.ofType(DetailsAction.DetailsOnlLoad::class.java),
            actions.ofType(DetailsAction.DetailsOnlIdLoad::class.java),
            { (movieDetails), (detailsOnlIdResult) ->
                detailsOnlReducer(movieDetails, detailsOnlIdResult)
            }
    )
    val detailsFav = actions
            .ofType(DetailsAction.DetailsFavLoad::class.java)
            .map { it.detailsFavLoad }
            .map(::detailsFavReducer)
    val favSave = actions
            .ofType(DetailsAction.FavSave::class.java)
            .map { favSaveDeleteReducer(true) }
    val favDelete = actions
            .ofType(DetailsAction.FavDelete::class.java)
            .skipUntil(
                    detailsOnl) // only show icon change and snackbar if coming from an online movie
            .map { favSaveDeleteReducer(false) }

    val reducers = listOf(detailsOnl, detailsFav, favSave, favDelete)
    return Observable.merge(reducers)
            .scan(initialState, { state, reducer -> reducer(state) })
            .skip(1) // skip initial scan emission
}

fun detailsOnlReducer(movieDetails: MovieDetails,
                      detailsOnlIdResult: Maybe<Cursor>
): DetailsStateReducer = {
    val details = listOf<DetailsRowViewModel>(
            DetailsInfoRowViewModel(
                    movieDetails.posterPath,
                    formatDateLong(movieDetails.releaseDate),
                    movieDetails.voteAverage,
                    movieDetails.overview
            )
    ).let {
        if (movieDetails.videosPage.videos.isNotEmpty()) {
            it
                    .plus(DetailsHeaderRowViewModel(R.string.header_trailers))
                    .plus(movieDetails.videosPage.videos.map {
                        DetailsVideoRowViewModel(it.key, it.name, it.site, it.size)
                    })
        } else it
    }.let {
        if (movieDetails.reviewsPage.totalResults > 0) {
            it
                    .plus(DetailsHeaderRowViewModel(R.string.header_reviews))
                    .plus(movieDetails.reviewsPage.reviews.map {
                        DetailsReviewRowViewModel(it.author, it.content)
                    })
        } else it
    }
    val favoured = detailsOnlIdResult is Maybe.Some

    it.copy(
            title = movieDetails.title,
            backdropPath = movieDetails.backdropPath,
            details = details,
            favoured = favoured,
            snackbar = it.snackbar.copy(show = false)
    )
}

fun detailsFavReducer(detailsFavLoad: Maybe<Cursor>): DetailsStateReducer = {
    when (detailsFavLoad) {
        is Maybe.None -> it
        is Maybe.Some -> {
            val cursor = detailsFavLoad.value
            val title = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_TITLE)
            val backdropPath = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_BACKDROP)
            val details = makeMovieDetails(cursor)

            it.copy(
                    title = title,
                    backdropPath = backdropPath,
                    details = details,
                    favoured = true,
                    snackbar = it.snackbar.copy(show = false)
            )
        }
    }
}

private fun makeMovieDetails(cursor: Cursor): List<DetailsRowViewModel> {
    val releaseDate = Date(cursor.getLongFromColumn(MovieContract.Movie.COLUMN_RELEASE_DATE))
    val posterPath = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_POSTER)
    val plot = cursor.getStringFromColumn(MovieContract.Movie.COLUMN_PLOT)
    val voteAverage = cursor.getDoubleFromColumn(MovieContract.Movie.COLUMN_VOTE_AVERAGE)
    val details = listOf<DetailsRowViewModel>(
            DetailsInfoRowViewModel(
                    posterPath,
                    formatDateLong(releaseDate),
                    voteAverage,
                    plot
            )
    )

    val (videos, reviews) = cursor.asMapSequence()
            .fold(VideosReviews(), { videosReviews, entry ->
                videosReviews.apply {
                    if (entry[MovieContract.Video.COLUMN_KEY] != null) {
                        getVideo(entry).let {
                            if (!videos.contains(it)) videos.add(it)
                        }
                    }

                    if (entry[MovieContract.Review.COLUMN_AUTHOR] != null) {
                        getReview(entry).let {
                            if (!reviews.contains(it)) reviews.add(it)
                        }
                    }
                }
            })

    return details.let {
        if (videos.isNotEmpty()) {
            it
                    .plus(DetailsHeaderRowViewModel(R.string.header_trailers))
                    .plus(videos)
        } else it
    }.let {
        if (reviews.isNotEmpty()) {
            it
                    .plus(DetailsHeaderRowViewModel(R.string.header_reviews))
                    .plus(reviews)
        } else it
    }
}

private fun getVideo(entry: Map<String, Any?>): DetailsVideoRowViewModel {
    val key = entry[MovieContract.Video.COLUMN_KEY] as String
    val name = entry[MovieContract.Video.COLUMN_NAME] as String
    val site = entry[MovieContract.Video.COLUMN_SITE] as String
    val size = entry[MovieContract.Video.COLUMN_SIZE] as Long
    return DetailsVideoRowViewModel(key, name, site, size.toInt())
}

private fun getReview(entry: Map<String, Any?>): DetailsReviewRowViewModel {
    val author = entry[MovieContract.Review.COLUMN_AUTHOR] as String
    val content = entry[MovieContract.Review.COLUMN_CONTENT] as String
    return DetailsReviewRowViewModel(author, content)
}

private fun favSaveDeleteReducer(favoured: Boolean): DetailsStateReducer = {
    val snackbar = it.snackbar.copy(
            show = true,
            message = if (favoured) R.string.snackbar_movie_added_to_favorites else R.string.snackbar_movie_removed_from_favorites
    )

    it.copy(favoured = favoured, snackbar = snackbar)
}
