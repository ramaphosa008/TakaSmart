package com.ramaphosa.takasmart.ui.screens.facility

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_CERTIFICATE
import com.ramaphosa.takasmart.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyDeliveryScreen(navController: NavController, jobId: String) {

    val db = FirebaseFirestore.getInstance()

    var collectorLoggedKg by remember { mutableDoubleStateOf(0.0) }
    var facilityKgInput   by remember { mutableStateOf("") }
    var isSaving          by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf("") }
    var disputeRaised by remember { mutableStateOf(false) }

    // Load the collector's logged weight
    LaunchedEffect(jobId) {
        db.collection("pickups").document(jobId)
            .get()
            .addOnSuccessListener { snap ->
                collectorLoggedKg = snap.getDouble("collector_logged_kg") ?: 0.0
            }
    }

    val facilityKg   = facilityKgInput.toDoubleOrNull() ?: 0.0
    val difference   = if (collectorLoggedKg > 0)
        abs(facilityKg - collectorLoggedKg) / collectorLoggedKg else 0.0
    val isWithinTolerance = difference <= 0.15
    val collectorPayout   = facilityKg * 20
    val platformFee       = facilityKg * 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Verify delivery",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Job #${jobId.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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

            Spacer(Modifier.height(12.dp))

            // ── Collector's logged weight ──────────────────────
            Surface(
                shape    = RoundedCornerShape(8.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "Collector logged at pickup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "%.2f kg".format(collectorLoggedKg),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Facility scale input ───────────────────────────
            Text(
                text  = "Enter facility scale reading (kg)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value         = facilityKgInput,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        facilityKgInput = it
                        errorMessage    = ""
                    }
                },
                placeholder     = { Text("0.00") },
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor
                )
            )

            // ── Tolerance indicator ────────────────────────────
            if (facilityKg > 0 && collectorLoggedKg > 0) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isWithinTolerance) GreenSurface else ErrorSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text  = "Difference",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWithinTolerance) GreenDark else ErrorDark
                            )
                            Text(
                                text  = "%.1f%%".format(difference * 100),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWithinTolerance) GreenDark else ErrorDark
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = if (isWithinTolerance)
                                "Within tolerance — payment will proceed"
                            else
                                "Above 15% threshold — dispute will be raised",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isWithinTolerance) GreenDark else ErrorDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Payout summary ─────────────────────────────────
            if (facilityKg > 0) {
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Collector payout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "KES %.0f".format(collectorPayout),
                                style = MaterialTheme.typography.titleSmall,
                                color = Green
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Platform fee (charged to you)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "KES %.0f".format(platformFee),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text  = errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Approve button ─────────────────────────────────
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = facilityKg > 0 && !isSaving,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isWithinTolerance) Teal else ErrorRed,
                    contentColor   = White
                ),
                onClick  = {
                    isSaving     = true
                    errorMessage = ""

                    // In the Approve button onClick, replace the dispute path:
                    val newStatus = if (isWithinTolerance) "completed" else "disputed"

                    db.collection("pickups").document(jobId)
                        .update(
                            mapOf(
                                "facility_verified_kg" to facilityKg,
                                "status"               to newStatus,
                                "verified_kg"          to facilityKg,
                                "dispute_reason"       to if (!isWithinTolerance)
                                    "Weight mismatch: collector %.2f kg, facility %.2f kg (%.1f%% difference)"
                                        .format(collectorLoggedKg, facilityKg, difference * 100)
                                else ""
                            )
                        )
                        .addOnSuccessListener {
                            isSaving = false
                            if (isWithinTolerance) {
                                navController.navigate(
                                    ROUT_CERTIFICATE.replace("{jobId}", jobId)
                                )
                            } else {
                                // Show dispute raised confirmation
                                disputeRaised = true  // add this state variable
                            }
                        }
                        .addOnFailureListener { e ->
                            isSaving     = false
                            errorMessage = e.message ?: "Failed. Try again."
                        }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = if (isWithinTolerance)
                            "Approve & trigger payment"
                        else
                            "Flag dispute",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Dispute raised confirmation ────────────────────────
            if (disputeRaised) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = AmberSurface,
                    modifier = Modifier.fillMaxWidth(),
                    border   = BorderStroke(0.5.dp, Amber)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text       = "Dispute raised",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = AmberDark,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "Collector logged %.2f kg. Your scale reads %.2f kg. The %.1f%% difference exceeds the 15%% threshold. Payout is held pending admin review — usually resolved within 24 hours."
                                .format(collectorLoggedKg, facilityKg, difference * 100),
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberDark
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text  = "Dispute ID: ${jobId.take(8).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    onClick  = { navController.popBackStack() },
                    border   = BorderStroke(0.5.dp, BorderColor),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Back to dashboard",
                        color = GrayMid,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyDeliveryScreenPreview() {
    TakaSmartTheme {
        VerifyDeliveryPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyDeliveryPreviewContent() {
    val collectorLoggedKg = 3.4
    var facilityKgInput   by remember { mutableStateOf("3.2") }
    val facilityKg        = facilityKgInput.toDoubleOrNull() ?: 0.0
    val difference        = abs(facilityKg - collectorLoggedKg) / collectorLoggedKg
    val isWithinTolerance = difference <= 0.15
    val collectorPayout   = facilityKg * 20
    val platformFee       = facilityKg * 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Verify delivery",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Job #PKP001",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            Spacer(Modifier.height(12.dp))

            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically) {
                    Text("Collector logged at pickup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.2f kg".format(collectorLoggedKg),
                        style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Enter facility scale reading (kg)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value         = facilityKgInput,
                onValueChange = { facilityKgInput = it },
                placeholder   = { Text("0.00") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor
                )
            )

            if (facilityKg > 0) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp),
                    color    = if (isWithinTolerance) GreenSurface else ErrorSurface,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Difference",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWithinTolerance) GreenDark else ErrorDark)
                            Text("%.1f%%".format(difference * 100),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWithinTolerance) GreenDark else ErrorDark)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isWithinTolerance) "Within tolerance — payment will proceed"
                            else "Above 15% threshold — dispute will be raised",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isWithinTolerance) GreenDark else ErrorDark
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Collector payout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("KES %.0f".format(collectorPayout),
                                style = MaterialTheme.typography.titleSmall,
                                color = Green)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform fee (charged to you)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("KES %.0f".format(platformFee),
                                style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isWithinTolerance) Teal else ErrorRed,
                    contentColor   = White),
                onClick  = {}
            ) {
                Text(
                    if (isWithinTolerance) "Approve & trigger payment" else "Flag dispute",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}