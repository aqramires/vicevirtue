package com.aliceqr.vicevirtue.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TrackableCard
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import com.aliceqr.vicevirtue.ui.screens.history.HistoryItem
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLog: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("MMMM d, yyyy · HH:mm", Locale.getDefault()) }

    var eventToEdit by remember { mutableStateOf<TrackableEvent?>(null) }
    var eventsToDelete by remember { mutableStateOf<List<TrackableEvent>?>(null) }

    val tabs = listOf(
        TabItem(stringResource(R.string.vices), TrackableType.VICE, Icons.Default.Warning, Color(0xFFC0392B)),
        TabItem(stringResource(R.string.principal), null, Icons.Default.History, Color.Black),
        TabItem(stringResource(R.string.virtues), TrackableType.VIRTUE, Icons.Default.Shield, Color(0xFF2E4FA3))
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = 1)
    
    val isDark = uiState.themeMode == com.aliceqr.vicevirtue.data.repository.ThemeMode.DARK || 
            (uiState.themeMode == com.aliceqr.vicevirtue.data.repository.ThemeMode.SYSTEM && androidx.compose.foundation.isSystemInDarkTheme())

    // Static colors for content to avoid full-screen recomposition during swipe
    val contentColor = remember(pagerState.currentPage, isDark) {
        when (pagerState.currentPage) {
            0 -> if (isDark) Color(0xFFFF897D) else Color(0xFFC0392B)
            2 -> if (isDark) Color(0xFFD0E4FF) else Color(0xFF2E4FA3)
            else -> if (isDark) Color.White else Color.Black
        }
    }

    // Helper to get background color for a page
    fun getPageBgColor(page: Int): Color {
        return when (page) {
            1 -> if (isDark) Color.Black else Color(0xFFF5F5F5)
            else -> if (isDark) Color(0xFF1A1A1A) else Color.White
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tabs[pagerState.currentPage].icon,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = contentColor
                        )
                        Text(
                            tabs[pagerState.currentPage].title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent, // Managed by drawBehind
        modifier = Modifier.drawBehind {
            val offset = Math.abs(pagerState.currentPageOffsetFraction)
            val currentBg = getPageBgColor(pagerState.currentPage)
            
            drawRect(currentBg)
            
            if (offset > 0.01f) {
                val targetPage = if (pagerState.currentPageOffsetFraction > 0f) {
                    (pagerState.currentPage + 1).coerceAtMost(tabs.size - 1)
                } else {
                    (pagerState.currentPage - 1).coerceAtLeast(0)
                }
                val targetBg = getPageBgColor(targetPage)
                drawRect(targetBg, alpha = offset)
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage != 1) {
                FloatingActionButton(
                    onClick = {
                        val type = tabs[pagerState.currentPage].type?.name
                        navController.navigate(com.aliceqr.vicevirtue.ui.navigation.Screen.AddTrackable.createRoute(type = type))
                    },
                    containerColor = contentColor,
                    contentColor = if (isDark) Color.Black else Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_trackable))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = contentColor,
                indicator = { tabPositions: List<androidx.compose.material3.TabPosition> ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.drawWithContent {
                                val offsetFraction = pagerState.currentPageOffsetFraction
                                val offset = Math.abs(offsetFraction)
                                val currentTab = tabPositions[pagerState.currentPage]
                                val targetPage = if (offsetFraction > 0f) {
                                    (pagerState.currentPage + 1).coerceAtMost(tabPositions.size - 1)
                                } else if (offsetFraction < 0f) {
                                    (pagerState.currentPage - 1).coerceAtLeast(0)
                                } else {
                                    pagerState.currentPage
                                }
                                val nextTab = tabPositions[targetPage]
                                
                                val indicatorWidth = currentTab.width + (nextTab.width - currentTab.width) * offset
                                val indicatorOffset = currentTab.left + (nextTab.left - currentTab.left) * offset

                                // We use drawWithContent to avoid recomposing the indicator itself too often
                                // but actually for the indicator, offset() is also fine if limited to this scope.
                                // Let's use a simpler way:
                                this.drawContent()
                            }
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset {
                                val offsetFraction = pagerState.currentPageOffsetFraction
                                val offset = Math.abs(offsetFraction)
                                val currentTab = tabPositions[pagerState.currentPage]
                                val targetPage = if (offsetFraction > 0f) {
                                    (pagerState.currentPage + 1).coerceAtMost(tabPositions.size - 1)
                                } else if (offsetFraction < 0f) {
                                    (pagerState.currentPage - 1).coerceAtLeast(0)
                                } else {
                                    pagerState.currentPage
                                }
                                val nextTab = tabPositions[targetPage]
                                
                                val indicatorOffset = currentTab.left + (nextTab.left - currentTab.left) * offset
                                androidx.compose.ui.unit.IntOffset(indicatorOffset.roundToPx(), 0)
                            }
                            .width(
                                remember(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
                                    val offsetFraction = pagerState.currentPageOffsetFraction
                                    val offset = Math.abs(offsetFraction)
                                    val currentTab = tabPositions[pagerState.currentPage]
                                    val targetPage = if (offsetFraction > 0f) {
                                        (pagerState.currentPage + 1).coerceAtMost(tabPositions.size - 1)
                                    } else if (offsetFraction < 0f) {
                                        (pagerState.currentPage - 1).coerceAtLeast(0)
                                    } else {
                                        pagerState.currentPage
                                    }
                                    val nextTab = tabPositions[targetPage]
                                    currentTab.width + (nextTab.width - currentTab.width) * offset
                                }
                            ),
                            color = contentColor
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) contentColor else contentColor.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                val currentType = tabs[pageIndex].type
                val (filteredTrackables, filteredEvents) = when (pageIndex) {
                    0 -> uiState.vices to uiState.vicesEvents
                    2 -> uiState.virtues to uiState.virtuesEvents
                    else -> uiState.allTrackables to uiState.allRecentEvents
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredTrackables.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentType == null) stringResource(R.string.no_trackables_yet)
                                  else if (currentType == TrackableType.VICE) stringResource(R.string.no_vices_yet)
                                  else stringResource(R.string.no_virtues_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    val isHistoryExpanded = uiState.expandedSections[pageIndex] ?: false

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(ViceVirtueTokens.SpaceM.dp),
                        verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
                    ) {
                        items(filteredTrackables, key = { it.trackable.id }) { item ->
                            TrackableCard(
                                trackable = item.trackable,
                                streak = item.streak,
                                onLog = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (uiState.isCommentaryEnabled) {
                                        onNavigateToLog(item.trackable.id)
                                    } else {
                                        viewModel.logEvent(item.trackable)
                                    }
                                },
                                onClick = { onNavigateToDetail(item.trackable.id) },
                                onLongPress = { onNavigateToLog(item.trackable.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceL.dp))

                            val historyTitle = when (currentType) {
                                TrackableType.VICE -> stringResource(R.string.history_failures)
                                TrackableType.VIRTUE -> stringResource(R.string.history_triumphs)
                                else -> stringResource(R.string.overall_history)
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleSection(pageIndex) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = historyTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                                
                                if (isHistoryExpanded) {
                                    TextButton(
                                        onClick = {
                                            val type = currentType?.name
                                            navController.navigate(com.aliceqr.vicevirtue.ui.navigation.Screen.History.createRoute(type = type))
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.view_all),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceS.dp))
                        }

                        if (isHistoryExpanded) {
                            if (filteredEvents.isEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.no_events_logged),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(filteredEvents) { consolidated ->
                                    HistoryItem(
                                        consolidated = consolidated,
                                        onEditEvent = { eventToEdit = it },
                                        onDeleteEvents = { eventsToDelete = it },
                                        fullDateFormat = fullDateFormat,
                                        timeFormat = timeFormat,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Centralized Dialogs
    if (eventToEdit != null) {
        var editDescription by remember { mutableStateOf(eventToEdit!!.description) }
        AlertDialog(
            onDismissRequest = { eventToEdit = null },
            title = { Text(stringResource(R.string.edit_activity)) },
            text = {
                Column {
                    Text(
                        text = fullDateFormat.format(Date(eventToEdit!!.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text(stringResource(R.string.description_reason)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateEvent(eventToEdit!!, editDescription)
                        eventToEdit = null
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToEdit = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (eventsToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventsToDelete = null },
            title = { Text(stringResource(R.string.delete_entry_q)) },
            text = { Text(stringResource(R.string.delete_entry_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvents(eventsToDelete!!)
                        eventsToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { eventsToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

data class TabItem(
    val title: String,
    val type: TrackableType?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
