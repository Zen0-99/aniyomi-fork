package eu.kanade.tachiyomi.ui.reader.novel

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.util.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NovelHighlightsScreen(
    private val novelTitle: String,
    private val novelId: Long,
) : Screen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val highlightManager = remember { NovelHighlightManager(context) }
        val novelKey = remember { NovelHighlightManager.NovelKey(title = novelTitle, novelId = novelId) }

        var highlightsData by remember { mutableStateOf<NovelHighlightManager.NovelHighlightsData?>(null) }
        var showActionsFor by remember { mutableStateOf<Pair<NovelHighlightManager.HighlightEntry, Double>?>(null) }

        LaunchedEffect(novelKey) {
            withContext(Dispatchers.IO) {
                highlightsData = highlightManager.getAllHighlights(novelKey)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Highlights: $novelTitle") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { paddingValues ->
            val data = highlightsData
            if (data == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Loading...")
                }
                return@Scaffold
            }

            val items = buildHighlightItems(data)

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No highlights yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                items(items) { item ->
                    when (item) {
                        is HighlightListItem.ChapterHeader -> {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            )
                        }
                        is HighlightListItem.Highlight -> {
                            HighlightCard(
                                entry = item.entry,
                                chapterNumber = item.chapterNumber,
                                onClick = { showActionsFor = item.entry to item.chapterNumber },
                            )
                        }
                    }
                }
            }
        }

        showActionsFor?.let { (entry, chapterNumber) ->
            HighlightActionsDialog(
                entry = entry,
                onDismiss = { showActionsFor = null },
                onCopy = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Highlight", entry.text))
                    showActionsFor = null
                },
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "\"${entry.text}\"\n— From: $novelTitle")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Highlight"))
                    showActionsFor = null
                },
                onDelete = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            highlightManager.deleteHighlight(novelKey, chapterNumber, entry.text, entry.timestamp)
                        }
                        highlightsData = withContext(Dispatchers.IO) {
                            highlightManager.getAllHighlights(novelKey)
                        }
                    }
                    showActionsFor = null
                },
            )
        }
    }

    @Composable
    private fun HighlightCard(
        entry: NovelHighlightManager.HighlightEntry,
        chapterNumber: Double,
        onClick: () -> Unit,
    ) {
        val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
        val color = remember(entry.color) {
            try {
                Color(android.graphics.Color.parseColor(entry.color ?: NovelHighlightManager.COLOR_YELLOW))
            } catch (_: Exception) {
                Color.Yellow
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "\"${entry.text}\"",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = dateFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!entry.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.note,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HighlightActionsDialog(
        entry: NovelHighlightManager.HighlightEntry,
        onDismiss: () -> Unit,
        onCopy: () -> Unit,
        onShare: () -> Unit,
        onDelete: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Highlight") },
            text = { Text("\"${entry.text}\"") },
            confirmButton = {
                Column {
                    TextButton(onClick = onCopy) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy text")
                    }
                    TextButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            },
        )
    }

    private fun buildHighlightItems(data: NovelHighlightManager.NovelHighlightsData): List<HighlightListItem> {
        val items = mutableListOf<HighlightListItem>()
        val sortedChapters = data.chapters.filter { it.highlights.isNotEmpty() }.sortedBy { it.chapterNumber }
        for (ch in sortedChapters) {
            items.add(HighlightListItem.ChapterHeader(ch.chapterTitle.ifBlank { "Chapter ${ch.chapterNumber}" }))
            for (hl in ch.highlights.sortedBy { it.timestamp }) {
                items.add(HighlightListItem.Highlight(hl, ch.chapterNumber))
            }
        }
        return items
    }

    private sealed class HighlightListItem {
        data class ChapterHeader(val title: String) : HighlightListItem()
        data class Highlight(
            val entry: NovelHighlightManager.HighlightEntry,
            val chapterNumber: Double,
        ) : HighlightListItem()
    }
}
