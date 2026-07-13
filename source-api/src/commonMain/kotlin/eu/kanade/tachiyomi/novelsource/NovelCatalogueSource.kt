package eu.kanade.tachiyomi.novelsource

import eu.kanade.tachiyomi.novelsource.model.NovelFilterList
import eu.kanade.tachiyomi.novelsource.model.NovelsPage

interface NovelCatalogueSource : NovelSource {

    /**
     * An ISO 639-1 compliant language code (two letters in lower case).
     */
    override val lang: String

    /**
     * Whether the source has support for latest updates.
     */
    val supportsLatest: Boolean

    /**
     * Get a page with a list of novels.
     *
     * @param page the page number to retrieve.
     */
    suspend fun getPopularNovels(page: Int): NovelsPage

    /**
     * Get a page with a list of novels.
     *
     * @param page the page number to retrieve.
     * @param query the search query.
     * @param filters the list of filters to apply.
     */
    suspend fun getSearchNovels(page: Int, query: String, filters: NovelFilterList): NovelsPage

    /**
     * Get a page with a list of latest novel updates.
     *
     * @param page the page number to retrieve.
     */
    suspend fun getLatestUpdates(page: Int): NovelsPage

    /**
     * Returns the list of filters for the source.
     */
    fun getFilterList(): NovelFilterList
}
