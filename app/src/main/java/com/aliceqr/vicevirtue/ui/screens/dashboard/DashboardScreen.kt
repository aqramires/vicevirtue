package com.aliceqr.vicevirtue.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TrackableCard
import com.aliceqr.vicevirtue.ui.components.TrackableSummaryCard
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToAdd: (String) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLog: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    
    val title = when (pagerState.currentPage) {
        0 -> stringResource(R.string.vices)
        1 -> stringResource(R.string.principal)
        else -> stringResource(R.string.virtues)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pagerState.currentPage == 0) {
                            com.aliceqr.vicevirtue.ui.components.TypeIconCircle(
                                type = com.aliceqr.vicevirtue.domain.model.TrackableType.VICE,
                                size = 32.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            title,
                            style = MaterialTheme.typography.displaySmall
                        ) 
                        if (pagerState.currentPage == 2) {
                            Spacer(Modifier.width(8.dp))
                            com.aliceqr.vicevirtue.ui.components.TypeIconCircle(
                                type = com.aliceqr.vicevirtue.domain.model.TrackableType.VIRTUE,
                                size = 32.dp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage != 1) {
                FloatingActionButton(
                    onClick = { 
                        val type = if (pagerState.currentPage == 0) TrackableType.VICE else TrackableType.VIRTUE
                        onNavigateToAdd(type.name) 
                    },
                    containerColor = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = when (pagerState.currentPage) {
                            0 -> MaterialTheme.colorScheme.secondary
                            1 -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.vices)) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.principal)) }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text(stringResource(R.string.virtues)) }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> TrackableList(
                            trackables = uiState.trackables.filter { it.trackable.type == TrackableType.VICE },
                            type = TrackableType.VICE,
                            isCommentaryEnabled = uiState.isCommentaryEnabled,
                            viewModel = viewModel,
                            onNavigateToLog = onNavigateToLog,
                            onNavigateToDetail = onNavigateToDetail,
                            haptic = haptic
                        )
                        1 -> MainSummary(
                            uiState = uiState,
                            onNavigateToDetail = onNavigateToDetail,
                            onNavigateToHistory = { navController.navigate(com.aliceqr.vicevirtue.ui.navigation.Screen.History.createRoute()) },
                            onDeleteEvents = { viewModel.deleteEvents(it) },
                            onUpdateEvent = { event, desc -> viewModel.updateEvent(event, desc) }
                        )
                        2 -> TrackableList(
                            trackables = uiState.trackables.filter { it.trackable.type == TrackableType.VIRTUE },
                            type = TrackableType.VIRTUE,
                            isCommentaryEnabled = uiState.isCommentaryEnabled,
                            viewModel = viewModel,
                            onNavigateToLog = onNavigateToLog,
                            onNavigateToDetail = onNavigateToDetail,
                            haptic = haptic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackableList(
    trackables: List<TrackableWithStreak>,
    type: TrackableType,
    isCommentaryEnabled: Boolean,
    viewModel: DashboardViewModel,
    onNavigateToLog: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    if (trackables.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (type == TrackableType.VICE) 
                    stringResource(R.string.no_vices_yet) 
                else 
                    stringResource(R.string.no_virtues_yet),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(ViceVirtueTokens.SpaceL.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(ViceVirtueTokens.SpaceM.dp),
            verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
        ) {
            items(trackables, key = { it.trackable.id }) { item ->
                TrackableCard(
                    trackable = item.trackable,
                    streak = item.streak,
                    onLog = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isCommentaryEnabled) {
                            onNavigateToLog(item.trackable.id)
                        } else {
                            viewModel.logEvent(item.trackable) 
                        }
                    },
                    onClick = { onNavigateToDetail(item.trackable.id) },
                    onDeleteEvent = { viewModel.deleteEvent(it) },
                    onUpdateEvent = { event, desc -> viewModel.updateEvent(event, desc) }
                )
            }
        }
    }
}

@Composable
fun MainSummary(
    uiState: DashboardUiState,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onDeleteEvents: (List<com.aliceqr.vicevirtue.domain.model.TrackableEvent>) -> Unit,
    onUpdateEvent: (com.aliceqr.vicevirtue.domain.model.TrackableEvent, String) -> Unit
) {
    var historyExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(ViceVirtueTokens.SpaceM.dp),
        verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.your_trackables),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        val allTrackables = uiState.trackables.sortedByDescending { it.streak }
        if (allTrackables.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_trackables_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = ViceVirtueTokens.SpaceL.dp)
                )
            }
        } else {
            items(allTrackables, key = { "main_${it.trackable.id}" }) { item ->
                TrackableSummaryCard(
                    trackable = item.trackable,
                    streak = item.streak,
                    onClick = { onNavigateToDetail(item.trackable.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceL.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { historyExpanded = !historyExpanded }) {
                    Icon(
                        imageVector = if (historyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.history)
                    )
                }
            }
        }

        if (historyExpanded) {
            item {
                TextButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.view_all_history))
                }
            }
            if (uiState.allRecentEvents.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_events_logged),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = ViceVirtueTokens.SpaceL.dp)
                    )
                }
            } else {
                items(uiState.allRecentEvents) { consolidated ->
                    com.aliceqr.vicevirtue.ui.screens.history.HistoryItem(
                        consolidated = consolidated,
                        onDeleteEvents = onDeleteEvents,
                        onUpdateEvent = onUpdateEvent,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
