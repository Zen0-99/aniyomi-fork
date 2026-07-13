package eu.kanade.tachiyomi.ui.reader.novel

import android.text.Spanned

sealed class TextItem {
    abstract val id: Long

    data class Paragraph(
        override val id: Long,
        val chapterId: Long,
        val paragraphIndex: Int,
        val text: Spanned,
        val startCharIndex: Int,
        val endCharIndex: Int,
    ) : TextItem()

    data class ChapterHeader(
        override val id: Long,
        val chapterId: Long,
        val chapterTitle: String,
    ) : TextItem()

    data class Loading(
        override val id: Long,
        val chapterId: Long,
        val loadingMessage: String = "Loading chapter...",
    ) : TextItem()

    data class Error(
        override val id: Long,
        val chapterId: Long,
        val errorMessage: String,
        val canRetry: Boolean = true,
    ) : TextItem()

    data class ChapterNavigation(
        override val id: Long,
        val direction: LoadDirection,
        val chapterTitle: String,
        val isEnabled: Boolean,
    ) : TextItem()

    enum class LoadDirection { PREVIOUS, NEXT }
}
