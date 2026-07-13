package tachiyomi.domain.savedsearches.anime.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.savedsearches.anime.repository.AnimeSavedSearchRepository
import tachiyomi.domain.savedsearches.model.SavedSearch

class GetAnimeSavedSearches(
    private val repository: AnimeSavedSearchRepository,
) {
    fun subscribe(sourceId: Long): Flow<List<SavedSearch>> {
        return repository.getSavedSearchesBySourceId(sourceId)
    }
}
