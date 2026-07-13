package eu.kanade.tachiyomi.ui.reader.novel

import android.content.Context
import android.content.Intent
import android.text.Spanned
import android.text.Html
import android.speech.tts.TextToSpeech
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.novelsource.model.SNovelChapterImpl
import eu.kanade.tachiyomi.novelsource.online.NovelHttpSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.entries.novel.interactor.GetNovel
import tachiyomi.domain.entries.novel.interactor.GetNovelWithChapters
import tachiyomi.domain.entries.novel.model.Novel
import eu.kanade.domain.items.chapter.interactor.SetNovelReadStatus
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.webview.WebViewActivity
import tachiyomi.domain.items.chapter.model.NovelChapter
import tachiyomi.domain.history.novel.interactor.UpsertNovelHistory
import tachiyomi.domain.history.novel.model.NovelHistoryUpdate
import tachiyomi.domain.source.novel.service.NovelSourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelReaderScreenModel(
    private val novelId: Long,
    private val chapterId: Long?,
    private val getNovel: GetNovel = Injekt.get(),
    private val getNovelWithChapters: GetNovelWithChapters = Injekt.get(),
    private val sourceManager: NovelSourceManager = Injekt.get(),
    private val setReadStatus: SetNovelReadStatus = Injekt.get(),
    private val upsertNovelHistory: UpsertNovelHistory = Injekt.get(),
    val preferences: NovelReaderPreferences = Injekt.get(),
    val readerPreferences: ReaderPreferences = Injekt.get(),
) : StateScreenModel<NovelReaderScreenModel.State>(State()) {

    private var currentChapterIndex = 0
    private var currentSource: NovelHttpSource? = null

    private val _events = MutableSharedFlow<NovelReaderEvent>()
    val events: SharedFlow<NovelReaderEvent> = _events.asSharedFlow()

    private val _contentItems = MutableStateFlow<List<TextItem>>(emptyList())
    val contentItems = _contentItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentChapter = MutableStateFlow<NovelChapter?>(null)
    val currentChapter = _currentChapter.asStateFlow()

    private val _novel = MutableStateFlow<Novel?>(null)
    val novel = _novel.asStateFlow()

    private val _chapters = MutableStateFlow<List<NovelChapter>>(emptyList())
    val chapters = _chapters.asStateFlow()

    private val _isControlsVisible = MutableStateFlow(true)
    val isControlsVisible = _isControlsVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible = _isSettingsVisible.asStateFlow()

    private val _isChaptersSheetVisible = MutableStateFlow(false)
    val isChaptersSheetVisible = _isChaptersSheetVisible.asStateFlow()

    private val _progressPercent = MutableStateFlow(0)
    val progressPercent = _progressPercent.asStateFlow()

    private val _textConfig = MutableStateFlow(buildTextConfig())
    val textConfig = _textConfig.asStateFlow()

    private val _dictionaryQuery = MutableStateFlow<String?>(null)
    val dictionaryQuery = _dictionaryQuery.asStateFlow()

    private var highlightManager: NovelHighlightManager? = null

    private val _showHighlightColorPicker = MutableStateFlow<String?>(null)
    val showHighlightColorPicker = _showHighlightColorPicker.asStateFlow()

    private var pendingSelectedText: String? = null

    fun initHighlightManager(context: Context) {
        if (highlightManager == null) {
            highlightManager = NovelHighlightManager(context)
        }
    }

    fun onTextSelected(selectedText: String) {
        if (selectedText.isNotBlank() && selectedText.length > 1) {
            pendingSelectedText = selectedText
            _showHighlightColorPicker.value = selectedText
        }
    }

    fun dismissHighlightPicker() {
        _showHighlightColorPicker.value = null
        pendingSelectedText = null
    }

    fun saveHighlight(color: String) {
        val text = pendingSelectedText ?: return
        val chapter = _currentChapter.value ?: return
        val novel = _novel.value ?: return
        val mgr = highlightManager ?: return

        screenModelScope.launchIO {
            mgr.saveHighlight(
                novelKey = NovelHighlightManager.NovelKey(title = novel.title, novelId = novel.id),
                chapterNumber = chapter.chapterNumber,
                chapterTitle = chapter.name,
                selectedText = text,
                color = color,
            )
            _events.emit(NovelReaderEvent.ShowMessage("Highlight saved"))
        }
        _showHighlightColorPicker.value = null
        pendingSelectedText = null
    }

    fun showDictionary(word: String) {
        _dictionaryQuery.value = word
    }

    fun dismissDictionary() {
        _dictionaryQuery.value = null
    }

    private var context: Context? = null
    private var tts: TextToSpeech? = null

    fun initContext(context: Context) {
        this.context = context.applicationContext
    }

    fun copyToClipboard(text: String) {
        val ctx = context ?: return
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Novel text", text))
        screenModelScope.launchIO {
            _events.emit(NovelReaderEvent.ShowMessage("Copied to clipboard"))
        }
    }

    fun shareText(text: String) {
        val ctx = context ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(Intent.createChooser(intent, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun readAloud(text: String) {
        val ctx = context ?: return
        if (tts == null) {
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    speakText(text)
                }
            }
        } else {
            speakText(text)
        }
    }

    private fun speakText(text: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "novelReadAloud")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun shutdownTts() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun buildTextConfig(): TextConfig {
        val theme = readerPreferences.readerTheme().get()
        val (backgroundColor, textColor) = when (theme) {
            0 -> android.graphics.Color.WHITE to android.graphics.Color.BLACK
            1 -> android.graphics.Color.BLACK to android.graphics.Color.WHITE
            2 -> android.graphics.Color.parseColor("#FF202020") to android.graphics.Color.WHITE
            else -> preferences.backgroundColor().get() to preferences.textColor().get()
        }
        return TextConfig(
            textSize = preferences.textSize().get(),
            textColor = textColor,
            backgroundColor = backgroundColor,
            lineSpacing = preferences.lineHeight().get(),
            paragraphSpacing = preferences.paragraphSpacing().get(),
            textAlignment = preferences.textAlignment().get(),
        )
    }

    fun refreshTextConfig() {
        _textConfig.value = buildTextConfig()
    }

    val textConfigValue: TextConfig
        get() = _textConfig.value

    data class State(
        val loading: Boolean = true,
        val error: String? = null,
    )

    fun initialize() {
        screenModelScope.launchIO {
            try {
                mutableState.update { it.copy(loading = true, error = null) }

                val novel = getNovel.await(novelId)
                if (novel == null) {
                    mutableState.update { it.copy(loading = false, error = "Novel not found") }
                    return@launchIO
                }
                _novel.value = novel

                val source = sourceManager.getOrStub(novel.source) as? NovelHttpSource
                currentSource = source
                if (source == null) {
                    mutableState.update { it.copy(loading = false, error = "Source not available") }
                    return@launchIO
                }

                val chapterList = getNovelWithChapters.awaitChapters(novelId)
                _chapters.value = chapterList.sortedBy { it.sourceOrder }

                val targetChapter = if (chapterId != null) {
                    chapterList.find { it.id == chapterId }
                } else {
                    chapterList.find { !it.read } ?: chapterList.firstOrNull()
                }

                if (targetChapter != null) {
                    currentChapterIndex = chapterList.indexOf(targetChapter)
                    loadChapter(targetChapter)
                } else {
                    mutableState.update { it.copy(loading = false, error = "No chapters available") }
                }
            } catch (e: Exception) {
                mutableState.update { it.copy(loading = false, error = "Failed: ${e.message}") }
            }
        }
    }

    private suspend fun loadChapter(chapter: NovelChapter) {
        _currentChapter.value = chapter
        _isLoading.value = true
        mutableState.update { it.copy(loading = true) }
        upsertHistory(chapter)

        try {
            val source = currentSource
            if (source == null) {
                mutableState.update { it.copy(loading = false, error = "No source available") }
                return
            }

            val sChapter = SNovelChapterImpl().apply {
                url = chapter.url
                name = chapter.name
            }
            val htmlContent = source.getChapterText(sChapter)

            val items = parseHtmlToParagraphs(htmlContent, chapter.id)
            val wrappedItems = wrapWithNavigation(items, chapter)

            _contentItems.value = wrappedItems
            mutableState.update { it.copy(loading = false, error = null) }
            _events.emit(NovelReaderEvent.ChapterChanged(chapter.name))
        } catch (e: Exception) {
            mutableState.update { it.copy(loading = false, error = "Failed to load chapter: ${e.message}") }
            _events.emit(NovelReaderEvent.ShowError("Failed to load chapter: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }

    private fun parseHtmlToParagraphs(html: String, chapterId: Long): List<TextItem> {
        @Suppress("DEPRECATION")
        fun renderHtml(source: String): Spanned {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(source)
            }
        }

        val paragraphs = html.split(Regex("</?(?:p|div|h[1-6]|br)\\s*/?>", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val items = mutableListOf<TextItem>()
        var charIndex = 0
        for ((index, paragraph) in paragraphs.withIndex()) {
            val spanned = renderHtml(paragraph)
            val endChar = charIndex + spanned.length
            items.add(
                TextItem.Paragraph(
                    id = (chapterId * 100000) + index.toLong(),
                    chapterId = chapterId,
                    paragraphIndex = index,
                    text = spanned,
                    startCharIndex = charIndex,
                    endCharIndex = endChar,
                ),
            )
            charIndex = endChar + 1
        }

        if (items.isEmpty()) {
            val spanned = renderHtml(html)
            items.add(
                TextItem.Paragraph(
                    id = (chapterId * 100000),
                    chapterId = chapterId,
                    paragraphIndex = 0,
                    text = spanned,
                    startCharIndex = 0,
                    endCharIndex = spanned.length,
                ),
            )
        }

        return items
    }

    private fun wrapWithNavigation(items: List<TextItem>, chapter: NovelChapter): List<TextItem> {
        val result = mutableListOf<TextItem>()

        result.add(
            TextItem.ChapterHeader(
                id = chapter.id * 1000000,
                chapterId = chapter.id,
                chapterTitle = chapter.name,
            ),
        )

        result.addAll(items)

        val hasPrev = currentChapterIndex > 0
        val hasNext = currentChapterIndex < _chapters.value.size - 1

        if (hasPrev) {
            result.add(
                1,
                TextItem.ChapterNavigation(
                    id = chapter.id * 1000000 + 1,
                    direction = TextItem.LoadDirection.PREVIOUS,
                    chapterTitle = _chapters.value.getOrNull(currentChapterIndex - 1)?.name ?: "",
                    isEnabled = true,
                ),
            )
        }

        if (hasNext || !hasNext) {
            result.add(
                TextItem.ChapterNavigation(
                    id = chapter.id * 1000000 + 2,
                    direction = TextItem.LoadDirection.NEXT,
                    chapterTitle = _chapters.value.getOrNull(currentChapterIndex + 1)?.name ?: "",
                    isEnabled = hasNext,
                ),
            )
        }

        return result
    }

    fun navigateToPreviousChapter() {
        if (currentChapterIndex > 0) {
            currentChapterIndex--
            val chapter = _chapters.value[currentChapterIndex]
            screenModelScope.launchIO {
                loadChapter(chapter)
            }
        }
    }

    fun navigateToNextChapter() {
        if (currentChapterIndex < _chapters.value.size - 1) {
            currentChapterIndex++
            val chapter = _chapters.value[currentChapterIndex]
            screenModelScope.launchIO {
                loadChapter(chapter)
                markPreviousChapterRead()
            }
        }
    }

    private suspend fun markPreviousChapterRead() {
        val prevIndex = currentChapterIndex - 1
        if (prevIndex >= 0) {
            val prevChapter = _chapters.value.getOrNull(prevIndex)
            if (prevChapter != null && !prevChapter.read) {
                setReadStatus.await(true, prevChapter)
            }
        }
    }

    private var chapterLoadTime: Long = 0L

    private fun upsertHistory(chapter: NovelChapter) {
        val now = System.currentTimeMillis()
        val duration = if (chapterLoadTime > 0) now - chapterLoadTime else 0L
        chapterLoadTime = now
        screenModelScope.launchIO {
            upsertNovelHistory.await(
                NovelHistoryUpdate(
                    chapterId = chapter.id,
                    readAt = java.util.Date(now),
                    sessionReadDuration = duration,
                ),
            )
        }
    }

    fun toggleControls() {
        _isControlsVisible.value = !_isControlsVisible.value
    }

    fun setMenuVisible(visible: Boolean) {
        _isControlsVisible.value = visible
    }

    fun showSettings() {
        _isControlsVisible.value = false
        _isSettingsVisible.value = true
    }

    fun dismissSettings() {
        _isSettingsVisible.value = false
    }

    fun showChapters() {
        _isControlsVisible.value = false
        _isChaptersSheetVisible.value = true
    }

    fun dismissChapters() {
        _isChaptersSheetVisible.value = false
    }

    fun openChapterInWebView() {
        val ctx = context ?: return
        val chapter = _currentChapter.value ?: return
        val n = _novel.value ?: return
        val intent = WebViewActivity.newIntent(ctx, chapter.url, n.source, n.title)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }

    fun applyReaderTheme(theme: Int) {
        readerPreferences.readerTheme().set(theme)
        refreshTextConfig()
    }

    fun updateProgress(percent: Int) {
        _progressPercent.value = percent
    }

    fun loadChapterById(id: Long) {
        val chapter = _chapters.value.find { it.id == id } ?: return
        currentChapterIndex = _chapters.value.indexOf(chapter)
        screenModelScope.launchIO {
            loadChapter(chapter)
        }
    }
}

sealed interface NovelReaderEvent {
    data class ShowError(val message: String) : NovelReaderEvent
    data class ShowMessage(val message: String) : NovelReaderEvent
    data class ChapterChanged(val title: String) : NovelReaderEvent
}
