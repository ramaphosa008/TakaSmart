package com.ramaphosa.takasmart.ui.screens.facility

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
import com.ramaphosa.takasmart.navigation.ROUT_VERIFY_DELIVERY
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.FacilityViewModel
import com.ramaphosa.takasmart.data.IncomingLoad
import com.ramaphosa.takasmart.navigation.ROUT_ACCOUNT
import com.ramaphosa.takasmart.ui.screens.shared.SectionLabel
import com.ramaphosa.takasmart.ui.screens.shared.StatCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityHomeScreen(navController: NavController) {

    val vm: FacilityViewModel = viewModel()
    val incomingLoads  by vm.incomingLoads.collectAsState()
    val completedToday by vm.completedToday.collectAsState()

    val waiting   = incomingLoads.filter { it.status == "at_facility" }
    val completed = incomingLoads.filter { it.status == "completed" }

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
                            color = PurpleSurface,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "F",
                                    color = Purple,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text  = "EcoAct Recyclers",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text  = "Incoming loads today",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text(
                            text     = "${incomingLoads.size} expected",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color    = TealDark,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(12.dp))
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
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "${waiting.size}",
                        label    = "Awaiting weigh-in",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "$completedToday",
                        label    = "Completed today",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Loads awaiting weigh-in ────────────────────────
            if (waiting.isNotEmpty()) {
                item { SectionLabel("Arrived — awaiting weigh-in") }
                items(waiting) { load ->
                    LoadCard(
                        load     = load,
                        onVerify = {
                            navController.navigate(
                                ROUT_VERIFY_DELIVERY.replace("{jobId}", load.id)
                            )
                        }
                    )
                }
            }

            // ── Completed loads ────────────────────────────────
            if (completed.isNotEmpty()) {
                item { SectionLabel("Completed today") }
                items(completed) { load ->
                    LoadCard(load = load, onVerify = null)
                }
            }

            // ── Empty state ────────────────────────────────────
            if (incomingLoads.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "No incoming loads today.",
                            modifier = Modifier.padding(24.dp),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun LoadCard(load: IncomingLoad, onVerify: (() -> Unit)?) {
    val isComplete = load.status == "completed"

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
                    text  = "Job #${load.id.take(8).uppercase()}",
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = CircleShape,
                    color = if (isComplete) GreenSurface else AmberSurface
                ) {
                    Text(
                        text     = if (isComplete) "Completed" else "Arrived",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color    = if (isComplete) GreenDark else AmberDark,
                        style    = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "${load.itemCount} items · Collector logged: %.2f kg".format(
                    load.collectorLoggedKg
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = load.scheduledAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onVerify != null) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick  = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Weigh & confirm this load", color = White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FacilityHomeScreenPreview() {
    TakaSmartTheme {
        FacilityHomePreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityHomePreviewContent(
) {
    val dummyLoads = listOf(
        IncomingLoad("1", "col1", "14 Gitanga Rd", "Sat 26 Apr · 9–11am",  3, "at_facility", 1.8, "hh1"),
        IncomingLoad("2", "col2", "Ring Rd West",  "Sat 26 Apr · 11am–1pm",2, "completed",   3.2, "hh2")
    )
    val waiting   = dummyLoads.filter { it.status == "at_facility" }
    val completed = dummyLoads.filter { it.status == "completed" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("EcoAct Recyclers",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Incoming loads today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text("${dummyLoads.size} expected",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(12.dp))
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
                    StatCard("${waiting.size}",   "Awaiting weigh-in",  Modifier.weight(1f))
                    StatCard("${completed.size}", "Completed today",     Modifier.weight(1f))
                }
            }

            if (waiting.isNotEmpty()) {
                item { SectionLabel("Arrived — awaiting weigh-in") }
                items(waiting) { load ->
                    LoadCard(load = load, onVerify = {})
                }
            }

            if (completed.isNotEmpty()) {
                item { SectionLabel("Completed today") }
                items(completed) { load ->
                    LoadCard(load = load, onVerify = null)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}