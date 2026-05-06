package com.aliceqr.vicevirtue.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.theme.ViceRed
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTokens
import com.aliceqr.vicevirtue.ui.theme.VirtueBlue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackableCard(
    trackable: Trackable,
    streak: Int,
    onLog: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteEvent: (TrackableEvent) -> Unit = {},
    onUpdateEvent: (TrackableEvent, String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = ViceVirtueTokens.ElevationCard.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (trackable.type == TrackableType.VICE) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(ViceVirtueTokens.SpaceM.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeIconCircle(type = trackable.type)

                Spacer(modifier = Modifier.width(ViceVirtueTokens.SpaceM.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trackable.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    StreakChip(streak = streak, type = trackable.type)

                    trackable.targetStreak?.let { target ->
                        if (target > 0) {
                            Spacer(modifier = Modifier.height(ViceVirtueTokens.SpaceS.dp))
                            val progress = (streak.toFloat() / target).coerceIn(0f, 1f)
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(0.8f).height(4.dp),
                                color = if (trackable.type == TrackableType.VIRTUE) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }

                Button(
                    onClick = onLog,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (trackable.type == TrackableType.VICE) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    val logText = if (trackable.type == TrackableType.VICE) 
                        stringResource(R.string.failed) 
                    else 
                        stringResource(R.string.triumphed)
                    Text(text = logText)
                }
            }
        }
    }
}
