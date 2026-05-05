package com.aliceqr.vicevirtue.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.components.TypeIconCircle
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            ViceVirtueTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onTrackableSelected = { trackableId ->
                        lifecycleScope.launch {
                            viewModel.saveWidgetBinding(appWidgetId, trackableId)

                            val resultIntent = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultIntent)

                            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(this@WidgetConfigActivity)
                            ViceVirtueWidget().let { widget ->
                                widget.refreshWidgetState(this@WidgetConfigActivity, manager.getGlanceIdBy(appWidgetId), appWidgetId)
                                widget.update(this@WidgetConfigActivity, manager.getGlanceIdBy(appWidgetId))
                            }
                            finish()
                        }
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    viewModel: WidgetConfigViewModel,
    onTrackableSelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.choose_trackable)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.vices.isEmpty() && uiState.virtues.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.add_first),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (uiState.vices.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.vices),
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(uiState.vices) { vice ->
                        TrackableRow(trackable = vice, onClick = { onTrackableSelected(vice.id) })
                    }
                }

                if (uiState.virtues.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = stringResource(R.string.virtues),
                            icon = Icons.Default.Shield,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(uiState.virtues) { virtue ->
                        TrackableRow(trackable = virtue, onClick = { onTrackableSelected(virtue.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TrackableRow(trackable: Trackable, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeIconCircle(type = trackable.type, size = 40.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = trackable.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
