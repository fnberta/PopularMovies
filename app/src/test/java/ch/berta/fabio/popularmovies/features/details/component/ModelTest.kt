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

import ch.berta.fabio.popularmovies.ImmediateSchedulersRule
import ch.berta.fabio.popularmovies.R
import ch.berta.fabio.popularmovies.data.MovieStorage
import ch.berta.fabio.popularmovies.data.dtos.Review
import ch.berta.fabio.popularmovies.data.dtos.Video
import ch.berta.fabio.popularmovies.data.dtos.YOUTUBE_BASE_URL
import ch.berta.fabio.popularmovies.data.dtos.YOU_TUBE
import ch.berta.fabio.popularmovies.data.localmoviedb.MovieDb
import ch.berta.fabio.popularmovies.data.localmoviedb.tables.*
import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.MovieInfo
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.ReviewsPage
import ch.berta.fabio.popularmovies.data.themoviedb.dtos.VideosPage
import ch.berta.fabio.popularmovies.features.details.vdos.rows.*
import ch.berta.fabio.popularmovies.features.grid.view.SelectedMovie
import ch.berta.fabio.popularmovies.formatLong
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.*

class ModelTest {

    private val theMovieDbService: TheMovieDbService = Mockito.mock(TheMovieDbService::class.java)
    private val movieDao: MovieDao = Mockito.mock(MovieDao::class.java)
    private val videoDao: VideoDao = Mockito.mock(VideoDao::class.java)
    private val reviewDao: ReviewDao = Mockito.mock(ReviewDao::class.java)
    private val movieDb: MovieDb = Mockito.mock(MovieDb::class.java)
    private val movieStorage = MovieStorage(theMovieDbService, movieDb)
    private val runnableCaptor = ArgumentCaptor.forClass(Runnable::class.java)

    private val actions: PublishRelay<DetailsAction> = PublishRelay.create()

    @Suppress("unused")
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun setUp() {
        Mockito.`when`(movieDb.movieDao()).thenReturn(movieDao)
        Mockito.`when`(movieDb.videoDao()).thenReturn(videoDao)
        Mockito.`when`(movieDb.reviewDao()).thenReturn(reviewDao)
    }

    @Test
    fun shouldClearTransientState() {
        val initialState = DetailsState(
                selectedVideoUrl = "http://some-url.com",
                movieDeletedFromFavScreen = true,
                message = R.string.snackbar_movie_delete_failed
        )
        val state = model(initialState, actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val expectedStates = listOf(DetailsState())

        actions.accept(DetailsAction.TransientClear)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldLoadSelectedFavMovieInfoWithVideosAndReviews() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(true)
        val initialDetails = getInitialDetails(selectedMovie)

        val movieEntity = getMovieEntity(selectedMovie.id)
        Mockito.`when`(movieDao.getById(selectedMovie.id)).thenReturn(Flowable.just(movieEntity))
        val videos = listOf(getVideoEntity(selectedMovie.id))
        Mockito.`when`(videoDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(videos))
        val reviews = listOf(getReviewEntity(selectedMovie.id))
        Mockito.`when`(reviewDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(reviews))
        val fetchedDetails = getFetchedDetailsFav(movieEntity, videos, reviews)

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetails,
                        favoured = true
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldLoadSelectedOnlMovieInfoWithVideosAndReviews() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(false)
        val initialDetails = getInitialDetails(selectedMovie)

        val reviewsPage = getReviewsPage()
        val videosPage = getVideosPage()
        val movieInfo = getMovieInfo(selectedMovie.id, reviewsPage, videosPage)
        Mockito.`when`(theMovieDbService.loadMovieInfo(selectedMovie.id)).thenReturn(Single.just(movieInfo))
        Mockito.`when`(movieDao.existsById(selectedMovie.id)).thenReturn(Flowable.just(0))
        val fetchedDetails = getFetchedDetailsOnl(movieInfo, videosPage, reviewsPage)

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetails,
                        favoured = false
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldLoadSelectedFavMovieInfoWithEmptyVideosAndReviews() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(true)
        val initialDetails = getInitialDetails(selectedMovie)
        val movieEntity = getMovieEntity(selectedMovie.id)
        Mockito.`when`(movieDao.getById(selectedMovie.id)).thenReturn(Flowable.just(movieEntity))
        Mockito.`when`(videoDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(emptyList()))
        Mockito.`when`(reviewDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(emptyList()))
        val fetchedDetails = listOf<DetailsRowViewData>(
                DetailsInfoRowViewData(
                        movieEntity.poster,
                        movieEntity.releaseDate.formatLong(),
                        movieEntity.voteAverage,
                        movieEntity.overview
                ))

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetails,
                        favoured = true
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectErrorOnSelectedMovieLoadFail() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(true)
        val initialDetails = getInitialDetails(selectedMovie)
        Mockito.`when`(movieDao.getById(0)).thenReturn(Flowable.error(Throwable("details error")))
        Mockito.`when`(videoDao.getByMovieId(0)).thenReturn(Flowable.just(emptyList()))
        Mockito.`when`(reviewDao.getByMovieId(0)).thenReturn(Flowable.just(emptyList()))

        val detailsWithoutLoading = initialDetails.minus(initialDetails.last())
        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = detailsWithoutLoading,
                        message = R.string.snackbar_movie_load_reviews_videos_failed
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = detailsWithoutLoading
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))
        actions.accept(DetailsAction.TransientClear)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldHaveCorrectVideoUrlOnVideoClick() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val videoRow = DetailsVideoRowViewData("key", "name", YOU_TUBE, 10)
        val videoUrl = "$YOUTUBE_BASE_URL${videoRow.key}"
        val expectedStates = listOf(DetailsState(selectedVideoUrl = videoUrl))

        actions.accept(DetailsAction.VideoClick(videoRow))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldAddMovieToFavIfNotFavOnFabClick() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(false)
        val initialDetails = getInitialDetails(selectedMovie)

        val videosPage = getVideosPage()
        val reviewsPage = getReviewsPage()
        val movieInfo = getMovieInfo(selectedMovie.id, reviewsPage, videosPage)
        val fetchedDetails = getFetchedDetailsOnl(movieInfo, videosPage, reviewsPage)
        Mockito.`when`(theMovieDbService.loadMovieInfo(selectedMovie.id)).thenReturn(Single.just(movieInfo))
        val existsQuery = BehaviorRelay.create<Int>()
        existsQuery.accept(0)
        Mockito.`when`(movieDao.existsById(selectedMovie.id)).thenReturn(
                existsQuery.toFlowable(BackpressureStrategy.LATEST))

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetails,
                        favoured = false
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetails,
                        message = R.string.snackbar_movie_added_to_favorites
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetails,
                        favoured = true
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))
        actions.accept(DetailsAction.FavClick)
        actions.accept(DetailsAction.TransientClear)
        existsQuery.accept(1)

        Mockito.verify(movieDb).runInTransaction(runnableCaptor.capture())
        runnableCaptor.value.run()
        val movieEntity = getMovieEntity(movieInfo)
        Mockito.verify(movieDao).insert(movieEntity)
        Mockito.verify(videoDao).insertAll(listOf(getVideoEntity(selectedMovie.id, videosPage)))
        Mockito.verify(reviewDao).insertAll(listOf(getReviewEntity(selectedMovie.id, reviewsPage)))

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldDeleteMovieFromFavIfFavOnFabClick() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovieFav = getSelectedMovie(true)
        Mockito.`when`(movieDao.deleteById(selectedMovieFav.id)).thenReturn(1)
        val initialDetailsFav = getInitialDetails(selectedMovieFav)
        val movieEntity = getMovieEntity(selectedMovieFav.id)
        Mockito.`when`(movieDao.getById(selectedMovieFav.id)).thenReturn(Flowable.just(movieEntity))
        val videos = listOf(getVideoEntity(selectedMovieFav.id))
        Mockito.`when`(videoDao.getByMovieId(selectedMovieFav.id)).thenReturn(Flowable.just(videos))
        val reviews = listOf(getReviewEntity(selectedMovieFav.id))
        Mockito.`when`(reviewDao.getByMovieId(selectedMovieFav.id)).thenReturn(Flowable.just(reviews))
        val fetchedDetailsFav = getFetchedDetailsFav(movieEntity, videos, reviews)

        val selectedMovieOnl = getSelectedMovie(false)
        val initialDetailsOnl = getInitialDetails(selectedMovieOnl)
        val reviewsPage = getReviewsPage()
        val videosPage = getVideosPage()
        val movieInfo = getMovieInfo(selectedMovieOnl.id, reviewsPage, videosPage)
        Mockito.`when`(theMovieDbService.loadMovieInfo(selectedMovieOnl.id)).thenReturn(Single.just(movieInfo))
        val existsQuery = BehaviorRelay.create<Int>()
        Mockito.`when`(movieDao.existsById(selectedMovieOnl.id)).thenReturn(
                existsQuery.toFlowable(BackpressureStrategy.LATEST))
        existsQuery.accept(1)
        val fetchedDetailsOnl = getFetchedDetailsOnl(movieInfo, videosPage, reviewsPage)

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = true,
                        title = selectedMovieFav.title,
                        backdrop = selectedMovieFav.backdrop,
                        details = initialDetailsFav
                ),
                DetailsState(
                        updateEnabled = true,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetailsFav,
                        favoured = true
                ),
                DetailsState(
                        message = R.string.snackbar_movie_removed_from_favorites,
                        movieDeletedFromFavScreen = true
                ),
                DetailsState(),
                DetailsState(
                        updateEnabled = false,
                        title = selectedMovieOnl.title,
                        backdrop = selectedMovieOnl.backdrop,
                        details = initialDetailsOnl
                ),
                DetailsState(
                        updateEnabled = false,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetailsOnl,
                        favoured = true
                ),
                DetailsState(
                        updateEnabled = false,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetailsOnl,
                        favoured = true,
                        message = R.string.snackbar_movie_removed_from_favorites
                ),
                DetailsState(
                        updateEnabled = false,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetailsOnl,
                        favoured = true
                ),
                DetailsState(
                        updateEnabled = false,
                        title = movieInfo.title,
                        backdrop = movieInfo.backdrop,
                        details = fetchedDetailsOnl,
                        favoured = false
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovieFav))
        actions.accept(DetailsAction.FavClick)
        actions.accept(DetailsAction.TransientClear)
        actions.accept(DetailsAction.MovieSelected(selectedMovieOnl))
        actions.accept(DetailsAction.FavClick)
        actions.accept(DetailsAction.TransientClear)
        existsQuery.accept(0)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    @Test
    fun shouldUpdateFavMovieOnRefreshSwipe() {
        val state = model(DetailsState(), actions, movieStorage)
        val observer = state.test()
        // assert nothing is emitted until an event happens
        observer.assertNoValues()

        val selectedMovie = getSelectedMovie(true)
        val initialDetails = getInitialDetails(selectedMovie)

        val movieEntity = getMovieEntity(selectedMovie.id)
        Mockito.`when`(movieDao.getById(selectedMovie.id)).thenReturn(Flowable.just(movieEntity))
        val videos = listOf(getVideoEntity(selectedMovie.id))
        Mockito.`when`(videoDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(videos))
        val reviews = listOf(getReviewEntity(selectedMovie.id))
        Mockito.`when`(reviewDao.getByMovieId(selectedMovie.id)).thenReturn(Flowable.just(reviews))
        val fetchedDetails = getFetchedDetailsFav(movieEntity, videos, reviews)

        val videosPage = getVideosPage()
        val reviewPage = getReviewsPage()
        val movieInfo = getMovieInfo(selectedMovie.id, reviewPage, videosPage)
        Mockito.`when`(theMovieDbService.loadMovieInfo(selectedMovie.id)).thenReturn(Single.just(movieInfo))

        val expectedStates = listOf(
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = selectedMovie.title,
                        backdrop = selectedMovie.backdrop,
                        details = initialDetails
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetails,
                        favoured = true
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetails,
                        favoured = true,
                        updating = true
                ),
                DetailsState(
                        updateEnabled = selectedMovie.fromFav,
                        title = movieEntity.title,
                        backdrop = movieEntity.backdrop,
                        details = fetchedDetails,
                        favoured = true,
                        message = R.string.snackbar_movie_updated
                )
        )

        actions.accept(DetailsAction.MovieSelected(selectedMovie))
        actions.accept(DetailsAction.UpdateSwipe)

        Mockito.verify(movieDb).runInTransaction(runnableCaptor.capture())
        runnableCaptor.value.run()
        val updatedMovieEntity = getMovieEntity(movieInfo)
        Mockito.verify(movieDao).update(updatedMovieEntity)
        Mockito.verify(videoDao).deleteByMovieId(selectedMovie.id)
        Mockito.verify(reviewDao).deleteByMovieId(selectedMovie.id)

        observer.assertValues(*expectedStates.toTypedArray())
        observer.assertNoErrors()
        observer.assertNotComplete()
    }

    private fun getInitialDetails(selectedMovie: SelectedMovie): List<DetailsRowViewData> = listOf(
            DetailsInfoRowViewData(
                    selectedMovie.poster,
                    selectedMovie.releaseDate,
                    selectedMovie.voteAverage,
                    selectedMovie.overview
            ),
            DetailsLoadingRowViewData()
    )

    private fun getFetchedDetailsOnl(
            movieInfo: MovieInfo,
            videosPage: VideosPage,
            reviewsPage: ReviewsPage
    ): List<DetailsRowViewData> = listOf<DetailsRowViewData>(
            DetailsInfoRowViewData(
                    movieInfo.poster,
                    movieInfo.releaseDate.formatLong(),
                    movieInfo.voteAverage,
                    movieInfo.overview
            ))
            .plus(DetailsHeaderRowViewData(R.string.header_trailers))
            .plus(videosPage.videos.map { DetailsVideoRowViewData(it.key, it.name, it.site, it.size) })
            .plus(DetailsHeaderRowViewData(R.string.header_reviews))
            .plus(reviewsPage.reviews.map { DetailsReviewRowViewData(it.author, it.content) })

    private fun getFetchedDetailsFav(
            movieEntity: MovieEntity,
            videos: List<VideoEntity>,
            reviews: List<ReviewEntity>
    ): List<DetailsRowViewData> = listOf<DetailsRowViewData>(
            DetailsInfoRowViewData(
                    movieEntity.poster,
                    movieEntity.releaseDate.formatLong(),
                    movieEntity.voteAverage,
                    movieEntity.overview
            ))
            .plus(DetailsHeaderRowViewData(R.string.header_trailers))
            .plus(videos.map { DetailsVideoRowViewData(it.key, it.name, it.site, it.size) })
            .plus(DetailsHeaderRowViewData(R.string.header_reviews))
            .plus(reviews.map { DetailsReviewRowViewData(it.author, it.content) })

    private fun getSelectedMovie(fromFav: Boolean) =
            SelectedMovie(0, "title", "releaseData", "overview", 12.5, "poster", "backdrop", fromFav)

    private fun getMovieEntity(movieId: Int) =
            MovieEntity(movieId, "title", Date(), 10.4, "overview", "poster", "backdrop")

    private fun getMovieEntity(movieInfo: MovieInfo) = MovieEntity(movieInfo.id, movieInfo.title, movieInfo.releaseDate,
            movieInfo.voteAverage, movieInfo.overview, movieInfo.poster, movieInfo.backdrop)

    private fun getReviewEntity(movieId: Int) = ReviewEntity(movieId, "author", "content", "url")

    private fun getReviewEntity(movieId: Int, reviewsPage: ReviewsPage): ReviewEntity {
        val review = reviewsPage.reviews[0]
        return ReviewEntity(movieId, review.author, review.content, review.url)
    }

    private fun getVideoEntity(movieId: Int) = VideoEntity(movieId, "name", "key", YOU_TUBE, 10, "type")

    private fun getVideoEntity(movieId: Int, videosPage: VideosPage): VideoEntity {
        val video = videosPage.videos[0]
        return VideoEntity(movieId, video.name, video.key, video.site, video.size, video.type)
    }

    private fun getMovieInfo(movieId: Int, reviewsPage: ReviewsPage, videosPage: VideosPage) =
            MovieInfo(movieId, "title", "overview", "backdrop", Date(), "poster", 10.2, reviewsPage, videosPage)

    private fun getVideosPage() = VideosPage(listOf(Video("name", "key", YOU_TUBE, 10, "type")))

    private fun getReviewsPage() = ReviewsPage(0, listOf(Review("author", "content", "url")), 1, 1)
}