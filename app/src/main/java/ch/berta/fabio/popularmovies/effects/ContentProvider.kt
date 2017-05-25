package ch.berta.fabio.popularmovies.effects

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.net.Uri
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.exceptions.Exceptions
import rx.schedulers.Schedulers

sealed class ContentProviderTarget {
    abstract val id: String

    data class BatchWrite(
            override val id: String,
            val authority: String,
            val ops: ArrayList<ContentProviderOperation>
    ) : ContentProviderTarget()

    data class Delete(override val id: String, val uri: Uri) : ContentProviderTarget()
}

data class ContentProviderResult(
        val id: String,
        val successful: Boolean
)

fun persistTo(
        resolver: ContentResolver,
        target: ContentProviderTarget
): Observable<ContentProviderResult> {
    val operation = when (target) {
        is ContentProviderTarget.BatchWrite -> Observable.just(target)
                .map {
                    try {
                        resolver.applyBatch(it.authority, it.ops)
                    } catch(e: Exception) {
                        throw Exceptions.propagate(e)
                    }
                }
        is ContentProviderTarget.Delete -> Observable.just(target)
                .map {
                    try {
                        resolver.delete(it.uri, null, null)
                    } catch(e: Exception) {
                        throw Exceptions.propagate(e)
                    }
                }
    }

    return operation
            .map { ContentProviderResult(target.id, true) }
            .onErrorReturn { ContentProviderResult(target.id, false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
