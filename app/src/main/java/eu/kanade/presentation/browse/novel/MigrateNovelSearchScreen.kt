package eu.kanade.presentation.browse.novel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import eu.kanade.presentation.browse.novel.components.GlobalNovelSearchToolbar
import eu.kanade.tachiyomi.novelsource.NovelCatalogueSource
import eu.kanade.tachiyomi.ui.browse.novel.source.globalsearch.NovelSearchScreenModel
import eu.kanade.tachiyomi.ui.browse.novel.source.globalsearch.NovelSourceFilter
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.presentation.core.components.material.Scaffold

@Composable
fun MigrateNovelSearchScreen(
    state: NovelSearchScreenModel.State,
    fromSourceId: Long?,
    navigateUp: () -> Unit,
    onChangeSearchQuery: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onChangeSearchFilter: (NovelSourceFilter) -> Unit,
    onToggleResults: () -> Unit,
    getNovel: @Composable (Novel) -> State<Novel>,
    onClickSource: (NovelCatalogueSource) -> Unit,
    onClickItem: (Novel) -> Unit,
    onLongClickItem: (Novel) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            GlobalNovelSearchToolbar(
                searchQuery = state.searchQuery,
                progress = state.progress,
                total = state.total,
                navigateUp = navigateUp,
                onChangeSearchQuery = onChangeSearchQuery,
                onSearch = onSearch,
                sourceFilter = state.sourceFilter,
                onChangeSearchFilter = onChangeSearchFilter,
                onlyShowHasResults = state.onlyShowHasResults,
                onToggleResults = onToggleResults,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        GlobalSearchContent(
            fromSourceId = fromSourceId,
            items = state.filteredItems,
            contentPadding = paddingValues,
            getNovel = getNovel,
            onClickSource = onClickSource,
            onClickItem = onClickItem,
            onLongClickItem = onLongClickItem,
        )
    }
}
