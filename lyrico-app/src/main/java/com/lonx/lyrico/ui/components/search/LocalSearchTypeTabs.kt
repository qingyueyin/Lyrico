package com.lonx.lyrico.ui.components.search

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lonx.lyrico.data.model.search.LocalSearchResultTab
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TabRowWithContour

@Composable
fun LocalSearchTypeTabs(
    selectedTab: LocalSearchResultTab,
    onTabSelected: (LocalSearchResultTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRowWithContour(
        modifier = modifier,
        tabs = LocalSearchResultTab.entries.map { stringResource(it.labelRes) },
        selectedTabIndex = LocalSearchResultTab.entries.indexOf(selectedTab),
        onTabSelected = {
            onTabSelected(LocalSearchResultTab.entries[it])
        }
    )
}
