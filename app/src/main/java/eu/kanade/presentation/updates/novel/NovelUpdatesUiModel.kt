package eu.kanade.presentation.updates.novel

import java.time.LocalDate

sealed interface NovelUpdatesUiModel {
    data class Header(val date: LocalDate) : NovelUpdatesUiModel
    data class Item(val item: eu.kanade.tachiyomi.ui.updates.novel.NovelUpdatesItem) : NovelUpdatesUiModel
}
