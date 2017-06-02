package ch.berta.fabio.popularmovies.di.modules

import ch.berta.fabio.popularmovies.data.themoviedb.TheMovieDbService
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton


const val MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/"
const val DATE_FORMAT = "yyyy-mm-dd"

/**
 * Defines the instantiation of the query movies service.
 */
@Module
class MovieServiceModule {

    @Provides
    @Singleton
    internal fun providesGson(): Gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
                private val dateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

                override fun deserialize(
                        json: JsonElement, typeOfT: Type,
                        context: JsonDeserializationContext
                ): Date {
                    try {
                        return dateFormat.parse(json.asString)
                    } catch (e: ParseException) {
                        return Date()
                    }
                }
            })
            .create()

    @Provides
    @Singleton
    internal fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()

    @Provides
    @Singleton
    internal fun provideMoviesService(gson: Gson, okHttpClient: OkHttpClient): TheMovieDbService = Retrofit.Builder()
            .baseUrl(ch.berta.fabio.popularmovies.di.modules.MOVIE_DB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(TheMovieDbService::class.java)
}
