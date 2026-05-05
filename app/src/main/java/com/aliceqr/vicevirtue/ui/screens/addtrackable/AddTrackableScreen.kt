package com.aliceqr.vicevirtue.ui.screens.addtrackable

import android.app.TimePickerDialog
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TypeIconCircle
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackableScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTrackableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                viewModel.addReminder(hour, minute)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.trackableId == -1L) stringResource(R.string.new_trackable) else stringResource(R.string.edit)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(scrollState)
                .padding(ViceVirtueTokens.SpaceM.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.name.isEmpty()
            )

            Text(
                text = stringResource(R.string.type),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceS.dp)
            ) {
                TrackableType.values().forEach { type ->
                    val isSelected = uiState.type == type
                    val containerColor = if (type == TrackableType.VIRTUE) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onTypeChange(type) },
                        enabled = uiState.trackableId == -1L,
                        label = { 
                            Text(if (type == TrackableType.VIRTUE) stringResource(R.string.virtue) else stringResource(R.string.vice)) 
                        },
                        leadingIcon = {
                            TypeIconCircle(
                                type = type, 
                                size = 24.dp,
                                iconTint = if (isSelected) MaterialTheme.colorScheme.onPrimary else null,
                                background = if (isSelected) androidx.compose.ui.graphics.Color.Transparent else null
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = containerColor,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Reminders Section
            Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceS.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reminders),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = ::showTimePicker) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_reminder))
                }
            }

            if (uiState.reminders.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_reminders),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = ViceVirtueTokens.SpaceS.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceS.dp)
                ) {
                    uiState.reminders.forEach { reminder ->
                        androidx.compose.runtime.key(reminder.id) {
                            ReminderItem(
                                reminder = reminder,
                                onToggle = { viewModel.toggleReminder(reminder) },
                                onDelete = { viewModel.deleteReminder(reminder) }
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                val errorMsg = when(uiState.error) {
                    "error_name_empty" -> stringResource(R.string.error_name_empty)
                    "error_name_exists" -> stringResource(R.string.error_name_exists)
                    "failed_to_save" -> stringResource(R.string.failed_to_save)
                    else -> uiState.error!!
                }
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val buttonColor = if (uiState.type == TrackableType.VIRTUE) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }

            Button(
                onClick = viewModel::saveTrackable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save_trackable))
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val timeText = String.format("%02d:%02d", reminder.hour, reminder.minute)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ViceVirtueTokens.RadiusM.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = ViceVirtueTokens.SpaceM.dp, vertical = ViceVirtueTokens.SpaceS.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(ViceVirtueTokens.SpaceM.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (reminder.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
