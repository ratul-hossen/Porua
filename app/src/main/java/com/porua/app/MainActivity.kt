package com.porua.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.porua.app.ui.navigation.PoruaNavGraph
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.PoruaTheme
import com.porua.app.ui.viewmodel.PoruaViewModel
import com.porua.app.ui.viewmodel.PoruaViewModelFactory

class MainActivity : ComponentActivity() {

    private val poruaViewModel: PoruaViewModel by viewModels {
        PoruaViewModelFactory((application as PoruaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoruaTheme {
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
}
