package eu.kanade.tachiyomi.ui.browse.novel.source.globalsearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.core.util.ifNovelSourcesLoaded
import eu.kanade.presentation.browse.novel.GlobalNovelSearchScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.novel.source.browse.BrowseNovelSourceScreen
import tachiyomi.presentation.core.screens.LoadingScreen

class GlobalNovelSearchScreen(
    val searchQuery: String = "",
    private val extensionFilter: String? = null,
) : Screen() {

    @Composable
    override fun Content() {
        if (!ifNovelSourcesLoaded()) {
            LoadingScreen()
            return
        }

        val navigator = LocalNavigator.currentOrThrow

        val screenModel = rememberScreenModel {
            GlobalNovelSearchScreenModel(
                initialQuery = searchQuery,
                initialExtensionFilter = extensionFilter,
            )
        }
        val state by screenModel.state.collectAsState()
        var showSingleLoadingScreen by remember {
            mutableStateOf(
                searchQuery.isNotEmpty() && !extensionFilter.isNullOrEmpty() && state.total == 1,
            )
        }

        if (showSingleLoadingScreen) {
            LoadingScreen()

            LaunchedEffect(state.items) {
                when (val result = state.items.values.singleOrNull()) {
                    NovelSearchItemResult.Loading -> return@LaunchedEffect
                    is NovelSearchItemResult.Success -> {
                        val novel = result.result.singleOrNull()
                        if (novel != null) {
                            navigator.replace(eu.kanade.tachiyomi.ui.entries.novel.NovelScreen(novel.id, true))
                            showSingleLoadingScreen = false
                        } else {
                            showSingleLoadingScreen = false
                        }
                    }
                    else -> showSingleLoadingScreen = false
                }
            }
        } else {
            GlobalNovelSearchScreen(
                state = state,
                navigateUp = navigator::pop,
                onChangeSearchQuery = screenModel::updateSearchQuery,
                onSearch = { screenModel.search() },
                getNovel = { screenModel.getNovel(it) },
                onChangeSearchFilter = screenModel::setSourceFilter,
                onToggleResults = screenModel::toggleFilterResults,
                onClickSource = {
                    navigator.push(BrowseNovelSourceScreen(it.id, state.searchQuery))
                },
                onClickItem = { navigator.push(eu.kanade.tachiyomi.ui.entries.novel.NovelScreen(it.id, true)) },
                onLongClickItem = { navigator.push(eu.kanade.tachiyomi.ui.entries.novel.NovelScreen(it.id, true)) },
            )
        }
    }
}
