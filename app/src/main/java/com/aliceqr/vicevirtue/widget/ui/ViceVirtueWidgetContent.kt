package com.aliceqr.vicevirtue.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aliceqr.vicevirtue.MainActivity
import com.aliceqr.vicevirtue.R
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.widget.WidgetActionCallback
import com.aliceqr.vicevirtue.widget.WidgetState
import com.aliceqr.vicevirtue.widget.WidgetStateDefinition
import kotlinx.coroutines.runBlocking

@Composable
fun ViceVirtueWidgetContent() {
    val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
    val state = WidgetStateDefinition.toWidgetState(prefs)
    
    GlanceTheme {
        when {
            state.isLoading -> WidgetLoadingState()
            state.isError -> WidgetErrorState()
            else -> {
                val type = try {
                    TrackableType.valueOf(state.trackableType)
                } catch (e: Exception) {
                    TrackableType.VICE
                }
                WidgetReadyState(
                    state = state,
                    type = type
                )
            }
        }
    }
}

@Composable
fun WidgetReadyState(
    state: WidgetState,
    type: TrackableType
) {
    val context = LocalContext.current
    val glanceId = LocalGlanceId.current
    
    val appWidgetId = remember {
        runBlocking { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }
    }

    val typeColor = ViceVirtueWidgetTheme.typeColorProvider(type)
    val typeSurface = ViceVirtueWidgetTheme.typeSurfaceProvider(type)
    val streakColor = ViceVirtueWidgetTheme.streakColorProvider(type)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(typeSurface)
            .cornerRadius(24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Row 1: Icon + Name
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .background(typeColor)
                        .cornerRadius(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(
                            if (type == TrackableType.VICE) R.drawable.ic_widget_skull
                            else R.drawable.ic_widget_shield
                        ),
                        contentDescription = type.name,
                        modifier = GlanceModifier.size(20.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                    )
                }
                Spacer(GlanceModifier.width(10.dp))
                Text(
                    text = state.trackableName,
                    style = TextStyle(
                        color = ViceVirtueWidgetTheme.onSurfaceProvider,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            Spacer(GlanceModifier.height(10.dp))

            // Row 2: Streak
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                Text(
                    text = "${state.streak}",
                    style = TextStyle(
                        color = streakColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = context.getString(
                        if (type == TrackableType.VICE) R.string.widget_days_clean 
                        else R.string.widget_days_strong
                    ),
                    style = TextStyle(
                        color = ViceVirtueWidgetTheme.subtleProvider,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(12.dp))

            // Row 3: Action Button
            val buttonLabel = context.getString(
                if (type == TrackableType.VICE) R.string.failed 
                else R.string.triumphed
            )

            Button(
                text = buttonLabel,
                onClick = actionRunCallback<WidgetActionCallback>(
                    parameters = actionParametersOf(
                        WidgetActionCallback.KEY_WIDGET_ID to appWidgetId
                    )
                ),
                modifier = GlanceModifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = typeColor,
                    contentColor = ColorProvider(Color.White)
                )
            )
        }
    }
}

@Composable
fun WidgetLoadingState() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ViceVirtueWidgetTheme.surfaceProvider)
            .cornerRadius(24.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(
                color = ViceVirtueWidgetTheme.subtleProvider,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
fun WidgetErrorState() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ViceVirtueWidgetTheme.surfaceProvider)
            .cornerRadius(24.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = "⚠",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = "Trackable deleted.\nRemove & re-add\nthis widget.",
                style = TextStyle(
                    color = ViceVirtueWidgetTheme.subtleProvider,
                    fontSize = 12.sp
                )
            )
        }
    }
}
