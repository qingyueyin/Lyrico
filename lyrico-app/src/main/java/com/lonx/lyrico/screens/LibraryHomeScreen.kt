package com.lonx.lyrico.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.lonx.lyrico.screens.library.AlbumsPage
import com.lonx.lyrico.screens.library.ArtistsPage
import com.lonx.lyrico.screens.library.LibraryTab
import com.lonx.lyrico.screens.library.SongsPage
import com.lonx.lyrico.ui.components.bar.SongBatchSelectionActions
import com.lonx.lyrico.ui.components.library.LibraryBottomNavigationBar
import com.lonx.lyrico.viewmodel.SongListViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinActivityViewModel
import top.yukonga.miuix.kmp.basic.Scaffold

val SECTIONS_ASC = listOf(
    "0"
) + ('A'..'Z').map { it.toString() } + listOf("#")

val SECTIONS_DESC = SECTIONS_ASC.asReversed()

enum class TopBarState {
    Selection, Default
}

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>(start = true, route = "library_home")
fun LibraryHomeScreen(
    navigator: DestinationsNavigator
) {
    val tabs = remember { LibraryTab.entries.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val songListViewModel: SongListViewModel = koinActivityViewModel()
    val songs by songListViewModel.songs.collectAsState()
    val isSelectionMode by songListViewModel.isSelectionMode.collectAsState(initial = false)
    val selectedSongUris by songListViewModel.selectedSongUris.collectAsState()
    var isBatchFabMenuExpanded by remember { mutableStateOf(false) }
    var bottomBarPadding by remember { mutableStateOf(0.dp) }

    BackHandler(enabled = isBatchFabMenuExpanded) {
        isBatchFabMenuExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                LibraryBottomNavigationBar(
                    tabs = tabs,
                    selectedTab = tabs[pagerState.currentPage],
                    onTabSelected = { tab ->
                        scope.launch {
                            isBatchFabMenuExpanded = false
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    }
                )
            }
        ) { paddingValues ->
            SideEffect {
                bottomBarPadding = paddingValues.calculateBottomPadding()
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) { page ->
                when (tabs[page]) {
                    LibraryTab.Songs -> SongsPage(navigator = navigator)
                    LibraryTab.Artists -> ArtistsPage(navigator = navigator)
                    LibraryTab.Albums -> AlbumsPage(navigator = navigator)
                }
            }
        }

        SongBatchSelectionActions(
            navigator = navigator,
            songs = songs,
            isSelectionMode = isSelectionMode && tabs[pagerState.currentPage] == LibraryTab.Songs,
            expanded = isBatchFabMenuExpanded,
            selectedSongUris = selectedSongUris,
            modifier = Modifier.padding(bottom = bottomBarPadding),
            onExpandedChange = { isBatchFabMenuExpanded = it },
            onSetSelectionUris = songListViewModel::setSelectionUris,
            onBatchDelete = songListViewModel::batchDelete,
            onBatchShare = songListViewModel::batchShare
        )
    }
}


