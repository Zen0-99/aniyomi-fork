package eu.kanade.presentation.entries.novel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.util.fastAny
import eu.kanade.presentation.components.relativeDateTimeText
import eu.kanade.presentation.entries.DownloadAction
import eu.kanade.presentation.entries.EntryScreenItem
import eu.kanade.presentation.entries.components.EntryBottomActionMenu
import eu.kanade.presentation.entries.components.EntryToolbar
import eu.kanade.presentation.entries.components.ItemHeader
import eu.kanade.presentation.entries.novel.components.ExpandableNovelDescription
import eu.kanade.presentation.entries.novel.components.NovelActionRow
import eu.kanade.presentation.entries.novel.components.NovelChapterDownloadAction
import eu.kanade.presentation.entries.novel.components.NovelChapterListItem
import eu.kanade.presentation.entries.novel.components.NovelInfoBox
import eu.kanade.tachiyomi.data.download.novel.model.NovelDownload
import eu.kanade.tachiyomi.ui.entries.novel.NovelChapterList
import eu.kanade.tachiyomi.ui.entries.novel.NovelScreenModel
import eu.kanade.tachiyomi.source.novel.isLocalOrStub
import tachiyomi.presentation.core.util.shouldExpandFAB
import tachiyomi.domain.items.chapter.model.NovelChapter
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun NovelScreen(
    state: NovelScreenModel.State.Success,
    snackbarHostState: SnackbarHostState,
    isTabletUi: Boolean,
    chapterSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    chapterSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    navigateUp: () -> Unit,
    onChapterClicked: (NovelChapter) -> Unit,
    onDownloadChapter: ((List<NovelChapterList.Item>, NovelChapterDownloadAction) -> Unit)?,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onTagSearch: (String) -> Unit,
    onFilterButtonClicked: () -> Unit,
    onRefresh: () -> Unit,
    onContinueReading: () -> Unit,
    onSearch: (String, Boolean) -> Unit,
    onShareClicked: (() -> Unit)?,
    onDownloadActionClicked: ((DownloadAction) -> Unit)?,
    onEditCategoryClicked: (() -> Unit)?,
    onMigrateClicked: (() -> Unit)?,
    onMultiBookmarkClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMarkPreviousAsReadClicked: (NovelChapter) -> Unit,
    onMultiDeleteClicked: (List<NovelChapter>) -> Unit,
    onChapterSwipe: (NovelChapterList.Item, LibraryPreferences.ChapterSwipeAction) -> Unit,
    onChapterSelected: (NovelChapterList.Item, Boolean, Boolean, Boolean) -> Unit,
    onAllChapterSelected: (Boolean) -> Unit,
    onInvertSelection: () -> Unit,
) {
    if (isTabletUi) {
        NovelScreenLargeImpl(
            state = state,
            snackbarHostState = snackbarHostState,
            chapterSwipeStartAction = chapterSwipeStartAction,
            chapterSwipeEndAction = chapterSwipeEndAction,
            navigateUp = navigateUp,
            onChapterClicked = onChapterClicked,
            onDownloadChapter = onDownloadChapter,
            onAddToLibraryClicked = onAddToLibraryClicked,
            onWebViewClicked = onWebViewClicked,
            onWebViewLongClicked = onWebViewLongClicked,
            onTagSearch = onTagSearch,
            onFilterButtonClicked = onFilterButtonClicked,
            onRefresh = onRefresh,
            onContinueReading = onContinueReading,
            onSearch = onSearch,
            onShareClicked = onShareClicked,
            onDownloadActionClicked = onDownloadActionClicked,
            onEditCategoryClicked = onEditCategoryClicked,
            onMigrateClicked = onMigrateClicked,
            onMultiBookmarkClicked = onMultiBookmarkClicked,
            onMultiMarkAsReadClicked = onMultiMarkAsReadClicked,
            onMarkPreviousAsReadClicked = onMarkPreviousAsReadClicked,
            onMultiDeleteClicked = onMultiDeleteClicked,
            onChapterSwipe = onChapterSwipe,
            onChapterSelected = onChapterSelected,
            onAllChapterSelected = onAllChapterSelected,
            onInvertSelection = onInvertSelection,
        )
    } else {
        NovelScreenSmallImpl(
            state = state,
            snackbarHostState = snackbarHostState,
            chapterSwipeStartAction = chapterSwipeStartAction,
            chapterSwipeEndAction = chapterSwipeEndAction,
            navigateUp = navigateUp,
            onChapterClicked = onChapterClicked,
            onDownloadChapter = onDownloadChapter,
            onAddToLibraryClicked = onAddToLibraryClicked,
            onWebViewClicked = onWebViewClicked,
            onWebViewLongClicked = onWebViewLongClicked,
            onTagSearch = onTagSearch,
            onFilterButtonClicked = onFilterButtonClicked,
            onRefresh = onRefresh,
            onContinueReading = onContinueReading,
            onSearch = onSearch,
            onShareClicked = onShareClicked,
            onDownloadActionClicked = onDownloadActionClicked,
            onEditCategoryClicked = onEditCategoryClicked,
            onMigrateClicked = onMigrateClicked,
            onMultiBookmarkClicked = onMultiBookmarkClicked,
            onMultiMarkAsReadClicked = onMultiMarkAsReadClicked,
            onMarkPreviousAsReadClicked = onMarkPreviousAsReadClicked,
            onMultiDeleteClicked = onMultiDeleteClicked,
            onChapterSwipe = onChapterSwipe,
            onChapterSelected = onChapterSelected,
            onAllChapterSelected = onAllChapterSelected,
            onInvertSelection = onInvertSelection,
        )
    }
}

@Composable
private fun NovelScreenSmallImpl(
    state: NovelScreenModel.State.Success,
    snackbarHostState: SnackbarHostState,
    chapterSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    chapterSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    navigateUp: () -> Unit,
    onChapterClicked: (NovelChapter) -> Unit,
    onDownloadChapter: ((List<NovelChapterList.Item>, NovelChapterDownloadAction) -> Unit)?,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onTagSearch: (String) -> Unit,
    onFilterButtonClicked: () -> Unit,
    onRefresh: () -> Unit,
    onContinueReading: () -> Unit,
    onSearch: (String, Boolean) -> Unit,
    onShareClicked: (() -> Unit)?,
    onDownloadActionClicked: ((DownloadAction) -> Unit)?,
    onEditCategoryClicked: (() -> Unit)?,
    onMigrateClicked: (() -> Unit)?,
    onMultiBookmarkClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMarkPreviousAsReadClicked: (NovelChapter) -> Unit,
    onMultiDeleteClicked: (List<NovelChapter>) -> Unit,
    onChapterSwipe: (NovelChapterList.Item, LibraryPreferences.ChapterSwipeAction) -> Unit,
    onChapterSelected: (NovelChapterList.Item, Boolean, Boolean, Boolean) -> Unit,
    onAllChapterSelected: (Boolean) -> Unit,
    onInvertSelection: () -> Unit,
) {
    val chapterListState = rememberLazyListState()

    val chapters = remember(state) { state.processedChapters }
    val isAnySelected = remember(state) { state.isAnySelected }

    BackHandler(onBack = {
        if (isAnySelected) {
            onAllChapterSelected(false)
        } else {
            navigateUp()
        }
    })

    Scaffold(
        topBar = {
            val selectedChapterCount = remember(chapters) {
                chapters.count { it.selected }
            }
            val isFirstItemVisible by remember {
                derivedStateOf { chapterListState.firstVisibleItemIndex == 0 }
            }
            val isFirstItemScrolled by remember {
                derivedStateOf { chapterListState.firstVisibleItemScrollOffset > 0 }
            }
            val titleAlpha by androidx.compose.animation.core.animateFloatAsState(
                if (!isFirstItemVisible) 1f else 0f,
                label = "Top Bar Title",
            )
            val backgroundAlpha by androidx.compose.animation.core.animateFloatAsState(
                if (!isFirstItemVisible || isFirstItemScrolled) 1f else 0f,
                label = "Top Bar Background",
            )
            EntryToolbar(
                title = state.novel.title,
                hasFilters = state.filterActive,
                navigateUp = navigateUp,
                onClickFilter = onFilterButtonClicked,
                onClickShare = onShareClicked,
                onClickDownload = onDownloadActionClicked,
                onClickEditCategory = onEditCategoryClicked,
                onClickRefresh = onRefresh,
                onClickMigrate = onMigrateClicked,
                onClickSettings = null,
                changeAnimeSkipIntro = null,
                actionModeCounter = selectedChapterCount,
                onCancelActionMode = { onAllChapterSelected(false) },
                onSelectAll = { onAllChapterSelected(true) },
                onInvertSelection = { onInvertSelection() },
                titleAlphaProvider = { titleAlpha },
                backgroundAlphaProvider = { backgroundAlpha },
                isManga = false,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val isFABVisible = remember(chapters) {
                chapters.any { !it.chapter.read } && !isAnySelected
            }
            AnimatedVisibility(
                visible = isFABVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        val isReading = remember(state.chapters) {
                            state.chapters.any { it.chapter.read }
                        }
                        Text(
                            text = stringResource(if (isReading) MR.strings.action_resume else MR.strings.action_start),
                        )
                    },
                    icon = { Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null) },
                    onClick = onContinueReading,
                    expanded = chapterListState.shouldExpandFAB(),
                )
            }
        },
    ) { contentPadding ->
        val topPadding = contentPadding.calculateTopPadding()

        PullRefresh(
            refreshing = state.isRefreshingData,
            onRefresh = onRefresh,
            enabled = !isAnySelected,
            indicatorPadding = PaddingValues(top = topPadding),
        ) {
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                state = chapterListState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding(),
                ),
            ) {
                item(
                    key = EntryScreenItem.INFO_BOX,
                    contentType = EntryScreenItem.INFO_BOX,
                ) {
                    NovelInfoBox(
                        appBarPadding = topPadding,
                        novel = state.novel,
                        sourceName = state.source.name,
                        onCoverClick = {},
                        doSearch = onSearch,
                    )
                }

                item(
                    key = EntryScreenItem.ACTION_ROW,
                    contentType = EntryScreenItem.ACTION_ROW,
                ) {
                    NovelActionRow(
                        favorite = state.novel.favorite,
                        onAddToLibraryClicked = onAddToLibraryClicked,
                        onWebViewClicked = onWebViewClicked,
                        onWebViewLongClicked = onWebViewLongClicked,
                        onEditCategory = onEditCategoryClicked,
                    )
                }

                item(
                    key = EntryScreenItem.DESCRIPTION_WITH_TAG,
                    contentType = EntryScreenItem.DESCRIPTION_WITH_TAG,
                ) {
                    ExpandableNovelDescription(
                        defaultExpandState = state.isFromSource,
                        description = state.novel.description,
                        tagsProvider = { state.novel.genre },
                        onTagSearch = onTagSearch,
                    )
                }

                item(
                    key = EntryScreenItem.ITEM_HEADER,
                    contentType = EntryScreenItem.ITEM_HEADER,
                ) {
                    ItemHeader(
                        enabled = !isAnySelected,
                        itemCount = chapters.size,
                        missingItemsCount = 0,
                        onClick = onFilterButtonClicked,
                        isManga = false,
                    )
                }

                items(
                    items = chapters,
                    key = { it.id },
                    contentType = { EntryScreenItem.ITEM },
                ) { chapterItem ->
                    NovelChapterListItem(
                        item = chapterItem,
                        isFromSource = state.isFromSource,
                        downloadIndicatorEnabled = !isAnySelected && !state.source.isLocalOrStub(),
                        date = relativeDateTimeText(chapterItem.chapter.dateUpload),
                        scanlator = chapterItem.chapter.scanlator,
                        onClick = { onChapterClicked(chapterItem.chapter) },
                        onLongClick = { onChapterSelected(chapterItem, !chapterItem.selected, true, true) },
                        onDownloadClick = if (onDownloadChapter != null) {
                            { onDownloadChapter(listOf(chapterItem), it) }
                        } else {
                            null
                        },
                        onSwipeLeft = { onChapterSwipe(chapterItem, chapterSwipeStartAction) },
                        onSwipeRight = { onChapterSwipe(chapterItem, chapterSwipeEndAction) },
                        onSelect = { selected -> onChapterSelected(chapterItem, selected, true, false) },
                        chapterSwipeStartAction = chapterSwipeStartAction,
                        chapterSwipeEndAction = chapterSwipeEndAction,
                    )
                }
            }

            val selected = chapters.filter { it.selected }
            EntryBottomActionMenu(
                visible = isAnySelected,
                isManga = false,
                onBookmarkClicked = {
                    onMultiBookmarkClicked(selected.map { it.chapter }, true)
                }.takeIf { selected.fastAny { !it.chapter.bookmark } },
                onRemoveBookmarkClicked = {
                    onMultiBookmarkClicked(selected.map { it.chapter }, false)
                }.takeIf { selected.fastAny { it.chapter.bookmark } },
                onMarkAsViewedClicked = {
                    onMultiMarkAsReadClicked(selected.map { it.chapter }, true)
                }.takeIf { selected.fastAny { !it.chapter.read } },
                onMarkAsUnviewedClicked = {
                    onMultiMarkAsReadClicked(selected.map { it.chapter }, false)
                }.takeIf { selected.fastAny { it.chapter.read } },
                onMarkPreviousAsViewedClicked = {
                    selected.firstOrNull()?.chapter?.let { onMarkPreviousAsReadClicked(it) }
                    Unit
                }.takeIf { selected.size == 1 },
                onDownloadClicked = {
                    onDownloadChapter!!(selected.toList(), NovelChapterDownloadAction.START)
                }.takeIf {
                    onDownloadChapter != null && selected.fastAny { it.downloadState != NovelDownload.State.DOWNLOADED }
                },
                onDeleteClicked = {
                    onMultiDeleteClicked(selected.map { it.chapter })
                }.takeIf { onDownloadChapter != null },
            )
        }
    }
}

@Composable
private fun NovelScreenLargeImpl(
    state: NovelScreenModel.State.Success,
    snackbarHostState: SnackbarHostState,
    chapterSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    chapterSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    navigateUp: () -> Unit,
    onChapterClicked: (NovelChapter) -> Unit,
    onDownloadChapter: ((List<NovelChapterList.Item>, NovelChapterDownloadAction) -> Unit)?,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onTagSearch: (String) -> Unit,
    onFilterButtonClicked: () -> Unit,
    onRefresh: () -> Unit,
    onContinueReading: () -> Unit,
    onSearch: (String, Boolean) -> Unit,
    onShareClicked: (() -> Unit)?,
    onDownloadActionClicked: ((DownloadAction) -> Unit)?,
    onEditCategoryClicked: (() -> Unit)?,
    onMigrateClicked: (() -> Unit)?,
    onMultiBookmarkClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<NovelChapter>, Boolean) -> Unit,
    onMarkPreviousAsReadClicked: (NovelChapter) -> Unit,
    onMultiDeleteClicked: (List<NovelChapter>) -> Unit,
    onChapterSwipe: (NovelChapterList.Item, LibraryPreferences.ChapterSwipeAction) -> Unit,
    onChapterSelected: (NovelChapterList.Item, Boolean, Boolean, Boolean) -> Unit,
    onAllChapterSelected: (Boolean) -> Unit,
    onInvertSelection: () -> Unit,
) {
    NovelScreenSmallImpl(
        state = state,
        snackbarHostState = snackbarHostState,
        chapterSwipeStartAction = chapterSwipeStartAction,
        chapterSwipeEndAction = chapterSwipeEndAction,
        navigateUp = navigateUp,
        onChapterClicked = onChapterClicked,
        onDownloadChapter = onDownloadChapter,
        onAddToLibraryClicked = onAddToLibraryClicked,
        onWebViewClicked = onWebViewClicked,
        onWebViewLongClicked = onWebViewLongClicked,
        onTagSearch = onTagSearch,
        onFilterButtonClicked = onFilterButtonClicked,
        onRefresh = onRefresh,
        onContinueReading = onContinueReading,
        onSearch = onSearch,
        onShareClicked = onShareClicked,
        onDownloadActionClicked = onDownloadActionClicked,
        onEditCategoryClicked = onEditCategoryClicked,
        onMigrateClicked = onMigrateClicked,
        onMultiBookmarkClicked = onMultiBookmarkClicked,
        onMultiMarkAsReadClicked = onMultiMarkAsReadClicked,
        onMarkPreviousAsReadClicked = onMarkPreviousAsReadClicked,
        onMultiDeleteClicked = onMultiDeleteClicked,
        onChapterSwipe = onChapterSwipe,
        onChapterSelected = onChapterSelected,
        onAllChapterSelected = onAllChapterSelected,
        onInvertSelection = onInvertSelection,
    )
}
