package com.ramaphosa.takasmart.ui.screens.collector


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.CollectorViewModel
import com.ramaphosa.takasmart.data.PayoutRecord
import com.ramaphosa.takasmart.ui.screens.shared.StatCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(navController: NavController) {

    val vm: CollectorViewModel = viewModel()

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            // ── Stat cards ─────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "KES %.0f".format(monthlyEarnings),
                        label    = "Total earned",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "$completedCount",
                        label    = "Pickups done",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                StatCard(
                    value    = "%.1f kg".format(totalKg),
                    label    = "Total collected",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Payout history ─────────────────────────────────
            item {
                Text(
                    text  = "Payout history",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (payouts.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "No completed pickups yet. Accept a job to start earning.",
                            modifier = Modifier.padding(16.dp),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(payouts) { payout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text  = "Job #${payout.jobId.take(8).uppercase()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text  = "${payout.date} · %.1f kg".format(payout.verifiedKg),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text  = "+KES %.0f".format(payout.amountKes),
                            style = MaterialTheme.typography.titleSmall,
                            color = Green
                        )
                    }
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            item {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("KES 1,240", "Total earned",   Modifier.weight(1f))
                    StatCard("18",        "Pickups done",   Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                StatCard("47.0 kg", "Total collected", Modifier.fillMaxWidth())
            }

            item {
                Text("Payout history", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp))
            }

            items(dummyPayouts) { payout ->
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Job #${payout.jobId.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("${payout.date} · %.1f kg".format(payout.verifiedKg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("+KES %.0f".format(payout.amountKes),
                        style = MaterialTheme.typography.titleSmall,
                        color = Green)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}