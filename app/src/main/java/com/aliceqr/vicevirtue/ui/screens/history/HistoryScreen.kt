package com.aliceqr.vicevirtue.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aliceqr.vicevirtue.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TypeIconCircle
import com.aliceqr.vicevirtue.ui.theme.ViceRed
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens
import com.aliceqr.vicevirtue.ui.theme.VirtueBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add

// ...

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showDatePicker by remember { mutableStateOf(false) }

    val title = when {
        uiState.filterTrackable != null -> {
            if (uiState.filterTrackable!!.type == TrackableType.VICE) stringResource(R.string.history_failures)
            else stringResource(R.string.history_triumphs)
        }
        uiState.filterType == TrackableType.VICE -> stringResource(R.string.history_failures)
        uiState.filterType == TrackableType.VIRTUE -> stringResource(R.string.history_triumphs)
        else -> stringResource(R.string.event_history)
    }

    val subtitle = when {
        uiState.filterTrackable != null -> {
            if (uiState.filterTrackable!!.type == TrackableType.VICE) stringResource(R.string.dont_give_up)
            else stringResource(R.string.congratulations)
        }
        uiState.filterType == TrackableType.VICE -> stringResource(R.string.dont_give_up)
        uiState.filterType == TrackableType.VIRTUE -> stringResource(R.string.congratulations)
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filters Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.filterTrackableId == null) {
                    FilterChip(
                        selected = uiState.filterType == TrackableType.VICE,
                        onClick = { viewModel.onFilterTypeChange(if (uiState.filterType == TrackableType.VICE) null else TrackableType.VICE) },
                        label = { Text(stringResource(R.string.vices)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    FilterChip(
                        selected = uiState.filterType == TrackableType.VIRTUE,
                        onClick = { viewModel.onFilterTypeChange(if (uiState.filterType == TrackableType.VIRTUE) null else TrackableType.VIRTUE) },
                        label = { Text(stringResource(R.string.virtues)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                FilterChip(
                    selected = uiState.startDate != null,
                    onClick = { showDatePicker = true },
                    label = { 
                        if (uiState.startDate != null && uiState.endDate != null) {
                            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                            val startStr = sdf.format(Date(uiState.startDate!!))
                            val endStr = sdf.format(Date(uiState.endDate!!))
                            if (startStr == endStr) {
                                Text(startStr)
                            } else {
                                Text("$startStr - $endStr")
                            }
                        } else {
                            Text(stringResource(R.string.date_range))
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (uiState.startDate != null) {
                        {
                            IconButton(onClick = { viewModel.onDateRangeChange(null, null) }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(14.dp))
                            }
                        }
                    } else null
                )
            }


            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.consolidatedEvents.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.no_events_match),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    val groupedEvents = remember(uiState.consolidatedEvents) {
                        uiState.consolidatedEvents.groupBy { it.date }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        groupedEvents.forEach { (date, events) ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(date)),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            items(events) { consolidated ->
                                HistoryItem(
                                    consolidated = consolidated,
                                    onDeleteEvents = viewModel::deleteEvents,
                                    onUpdateEvent = viewModel::updateEvent,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DateRangePickerModal(
            initialSelectedStartDateMillis = uiState.startDate,
            initialSelectedEndDateMillis = uiState.endDate,
            onDateRangeSelected = { (start, end) ->
                viewModel.onDateRangeChange(start, end)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    initialSelectedStartDateMillis: Long? = null,
    initialSelectedEndDateMillis: Long? = null,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialSelectedStartDateMillis,
        initialSelectedEndDateMillis = initialSelectedEndDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    onDateRangeSelected(Pair(start, end))
                    onDismiss()
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = stringResource(R.string.select_date_range),
                    modifier = Modifier.padding(16.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        )
    }
}

@Composable
fun HistoryItem(
    consolidated: ConsolidatedEvent,
    onDeleteEvents: (List<TrackableEvent>) -> Unit,
    onUpdateEvent: (TrackableEvent, String) -> Unit,
    modifier: Modifier = Modifier,
    showTrackableName: Boolean = true,
    showIcon: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<TrackableEvent?>(null) }
    var eventsToDelete by remember { mutableStateOf<List<TrackableEvent>?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (consolidated.trackable.type == TrackableType.VICE) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIcon) {
                    TypeIconCircle(type = consolidated.trackable.type, size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showTrackableName) {
                            Text(
                                text = consolidated.trackable.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        val statusColor = if (consolidated.trackable.type == TrackableType.VICE) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.primary

                        if (consolidated.occurrences.size > 1) {
                            Text(
                                text = "x${consolidated.occurrences.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                    }

                    if (consolidated.description.isNotEmpty()) {
                        Text(
                            text = consolidated.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = formatTimestamp(consolidated.occurrences.first().timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                }

                // Edit/Delete Shortcuts
                Row {
                    IconButton(
                        onClick = { eventToEdit = consolidated.occurrences.first() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = stringResource(R.string.edit), 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { eventsToDelete = consolidated.occurrences },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = stringResource(R.string.delete), 
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(4.dp))
                consolidated.occurrences.forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (showIcon) 44.dp else 0.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { eventToEdit = event },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { eventsToDelete = listOf(event) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (eventToEdit != null) {
        var editDescription by remember { mutableStateOf(eventToEdit!!.description) }
        AlertDialog(
            onDismissRequest = { eventToEdit = null },
            title = { Text(stringResource(R.string.edit_activity)) },
            text = {
                Column {
                    Text(
                        text = formatTimestamp(eventToEdit!!.timestamp),
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
                        onUpdateEvent(eventToEdit!!, editDescription)
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
                        onDeleteEvents(eventsToDelete!!)
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

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM d, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
