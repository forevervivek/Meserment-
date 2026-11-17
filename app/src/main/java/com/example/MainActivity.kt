package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MeasureSheetScreen
import com.example.ui.MeasureSheetViewModel
import com.example.ui.components.SplashOverlay
import com.example.ui.theme.MeasureSheetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MeasureSheetApp
        val viewModelFactory = MeasureSheetViewModel.Factory(app.repository)

        setContent {
            MeasureSheetTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                val viewModel: MeasureSheetViewModel = viewModel(factory = viewModelFactory)

                Box(modifier = Modifier.fillMaxSize()) {
                    MeasureSheetScreen(viewModel = viewModel)
                    if (showSplash) {
                        SplashOverlay(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}
