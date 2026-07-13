package tachiyomi.domain.savedsearches.novel.interactor

import tachiyomi.domain.savedsearches.novel.repository.NovelSavedSearchRepository
import tachiyomi.domain.savedsearches.model.SavedSearch

class InsertNovelSavedSearch(
    private val repository: NovelSavedSearchRepository,
) {
    suspend fun await(savedSearch: SavedSearch) {
        repository.insertSavedSearch(savedSearch)
    }
}
