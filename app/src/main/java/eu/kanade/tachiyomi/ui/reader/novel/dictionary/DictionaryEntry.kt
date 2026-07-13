package eu.kanade.tachiyomi.ui.reader.novel.dictionary

data class DictionaryEntry(
    val word: String,
    val phonetic: String?,
    val definitions: List<Definition>,
) {
    data class Definition(
        val pos: String,
        val meaning: String,
        val examples: List<String>,
    )
}
