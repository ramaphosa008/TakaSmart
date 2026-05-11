package com.ramaphosa.takasmart.ui.screens.household


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.ramaphosa.takasmart.data.HouseholdViewModel
import com.ramaphosa.takasmart.ui.screens.shared.SectionLabel
import com.ramaphosa.takasmart.ui.screens.shared.StatCard
import com.ramaphosa.takasmart.ui.theme.*



data class RewardOption(
    val label  : String,
    val points : Int,
    val type   : String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(navController: NavController) {

    val vm: HouseholdViewModel = viewModel()
    val points     by vm.points.collectAsState()
    val recycledKg by vm.recycledKg.collectAsState()

    var redeemingType    by remember { mutableStateOf("") }
    var successMessage   by remember { mutableStateOf("") }
    var errorMessage     by remember { mutableStateOf("") }

    val rewardOptions = listOf(
        RewardOption("Safaricom airtime — KES 50", 50,  "safaricom"),
        RewardOption("Airtel airtime — KES 50",    50,  "airtel"),
        RewardOption("Plant a tree in your name",  100, "tree")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Stat cards ─────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = "$points",
                        label    = "Points balance",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = "%.1f kg".format(recycledKg),
                        label    = "E-waste diverted",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Success / error messages ───────────────────────
            if (successMessage.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = successMessage,
                            modifier = Modifier.padding(12.dp),
                            color    = GreenDark,
                            style    = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = errorMessage,
                            modifier = Modifier.padding(12.dp),
                            color    = ErrorRed,
                            style    = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── Redeem section ─────────────────────────────────
            item {
                SectionLabel("Redeem points")
            }

            items(rewardOptions) { option ->
                val canAfford   = points >= option.points
                val isRedeeming = redeemingType == option.type

                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = option.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text  = "${option.points} points",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isRedeeming) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = Teal,
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick  = {
                                    if (!canAfford) {
                                        errorMessage   = "You need ${option.points} points. You have $points."
                                        successMessage = ""
                                        return@TextButton
                                    }
                                    // Wire Africa's Talking airtime API here later
                                    redeemingType  = option.type
                                    errorMessage   = ""
                                    successMessage = ""

                                    // Simulate success for now
                                    redeemingType  = ""
                                    successMessage = "Redemption submitted! Airtime arrives in seconds."
                                },
                                enabled  = canAfford
                            ) {
                                Text(
                                    text  = if (canAfford) "Redeem" else "Need ${option.points}",
                                    color = if (canAfford) Teal else GrayLight,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("$points",  "Points balance",  Modifier.weight(1f))
                    StatCard("%.1f kg".format(recycledKg), "E-waste diverted", Modifier.weight(1f))
                }
            }

            item {
                SectionLabel("Redeem points")
            }

            items(rewardOptions) { (label, pts, _) ->
                val canAfford = points >= pts
                Surface(shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            Text("$pts points", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {}, enabled = canAfford) {
                            Text(
                                text  = if (canAfford) "Redeem" else "Need $pts",
                                color = if (canAfford) Teal else GrayLight,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}