package eu.kanade.domain.entries.novel.interactor

import eu.kanade.domain.entries.novel.model.hasCustomCover
import eu.kanade.tachiyomi.data.cache.NovelCoverCache
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.entries.novel.model.NovelUpdate
import tachiyomi.domain.entries.novel.repository.NovelRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant

class UpdateNovel(
    private val novelRepository: NovelRepository,
    private val coverCache: NovelCoverCache = Injekt.get(),
) {

    suspend fun await(novelUpdate: NovelUpdate): Boolean {
        return novelRepository.updateNovel(novelUpdate)
    }

    suspend fun awaitAll(novelUpdates: List<NovelUpdate>): Boolean {
        return novelRepository.updateAllNovel(novelUpdates)
    }

    suspend fun awaitUpdateFromSource(
        localNovel: Novel,
        remoteTitle: String,
        remoteAuthor: String?,
        remoteArtist: String?,
        remoteDescription: String?,
        remoteGenre: List<String>?,
        remoteThumbnailUrl: String?,
        remoteStatus: Long,
        remoteUpdateStrategy: eu.kanade.tachiyomi.novelsource.model.NovelUpdateStrategy,
        manualFetch: Boolean,
    ): Boolean {
        val title = if (remoteTitle.isEmpty() || localNovel.favorite) null else remoteTitle

        val coverLastModified = when {
            remoteThumbnailUrl.isNullOrEmpty() -> null
            !manualFetch && localNovel.thumbnailUrl == remoteThumbnailUrl -> null
            localNovel.hasCustomCover(coverCache) -> {
                coverCache.deleteFromCache(localNovel, false)
                null
            }
            else -> {
                coverCache.deleteFromCache(localNovel, false)
                Instant.now().toEpochMilli()
            }
        }

        val thumbnailUrl = remoteThumbnailUrl?.takeIf { it.isNotEmpty() }

        return novelRepository.updateNovel(
            NovelUpdate(
                id = localNovel.id,
                title = title,
                coverLastModified = coverLastModified,
                author = remoteAuthor,
                artist = remoteArtist,
                description = remoteDescription,
                genre = remoteGenre,
                thumbnailUrl = thumbnailUrl,
                status = remoteStatus,
                updateStrategy = remoteUpdateStrategy,
                initialized = true,
            ),
        )
    }

    suspend fun awaitUpdateLastUpdate(novelId: Long): Boolean {
        return novelRepository.updateNovel(NovelUpdate(id = novelId, lastUpdate = Instant.now().toEpochMilli()))
    }

    suspend fun awaitUpdateCoverLastModified(novelId: Long): Boolean {
        return novelRepository.updateNovel(NovelUpdate(id = novelId, coverLastModified = Instant.now().toEpochMilli()))
    }

    suspend fun awaitUpdateFavorite(novelId: Long, favorite: Boolean): Boolean {
        val dateAdded = when (favorite) {
            true -> Instant.now().toEpochMilli()
            false -> 0
        }
        return novelRepository.updateNovel(
            NovelUpdate(id = novelId, favorite = favorite, dateAdded = dateAdded),
        )
    }
}
