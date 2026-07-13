package eu.kanade.tachiyomi.ui.reader.novel

enum class TextAlignment(val value: Int) {
    LEFT(0),
    CENTER(1),
    JUSTIFY(2),
    RIGHT(3),
}

enum class NovelReadingMode(val prefValue: Int) {
    DEFAULT(0),
    INFINITE_SCROLL(1),
    OVERSCROLL(3),
}
