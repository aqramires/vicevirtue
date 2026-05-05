package com.aliceqr.vicevirtue.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TrackableCard
import com.aliceqr.vicevirtue.ui.screens.history.HistoryItem
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens

import androidx.compose.ui.res.stringResource
import com.aliceqr.vicevirtue.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLog: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.overview),
                        style = MaterialTheme.typography.displaySmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_trackable))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ViceVirtueTokens.SpaceM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.showVices,
                    onClick = { viewModel.toggleViceFilter() },
                    label = { Text(stringResource(R.string.vices)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                Spacer(modifier = Modifier.width(ViceVirtueTokens.SpaceS.dp))
                FilterChip(
                    selected = uiState.showVirtues,
                    onClick = { viewModel.toggleVirtueFilter() },
                    label = { Text(stringResource(R.string.virtues)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            val filteredTrackables = remember(uiState.trackables, uiState.showVices, uiState.showVirtues) {
                uiState.trackables.filter {
                    (it.trackable.type == TrackableType.VICE && uiState.showVices) ||
                    (it.trackable.type == TrackableType.VIRTUE && uiState.showVirtues)
                }
            }

            val filteredEvents = remember(uiState.allRecentEvents, uiState.showVices, uiState.showVirtues) {
                uiState.allRecentEvents.filter {
                    (it.trackable.type == TrackableType.VICE && uiState.showVices) ||
                    (it.trackable.type == TrackableType.VIRTUE && uiState.showVirtues)
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.trackables.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_trackables_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(ViceVirtueTokens.SpaceM.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
                ) {
                    items(filteredTrackables, key = { it.trackable.id }) { item ->
                        TrackableCard(
                            trackable = item.trackable,
                            streak = item.streak,
                            onLog = { viewModel.logEvent(item.trackable) },
                            onClick = { onNavigateToDetail(item.trackable.id) },
                            onLongPress = { onNavigateToLog(item.trackable.id) },
                            onDeleteEvent = { viewModel.deleteEvent(it) },
                            onUpdateEvent = { event, desc -> viewModel.updateEvent(event, desc) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceL.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.event_history),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(com.aliceqr.vicevirtue.ui.navigation.Screen.History.createRoute()) }
                            ) {
                                Text(stringResource(R.string.view_all))
                            }
                        }
                        Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceS.dp))
                    }

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
                                onDeleteEvents = viewModel::deleteEvents,
                                onUpdateEvent = viewModel::updateEvent,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
