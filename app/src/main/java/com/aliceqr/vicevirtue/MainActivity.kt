package com.aliceqr.vicevirtue

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
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
                    val navController = rememberNavController()
                    
                    LaunchedEffect(intent) {
                        val deepLinkId = intent.getLongExtra("deep_link_trackable_id", -1L)
                        if (deepLinkId != -1L) {
                            Log.d("MainActivity", "Deep link ID detected: $deepLinkId")
                            navController.navigate(com.aliceqr.vicevirtue.ui.navigation.Screen.Detail.createRoute(deepLinkId)) {
                                popUpTo(com.aliceqr.vicevirtue.ui.navigation.Screen.Dashboard.route) { inclusive = false }
                            }
                        }
                    }

                    ViceVirtueNavGraph(
                        navController = navController,
                        startDestination = com.aliceqr.vicevirtue.ui.navigation.Screen.Dashboard.route
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
