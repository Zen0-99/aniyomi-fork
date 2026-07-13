package eu.kanade.domain.source.novel.interactor

import eu.kanade.domain.source.service.SourcePreferences
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.domain.source.novel.model.NovelSource

class ToggleNovelSourcePin(
    private val preferences: SourcePreferences,
) {

    fun await(source: NovelSource) {
        val isPinned = source.id.toString() in preferences.pinnedNovelSources().get()
        preferences.pinnedNovelSources().getAndSet { pinned ->
            if (isPinned) pinned.minus("${source.id}") else pinned.plus("${source.id}")
        }
    }
}
