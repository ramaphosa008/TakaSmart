package com.ramaphosa.takasmart.ui.screens.collector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramaphosa.takasmart.data.CollectorViewModel
import com.ramaphosa.takasmart.data.JobSummary
import com.ramaphosa.takasmart.navigation.ROUT_ACCOUNT
import com.ramaphosa.takasmart.navigation.ROUT_ACTIVE_PICKUP
import com.ramaphosa.takasmart.navigation.ROUT_EARNINGS
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorHomeScreen(navController: NavController) {

    val vm            : CollectorViewModel = viewModel()
    val availableJobs by vm.availableJobs.collectAsState()
    val activeJob     by vm.activeJob.collectAsState()
    val monthlyEarnings by vm.monthlyEarnings.collectAsState()
    val completedCount  by vm.completedCount.collectAsState()
    val totalKg         by vm.totalKg.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            onClick = {
                                navController.navigate(ROUT_ACCOUNT)
                            },
                            shape = CircleShape,
                            color = AmberSurface,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "C",
                                    color = Amber,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp)) //

                        Column {
                            Text(
                                text = "TakaSmart",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TealSurface
                    ) {
                        Row(
                            modifier          = Modifier.padding(
                                horizontal = 10.dp,
                                vertical   = 5.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Teal)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text  = "Online",
                                color = TealDark,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    TextButton(onClick = { navController.navigate(ROUT_EARNINGS) }) {
                        Text(
                            text  = "Earnings",
                            color = Teal,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = GraySurface
    ) { padding ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Earnings hero card ─────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = TealDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text  = "Total earned this month",
                                style = MaterialTheme.typography.bodySmall,
                                color = TealLight
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text       = "KES %.0f".format(monthlyEarnings),
                                style      = MaterialTheme.typography.titleLarge,
                                color      = White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "$completedCount pickups · %.1f kg collected".format(totalKg),
                                style = MaterialTheme.typography.bodySmall,
                                color = TealMid
                            )
                        }

                    }
                }
            }

            // ── Active job banner ──────────────────────────────
            activeJob?.let { job ->
                item {
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = TealSurface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, TealMid)
                    ) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Teal)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text       = "Active job in progress",
                                        style      = MaterialTheme.typography.titleSmall,
                                        color      = TealDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text  = job.address.ifEmpty { "No address" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Teal
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    navController.navigate(
                                        ROUT_ACTIVE_PICKUP.replace("{jobId}", job.id)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Teal,
                                    contentColor   = White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 14.dp,
                                    vertical   = 8.dp
                                )
                            ) {
                                Text(
                                    text  = "Resume",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // ── Section label ──────────────────────────────────
            item {
                Text(
                    text     = "AVAILABLE PICKUPS",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = GrayMid,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Empty state ────────────────────────────────────
            if (availableJobs.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, BorderColor)
                    ) {
                        Column(
                            modifier            = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text       = "No jobs available right now",
                                style      = MaterialTheme.typography.titleSmall,
                                color      = GrayDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text  = "New pickup requests will appear here",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayMid
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

@Composable
fun JobCard(job: JobSummary, onAccept: () -> Unit) {
    val estimatedKg     = job.itemCount * 0.5
    val estimatedPayout = estimatedKg * 20

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        border   = BorderStroke(0.5.dp, BorderColor)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "${job.itemCount} item${if (job.itemCount != 1) "s" else ""}",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color      = GrayDark
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AmberSurface
                ) {
                    Text(
                        text     = "Nearby",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical   = 4.dp
                        ),
                        color = AmberDark,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text  = job.address.ifEmpty { "Address not set" },
                style = MaterialTheme.typography.bodySmall,
                color = GrayMid
            )
            Text(
                text  = job.scheduledAt,
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight
            )

            Spacer(Modifier.height(10.dp))

            // Chips row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GraySurface
                ) {
                    Text(
                        text     = "Est. %.1f kg".format(estimatedKg),
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical   = 4.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayMid
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenSurface
                ) {
                    Text(
                        text     = "KES %.0f payout".format(estimatedPayout),
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical   = 4.dp
                        ),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = GreenDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick  = onAccept,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Teal,
                        contentColor   = White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text  = "Accept job",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OutlinedButton(
                    onClick  = {},
                    modifier = Modifier.weight(1f),
                    border   = BorderStroke(0.5.dp, BorderColor),
                    shape    = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text  = "Skip",
                        color = GrayMid,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
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
        JobSummary("1", "14 Gitanga Rd, Lavington",    "Sat 26 Apr · 9–11am",   3, "requested", "hh1"),
        JobSummary("2", "Ring Rd Westlands",            "Sat 26 Apr · 11am–1pm", 2, "requested", "hh2"),
        JobSummary("3", "Argwings Kodhek Rd, Kilimani", "Sat 26 Apr · 2–4pm",   5, "requested", "hh3")
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Available pickups", style = MaterialTheme.typography.titleMedium)
                        Text("${dummyJobs.size} jobs nearby",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Surface(shape = RoundedCornerShape(20.dp), color = TealSurface) {
                        Text("Online",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {}) {
                        Text("Earnings", color = Teal, style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        containerColor = GraySurface
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(dummyJobs) { job -> JobCard(job = job, onAccept = {}) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}