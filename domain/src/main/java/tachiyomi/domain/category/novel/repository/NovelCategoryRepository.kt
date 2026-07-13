package tachiyomi.domain.category.novel.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate

interface NovelCategoryRepository {

    fun subscribeAll(): Flow<List<Category>>

    fun subscribeAllVisible(): Flow<List<Category>>

    suspend fun getAllNovelCategories(): List<Category>

    suspend fun getNovelCategory(id: Long): Category?

    suspend fun getCategoriesByNovelId(novelId: Long): List<Category>

    suspend fun getVisibleCategoriesByNovelId(novelId: Long): List<Category>

    suspend fun insert(name: String, order: Long, flags: Long): Long?

    suspend fun update(categoryId: Long, name: String?, order: Long?, flags: Long?, hidden: Boolean?)

    suspend fun updatePartialNovelCategory(update: CategoryUpdate)

    suspend fun updateAllNovelCategoryFlags(flags: Long?)

    suspend fun delete(categoryId: Long)
}
