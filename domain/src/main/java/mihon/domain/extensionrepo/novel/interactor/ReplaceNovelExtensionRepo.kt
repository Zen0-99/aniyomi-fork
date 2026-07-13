package mihon.domain.extensionrepo.novel.interactor

import mihon.domain.extensionrepo.novel.repository.NovelExtensionRepoRepository
import mihon.domain.extensionrepo.model.ExtensionRepo

class ReplaceNovelExtensionRepo(
    private val repository: NovelExtensionRepoRepository,
) {
    suspend fun await(repo: ExtensionRepo) {
        repository.replaceRepo(repo)
    }
}
