package eu.kanade.tachiyomi.data.cache

import android.content.Context
import eu.kanade.tachiyomi.util.storage.DiskUtil
import tachiyomi.domain.entries.novel.model.Novel
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Class used to create cover cache.
 * It is used to store the covers of the library.
 * Names of files are created with the md5 of the thumbnail URL.
 *
 * @param context the application context.
 * @constructor creates an instance of the cover cache.
 */
class NovelCoverCache(private val context: Context) {

    companion object {
        private const val COVERS_DIR = "novelcovers"
        private const val CUSTOM_COVERS_DIR = "novelcovers/custom"
    }

    /**
     * Cache directory used for cache management.
     */
    private val cacheDir = getCacheDir(COVERS_DIR)

    private val customCoverCacheDir = getCacheDir(CUSTOM_COVERS_DIR)

    /**
     * Returns the cover from cache.
     *
     * @param novelThumbnailUrl thumbnail url for the novel.
     * @return cover image.
     */
    fun getCoverFile(novelThumbnailUrl: String?): File? {
        return novelThumbnailUrl?.let {
            File(cacheDir, DiskUtil.hashKeyForDisk(it))
        }
    }

    /**
     * Returns the custom cover from cache.
     *
     * @param novelId the novel id.
     * @return cover image.
     */
    fun getCustomCoverFile(novelId: Long?): File {
        return File(customCoverCacheDir, DiskUtil.hashKeyForDisk(novelId.toString()))
    }

    /**
     * Saves the given stream as the novel's custom cover to cache.
     *
     * @param novel the novel.
     * @param inputStream the stream to copy.
     * @throws IOException if there's any error.
     */
    @Throws(IOException::class)
    fun setCustomCoverToCache(novel: Novel, inputStream: InputStream) {
        getCustomCoverFile(novel.id).outputStream().use {
            inputStream.copyTo(it)
        }
    }

    /**
     * Delete the cover files of the novel from the cache.
     *
     * @param novel the novel.
     * @param deleteCustomCover whether the custom cover should be deleted.
     * @return number of files that were deleted.
     */
    fun deleteFromCache(novel: Novel, deleteCustomCover: Boolean = false): Int {
        var deleted = 0

        getCoverFile(novel.thumbnailUrl)?.let {
            if (it.exists() && it.delete()) ++deleted
        }

        if (deleteCustomCover) {
            if (deleteCustomCover(novel.id)) ++deleted
        }

        return deleted
    }

    /**
     * Delete custom cover of the novel from the cache
     *
     * @param novelId the novel id.
     * @return whether the cover was deleted.
     */
    fun deleteCustomCover(novelId: Long?): Boolean {
        return getCustomCoverFile(novelId).let {
            it.exists() && it.delete()
        }
    }

    private fun getCacheDir(dir: String): File {
        return context.getExternalFilesDir(dir)
            ?: File(context.filesDir, dir).also { it.mkdirs() }
    }
}
