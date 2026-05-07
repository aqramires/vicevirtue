package com.aliceqr.vicevirtue.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.StreakChip
import com.aliceqr.vicevirtue.ui.components.TypeIconCircle
import com.aliceqr.vicevirtue.ui.navigation.Screen
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.ui.screens.history.ConsolidatedEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.aliceqr.vicevirtue.ui.theme.ViceRed
import com.aliceqr.vicevirtue.ui.theme.VirtueBlue

import androidx.compose.ui.res.stringResource
import com.aliceqr.vicevirtue.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("MMMM d, yyyy · HH:mm", Locale.getDefault()) }

    var eventToEdit by remember { mutableStateOf<TrackableEvent?>(null) }
    var eventsToDelete by remember { mutableStateOf<List<TrackableEvent>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.trackable?.name ?: stringResource(R.string.principal)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        uiState.trackable?.let {
                            navController.navigate(Screen.AddTrackable.createRoute(trackableId = it.id))
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val trackable = uiState.trackable!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (trackable.type == TrackableType.VICE) 
                                MaterialTheme.colorScheme.secondaryContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                TypeIconCircle(type = trackable.type, size = 64.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                StreakChip(streak = uiState.streak, type = trackable.type)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = trackable.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recent History Section
                    val historyTitle = if (trackable.type == TrackableType.VICE) 
                        stringResource(R.string.history_failures) 
                    else 
                        stringResource(R.string.history_triumphs)
                    
                    val historySubtitle = if (trackable.type == TrackableType.VICE) 
                        stringResource(R.string.dont_give_up) 
                    else 
                        stringResource(R.string.congratulations)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = historyTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = historySubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        TextButton(
                            onClick = { navController.navigate(Screen.History.createRoute(trackable.id)) },
                            modifier = Modifier.padding(top = 0.dp)
                        ) {
                            Text(stringResource(R.string.view_all))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.recentEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_events_logged))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uiState.recentEvents) { consolidated ->
                                com.aliceqr.vicevirtue.ui.screens.history.HistoryItem(
                                    consolidated = consolidated,
                                    onEditEvent = { eventToEdit = it },
                                    onDeleteEvents = { eventsToDelete = it },
                                    fullDateFormat = fullDateFormat,
                                    timeFormat = timeFormat,
                                    showTrackableName = false,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val buttonColor = if (trackable.type == TrackableType.VICE) 
                        MaterialTheme.colorScheme.secondary 
                    else 
                        MaterialTheme.colorScheme.primary
                    val buttonText = if (trackable.type == TrackableType.VICE) stringResource(R.string.failed) else stringResource(R.string.triumphed)

                    Button(
                        onClick = { navController.navigate(Screen.LogEvent.createRoute(trackable.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text(buttonText)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_trackable_q)) },
            text = { Text(stringResource(R.string.delete_trackable_confirm, uiState.trackable?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrackable()
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
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
