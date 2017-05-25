package ch.berta.fabio.popularmovies.features.grid.component

import android.support.v4.app.FragmentTransaction
import ch.berta.fabio.popularmovies.effects.FragmentAction
import ch.berta.fabio.popularmovies.effects.NavigationTarget
import ch.berta.fabio.popularmovies.features.details.view.DetailsActivity
import ch.berta.fabio.popularmovies.features.details.view.DetailsActivityArgs
import ch.berta.fabio.popularmovies.features.grid.SortOption
import ch.berta.fabio.popularmovies.features.grid.view.GridFavFragment
import ch.berta.fabio.popularmovies.features.grid.view.GridOnlFragment
import rx.Observable

const val RQ_DETAILS = 1

fun navigate(actions: Observable<GridAction>): Observable<NavigationTarget> {
    val movieClicks = actions
            .ofType(GridAction.MovieClick::class.java)
            .map {
                when (it.selectedMovie) {
                    is SelectedMovie.Fav -> NavigationTarget.Activity(DetailsActivity::class.java,
                            DetailsActivityArgs.Fav(it.selectedMovie.id), RQ_DETAILS,
                            it.selectedMovie.posterView)
                    is SelectedMovie.Onl -> NavigationTarget.Activity(DetailsActivity::class.java,
                            DetailsActivityArgs.Onl(it.selectedMovie.id), RQ_DETAILS,
                            it.selectedMovie.posterView)
                }
            }
    val sortSelections = actions
            .ofType(GridAction.SortSelection::class.java)
    val openFav = sortSelections
            .filter { it.sort.option == SortOption.SORT_FAVORITE }
            .map { NavigationTarget.Fragment(GridFavFragment(), FragmentAction.REPLACE) }
    val openOnl = sortSelections
            .filter {
                it.sort != it.sortPrev && it.sort.option != SortOption.SORT_FAVORITE
                        && it.sortPrev.option == SortOption.SORT_FAVORITE
            }
            .map {
                NavigationTarget.Fragment(GridOnlFragment.newInstance(it.sort.value),
                        FragmentAction.REPLACE, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

    val navigators = listOf(movieClicks, openFav, openOnl)
    return Observable.merge(navigators)
}
