package ch.berta.fabio.popularmovies

sealed class Maybe<out T : Any> {

    companion object {
        fun <T : Any> maybeOf(value: T?): Maybe<T> =
                if (value == null) Maybe.None else Maybe.Some(value)
    }

    data class Some<out T : Any>(val value: T) : Maybe<T>()
    object None : Maybe<Nothing>() {
        override fun toString(): String = "None"
    }

    fun isSome(): Boolean = this is Maybe.Some

    fun isNone(): Boolean = !isSome()

    fun <R : Any> map(mapper: (T) -> R): Maybe<R> = when (this) {
        is Maybe.Some -> maybeOf(mapper(value))
        is Maybe.None -> this
    }

    fun <R : Any> flatMap(mapper: (T) -> Maybe<R>): Maybe<R> = when (this) {
        is Maybe.Some -> mapper(value)
        is Maybe.None -> this
    }

    fun filter(predicate: (T) -> Boolean): Maybe<T> = when (this) {
        is Maybe.Some -> if (predicate(value)) this else Maybe.None
        is Maybe.None -> this
    }
}