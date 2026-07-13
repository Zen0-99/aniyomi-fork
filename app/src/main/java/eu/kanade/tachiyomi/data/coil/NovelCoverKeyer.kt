package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.domain.entries.novel.model.hasCustomCover
import eu.kanade.tachiyomi.data.cache.NovelCoverCache
import tachiyomi.domain.entries.novel.model.NovelCover
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import tachiyomi.domain.entries.novel.model.Novel as DomainNovel

class NovelKeyer : Keyer<DomainNovel> {
    override fun key(data: DomainNovel, options: Options): String {
        return if (data.hasCustomCover()) {
            "novel;${data.id};${data.coverLastModified}"
        } else {
            "novel;${data.thumbnailUrl};${data.coverLastModified}"
        }
    }
}

class NovelCoverKeyer(
    private val coverCache: NovelCoverCache = Injekt.get(),
) : Keyer<NovelCover> {
    override fun key(data: NovelCover, options: Options): String {
        return if (coverCache.getCustomCoverFile(data.novelId).exists()) {
            "novel;${data.novelId};${data.lastModified}"
        } else {
            "novel;${data.url};${data.lastModified}"
        }
    }
}
