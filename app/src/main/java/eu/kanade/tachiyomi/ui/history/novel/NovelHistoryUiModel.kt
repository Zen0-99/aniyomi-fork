package eu.kanade.tachiyomi.ui.history.novel

import java.time.LocalDate
import tachiyomi.domain.history.novel.model.NovelHistoryWithRelations

sealed interface NovelHistoryUiModel {
    data class Header(val date: LocalDate) : NovelHistoryUiModel
    data class Item(val item: NovelHistoryWithRelations) : NovelHistoryUiModel
}
