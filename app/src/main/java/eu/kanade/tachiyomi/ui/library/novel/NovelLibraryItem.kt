package eu.kanade.tachiyomi.ui.library.novel

import tachiyomi.domain.library.novel.LibraryNovel
import tachiyomi.domain.source.novel.service.NovelSourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelLibraryItem(
    val libraryNovel: LibraryNovel,
    var downloadCount: Long = -1,
    var unreadCount: Long = -1,
    var isLocal: Boolean = false,
    var sourceLanguage: String = "",
    private val sourceManager: NovelSourceManager = Injekt.get(),
) {
    fun matches(constraint: String): Boolean {
        val sourceName by lazy { sourceManager.getOrStub(libraryNovel.novel.source).toString() }
        if (constraint.startsWith("id:", true)) {
            val id = constraint.substringAfter("id:").toLongOrNull()
            return libraryNovel.id == id
        }
        return libraryNovel.novel.title.contains(constraint, true) ||
            (libraryNovel.novel.author?.contains(constraint, true) ?: false) ||
            (libraryNovel.novel.artist?.contains(constraint, true) ?: false) ||
            (libraryNovel.novel.description?.contains(constraint, true) ?: false) ||
            constraint.split(",").map { it.trim() }.all { subconstraint ->
                checkNegatableConstraint(subconstraint) {
                    sourceName.contains(it, true) ||
                        (libraryNovel.novel.genre?.any { genre -> genre.equals(it, true) } ?: false)
                }
            }
    }

    private fun checkNegatableConstraint(
        constraint: String,
        predicate: (String) -> Boolean,
    ): Boolean {
        return if (constraint.startsWith("-")) {
            !predicate(constraint.substringAfter("-").trimStart())
        } else {
            predicate(constraint)
        }
    }
}
