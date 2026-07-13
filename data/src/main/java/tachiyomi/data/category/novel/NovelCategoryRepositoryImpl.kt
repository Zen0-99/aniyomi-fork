package tachiyomi.data.category.novel

import kotlinx.coroutines.flow.Flow
import tachiyomi.data.handlers.novel.NovelDatabaseHandler
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.novel.repository.NovelCategoryRepository

class NovelCategoryRepositoryImpl(
    private val handler: NovelDatabaseHandler,
) : NovelCategoryRepository {

    override fun subscribeAll(): Flow<List<Category>> {
        return handler.subscribeToList {
            novelcategoriesQueries.getCategories(::mapCategory)
        }
    }

    override fun subscribeAllVisible(): Flow<List<Category>> {
        return handler.subscribeToList {
            novelcategoriesQueries.getVisibleCategories(::mapCategory)
        }
    }

    override suspend fun getAllNovelCategories(): List<Category> {
        return handler.awaitList { novelcategoriesQueries.getCategories(::mapCategory) }
    }

    override suspend fun getNovelCategory(id: Long): Category? {
        return handler.awaitOneOrNull { novelcategoriesQueries.getCategory(id, ::mapCategory) }
    }

    override suspend fun getCategoriesByNovelId(novelId: Long): List<Category> {
        return handler.awaitList {
            novelcategoriesQueries.getCategoriesByNovelId(novelId, ::mapCategory)
        }
    }

    override suspend fun getVisibleCategoriesByNovelId(novelId: Long): List<Category> {
        return handler.awaitList {
            novelcategoriesQueries.getVisibleCategoriesByNovelId(novelId, ::mapCategory)
        }
    }

    override suspend fun insert(name: String, order: Long, flags: Long): Long? {
        return handler.await(inTransaction = true) {
            novelcategoriesQueries.insert(name, order, flags)
            novelcategoriesQueries.selectLastInsertedRowId().executeAsOneOrNull()
        }
    }

    override suspend fun update(
        categoryId: Long,
        name: String?,
        order: Long?,
        flags: Long?,
        hidden: Boolean?,
    ) {
        handler.await {
            novelcategoriesQueries.update(
                name = name,
                `order` = order,
                flags = flags,
                hidden = hidden?.let { if (it) 1L else 0L },
                categoryId = categoryId,
            )
        }
    }

    override suspend fun delete(categoryId: Long) {
        handler.await {
            novelcategoriesQueries.delete(categoryId)
        }
    }

    override suspend fun updatePartialNovelCategory(update: CategoryUpdate) {
        handler.await {
            novelcategoriesQueries.update(
                name = update.name,
                `order` = update.order,
                flags = update.flags,
                hidden = update.hidden?.let { if (it) 1L else 0L },
                categoryId = update.id,
            )
        }
    }

    override suspend fun updateAllNovelCategoryFlags(flags: Long?) {
        handler.await {
            novelcategoriesQueries.updateAllFlags(flags)
        }
    }

    private fun mapCategory(
        id: Long,
        name: String,
        order: Long,
        flags: Long,
        hidden: Long,
    ): Category = Category(
        id = id,
        name = name,
        order = order,
        flags = flags,
        hidden = hidden == 1L,
    )
}
