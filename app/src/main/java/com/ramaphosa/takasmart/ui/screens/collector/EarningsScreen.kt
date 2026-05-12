package com.ramaphosa.takasmart.ui.screens.collector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramaphosa.takasmart.data.CollectorViewModel
import com.ramaphosa.takasmart.data.PayoutRecord
import com.ramaphosa.takasmart.ui.screens.shared.StatCard
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(navController: NavController) {

    val vm              : CollectorViewModel = viewModel()
    val monthlyEarnings by vm.monthlyEarnings.collectAsState()
    val completedCount  by vm.completedCount.collectAsState()
    val totalKg         by vm.totalKg.collectAsState()
    val payouts         by vm.payouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "My earnings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "All time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenSurface
                    ) {
                        Text(
                            text     = "Export",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = GreenDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.width(12.dp))
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

            // ── Earnings hero ──────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = TealDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text  = "Total earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = TealLight
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = "KES %.0f".format(monthlyEarnings),
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "$completedCount pickups · %.1f kg collected".format(totalKg),
                            style = MaterialTheme.typography.bodySmall,
                            color = TealMid
                        )
                    }
                }
            }

            // ── Stat cards ─────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "$completedCount",
                        label    = "Pickups done",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "%.1f kg".format(totalKg),
                        label    = "Total collected",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Payout history header ──────────────────────────
            item {
                Text(
                    text       = "PAYOUT HISTORY",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = GrayMid,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.padding(top = 4.dp)
                )
            }

            // ── Empty state ────────────────────────────────────
            if (payouts.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, BorderColor)
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text  = "No completed pickups yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = GrayDark
                            )
                            Text(
                                text  = "Accept a job to start earning",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayMid
                            )
                        }
                    }
                }
            } else {
                items(payouts) { payout ->
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, BorderColor)
                    ) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Icon circle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TealSurface,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🛵", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text       = "Job #${payout.jobId.take(8).uppercase()}",
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color      = GrayDark
                                    )
                                    Text(
                                        text  = "${payout.date} · %.1f kg".format(payout.verifiedKg),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMid
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text       = "+KES %.0f".format(payout.amountKes),
                                    style      = MaterialTheme.typography.titleSmall,
                                    color      = GreenDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GreenSurface
                                ) {
                                    Text(
                                        text     = "Paid",
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical   = 2.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
fun EarningsScreenPreview() {
    TakaSmartTheme {
        EarningsPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsPreviewContent() {
    val dummyPayouts = listOf(
        PayoutRecord("abc123", 62.0,  3.1, "18 Apr · 9–11am"),
        PayoutRecord("def456", 36.0,  1.8, "17 Apr · 2–4pm"),
        PayoutRecord("ghi789", 126.0, 4.2, "15 Apr · 11am–1pm")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My earnings", style = MaterialTheme.typography.titleMedium)
                        Text("All time", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = GraySurface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = TealDark,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Total earned", style = MaterialTheme.typography.bodySmall,
                            color = TealLight)
                        Spacer(Modifier.height(4.dp))
                        Text("KES 1,240", style = MaterialTheme.typography.headlineMedium,
                            color = White, fontWeight = FontWeight.Medium)
                        Text("18 pickups · 47.0 kg collected",
                            style = MaterialTheme.typography.bodySmall, color = TealMid)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("18",       "Pickups done",    Modifier.weight(1f))
                    StatCard("47.0 kg",  "Total collected", Modifier.weight(1f))
                }
            }
            item {
                Text("PAYOUT HISTORY", style = MaterialTheme.typography.labelSmall,
                    color = GrayMid, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp))
            }
            items(dummyPayouts) { payout ->
                Surface(shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(0.5.dp, BorderColor)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = TealSurface,
                                modifier = Modifier.size(38.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛵", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Job #${payout.jobId.take(8).uppercase()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium, color = GrayDark)
                                Text("${payout.date} · %.1f kg".format(payout.verifiedKg),
                                    style = MaterialTheme.typography.bodySmall, color = GrayMid)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+KES %.0f".format(payout.amountKes),
                                style = MaterialTheme.typography.titleSmall,
                                color = GreenDark, fontWeight = FontWeight.Medium)
                            Surface(shape = RoundedCornerShape(4.dp), color = GreenSurface) {
                                Text("Paid", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, color = GreenDark)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}