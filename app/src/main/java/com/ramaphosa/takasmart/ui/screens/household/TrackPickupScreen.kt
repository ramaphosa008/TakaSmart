package com.ramaphosa.takasmart.ui.screens.household


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.ui.theme.*

data class TimelineStep(
    val status : String,
    val label  : String,
    val subLabel: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPickupScreen(navController: NavController, pickupId: String) {

    val db = FirebaseFirestore.getInstance()

    var currentStatus  by remember { mutableStateOf("requested") }
    var scheduledAt    by remember { mutableStateOf("") }
    var collectorName  by remember { mutableStateOf("Awaiting assignment") }
    var itemCount      by remember { mutableIntStateOf(0) }

    // Listen to this specific pickup document in real time
    LaunchedEffect(pickupId) {
        db.collection("pickups").document(pickupId)
            .addSnapshotListener { snap, _ ->
                currentStatus = snap?.getString("status") ?: "requested"
                scheduledAt   = snap?.getString("scheduled_at") ?: ""
                itemCount     = (snap?.get("item_ids") as? List<*>)?.size ?: 0
            }
    }

    // The ordered steps a pickup goes through
    val steps = listOf(
        TimelineStep("requested",    "Pickup confirmed",       "Request received"),
        TimelineStep("confirmed",    "Collector assigned",     collectorName),
        TimelineStep("en_route",     "Collector en route",     "On the way to you"),
        TimelineStep("at_household", "Items collected",        "Collector has your items"),
        TimelineStep("at_facility",  "Delivered to facility",  "Being processed"),
        TimelineStep("completed",    "Points credited",        "Check your rewards")
    )

    val currentIndex = steps.indexOfFirst { it.status == currentStatus }.coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Pickup #${pickupId.take(8).uppercase()}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = scheduledAt.ifEmpty { "Scheduling..." },
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
                    StatusBadgeSmall(currentStatus)
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
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Summary card ───────────────────────────────────
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text  = "$itemCount item${if (itemCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text  = "Collector: $collectorName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text  = "Status",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // ── Timeline ───────────────────────────────────────
            steps.forEachIndexed { index, step ->
                val isDone   = index < currentIndex
                val isActive = index == currentIndex
                val isFuture = index > currentIndex

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Dot column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone   -> Teal
                                        isActive -> Teal
                                        else     -> BorderColor
                                    }
                                )
                        )
                        // Connector line — not shown after last item
                        if (index < steps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(
                                        if (isDone) Teal else BorderColor
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Text column
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text       = step.label,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = when {
                                isActive -> Teal
                                isFuture -> GrayLight
                                else     -> GrayMid
                            },
                            fontWeight = if (isActive)
                               FontWeight.Medium
                            else
                                FontWeight.Normal
                        )
                        if (step.subLabel.isNotEmpty() && !isFuture) {
                            Text(
                                text  = step.subLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Small badge used in the top bar
@Composable
fun StatusBadgeSmall(status: String) {
    val (bg, fg, label) = when (status) {
        "completed"    -> Triple(GreenSurface,  GreenDark,  "Completed")
        "en_route"     -> Triple(TealSurface,   TealDark,   "En route")
        "at_household" -> Triple(TealSurface,   TealDark,   "At your door")
        "at_facility"  -> Triple(PurpleSurface, PurpleDark, "At facility")
        "confirmed"    -> Triple(TealSurface,   TealDark,   "Confirmed")
        else           -> Triple(AmberSurface,  AmberDark,  "Pending")
    }
    Surface(shape = CircleShape, color = bg) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color    = fg,
            style    = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackPickupScreenPreview() {
    TakaSmartTheme {
        TrackPickupPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPickupPreviewContent() {
    val currentStatus = "en_route"

    val steps = listOf(
        Triple("requested",    "Pickup confirmed",      "Request received"),
        Triple("confirmed",    "Collector assigned",    "James M."),
        Triple("en_route",     "Collector en route",    "On the way to you"),
        Triple("at_household", "Items collected",       ""),
        Triple("at_facility",  "Delivered to facility", ""),
        Triple("completed",    "Points credited",       "")
    )
    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pickup #PKP001",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Sat 26 Apr · 9–11am",
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
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text("En route",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall)
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
        ) {
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("3 items", style = MaterialTheme.typography.titleSmall)
                        Text("Collector: James M.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Status", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            steps.forEachIndexed { index, (status, label, sub) ->
                val isDone   = index < currentIndex
                val isActive = index == currentIndex
                val isFuture = index > currentIndex

                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isDone || isActive) Teal else BorderColor))
                        if (index < steps.lastIndex) {
                            Box(modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(if (isDone) Teal else BorderColor))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text       = label,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = when {
                                isActive -> Teal
                                isFuture -> GrayLight
                                else     -> GrayMid
                            },
                            fontWeight = if (isActive) FontWeight.Medium
                            else FontWeight.Normal
                        )
                        if (sub.isNotEmpty() && !isFuture) {
                            Text(sub, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}