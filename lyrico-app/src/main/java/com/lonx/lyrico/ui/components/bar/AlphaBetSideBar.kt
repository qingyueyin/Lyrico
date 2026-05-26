package com.lonx.lyrico.ui.components.bar

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.lyrico.R
import com.lonx.lyrico.viewmodel.SortOrder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private sealed interface AlphabetSideBarItem {
    data object ScrollTop : AlphabetSideBarItem
    data class Section(val value: String) : AlphabetSideBarItem
}

@Composable
fun AlphabetSideBar(
    sections: List<String>,
    sectionIndexMap: Map<String, Int>,
    order: SortOrder,
    scrollController: AlphabetSideBarScrollController,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var currentItem by remember { mutableStateOf<AlphabetSideBarItem?>(null) }
    var lastSelectedIndex by remember { mutableIntStateOf(-1) }

    val items = remember(sections) {
        listOf<AlphabetSideBarItem>(AlphabetSideBarItem.ScrollTop) +
                sections.map { AlphabetSideBarItem.Section(it) }
    }

    val resolvedSectionIndexMap = remember(sectionIndexMap, sections, order) {
        sections.associateWith { section ->
            findScrollIndex(
                section = section,
                sectionIndexMap = sectionIndexMap,
                order = order
            )
        }
    }

    LaunchedEffect(sections, sectionIndexMap, order, scrollController) {
        scrollJob?.cancel()
        scrollJob = null
        currentItem = null
        lastSelectedIndex = -1
    }

    fun scrollToIndexFromSideBar(index: Int) {
        if (index < 0) return

        if (scrollController.firstVisibleItemIndex == index) {
            return
        }

        scrollJob?.cancel()
        scrollJob = scope.launch {
            scrollController.scrollToItem(index)
        }
    }

    fun handleItemSelected(item: AlphabetSideBarItem) {
        when (item) {
            AlphabetSideBarItem.ScrollTop -> {
                scrollToIndexFromSideBar(0)
            }

            is AlphabetSideBarItem.Section -> {
                val targetIndex = resolvedSectionIndexMap[item.value] ?: 0
                scrollToIndexFromSideBar(targetIndex)
            }
        }
    }

    fun updateSelection(index: Int) {
        if (index == -1 || index == lastSelectedIndex) return
        if (index !in items.indices) return

        lastSelectedIndex = index

        val item = items[index]
        currentItem = item

        handleItemSelected(item)

        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun clearSelection() {
        currentItem = null
        lastSelectedIndex = -1
    }

    BoxWithConstraints(
        modifier = modifier
    ) {
        if (items.isEmpty()) {
            return@BoxWithConstraints
        }

        val preferredCellSize = 28.dp
        val dividerHeight = 0.5.dp
        val itemCount = items.size

        val totalDividerHeight = dividerHeight * (itemCount - 1)
        val availableCellHeight = ((maxHeight - totalDividerHeight) / itemCount.toFloat())
            .coerceAtLeast(1.dp)

        val cellSize = minOf(preferredCellSize, availableCellHeight)
        val barHeight = cellSize * itemCount + totalDividerHeight

        val cellHeightPx = with(density) { cellSize.toPx() }
        val dividerHeightPx = with(density) { dividerHeight.toPx() }

        fun getItemIndex(offsetY: Float): Int {
            val slotHeight = cellHeightPx + dividerHeightPx
            if (slotHeight <= 0f) return -1

            return (offsetY / slotHeight)
                .toInt()
                .coerceIn(0, items.lastIndex)
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(maxHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            AlphabetSideBarIndicator(
                item = currentItem
            )

            Box(
                modifier = Modifier.width(16.dp)
            )

            Column(
                modifier = Modifier
                    .width(cellSize)
                    .height(barHeight)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = 0.5.dp,
                        color = MiuixTheme.colorScheme.outline.copy(alpha = 0.46f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(MiuixTheme.colorScheme.surfaceContainer)
                    .pointerInput(items, cellSize, barHeight, resolvedSectionIndexMap) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                updateSelection(getItemIndex(offset.y))
                            },
                            onDragEnd = {
                                clearSelection()
                            },
                            onDragCancel = {
                                clearSelection()
                            }
                        ) { change, _ ->
                            change.consume()
                            updateSelection(getItemIndex(change.position.y))
                        }
                    }
                    .pointerInput(items, cellSize, barHeight, resolvedSectionIndexMap) {
                        detectTapGestures(
                            onPress = { offset ->
                                updateSelection(getItemIndex(offset.y))
                                tryAwaitRelease()
                                clearSelection()
                            }
                        )
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items.forEachIndexed { index, item ->
                    AlphabetSideBarCell(
                        item = item,
                        selected = currentItem == item,
                        size = cellSize
                    )

                    if (index != items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dividerHeight)
                                .background(
                                    MiuixTheme.colorScheme.outline.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun AlphabetSideBarIndicator(
    item: AlphabetSideBarItem?
) {
    AnimatedVisibility(
        visible = item != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        if (item != null) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .background(
                        color = MiuixTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    AlphabetSideBarItem.ScrollTop -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_up_24dp),
                            modifier = Modifier.size(30.dp),
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onPrimary
                        )
                    }

                    is AlphabetSideBarItem.Section -> {
                        Text(
                            text = item.value,
                            style = MiuixTheme.textStyles.title1,
                            color = MiuixTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
interface AlphabetSideBarScrollController {
    val firstVisibleItemIndex: Int
    suspend fun scrollToItem(index: Int)
}

private class LazyListAlphabetScrollController(
    private val state: LazyListState
) : AlphabetSideBarScrollController {
    override val firstVisibleItemIndex: Int
        get() = state.firstVisibleItemIndex

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }
}

private class LazyGridAlphabetScrollController(
    private val state: LazyGridState
) : AlphabetSideBarScrollController {
    override val firstVisibleItemIndex: Int
        get() = state.firstVisibleItemIndex

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }
}

@Composable
fun rememberAlphabetSideBarScrollController(
    state: LazyListState
): AlphabetSideBarScrollController {
    return remember(state) {
        LazyListAlphabetScrollController(state)
    }
}

@Composable
fun rememberAlphabetSideBarScrollController(
    state: LazyGridState
): AlphabetSideBarScrollController {
    return remember(state) {
        LazyGridAlphabetScrollController(state)
    }
}
@Composable
private fun AlphabetSideBarCell(
    item: AlphabetSideBarItem,
    selected: Boolean,
    size: Dp
) {
    val fontSize = when {
        size < 15.dp -> 6.sp
        size < 20.dp -> 9.sp
        size < 24.dp -> 10.sp
        else -> 12.sp
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if (selected) {
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MiuixTheme.colorScheme.surfaceContainer
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            AlphabetSideBarItem.ScrollTop -> {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_up_24dp),
                    contentDescription = null,
                    tint = if (selected) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onSurfaceContainer
                    },
                    modifier = Modifier.size(size * 0.62f)
                )
            }

            is AlphabetSideBarItem.Section -> {
                Text(
                    text = item.value,
                    style = MiuixTheme.textStyles.body2.copy(fontSize = fontSize),
                    color = if (selected) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.onSurfaceContainer
                    },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}


fun findScrollIndex(
    section: String,
    sectionIndexMap: Map<String, Int>,
    order: SortOrder
): Int {
    if (sectionIndexMap.isEmpty()) return 0

    sectionIndexMap[section]?.let { return it }

    val keys = sectionIndexMap.keys.sorted()
    if (keys.isEmpty()) return 0

    return if (order == SortOrder.ASC) {
        keys.firstOrNull { it >= section }
            ?.let { sectionIndexMap[it] }
            ?: sectionIndexMap[keys.last()]
            ?: 0
    } else {
        keys.lastOrNull { it <= section }
            ?.let { sectionIndexMap[it] }
            ?: sectionIndexMap[keys.first()]
            ?: 0
    }
}