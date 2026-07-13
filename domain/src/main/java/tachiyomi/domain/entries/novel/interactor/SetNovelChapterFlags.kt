package tachiyomi.domain.entries.novel.interactor

import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.entries.novel.model.NovelUpdate
import tachiyomi.domain.entries.novel.repository.NovelRepository

class SetNovelChapterFlags(
    private val novelRepository: NovelRepository,
) {

    suspend fun awaitSetAllFlags(
        novel: Novel,
        unreadFilter: Long,
        downloadedFilter: Long,
        bookmarkedFilter: Long,
        sortingMode: Long,
        sortingDirection: Long,
        displayMode: Long,
    ): Boolean {
        val flags = novel.chapterFlags
            .and(Novel.CHAPTER_UNREAD_MASK.inv()).or(unreadFilter)
            .and(Novel.CHAPTER_DOWNLOADED_MASK.inv()).or(downloadedFilter)
            .and(Novel.CHAPTER_BOOKMARKED_MASK.inv()).or(bookmarkedFilter)
            .and(Novel.CHAPTER_SORTING_MASK.inv()).or(sortingMode)
            .and(Novel.CHAPTER_SORT_DIR_MASK.inv()).or(sortingDirection)
            .and(Novel.CHAPTER_DISPLAY_MASK.inv()).or(displayMode)
        return novelRepository.updateNovel(
            NovelUpdate(id = novel.id, chapterFlags = flags),
        )
    }

    suspend fun awaitSetDownloadedFilter(novel: Novel, flag: Long): Boolean {
        return novelRepository.updateNovel(
            NovelUpdate(
                id = novel.id,
                chapterFlags = novel.chapterFlags and Novel.CHAPTER_DOWNLOADED_MASK.inv() or flag,
            ),
        )
    }

    suspend fun awaitSetUnreadFilter(novel: Novel, flag: Long): Boolean {
        return novelRepository.updateNovel(
            NovelUpdate(
                id = novel.id,
                chapterFlags = novel.chapterFlags and Novel.CHAPTER_UNREAD_MASK.inv() or flag,
            ),
        )
    }

    suspend fun awaitSetBookmarkFilter(novel: Novel, flag: Long): Boolean {
        return novelRepository.updateNovel(
            NovelUpdate(
                id = novel.id,
                chapterFlags = novel.chapterFlags and Novel.CHAPTER_BOOKMARKED_MASK.inv() or flag,
            ),
        )
    }

    suspend fun awaitSetDisplayMode(novel: Novel, flag: Long): Boolean {
        return novelRepository.updateNovel(
            NovelUpdate(
                id = novel.id,
                chapterFlags = novel.chapterFlags and Novel.CHAPTER_DISPLAY_MASK.inv() or flag,
            ),
        )
    }

    suspend fun awaitSetSortingModeOrFlipOrder(novel: Novel, flag: Long): Boolean {
        val newFlags = novel.chapterFlags.let {
            if (novel.sorting == flag) {
                it xor Novel.CHAPTER_SORT_DIR_MASK
            } else {
                it and Novel.CHAPTER_SORTING_MASK.inv() or flag
            }
        }
        return novelRepository.updateNovel(
            NovelUpdate(id = novel.id, chapterFlags = newFlags),
        )
    }
}
