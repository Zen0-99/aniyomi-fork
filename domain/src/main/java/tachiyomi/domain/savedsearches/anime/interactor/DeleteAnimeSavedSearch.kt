package tachiyomi.domain.savedsearches.anime.interactor

import tachiyomi.domain.savedsearches.anime.repository.AnimeSavedSearchRepository

class DeleteAnimeSavedSearch(
    private val repository: AnimeSavedSearchRepository,
) {
    suspend fun await(id: Long) {
        repository.deleteSavedSearch(id)
    }
}
