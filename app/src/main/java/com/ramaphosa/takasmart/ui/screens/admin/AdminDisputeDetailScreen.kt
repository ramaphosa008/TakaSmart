package com.ramaphosa.takasmart.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.data.AdminViewModel
import com.ramaphosa.takasmart.navigation.ROUT_ADMIN_HOME
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDisputeDetailScreen(
    navController : NavController,
    pickupId      : String
) {

    val db  = FirebaseFirestore.getInstance()
    val vm  : AdminViewModel = viewModel()

    var collectorLoggedKg  by remember { mutableDoubleStateOf(0.0) }
    var facilityVerifiedKg by remember { mutableDoubleStateOf(0.0) }
    var address            by remember { mutableStateOf("") }
    var scheduledAt        by remember { mutableStateOf("") }
    var collectorId        by remember { mutableStateOf("") }
    var householdId        by remember { mutableStateOf("") }
    var disputeReason      by remember { mutableStateOf("") }
    var isLoading          by remember { mutableStateOf(false) }
    var successMessage     by remember { mutableStateOf("") }
    var errorMessage       by remember { mutableStateOf("") }
    var showRejectDialog   by remember { mutableStateOf(false) }

    val diffPct = if (collectorLoggedKg > 0)
        kotlin.math.abs(facilityVerifiedKg - collectorLoggedKg) / collectorLoggedKg * 100
    else 0.0

    LaunchedEffect(pickupId) {
        db.collection("pickups").document(pickupId)
            .get()
            .addOnSuccessListener { snap ->
                collectorLoggedKg  = snap.getDouble("collector_logged_kg") ?: 0.0
                facilityVerifiedKg = snap.getDouble("facility_verified_kg") ?: 0.0
                address            = snap.getString("address") ?: ""
                scheduledAt        = snap.getString("scheduled_at") ?: ""
                collectorId        = snap.getString("collector_id") ?: ""
                householdId        = snap.getString("household_id") ?: ""
                disputeReason      = snap.getString("dispute_reason") ?: ""
            }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title   = { Text("Reject this pickup?") },
            text    = {
                Text(
                    "The pickup will be marked as rejected. No payment will be made and no certificate issued. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        isLoading        = true
                        vm.rejectPickup(
                            pickupId  = pickupId,
                            reason    = "Rejected by admin — significant weight discrepancy",
                            onSuccess = {
                                isLoading      = false
                                successMessage = "Pickup rejected. No payment will be made."
                            },
                            onError   = { msg ->
                                isLoading    = false
                                errorMessage = msg
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Confirm Reject", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Review Discrepancy",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text  = "Job #${pickupId.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = ErrorSurface
                    ) {
                        Text(
                            text = "FLAGGED",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ErrorRed
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Weight Comparison Header ─────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorSurface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f%%".format(diffPct),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = ErrorRed
                    )
                    Text(
                        text = "WEIGHT DIFFERENCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = ErrorRed.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ComparisonItem(label = "Collector", weight = collectorLoggedKg, icon = Icons.Default.DirectionsBike)
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GrayMid)
                        ComparisonItem(label = "Facility", weight = facilityVerifiedKg, icon = Icons.Default.Factory)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Details ───────────────────────────────
            Text(
                text = "PICKUP LOGS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = GrayMid
            )
            Spacer(Modifier.height(8.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    DetailRowItem("Pickup Point", address)
                    DetailRowItem("Time Logged", scheduledAt)
                    DetailRowItem("Collector UID", collectorId.take(12))
                    DetailRowItem("Household UID", householdId.take(12))
                    
                    if (disputeReason.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text("Collector's Note:", style = MaterialTheme.typography.labelSmall, color = GrayMid)
                        Text(disputeReason, style = MaterialTheme.typography.bodyMedium, color = GrayDark)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Success/Error ────────────────────────────────
            if (successMessage.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(successMessage, Modifier.padding(16.dp), color = GreenDark, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                }
            } else {
                // ── Action Buttons ───────────────────────────────
                Text(
                    text = "RESOLUTION ACTIONS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = GrayMid
                )
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        isLoading = true
                        vm.approveWithFacilityWeight(pickupId, facilityVerifiedKg, {
                            isLoading = false
                            successMessage = "Resolved using Facility Weight."
                        }, { errorMessage = it; isLoading = false })
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    enabled = !isLoading
                ) {
                    Text("Accept Facility Scale (%.2f kg)".format(facilityVerifiedKg), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isLoading = true
                        vm.approveWithCollectorWeight(pickupId, collectorLoggedKg, {
                            isLoading = false
                            successMessage = "Resolved using Collector Weight."
                        }, { errorMessage = it; isLoading = false })
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Teal),
                    enabled = !isLoading
                ) {
                    Text("Accept Collector Reading (%.2f kg)".format(collectorLoggedKg), color = Teal, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Flag as Fraudulent / Reject", color = ErrorRed, fontWeight = FontWeight.Medium)
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = ErrorRed, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun ComparisonItem(label: String, weight: Double, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(White.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GrayDark, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GrayMid)
        Text(text = "%.2f kg".format(weight), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GrayDark)
    }
}

@Composable
fun DetailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = GrayMid)
        Text(text = value.ifBlank { "N/A" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = GrayDark)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDisputeDetailScreenPreview() {
    TakaSmartTheme {
        AdminDisputeDetailScreen(rememberNavController(), "sample_id")
    }
}
