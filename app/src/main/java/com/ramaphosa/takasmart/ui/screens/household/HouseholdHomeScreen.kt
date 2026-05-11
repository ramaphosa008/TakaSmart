package com.ramaphosa.takasmart.ui.screens.household

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.navigation.ROUT_LOG_ITEM
import com.ramaphosa.takasmart.navigation.ROUT_SCHEDULE_PICKUP
import com.ramaphosa.takasmart.navigation.ROUT_REWARDS
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.HouseholdViewModel
import com.ramaphosa.takasmart.data.PickupSummary
import com.ramaphosa.takasmart.ui.screens.shared.SectionLabel
import com.ramaphosa.takasmart.ui.screens.shared.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdHomeScreen(navController: NavController) {

    val vm: HouseholdViewModel = viewModel()

    val points        by vm.points.collectAsState()
    val recycledKg    by vm.recycledKg.collectAsState()
    val wasteItems         by vm.items.collectAsState()
    val upcomingPickup by vm.upcomingPickup.collectAsState()
    val pickupsDone   by vm.pickupsDone.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Taka Smart",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Nairobi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Points badge top right
                    Surface(
                        shape = CircleShape,
                        color = GreenSurface
                    ) {
                        Text(
                            text     = "$points pts",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color    = GreenDark,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = { navController.navigate(ROUT_LOG_ITEM) },
                containerColor   = Teal,
                contentColor     = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log item")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Stat cards ─────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "%.1f kg".format(recycledKg),
                        label    = "E-waste recycled",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "$pickupsDone",
                        label    = "Pickups done",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Upcoming pickup ────────────────────────────────
            upcomingPickup?.let { pickup ->
                item {
                    SectionLabel("Upcoming pickup")
                    UpcomingPickupCard(
                        pickup       = pickup
                    )
                }
            }

            // ── Items list header ──────────────────────────────
            item {
                Row(
                    modifier       = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionLabel("My logged items")
                    if (wasteItems.isNotEmpty()) {
                        TextButton(
                            onClick = { navController.navigate(ROUT_SCHEDULE_PICKUP) }
                        ) {
                            Text(
                                text  = "Schedule pickup",
                                color = Teal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ── Empty state ────────────────────────────────────
            if (wasteItems.isEmpty()) {
                item {
                    EmptyItemsCard(
                        onLogItem = { navController.navigate(ROUT_LOG_ITEM) }
                    )
                }
            }

            // ── Item rows ──────────────────────────────────────
            items(wasteItems) { item ->
                EwasteItemRow(item = item)
            }

            // ── Rewards shortcut ───────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick  = { navController.navigate(ROUT_REWARDS) },
                    border   = androidx.compose.foundation.BorderStroke(
                        width = 0.5.dp,
                        color = GreenBorder
                    ),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text  = "View rewards & impact",
                        color = Green,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(Modifier.height(80.dp)) // space for FAB
            }
        }
    }
}



// ── Upcoming pickup card ───────────────────────────────────────────────────
@Composable
fun UpcomingPickupCard(pickup: PickupSummary) {
    val (badgeColor, badgeText, badgeBg) = when (pickup.status) {
        "confirmed" -> Triple(TealDark,  "Confirmed", TealSurface)
        "en_route"  -> Triple(GreenDark, "En route",  GreenSurface)
        else        -> Triple(AmberDark, "Pending",   AmberSurface)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
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
                    text  = "${pickup.itemCount} item${if (pickup.itemCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = pickup.scheduledAt.ifEmpty { "Date not set" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(shape = CircleShape, color = badgeBg) {
                Text(
                    text     = badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    color    = badgeColor,
                    style    = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ── Single item row ────────────────────────────────────────────────────────
@Composable
fun EwasteItemRow(item: com.ramaphosa.takasmart.data.EwasteItem) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = item.category,
                style = MaterialTheme.typography.bodyMedium
            )
            if (item.model.isNotEmpty()) {
                Text(
                    text  = item.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = CircleShape, color = AmberSurface) {
            Text(
                text     = "Pending",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                color    = AmberDark,
                style    = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ── Empty state card ───────────────────────────────────────────────────────
@Composable
fun EmptyItemsCard(onLogItem: () -> Unit) {
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
                text  = "No items logged yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Tap the + button to add your first e-waste item",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onLogItem,
                colors  = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("Log first item", color = White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HouseholdHomeScreenPreview() {
    TakaSmartTheme {
        HouseholdHomeScreen(rememberNavController())
    }
}

