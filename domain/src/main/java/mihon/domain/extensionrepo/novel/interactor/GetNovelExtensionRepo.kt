package mihon.domain.extensionrepo.novel.interactor

import kotlinx.coroutines.flow.Flow
import mihon.domain.extensionrepo.novel.repository.NovelExtensionRepoRepository
import mihon.domain.extensionrepo.model.ExtensionRepo

class GetNovelExtensionRepo(
    private val repository: NovelExtensionRepoRepository,
) {
    fun subscribeAll(): Flow<List<ExtensionRepo>> = repository.subscribeAll()

    suspend fun getAll(): List<ExtensionRepo> = repository.getAll()
}
