package tachiyomi.domain.category.novel.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.novel.repository.NovelCategoryRepository

class GetVisibleNovelCategories(
    private val categoryRepository: NovelCategoryRepository,
) {

    fun subscribe(): Flow<List<Category>> {
        return categoryRepository.subscribeAllVisible()
    }

    suspend fun await(novelId: Long?): List<Category> {
        return if (novelId != null) {
            categoryRepository.getVisibleCategoriesByNovelId(novelId)
        } else {
            categoryRepository.subscribeAllVisible().first()
        }
    }
}
