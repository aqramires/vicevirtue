package com.aliceqr.vicevirtue.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.ui.theme.StreakElectric
import com.aliceqr.vicevirtue.ui.theme.StreakGold

@Composable
fun StreakChip(
    streak: Int,
    type: TrackableType,
    modifier: Modifier = Modifier
) {
    val color = if (type == TrackableType.VICE) {
        StreakGold
    } else {
        StreakElectric
    }

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = streak.toString(),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
