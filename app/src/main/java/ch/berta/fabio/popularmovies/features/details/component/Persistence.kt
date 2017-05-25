package ch.berta.fabio.popularmovies.features.details.component

import android.content.ContentProviderOperation
import android.database.Cursor
import ch.berta.fabio.popularmovies.Maybe
import ch.berta.fabio.popularmovies.data.services.dtos.MovieDetails
import ch.berta.fabio.popularmovies.data.services.dtos.Video
import ch.berta.fabio.popularmovies.data.storage.MovieContract
import ch.berta.fabio.popularmovies.effects.ContentProviderTarget
import ch.berta.fabio.popularmovies.extensions.getRowId
import rx.Observable

data class DetailsWithId(
        val movieDetails: MovieDetails,
        val onlIdResult: Maybe<Cursor>
)

const val SAVE_AS_FAV = "SAVE_AS_FAV"
const val DELETE_FAV = "DELETE_FAV"

fun persist(actions: Observable<DetailsAction>): Observable<ContentProviderTarget> {
//    val updateSwipes = actions
//            .ofType(DetailsAction.UpdateSwipe::class.java)
//            .map { DetailsSink.Persistence(Unit) }
    val onlDetails = actions.ofType(DetailsAction.DetailsOnlLoad::class.java)
    val onlId = actions.ofType(DetailsAction.DetailsOnlIdLoad::class.java)
    val favClicks = actions.ofType(DetailsAction.FavClick::class.java)
    val favClicksOnl = favClicks
            .withLatestFrom(onlDetails, onlId,
                    { _, (movieDetails), (detailsOnlIdResult) ->
                        DetailsWithId(movieDetails, detailsOnlIdResult)
                    })
            .map {
                when (it.onlIdResult) {
                    is Maybe.Some -> getDeleteOperations(it.onlIdResult.value.getRowId())
                    is Maybe.None -> getInsertOperations(it.movieDetails)
                }
            }

    val favDetails = actions
            .ofType(DetailsAction.DetailsFavLoad::class.java)
            .filter { it.detailsFavLoad.isSome() }
            .map { it.detailsFavLoad as Maybe.Some<Cursor> }
            .map { it.value }
    val favClicksFav = favClicks
            .withLatestFrom(favDetails, { _, cursor -> cursor })
            .map { getDeleteOperations(it.getRowId()) }

    val persistence = listOf(favClicksOnl, favClicksFav)
    return Observable.merge(persistence)
}

private fun getInsertOperations(movieDetails: MovieDetails): ContentProviderTarget.BatchWrite {
    val operations = listOf(
            ContentProviderOperation
                    .newInsert(MovieContract.Movie.contentUri)
                    .withValues(movieDetails.getContentValuesEntry())
                    .build()
    )
            .plus(movieDetails.reviewsPage.reviews
                    .map {
                        ContentProviderOperation
                                .newInsert(MovieContract.Review.contentUri)
                                .withValueBackReference(MovieContract.Review.COLUMN_MOVIE_ID, 0)
                                .withValues(it.getContentValuesEntry())
                                .build()
                    })
            .plus(movieDetails.videosPage.videos
                    .filter(Video::siteIsYouTube)
                    .map {
                        ContentProviderOperation
                                .newInsert(MovieContract.Video.contentUri)
                                .withValueBackReference(MovieContract.Video.COLUMN_MOVIE_ID, 0)
                                .withValues(it.getContentValuesEntry())
                                .build()
                    })

    return ContentProviderTarget.BatchWrite(SAVE_AS_FAV,
            MovieContract.CONTENT_AUTHORITY, operations as ArrayList)
}

private fun getDeleteOperations(movieRowId: Long): ContentProviderTarget.Delete =
        ContentProviderTarget.Delete(DELETE_FAV, MovieContract.Movie.buildMovieUri(movieRowId))