package com.porua.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.porua.app.ui.navigation.PoruaNavGraph
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.PoruaTheme
import com.porua.app.ui.viewmodel.PoruaViewModel
import com.porua.app.ui.viewmodel.PoruaViewModelFactory

class MainActivity : ComponentActivity() {

    private val poruaViewModel: PoruaViewModel by viewModels {
        val app = application as PoruaApplication
        PoruaViewModelFactory(app.repository, app.authDataStore)
    }

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            poruaViewModel.refreshCountdown()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by poruaViewModel.uiState.collectAsState()
            PoruaTheme(darkTheme = uiState.isDarkMode) {
                val colors = LocalPoruaColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.bg
                ) {
                    val navController = rememberNavController()
                    PoruaNavGraph(
                        navController = navController,
                        viewModel = poruaViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        poruaViewModel.refreshCountdown()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(dateChangeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(dateChangeReceiver)
        } catch (_: Exception) {}
    }
}
