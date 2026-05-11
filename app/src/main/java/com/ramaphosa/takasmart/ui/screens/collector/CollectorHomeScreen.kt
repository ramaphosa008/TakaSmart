package com.ramaphosa.takasmart.ui.screens.collector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramaphosa.takasmart.navigation.ROUT_ACTIVE_PICKUP
import com.ramaphosa.takasmart.navigation.ROUT_EARNINGS
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.CollectorViewModel
import com.ramaphosa.takasmart.data.JobSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorHomeScreen(navController: NavController) {

    val vm: CollectorViewModel = viewModel()
    val availableJobs by vm.availableJobs.collectAsState()
    val activeJob     by vm.activeJob.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Available pickups",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "${availableJobs.size} jobs nearby",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Online badge
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text(
                            text     = "Online",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color    = TealDark,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Earnings shortcut
                    TextButton(onClick = { navController.navigate(ROUT_EARNINGS) }) {
                        Text(
                            text  = "Earnings",
                            color = Teal,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Active job banner ──────────────────────────────
            activeJob?.let { job ->
                item {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = TealSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment   = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text  = "Active job in progress",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TealDark
                                )
                                Text(
                                    text  = job.address.ifEmpty { "No address" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TealDark
                                )
                            }
                            Button(
                                onClick = {
                                    navController.navigate(
                                        ROUT_ACTIVE_PICKUP.replace("{jobId}", job.id)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Teal
                                )
                            ) {
                                Text("Resume", color = White)
                            }
                        }
                    }
                }
            }

            // ── Available jobs ─────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
            }

            if (availableJobs.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text  = "No jobs available right now",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = "Pull to refresh or wait for new pickups",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(availableJobs) { job ->
                    JobCard(
                        job      = job,
                        onAccept = {
                            vm.acceptJob(job.id) {
                                navController.navigate(
                                    ROUT_ACTIVE_PICKUP.replace("{jobId}", job.id)
                                )
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}


// ── Job card composable ────────────────────────────────────────────────────


@Composable
fun JobCard(job: JobSummary, onAccept: () -> Unit) {
    val estimatedKg     = job.itemCount * 0.5
    val estimatedPayout = estimatedKg * 20

    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "${job.itemCount} item${if (job.itemCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(shape = CircleShape, color = AmberSurface) {
                    Text(
                        text     = "Nearby",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color    = AmberDark,
                        style    = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text  = job.address.ifEmpty { "Address not set" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "${job.scheduledAt} · Est. %.1f kg · KES %.0f payout".format(
                    estimatedKg, estimatedPayout
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick  = onAccept,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = GreenSurface,
                        contentColor   = GreenDark
                    )
                ) {
                    Text("Accept", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick  = { /* skip — dismisses locally */ },
                    modifier = Modifier.weight(1f),
                    border   = androidx.compose.foundation.BorderStroke(
                        0.5.dp, BorderColor
                    )
                ) {
                    Text(
                        "Skip",
                        color = GrayMid,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectorHomeScreenPreview() {
    TakaSmartTheme {
        CollectorHomePreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorHomePreviewContent() {
    val dummyJobs = listOf(
        JobSummary("1", "14 Gitanga Rd, Lavington",   "Sat 26 Apr · 9–11am",  3, "requested", "hh1"),
        JobSummary("2", "Ring Rd Westlands",           "Sat 26 Apr · 11am–1pm", 2, "requested", "hh2"),
        JobSummary("3", "Argwings Kodhek Rd, Kilimani","Sat 26 Apr · 2–4pm",   5, "requested", "hh3")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Available pickups",
                            style = MaterialTheme.typography.titleMedium)
                        Text("${dummyJobs.size} jobs nearby",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text("Online",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {}) {
                        Text("Earnings", color = Teal,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(dummyJobs) { job ->
                JobCard(job = job, onAccept = {})
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}