package com.ramaphosa.takasmart.ui.screens.household

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.navigation.ROUT_LOG_ITEM
import com.ramaphosa.takasmart.navigation.ROUT_SCHEDULE_PICKUP
import com.ramaphosa.takasmart.navigation.ROUT_REWARDS
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.HouseholdViewModel
import com.ramaphosa.takasmart.data.PickupSummary
import com.ramaphosa.takasmart.navigation.ROUT_TRACK_PICKUP

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
                            text = "TakaSmart",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Spacer(Modifier.width(4.dp))

                        }
                    }
                },
                actions = {
                    // Notification & Points
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TealSurface,
                            modifier = Modifier.clickable { navController.navigate(ROUT_REWARDS) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Teal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "$points",
                                    color = TealDark,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(ROUT_LOG_ITEM) },
                containerColor = Teal,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log E-Waste", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Hero Section (Impact Summary) ──────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                ImpactDashboardCard(recycledKg, pickupsDone)
            }

            // ── Upcoming pickup ────────────────────────────────
            upcomingPickup?.let { pickup ->
                item {
                    SectionHeader(
                        title = "Upcoming Pickup",
                        actionText = "Track",
                        onAction = { navController.navigate(ROUT_TRACK_PICKUP)}
                    )
                    UpcomingPickupCard(pickup = pickup)
                }
            }

            // ── Items list header ──────────────────────────────
            item {
                SectionHeader(
                    title = "My Logged Items",
                    actionText = if (wasteItems.isNotEmpty()) "Schedule Pickup" else null,
                    onAction = { navController.navigate(ROUT_SCHEDULE_PICKUP) }
                )
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

            // ── Footer Spacer ───────────────────────────────
            item {
                Spacer(Modifier.height(100.dp)) // space for FAB
            }
        }
    }
}

@Composable
fun ImpactDashboardCard(recycledKg: Double, pickupsDone: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImpactStatItem(
                value = "%.1f".format(recycledKg),
                unit = "kg",
                label = "Recycled",
                icon = Icons.Default.Recycling,
                color = Teal
            )
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            ImpactStatItem(
                value = "$pickupsDone",
                unit = "times",
                label = "Pickups",
                icon = Icons.Default.LocalShipping,
                color = AmberDark
            )
        }
    }
}

@Composable
fun ImpactStatItem(value: String, unit: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        if (actionText != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = Teal
                )
            }
        }
    }
}


// ── Upcoming pickup card ───────────────────────────────────────────────────
@Composable
fun UpcomingPickupCard(pickup: PickupSummary) {
    val (badgeColor, badgeText, badgeBg) = when (pickup.status) {
        "confirmed" -> Triple(TealDark, "Confirmed", TealSurface)
        "en_route" -> Triple(GreenDark, "En route", GreenSurface)
        else -> Triple(AmberDark, "Pending", AmberSurface)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Teal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${pickup.itemCount} Items Scheduled",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = pickup.scheduledAt.ifEmpty { "Date not set" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = badgeBg
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = badgeColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ── Single item row ────────────────────────────────────────────────────────
@Composable
fun EwasteItemRow(item: com.ramaphosa.takasmart.data.EwasteItem) {
    val icon = when (item.category.lowercase()) {
        "phone", "mobile" -> Icons.Default.PhoneIphone
        "laptop", "computer" -> Icons.Default.Laptop
        "battery" -> Icons.Default.BatteryChargingFull
        "cable" -> Icons.Default.Usb
        else -> Icons.Default.DevicesOther
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                if (item.model.isNotEmpty()) {
                    Text(
                        text = item.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = AmberSurface.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "Pending",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    color = AmberDark,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ── Empty state card ───────────────────────────────────────────────────────
@Composable
fun EmptyItemsCard(onLogItem: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Items Logged",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Start your recycling journey by logging your first e-waste item.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLogItem,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log First Item", fontWeight = FontWeight.Bold)
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
