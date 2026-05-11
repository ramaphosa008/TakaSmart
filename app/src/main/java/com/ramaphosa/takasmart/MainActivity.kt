package com.ramaphosa.takasmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.navigation.AppNavHost
import com.ramaphosa.takasmart.ui.theme.TakaSmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TakaSmartTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}