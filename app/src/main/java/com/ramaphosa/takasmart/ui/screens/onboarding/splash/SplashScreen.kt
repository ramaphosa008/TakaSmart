package com.ramaphosa.takasmart.ui.screens.onboarding.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.navigation.ROUT_ROLE_SELECT
import com.ramaphosa.takasmart.navigation.ROUT_SPLASH
import com.ramaphosa.takasmart.ui.theme.GraySurface
import com.ramaphosa.takasmart.ui.theme.Teal
import com.ramaphosa.takasmart.ui.theme.GrayMid
import com.ramaphosa.takasmart.ui.theme.TakaSmartTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(4000)
        navController.navigate(ROUT_ROLE_SELECT) {
            popUpTo(ROUT_SPLASH ) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GraySurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text     = "♻",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text  = "Taka Smart",
                style = MaterialTheme.typography.titleLarge,
                color = Teal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = "Recycle. Earn. Impact.",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMid
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TakaSmartTheme {
        SplashScreen(rememberNavController())
    }
}
