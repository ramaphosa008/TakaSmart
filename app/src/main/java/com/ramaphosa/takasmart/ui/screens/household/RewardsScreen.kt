package com.ramaphosa.takasmart.ui.screens.household

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
import com.ramaphosa.takasmart.data.HouseholdViewModel
import com.ramaphosa.takasmart.ui.screens.shared.StatCard
import com.ramaphosa.takasmart.ui.theme.*

data class RewardOption(
    val label    : String,
    val points   : Int,
    val type     : String,
    val emoji    : String,
    val bgColor  : androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(navController: NavController) {

    val vm         : HouseholdViewModel = viewModel()
    val points     by vm.points.collectAsState()
    val recycledKg by vm.recycledKg.collectAsState()

    var redeemingType  by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage   by remember { mutableStateOf("") }

    val rewardOptions = listOf(
        RewardOption("Safaricom airtime — KES 50", 50,  "safaricom", "🟢", GreenSurface,  GreenDark),
        RewardOption("Airtel airtime — KES 50",    50,  "airtel",    "🔴", ErrorSurface,  ErrorDark),
        RewardOption("Plant a tree in your name",  100, "tree",      "🌳", TealSurface,   TealDark)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Your impact",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Since you joined",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Points hero ────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = Green,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text  = "Points balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text       = "$points pts",
                                style      = MaterialTheme.typography.headlineMedium,
                                color      = White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "= KES $points redeemable value",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GreenSurface.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text     = "🌿 Active",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = White
                            )
                        }
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
                        value    = "%.1f kg".format(recycledKg),
                        label    = "E-waste diverted",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "KES ${points}",
                        label    = "Redeemable value",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Success message ────────────────────────────────
            if (successMessage.isNotEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = GreenSurface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, GreenBorder)
                    ) {
                        Row(
                            modifier          = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓  ", style = MaterialTheme.typography.titleSmall, color = Green)
                            Text(
                                text  = successMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenDark
                            )
                        }
                    }
                }
            }

            // ── Error message ──────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = ErrorSurface,
                        modifier = Modifier.fillMaxWidth(),
                        border   = BorderStroke(0.5.dp, ErrorRed.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text     = errorMessage,
                            modifier = Modifier.padding(12.dp),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = ErrorRed
                        )
                    }
                }
            }

            // ── Redeem section ─────────────────────────────────
            item {
                Text(
                    text       = "REDEEM POINTS",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = GrayMid,
                    fontWeight = FontWeight.Medium
                )
            }

            items(rewardOptions) { option ->
                val canAfford   = points >= option.points
                val isRedeeming = redeemingType == option.type

                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    border   = BorderStroke(
                        width = if (canAfford) 0.5.dp else 0.5.dp,
                        color = if (canAfford) BorderColor else BorderColor.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = option.label,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color      = if (canAfford) GrayDark
                                else GrayLight
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (canAfford) GreenSurface else GraySurface
                                ) {
                                    Text(
                                        text     = "${option.points} pts",
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical   = 2.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (canAfford) GreenDark else GrayLight
                                    )
                                }
                                if (!canAfford) {
                                    Text(
                                        text  = "Need ${option.points - points} more",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GrayLight
                                    )
                                }
                            }
                        }

                        if (isRedeeming) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = Teal,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick  = {
                                    if (!canAfford) {
                                        errorMessage   = "You need ${option.points} pts. You have $points."
                                        successMessage = ""
                                        return@Button
                                    }
                                    redeemingType  = option.type
                                    errorMessage   = ""
                                    successMessage = ""
                                    redeemingType  = ""
                                    successMessage = "Redemption submitted! Airtime arrives in seconds."
                                },
                                enabled  = canAfford,
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor         = Teal,
                                    contentColor           = White,
                                    disabledContainerColor = GraySurface,
                                    disabledContentColor   = GrayLight
                                ),
                                shape            = RoundedCornerShape(8.dp),
                                contentPadding   = PaddingValues(
                                    horizontal = 14.dp,
                                    vertical   = 8.dp
                                )
                            ) {
                                Text(
                                    text  = if (canAfford) "Redeem" else "Locked",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // ── Environmental impact card ──────────────────────
            item {
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = GreenSurface,
                    modifier = Modifier.fillMaxWidth(),
                    border   = BorderStroke(0.5.dp, GreenBorder)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text       = "Your environmental impact",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = GreenDark,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(10.dp))

                        listOf(
                            "%.1f kg of toxic e-waste kept from Dandora".format(recycledKg),
                            "Equivalent to approx. %.1f kg of recovered copper".format(recycledKg * 0.07),
                            "$points points earned through responsible recycling"
                        ).forEach { point ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape    = RoundedCornerShape(4.dp),
                                    color    = Green,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text  = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GreenDark
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
fun RewardsScreenPreview() {
    TakaSmartTheme {
        RewardsPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsPreviewContent() {
    val points     = 142
    val recycledKg = 4.2

    val rewardOptions = listOf(
        Triple("Safaricom airtime — KES 50", 50,  "safaricom"),
        Triple("Airtel airtime — KES 50",    50,  "airtel"),
        Triple("Plant a tree in your name",  100, "tree")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Your impact", style = MaterialTheme.typography.titleMedium)
                        Text("Since you joined", style = MaterialTheme.typography.bodySmall,
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
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = Green,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Points balance", style = MaterialTheme.typography.bodySmall,
                            color = GreenSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("$points pts", style = MaterialTheme.typography.headlineMedium,
                            color = White, fontWeight = FontWeight.Medium)
                        Text("= KES $points redeemable value",
                            style = MaterialTheme.typography.bodySmall, color = GreenSurface)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("%.1f kg".format(recycledKg), "E-waste diverted", Modifier.weight(1f))
                    StatCard("KES $points",                "Redeemable value", Modifier.weight(1f))
                }
            }
            item {
                Text("REDEEM POINTS", style = MaterialTheme.typography.labelSmall,
                    color = GrayMid, fontWeight = FontWeight.Medium)
            }
            items(rewardOptions) { (label, pts, _) ->
                val canAfford = points >= pts
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(0.5.dp, BorderColor)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (canAfford) GrayDark else GrayLight)
                            Text("$pts pts", style = MaterialTheme.typography.bodySmall,
                                color = if (canAfford) GreenDark else GrayLight)
                        }
                        Button(onClick = {}, enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Teal, contentColor = White,
                                disabledContainerColor = GraySurface,
                                disabledContentColor = GrayLight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(if (canAfford) "Redeem" else "Locked",
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}