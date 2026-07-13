package tachiyomi.domain.entries.novel.interactor

import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.entries.novel.repository.NovelRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NetworkToLocalNovel(
    private val novelRepository: NovelRepository = Injekt.get(),
) {
    suspend fun await(novel: Novel): Novel {
        val localNovel = novelRepository.getNovelByUrlAndSourceId(novel.url, novel.source)
        return when {
            localNovel == null || !localNovel.favorite -> {
                val newNovel = novel.copy(id = localNovel?.id ?: -1L)
                val id = novelRepository.insertNovel(newNovel)
                newNovel.copy(id = id ?: -1L)
            }
            else -> localNovel.copy(
                title = novel.title,
                artist = novel.artist ?: localNovel.artist,
                author = novel.author ?: localNovel.author,
                description = novel.description ?: localNovel.description,
                genre = novel.genre ?: localNovel.genre,
                status = novel.status,
                thumbnailUrl = novel.thumbnailUrl ?: localNovel.thumbnailUrl,
                updateStrategy = novel.updateStrategy,
                initialized = novel.initialized,
            )
        }
    }
}
