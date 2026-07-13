package eu.kanade.tachiyomi.novelsource

import eu.kanade.tachiyomi.novelsource.model.SNovel
import eu.kanade.tachiyomi.novelsource.model.SNovelChapter

/**
 * A basic interface for creating a novel source. It could be an online source, a local source, etc.
 */
interface NovelSource {

    /**
     * ID for the source. Must be unique.
     */
    val id: Long

    /**
     * Name of the source.
     */
    val name: String

    val lang: String
        get() = ""

    /**
     * Get the updated details for a novel.
     *
     * @param novel the novel to update.
     * @return the updated novel.
     */
    suspend fun getNovelDetails(novel: SNovel): SNovel

    /**
     * Get all the available chapters for a novel.
     *
     * @param novel the novel to update.
     * @return the chapters for the novel.
     */
    suspend fun getChapterList(novel: SNovel): List<SNovelChapter>

    /**
     * Get the text content of a chapter.
     *
     * @param chapter the chapter.
     * @return the text content (HTML or plain text) for the chapter.
     */
    suspend fun getChapterText(chapter: SNovelChapter): String
}
