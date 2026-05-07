package com.aliceqr.vicevirtue.ui.screens.logevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.trackable?.name ?: stringResource(R.string.log_event)) },
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
                .padding(ViceVirtueTokens.SpaceM.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ViceVirtueTokens.SpaceM.dp)
        ) {
            if (uiState.trackable == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text(
                    text = stringResource(R.string.add_commentary),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Start)
                )

                Text(
                    text = stringResource(R.string.add_commentary_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                var description by remember(uiState.trackable) { mutableStateOf(uiState.description) }

                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        viewModel.onDescriptionChange(it) 
                    },
                    label = { Text(stringResource(R.string.commentary_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val buttonColor = if (uiState.trackable?.type == TrackableType.VICE) 
                    MaterialTheme.colorScheme.secondary 
                else 
                    MaterialTheme.colorScheme.primary

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.saveEvent()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        val actionText = if (uiState.trackable?.type == TrackableType.VICE) 
                            stringResource(R.string.log_incident) 
                        else 
                            stringResource(R.string.log_progress)
                        Text(actionText)
                    }
                }
            }
        }
    }
}
