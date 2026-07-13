package eu.kanade.domain.items.chapter.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.lang.withNonCancellableContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.items.chapter.model.NovelChapter
import tachiyomi.domain.items.chapter.model.NovelChapterUpdate
import tachiyomi.domain.items.chapter.repository.NovelChapterRepository

class SetNovelReadStatus(
    private val novelChapterRepository: NovelChapterRepository,
) {

    private val mapper = { chapter: NovelChapter, read: Boolean ->
        NovelChapterUpdate(
            read = read,
            lastCharRead = if (!read) 0 else null,
            id = chapter.id,
        )
    }

    suspend fun await(read: Boolean, vararg chapters: NovelChapter): Result = withNonCancellableContext {
        val chaptersToUpdate = chapters.filter {
            when (read) {
                true -> !it.read
                false -> it.read || it.lastCharRead > 0
            }
        }
        if (chaptersToUpdate.isEmpty()) {
            return@withNonCancellableContext Result.NoChapters
        }

        try {
            novelChapterRepository.updateAllNovelChapters(
                chaptersToUpdate.map { mapper(it, read) },
            )
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            return@withNonCancellableContext Result.InternalError(e)
        }

        Result.Success
    }

    suspend fun await(novelId: Long, read: Boolean): Result = withNonCancellableContext {
        await(
            read = read,
            chapters = novelChapterRepository
                .getNovelChaptersByNovelId(novelId)
                .toTypedArray(),
        )
    }

    sealed interface Result {
        data object Success : Result
        data object NoChapters : Result
        data class InternalError(val error: Throwable) : Result
    }
}
