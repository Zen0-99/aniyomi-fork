package eu.kanade.tachiyomi.novelsource.online

import eu.kanade.tachiyomi.novelsource.NovelSource
import eu.kanade.tachiyomi.novelsource.model.SNovel
import eu.kanade.tachiyomi.novelsource.model.SNovelChapter

/**
 * A source that may handle opening an SNovel or SNovelChapter for a given URI.
 */
interface ResolvableNovelSource : NovelSource {

    /**
     * Returns what the given URI may open.
     * Returns [NovelUriType.Unknown] if the source is not able to resolve the URI.
     */
    fun getUriType(uri: String): NovelUriType

    /**
     * Called if [getUriType] is [NovelUriType.Novel].
     * Returns the corresponding SNovel, if possible.
     */
    suspend fun getNovel(uri: String): SNovel?

    /**
     * Called if [getUriType] is [NovelUriType.Chapter].
     * Returns the corresponding SNovelChapter, if possible.
     */
    suspend fun getChapter(uri: String): SNovelChapter?
}

sealed interface NovelUriType {
    data object Novel : NovelUriType
    data object Chapter : NovelUriType
    data object Unknown : NovelUriType
}
