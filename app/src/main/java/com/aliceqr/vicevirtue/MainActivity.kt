package com.aliceqr.vicevirtue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aliceqr.vicevirtue.ui.navigation.ViceVirtueNavGraph
import com.aliceqr.vicevirtue.ui.theme.ViceVirtueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ViceVirtueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val deepLinkId = intent.getLongExtra("deep_link_trackable_id", -1L)
                    val startDestination = if (deepLinkId != -1L) {
                        com.aliceqr.vicevirtue.ui.navigation.Screen.Detail.createRoute(deepLinkId)
                    } else {
                        com.aliceqr.vicevirtue.ui.navigation.Screen.Dashboard.route
                    }

                    val navController = rememberNavController()
                    ViceVirtueNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
