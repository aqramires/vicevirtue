package com.aliceqr.vicevirtue.widget.ui

import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.unit.ColorProvider
import com.aliceqr.vicevirtue.domain.model.TrackableType

object ViceVirtueWidgetTheme {

    val viceRedProvider = ColorProvider(Color(0xFFC0392B))
    val virtueBlueProvider = ColorProvider(Color(0xFF2E4FA3))
    val surfaceProvider = ColorProvider(Color(0xFFFFFFFF))
    val onSurfaceProvider = ColorProvider(Color(0xFF0E0E0E))
    val subtleProvider = ColorProvider(Color(0xFF8E8E93))
    val viceSurfaceProvider = ColorProvider(Color(0xFFFFF0F0))
    val virtueSurfaceProvider = ColorProvider(Color(0xFFF0F4FF))
    val streakGoldProvider = ColorProvider(Color(0xFFFFB800))
    val streakElectricProvider = ColorProvider(Color(0xFF00C2FF))

    fun typeColorProvider(type: TrackableType) = when (type) {
        TrackableType.VICE -> viceRedProvider
        TrackableType.VIRTUE -> virtueBlueProvider
    }

    fun typeSurfaceProvider(type: TrackableType) = when (type) {
        TrackableType.VICE -> viceSurfaceProvider
        TrackableType.VIRTUE -> virtueSurfaceProvider
    }

    fun streakColorProvider(type: TrackableType) = when (type) {
        TrackableType.VICE -> streakGoldProvider
        TrackableType.VIRTUE -> streakElectricProvider
    }
}
