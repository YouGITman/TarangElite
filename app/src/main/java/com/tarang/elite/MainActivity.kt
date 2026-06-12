package com.tarang.elite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.tarang.elite.ui.navigation.RootScaffold
import com.tarang.elite.ui.theme.TarangTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarangTheme {
                RootScaffold()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val container = (application as TarangApp).container
        lifecycleScope.launch { container.healthRepo.safeSync() }
    }
}
