package com.aliceqr.vicevirtue.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ViceVirtueShapes = Shapes(
    // Chips, small badges
    extraSmall = RoundedCornerShape(4.dp),
    // Input fields, small cards
    small = RoundedCornerShape(8.dp),
    // Standard cards, buttons
    medium = RoundedCornerShape(16.dp),
    // Hero sections, bottom sheets
    large = RoundedCornerShape(24.dp),
    // Full-bleed modals
    extraLarge = RoundedCornerShape(32.dp)
)
