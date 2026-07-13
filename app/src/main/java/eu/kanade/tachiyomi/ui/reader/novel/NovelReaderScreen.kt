package eu.kanade.tachiyomi.ui.reader.novel

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.presentation.core.util.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.novel.reader.NovelChaptersSheet
import eu.kanade.presentation.novel.reader.NovelReaderChrome
import eu.kanade.presentation.novel.reader.NovelReaderSettingsDialog
import eu.kanade.presentation.novel.reader.NovelReaderSettingsScreenModel
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.reader.novel.dictionary.DictionaryBottomSheet
import eu.kanade.tachiyomi.util.system.hasDisplayCutout
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.presentation.core.screens.LoadingScreen

class NovelReaderScreen(
    private val novelId: Long,
    private val chapterId: Long? = null,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val activity = remember(context) { context.findActivity() }
        val screenModel = rememberScreenModel {
            NovelReaderScreenModel(novelId, chapterId)
        }

        LaunchedEffect(Unit) {
            screenModel.initContext(context)
            screenModel.initHighlightManager(context)
            screenModel.initialize()
        }

        DisposableEffect(Unit) {
            onDispose {
                screenModel.shutdownTts()
            }
        }

        val state by screenModel.state.collectAsStateWithLifecycle()
        val contentItems by screenModel.contentItems.collectAsStateWithLifecycle()
        val isLoading by screenModel.isLoading.collectAsStateWithLifecycle()
        val currentChapter by screenModel.currentChapter.collectAsStateWithLifecycle()
        val novel by screenModel.novel.collectAsStateWithLifecycle()
        val chapters by screenModel.chapters.collectAsStateWithLifecycle()
        val isControlsVisible by screenModel.isControlsVisible.collectAsStateWithLifecycle()
        val isSettingsVisible by screenModel.isSettingsVisible.collectAsStateWithLifecycle()
        val isChaptersSheetVisible by screenModel.isChaptersSheetVisible.collectAsStateWithLifecycle()
        val textConfig by screenModel.textConfig.collectAsStateWithLifecycle()
        val dictionaryQuery by screenModel.dictionaryQuery.collectAsStateWithLifecycle()
        val showHighlightPicker by screenModel.showHighlightColorPicker.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                when (event) {
                    is NovelReaderEvent.ShowError -> {}
                    is NovelReaderEvent.ShowMessage -> {}
                    is NovelReaderEvent.ChapterChanged -> {}
                }
            }
        }

        if (state.loading && contentItems.isEmpty()) {
            LoadingScreen()
            return
        }

        state.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = error, color = Color.Red)
            }
            return
        }

        val bgColor = Color(textConfig.backgroundColor)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
        ) {
            NovelReaderContent(
                screenModel = screenModel,
                contentItems = contentItems,
                textConfig = textConfig,
                isLoading = isLoading,
                onToggleControls = { screenModel.toggleControls() },
            )

            NovelReaderChrome(
                isMenuVisible = isControlsVisible,
                title = currentChapter?.name ?: "Loading...",
                subtitle = novel?.title ?: "",
                onBackClick = { navigator.pop() },
                onChaptersClick = { screenModel.showChapters() },
                onWebviewClick = { screenModel.openChapterInWebView() },
                onHighlightsClick = {
                    novel?.let { n ->
                        navigator.push(NovelHighlightsScreen(n.title, n.id))
                    }
                },
                onSettingsClick = { screenModel.showSettings() },
            )
        }

        if (isSettingsVisible) {
            val hasDisplayCutout = remember { activity?.hasDisplayCutout() == true }
            NovelReaderSettingsDialog(
                onDismissRequest = { screenModel.dismissSettings() },
                onShowMenus = { screenModel.setMenuVisible(true) },
                screenModel = remember {
                    NovelReaderSettingsScreenModel(
                        hasDisplayCutout = hasDisplayCutout,
                        onReadingModeChange = { screenModel.refreshTextConfig() },
                        onBackgroundColorChange = { theme -> screenModel.applyReaderTheme(theme) },
                        onTextSettingChange = { screenModel.refreshTextConfig() },
                    )
                },
            )
        }

        if (isChaptersSheetVisible) {
            NovelChaptersSheet(
                chapters = chapters,
                currentChapterId = currentChapter?.id,
                onChapterClick = { chapter ->
                    screenModel.dismissChapters()
                    screenModel.setMenuVisible(true)
                    screenModel.loadChapterById(chapter.id)
                },
                onDismiss = { screenModel.dismissChapters() },
            )
        }

        ApplyReaderWindowSettings(activity, screenModel)

        dictionaryQuery?.let { query ->
            DictionaryBottomSheet(
                selectedText = query,
                onDismiss = { screenModel.dismissDictionary() },
            )
        }

        showHighlightPicker?.let { selectedText ->
            HighlightColorPickerDialog(
                selectedText = selectedText,
                onDismiss = { screenModel.dismissHighlightPicker() },
                onColorSelected = { color -> screenModel.saveHighlight(color) },
                onDictionaryLookup = {
                    screenModel.dismissHighlightPicker()
                    screenModel.showDictionary(selectedText)
                },
            )
        }
    }

    @Composable
    private fun ApplyReaderWindowSettings(
        activity: Activity?,
        screenModel: NovelReaderScreenModel,
    ) {
        if (activity == null) return

        val fullscreen by screenModel.readerPreferences.fullscreen().collectAsState()
        val keepScreenOn by screenModel.readerPreferences.keepScreenOn().collectAsState()
        val readerTheme by screenModel.readerPreferences.readerTheme().collectAsState()

        DisposableEffect(fullscreen, keepScreenOn, readerTheme, activity) {
            val window = activity.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)

            WindowCompat.setDecorFitsSystemWindows(window, !fullscreen)
            if (fullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            val isDark = when (readerTheme) {
                0 -> false
                1, 2 -> true
                else -> false
            }
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark

            onDispose {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    @Composable
    private fun NovelReaderContent(
        screenModel: NovelReaderScreenModel,
        contentItems: List<TextItem>,
        textConfig: TextConfig,
        isLoading: Boolean,
        onToggleControls: () -> Unit,
    ) {
        var recyclerView by remember { mutableStateOf<RecyclerView?>(null) }
        var adapter by remember { mutableStateOf<TextAdapter?>(null) }

        AndroidView(
            factory = { ctx ->
                val rv = RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    setHasFixedSize(false)
                }

                val gestureDetector = GestureDetector(
                    ctx,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            onToggleControls()
                            return true
                        }
                    },
                )
                rv.setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    false
                }

                val textAdapter = TextAdapter(
                    getConfig = { screenModel.textConfigValue },
                    onNavigationClick = { direction ->
                        when (direction) {
                            TextItem.LoadDirection.PREVIOUS -> screenModel.navigateToPreviousChapter()
                            TextItem.LoadDirection.NEXT -> screenModel.navigateToNextChapter()
                        }
                    },
                    onTextSelected = { selectedText ->
                        screenModel.onTextSelected(selectedText)
                    },
                    onHighlight = { selectedText, _, _ ->
                        screenModel.onTextSelected(selectedText)
                    },
                    onCopy = { selectedText ->
                        screenModel.copyToClipboard(selectedText)
                    },
                    onShare = { selectedText ->
                        screenModel.shareText(selectedText)
                    },
                    onReadAloud = { selectedText ->
                        screenModel.readAloud(selectedText)
                    },
                )
                rv.adapter = textAdapter
                adapter = textAdapter
                textAdapter.submitList(contentItems)
                recyclerView = rv
                rv
            },
            update = { rv ->
                adapter?.submitList(contentItems)
            },
            modifier = Modifier.fillMaxSize(),
        )

        DisposableEffect(Unit) {
            onDispose {
                recyclerView?.adapter = null
            }
        }

        DisposableEffect(textConfig) {
            adapter?.notifyDataSetChanged()
            onDispose {}
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun HighlightColorPickerDialog(
    selectedText: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit,
    onDictionaryLookup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Highlight") },
        text = {
            Column {
                Text(
                    text = "\"${selectedText.take(100)}${if (selectedText.length > 100) "..." else ""}\"",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Choose a color:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NovelHighlightManager.DEFAULT_COLORS.forEach { colorHex ->
                        val color = remember(colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (_: Exception) {
                                Color.Yellow
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(colorHex) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDictionaryLookup) {
                Text("Dictionary")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
