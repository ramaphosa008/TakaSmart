package com.ramaphosa.takasmart.ui.screens.facility

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_FACILITY_HOME
import com.ramaphosa.takasmart.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(navController: NavController, jobId: String) {

    val db = FirebaseFirestore.getInstance()

    var verifiedKg    by remember { mutableDoubleStateOf(0.0) }
    var householdId by rememberSaveable { mutableStateOf("") }
    var collectorId   by remember { mutableStateOf("") }
    var scheduledAt   by remember { mutableStateOf("") }
    var isSending     by remember { mutableStateOf(false) }
    var sentSuccess   by remember { mutableStateOf(false) }

    val collectorPayout = verifiedKg * 20
    val householdPoints = (verifiedKg * 10).toInt()
    val platformFee     = verifiedKg * 10

    LaunchedEffect(jobId) {
        db.collection("pickups").document(jobId)
            .get()
            .addOnSuccessListener { snap ->
                verifiedKg  = snap.getDouble("verified_kg") ?: 0.0
                householdId = snap.getString("household_id") ?: ""
                collectorId = snap.getString("collector_id") ?: ""
                scheduledAt = snap.getString("scheduled_at") ?: ""
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Certificate issued",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Job #${jobId.take(8).uppercase()} complete",
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
                        color = GreenSurface
                    ) {
                        Text(
                            text     = "Done",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color    = GreenDark,
                            style    = MaterialTheme.typography.labelSmall
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Certificate card ───────────────────────────────
            OutlinedCard(
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(0.5.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = "RECYCLING CERTIFICATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "%.2f kg e-waste".format(verifiedKg),
                        style     = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = "Certified by EcoAct Recyclers",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text      = scheduledAt,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = "Job #${jobId.take(8).uppercase()}",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Payment summary ────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    SummaryRow("Collector paid", "KES %.0f via M-Pesa".format(collectorPayout), Green)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Household rewarded", "+$householdPoints points", Teal)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow(
                        "Platform fee charged",
                        "KES %.0f (monthly invoice)".format(platformFee),
                        GrayMid
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Success message ────────────────────────────────
            if (sentSuccess) {
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = GreenSurface,
                    modifier = Modifier.fillMaxWidth(),
                    border   = BorderStroke(0.5.dp, GreenBorder)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text       = "✓  Certificate issued successfully",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = GreenDark,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "Points credited to household",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenDark
                        )
                        Spacer(Modifier.height(4.dp))
                        // Mocked M-Pesa confirmation
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    text  = "M-Pesa payment initiated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrayMid
                                )
                                Text(
                                    text  = "KES %.0f → Collector · Ref: TKS${jobId.take(6).uppercase()}".format(collectorPayout),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayDark
                                )
                                Text(
                                    text  = "Status: Processing (2–5 min)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Amber
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
                Spacer(Modifier.height(12.dp))
            }

            // ── Send certificate button ────────────────────────
            if (!sentSuccess) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled =
                        verifiedKg > 0 &&
                                householdId.isNotBlank() &&
                                collectorId.isNotBlank() &&
                                !isSending,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Teal,
                        contentColor   = White
                    ),
                    onClick = {
                        isSending = true

                        // ── Step 1: Write certificate document ────────────────
                        val certificateData = mapOf(
                            "pickup_id"    to jobId,
                            "household_id" to householdId,
                            "collector_id" to collectorId,
                            "kg_processed" to verifiedKg,
                            "issued_at"    to FieldValue.serverTimestamp()
                        )

                        db.collection("certificates")
                            .add(certificateData)
                            .addOnSuccessListener { certRef ->

                                // ── Step 2: Credit household points ───────────
                                db.collection("users")
                                    .document(householdId)
                                    .update(
                                        mapOf(
                                            "points_balance" to FieldValue.increment(householdPoints.toLong()),
                                            "recycled_kg"    to FieldValue.increment(verifiedKg),
                                            "pickups_done"   to FieldValue.increment(1L)
                                        )
                                    )
                                    .addOnSuccessListener {

                                        // ── Step 3: Write reward record ────────
                                        db.collection("rewards").add(
                                            mapOf(
                                                "user_id"      to householdId,
                                                "points_earned" to householdPoints,
                                                "reason"       to "Pickup #${jobId.take(8)} — %.2f kg verified".format(verifiedKg),
                                                "pickup_id"    to jobId,
                                                "cert_id"      to certRef.id,
                                                "created_at"   to FieldValue.serverTimestamp()
                                            )
                                        )
                                            .addOnSuccessListener {
                                                isSending   = false
                                                sentSuccess = true
                                            }
                                            .addOnFailureListener {
                                                // Reward record failed but points were already credited
                                                // Still mark as success — non-critical failure
                                                isSending   = false
                                                sentSuccess = true
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isSending = false
                                        // Show error — points not credited
                                    }
                            }
                            .addOnFailureListener { e ->
                                isSending = false
                                // Show error — certificate not created
                            }
                    }
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Send certificate to household",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Back to facility home ──────────────────────────
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick  = {
                    navController.navigate(ROUT_FACILITY_HOME) {
                        popUpTo(ROUT_FACILITY_HOME) { inclusive = true }
                    }
                },
                border   = BorderStroke(0.5.dp, BorderColor),
                shape    = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Back to dashboard",
                    color = GrayMid,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }


// ── Summary row helper ─────────────────────────────────────────────────────
@Composable
fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CertificateScreenPreview() {
    TakaSmartTheme {
        CertificatePreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatePreviewContent() {
    val verifiedKg      = 3.2
    val collectorPayout = verifiedKg * 20
    val householdPoints = (verifiedKg * 10).toInt()
    val platformFee     = verifiedKg * 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Certificate issued",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Job #PKP001 complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = GreenSurface) {
                        Text("Done",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = GreenDark, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(12.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            OutlinedCard(
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(0.5.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RECYCLING CERTIFICATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("%.2f kg e-waste".format(verifiedKg),
                        style     = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("Certified by EcoAct Recyclers",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Text("Sat 26 Apr 2025",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("Job #PKP001",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    SummaryRow("Collector paid",
                        "KES %.0f via M-Pesa".format(collectorPayout), Green)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Household rewarded",
                        "+$householdPoints points", Teal)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Platform fee charged",
                        "KES %.0f (monthly invoice)".format(platformFee), GrayMid)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal, contentColor = White),
                onClick  = {}
            ) {
                Text("Send certificate to household",
                    style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick  = {},
                border   = BorderStroke(0.5.dp, BorderColor),
                shape    = RoundedCornerShape(10.dp)
            ) {
                Text("Back to dashboard", color = GrayMid,
                    style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}