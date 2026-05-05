package com.aliceqr.vicevirtue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.theme.ViceRed
import com.aliceqr.vicevirtue.ui.theme.VirtueBlue

@Composable
fun TypeIconCircle(
    type: TrackableType,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (type == TrackableType.VICE) ViceRed.copy(alpha = 0.15f) else VirtueBlue.copy(alpha = 0.15f)
    val color = if (type == TrackableType.VICE) ViceRed else VirtueBlue
    val icon = if (type == TrackableType.VICE) Icons.Default.Warning else Icons.Default.Shield

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
