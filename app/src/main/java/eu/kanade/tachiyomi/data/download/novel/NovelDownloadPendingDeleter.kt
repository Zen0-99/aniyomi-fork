package eu.kanade.tachiyomi.data.download.novel

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.items.chapter.model.NovelChapter
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelDownloadPendingDeleter(
    context: Context,
    private val json: Json = Injekt.get(),
) {

    private val preferences = context.getSharedPreferences(
        "novel_chapters_to_delete",
        Context.MODE_PRIVATE,
    )

    private var lastAddedEntry: Entry? = null

    @Synchronized
    fun addChapters(chapters: List<NovelChapter>, novel: Novel) {
        val lastEntry = lastAddedEntry

        val newEntry = if (lastEntry != null && lastEntry.novel.id == novel.id) {
            val newChapters = lastEntry.chapters.addUniqueById(chapters)
            if (newChapters.size == lastEntry.chapters.size) return
            lastEntry.copy(chapters = newChapters)
        } else {
            val existingEntry = preferences.getString(novel.id.toString(), null)
            if (existingEntry != null) {
                val savedEntry = json.decodeFromString<Entry>(existingEntry)
                val newChapters = savedEntry.chapters.addUniqueById(chapters)
                if (newChapters.size == savedEntry.chapters.size) return
                savedEntry.copy(chapters = newChapters)
            } else {
                Entry(chapters.map { it.toEntry() }, novel.toEntry())
            }
        }

        val jsonStr = json.encodeToString(newEntry)
        preferences.edit {
            putString(newEntry.novel.id.toString(), jsonStr)
        }
        lastAddedEntry = newEntry
    }

    @Synchronized
    fun getPendingChapters(): Map<Novel, List<NovelChapter>> {
        val entries = decodeAll()
        preferences.edit {
            clear()
        }
        lastAddedEntry = null

        return entries.associate { (chapters, novel) ->
            novel.toModel() to chapters.map { it.toModel() }
        }
    }

    private fun decodeAll(): List<Entry> {
        return preferences.all.values.mapNotNull { rawEntry ->
            try {
                (rawEntry as? String)?.let { json.decodeFromString<Entry>(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun List<ChapterEntry>.addUniqueById(chapters: List<NovelChapter>): List<ChapterEntry> {
        val newList = toMutableList()
        for (chapter in chapters) {
            if (none { it.id == chapter.id }) {
                newList.add(chapter.toEntry())
            }
        }
        return newList
    }

    private fun Novel.toEntry() = NovelEntry(id, url, title, source)

    private fun NovelChapter.toEntry() = ChapterEntry(id, url, name, scanlator)

    private fun NovelEntry.toModel() = Novel.create().copy(
        url = url,
        title = title,
        source = source,
        id = id,
    )

    private fun ChapterEntry.toModel() = NovelChapter.create().copy(
        id = id,
        url = url,
        name = name,
        scanlator = scanlator,
    )

    @Serializable
    private data class Entry(
        val chapters: List<ChapterEntry>,
        val novel: NovelEntry,
    )

    @Serializable
    private data class ChapterEntry(
        val id: Long,
        val url: String,
        val name: String,
        val scanlator: String? = null,
    )

    @Serializable
    private data class NovelEntry(
        val id: Long,
        val url: String,
        val title: String,
        val source: Long,
    )
}
