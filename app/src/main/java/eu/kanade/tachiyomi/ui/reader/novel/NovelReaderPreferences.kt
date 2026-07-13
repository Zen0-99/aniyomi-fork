package eu.kanade.tachiyomi.ui.reader.novel

import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum

class NovelReaderPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun textSize() = preferenceStore.getFloat("pref_novel_text_size", 16f)

    fun lineHeight() = preferenceStore.getFloat("pref_novel_line_height", 1.5f)

    fun paragraphSpacing() = preferenceStore.getInt("pref_novel_paragraph_spacing", 16)

    fun textAlignment() = preferenceStore.getEnum("pref_novel_text_alignment", TextAlignment.JUSTIFY)

    fun readingMode() = preferenceStore.getEnum("pref_novel_reading_mode", NovelReadingMode.DEFAULT)

    fun backgroundColor() = preferenceStore.getInt("pref_novel_bg_color", 0xFFFFFFFF.toInt())

    fun textColor() = preferenceStore.getInt("pref_novel_text_color", 0xFF000000.toInt())

    fun keepScreenOn() = preferenceStore.getBoolean("pref_novel_keep_screen_on", true)

    fun showReadingProgress() = preferenceStore.getBoolean("pref_novel_show_progress", true)
}
