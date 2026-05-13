package com.example.fitme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fitme.ui.MainScreen
import androidx.lifecycle.lifecycleScope
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.seed.DatabaseSeeder
import com.example.fitme.ui.MainScreen
import com.example.fitme.ui.theme.FitmeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseSeeder(AppDatabase.getInstance(applicationContext)).seedIfNeeded()
        }

        enableEdgeToEdge()
        setContent {
            FitmeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}