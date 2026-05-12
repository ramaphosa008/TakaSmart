package com.ramaphosa.takasmart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ramaphosa.takasmart.ui.screens.collector.ActivePickupScreen
import com.ramaphosa.takasmart.ui.screens.collector.CollectorHomeScreen
import com.ramaphosa.takasmart.ui.screens.collector.EarningsScreen
import com.ramaphosa.takasmart.ui.screens.collector.WeighItemsScreen
import com.ramaphosa.takasmart.ui.screens.facility.CertificateScreen
import com.ramaphosa.takasmart.ui.screens.facility.FacilityHomeScreen
import com.ramaphosa.takasmart.ui.screens.facility.VerifyDeliveryScreen
import com.ramaphosa.takasmart.ui.screens.household.HouseholdHomeScreen
import com.ramaphosa.takasmart.ui.screens.household.LogItemScreen
import com.ramaphosa.takasmart.ui.screens.household.RewardsScreen
import com.ramaphosa.takasmart.ui.screens.household.SchedulePickupScreen
import com.ramaphosa.takasmart.ui.screens.household.TrackPickupScreen
import com.ramaphosa.takasmart.ui.screens.onboarding.otpverification.OtpVerifyScreen
import com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry.PhoneEntryScreen
import com.ramaphosa.takasmart.ui.screens.onboarding.roleselection.RoleSelectScreen
import com.ramaphosa.takasmart.ui.screens.onboarding.splash.SplashScreen



@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUT_SPLASH
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {

        // ── Onboarding ──────────────────────────────────────────
        composable(ROUT_SPLASH) {
            SplashScreen(navController)
        }

        composable(ROUT_ROLE_SELECT) {
            RoleSelectScreen(navController)
        }

        composable(
            route     = ROUT_PHONE_ENTRY,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "household"
            PhoneEntryScreen(navController, role)
        }

        // Change the OTP route to carry role and entityId
        composable(
            route     = ROUT_OTP_VERIFY,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType },
                navArgument("role")           { type = NavType.StringType
                    defaultValue = "household" },
                navArgument("entityId")       { type = NavType.StringType
                    defaultValue = "" }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId")!!
            val role           = backStackEntry.arguments?.getString("role") ?: "household"
            val entityId       = backStackEntry.arguments?.getString("entityId") ?: ""
            OtpVerifyScreen(navController, verificationId, role, entityId)
        }

        // ── Household ────────────────────────────────────────────
        composable(ROUT_HOUSEHOLD_HOME) {
            HouseholdHomeScreen(navController)
        }

        composable(ROUT_LOG_ITEM) {
            LogItemScreen(navController)
        }

        composable(ROUT_SCHEDULE_PICKUP) {
            SchedulePickupScreen(navController)
        }

        composable(ROUT_REWARDS) {
            RewardsScreen(navController)
        }

        composable(
            route     = ROUT_TRACK_PICKUP,
            arguments = listOf(navArgument("pickupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pickupId = backStackEntry.arguments?.getString("pickupId")!!
            TrackPickupScreen(navController, pickupId)
        }

        // ── Collector ────────────────────────────────────────────
        composable(ROUT_COLLECTOR_HOME) {
            CollectorHomeScreen(navController)
        }

        composable(ROUT_EARNINGS) {
            EarningsScreen(navController)
        }

        composable(
            route     = ROUT_ACTIVE_PICKUP,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")!!
            ActivePickupScreen(navController, jobId)
        }

        composable(
            route     = ROUT_WEIGH_ITEMS,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")!!
            WeighItemsScreen(navController, jobId)
        }

        // ── Facility ─────────────────────────────────────────────
        composable(ROUT_FACILITY_HOME) {
            FacilityHomeScreen(navController)
        }

        composable(
            route     = ROUT_VERIFY_DELIVERY,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")!!
            VerifyDeliveryScreen(navController, jobId)
        }

        composable(
            route     = ROUT_CERTIFICATE,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")!!
            CertificateScreen(navController, jobId)
        }

    }
}

