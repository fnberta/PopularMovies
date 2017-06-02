package ch.berta.fabio.popularmovies.features.details.component

import ch.berta.fabio.popularmovies.data.GetMovieDetailsResult
import ch.berta.fabio.popularmovies.data.LocalDbWriteResult
import ch.berta.fabio.popularmovies.data.MovieStorage
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

fun localMovieDbWrites(
        actions: Observable<DetailsAction>,
        movieStorage: MovieStorage,
        getMovieDetails: Observable<GetMovieDetailsResult>,
        movieDbId: Int
): Observable<LocalDbWriteResult> {
    val favMovie = actions
            .ofType(DetailsAction.FavClick::class.java)
            .withLatestFrom(getMovieDetails,
                    BiFunction<DetailsAction, GetMovieDetailsResult, GetMovieDetailsResult> { _, result -> result })
            .ofType(GetMovieDetailsResult.Success::class.java)
            .map { it.movieDetails }
            .flatMap {
                if (it.isFav) movieStorage.deleteMovieFromFav(it)
                else movieStorage.saveMovieAsFav(it)
            }

    val updateFavMovie =actions
            .ofType(DetailsAction.UpdateSwipe::class.java)
            .flatMap { movieStorage.updateFavMovie(movieDbId) }

    val dbWrites = listOf(favMovie, updateFavMovie)
    return Observable.merge(dbWrites)
}
