package tachiyomi.domain.savedsearches.anime.interactor

import tachiyomi.domain.savedsearches.anime.repository.AnimeSavedSearchRepository
import tachiyomi.domain.savedsearches.model.SavedSearch

class InsertAnimeSavedSearch(
    private val repository: AnimeSavedSearchRepository,
) {
    suspend fun await(savedSearch: SavedSearch) {
        repository.insertSavedSearch(savedSearch)
    }
}
