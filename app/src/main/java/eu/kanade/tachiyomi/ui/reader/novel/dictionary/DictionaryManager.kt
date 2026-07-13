package eu.kanade.tachiyomi.ui.reader.novel.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import org.json.JSONArray

class DictionaryManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private var database: SQLiteDatabase? = null
    private val dbFile = File(appContext.filesDir, "dictionary.db")

    companion object {
        @Volatile
        private var instance: DictionaryManager? = null

        fun getInstance(context: Context): DictionaryManager {
            return instance ?: synchronized(this) {
                instance ?: DictionaryManager(context).also { instance = it }
            }
        }
    }

    fun initialize() {
        if (database != null) return
        if (!dbFile.exists()) {
            extractDatabase()
        }
        if (dbFile.exists()) {
            database = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }
    }

    private fun extractDatabase() {
        try {
            appContext.assets.open("dictionary.db.gz").use { gzipInput ->
                GZIPInputStream(gzipInput).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Log.d("DictionaryManager", "Extracted dictionary.db.gz to ${dbFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("DictionaryManager", "Failed to extract dictionary.db.gz", e)
            try {
                appContext.assets.open("dictionary.db").use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("DictionaryManager", "Copied uncompressed dictionary.db as fallback")
            } catch (e2: Exception) {
                Log.e("DictionaryManager", "Failed to copy fallback dictionary.db", e2)
            }
        }
    }

    fun lookup(word: String): DictionaryEntry? {
        val clean = word.lowercase().trim()
        val db = database ?: return null

        lookupRaw(db, clean)?.let { return it.copy(word = word) }

        generateStems(clean).forEach { stem ->
            lookupRaw(db, stem)?.let { return it.copy(word = word) }
        }

        return null
    }

    fun isRealWord(word: String): Boolean {
        val clean = word.lowercase().trim()
        if (clean.isBlank() || clean.length < 2) return false
        val db = database ?: return false

        db.rawQuery(
            "SELECT 1 FROM entries WHERE word = ? COLLATE NOCASE LIMIT 1",
            arrayOf(clean),
        ).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        generateStems(clean).forEach { stem ->
            db.rawQuery(
                "SELECT 1 FROM entries WHERE word = ? COLLATE NOCASE LIMIT 1",
                arrayOf(stem),
            ).use { cursor ->
                if (cursor.moveToFirst()) return true
            }
        }

        return false
    }

    private fun lookupRaw(db: SQLiteDatabase, word: String): DictionaryEntry? {
        val cursor = db.rawQuery(
            "SELECT phonetic, definitions_json FROM entries WHERE word = ? COLLATE NOCASE",
            arrayOf(word),
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val phonetic = it.getString(0).takeIf { it.isNotBlank() }
                val definitionsJson = it.getString(1)
                val definitions = parseDefinitions(definitionsJson)
                DictionaryEntry(word, phonetic, definitions)
            } else {
                null
            }
        }
    }

    private fun generateStems(word: String): List<String> {
        val stems = mutableListOf<String>()

        if (word.endsWith("ies") && word.length > 4) {
            stems.add(word.dropLast(3) + "y")
        }
        if (word.endsWith("es") && word.length > 3) {
            stems.add(word.dropLast(2))
            stems.add(word.dropLast(1))
        }
        if (word.endsWith("s") && !word.endsWith("ss") && word.length > 2) {
            stems.add(word.dropLast(1))
        }

        if (word.endsWith("ied") && word.length > 4) {
            stems.add(word.dropLast(3) + "y")
        }
        if (word.endsWith("ed") && word.length > 3) {
            stems.add(word.dropLast(2))
            stems.add(word.dropLast(1))
        }

        if (word.endsWith("ying") && word.length > 5) {
            stems.add(word.dropLast(4) + "ie")
        }
        if (word.endsWith("ing") && word.length > 4) {
            stems.add(word.dropLast(3))
            stems.add(word.dropLast(3) + "e")
        }

        if (word.endsWith("er") && word.length > 3) {
            stems.add(word.dropLast(2))
        }
        if (word.endsWith("est") && word.length > 4) {
            stems.add(word.dropLast(3))
        }

        if (word.endsWith("ly") && word.length > 3) {
            stems.add(word.dropLast(2))
            stems.add(word.dropLast(2) + "e")
        }

        if (word.endsWith("let") && word.length > 4) {
            stems.add(word.dropLast(3))
        }

        if (word.endsWith("ness") && word.length > 5) {
            stems.add(word.dropLast(4))
            stems.add(word.dropLast(4) + "y")
        }

        if (word.endsWith("ment") && word.length > 5) {
            stems.add(word.dropLast(4))
            stems.add(word.dropLast(4) + "e")
        }

        return stems.distinct()
    }

    private fun parseDefinitions(json: String): List<DictionaryEntry.Definition> {
        val list = mutableListOf<DictionaryEntry.Definition>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val pos = obj.getString("pos")
            val meaning = obj.getString("meaning")
            val examplesArr = obj.optJSONArray("examples") ?: JSONArray()
            val examples = mutableListOf<String>()
            for (j in 0 until examplesArr.length()) {
                examples.add(examplesArr.getString(j))
            }
            list.add(DictionaryEntry.Definition(pos, meaning, examples))
        }
        return list
    }
}
