package eu.kanade.tachiyomi.data.download.novel

import android.content.Context
import androidx.core.content.edit
import eu.kanade.tachiyomi.data.download.novel.model.NovelDownload
import eu.kanade.tachiyomi.novelsource.online.NovelHttpSource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tachiyomi.domain.entries.novel.interactor.GetNovel
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.items.chapter.interactor.GetNovelChapter
import tachiyomi.domain.source.novel.service.NovelSourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelDownloadStore(
    context: Context,
    private val sourceManager: NovelSourceManager = Injekt.get(),
    private val json: Json = Injekt.get(),
    private val getNovel: GetNovel = Injekt.get(),
    private val getNovelChapter: GetNovelChapter = Injekt.get(),
) {

    private val preferences = context.getSharedPreferences("novel_active_downloads", Context.MODE_PRIVATE)

    private var counter = 0

    fun addAll(downloads: List<NovelDownload>) {
        preferences.edit {
            downloads.forEach { putString(getKey(it), serialize(it)) }
        }
    }

    fun remove(download: NovelDownload) {
        preferences.edit {
            remove(getKey(download))
        }
    }

    fun removeAll(downloads: List<NovelDownload>) {
        preferences.edit {
            downloads.forEach { remove(getKey(it)) }
        }
    }

    fun clear() {
        preferences.edit {
            clear()
        }
    }

    private fun getKey(download: NovelDownload): String {
        return download.chapter.id.toString()
    }

    fun restore(): List<NovelDownload> {
        val objs = preferences.all
            .mapNotNull { it.value as? String }
            .mapNotNull { deserialize(it) }
            .sortedBy { it.order }

        val downloads = mutableListOf<NovelDownload>()
        if (objs.isNotEmpty()) {
            val cachedNovel = mutableMapOf<Long, Novel?>()
            for ((novelId, chapterId) in objs) {
                val novel = cachedNovel.getOrPut(novelId) {
                    runBlocking { getNovel.await(novelId) }
                } ?: continue
                val source = sourceManager.get(novel.source) as? NovelHttpSource ?: continue
                val chapter = runBlocking { getNovelChapter.await(chapterId) } ?: continue
                downloads.add(NovelDownload(source, novel, chapter))
            }
        }

        clear()
        return downloads
    }

    private fun serialize(download: NovelDownload): String {
        val obj = NovelDownloadObject(download.novel.id, download.chapter.id, counter++)
        return json.encodeToString(obj)
    }

    private fun deserialize(string: String): NovelDownloadObject? {
        return try {
            json.decodeFromString<NovelDownloadObject>(string)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
private data class NovelDownloadObject(val novelId: Long, val chapterId: Long, val order: Int)
