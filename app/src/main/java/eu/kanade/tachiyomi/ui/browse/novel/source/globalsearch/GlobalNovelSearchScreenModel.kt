package eu.kanade.tachiyomi.ui.browse.novel.source.globalsearch

import eu.kanade.tachiyomi.novelsource.NovelCatalogueSource

class GlobalNovelSearchScreenModel(
    initialQuery: String = "",
    initialExtensionFilter: String? = null,
) : NovelSearchScreenModel(
    State(
        searchQuery = initialQuery,
    ),
) {

    init {
        extensionFilter = initialExtensionFilter
        if (initialQuery.isNotBlank() || !initialExtensionFilter.isNullOrBlank()) {
            if (extensionFilter != null) {
                setSourceFilter(NovelSourceFilter.All)
            }
            search()
        }
    }

    override fun getEnabledSources(): List<NovelCatalogueSource> {
        return super.getEnabledSources()
            .filter { state.value.sourceFilter != NovelSourceFilter.PinnedOnly || "${it.id}" in pinnedSources }
    }
}
