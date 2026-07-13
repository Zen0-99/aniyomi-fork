package tachiyomi.domain.category.novel.interactor

import tachiyomi.domain.entries.novel.repository.NovelRepository

class SetNovelCategories(
    private val novelRepository: NovelRepository,
) {

    suspend fun await(novelId: Long, categoryIds: List<Long>) {
        novelRepository.setNovelCategories(novelId, categoryIds)
    }
}
