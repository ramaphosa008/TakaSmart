package com.ramaphosa.takasmart.ui.screens.collector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_COLLECTOR_HOME
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighItemsScreen(navController: NavController, jobId: String) {

    val db = FirebaseFirestore.getInstance()

    var itemIds      by remember { mutableStateOf<List<String>>(emptyList()) }
    val weights      = remember { mutableStateMapOf<String, String>() }
    var isSaving     by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Facilities selection
    var facilities by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    var selectedFacilityId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Load item IDs and Facilities
    LaunchedEffect(jobId) {
        db.collection("pickups").document(jobId)
            .get()
            .addOnSuccessListener { snap ->
                itemIds = (snap.get("item_ids") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
            }
            
        // Fetch from the 'facilities' collection to get the Entity IDs (e.g. FAC002)
        db.collection("facilities")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snaps ->
                facilities = snaps.documents.map { 
                    it.id to (it.getString("name") ?: "Facility ${it.id}")
                }
                if (facilities.isNotEmpty()) {
                    selectedFacilityId = facilities.first().first
                }
            }
    }

    // Calculate totals in real time as user types
    val totalKg     = weights.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val payoutEstimate = totalKg * 20

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Log weights",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Weigh each item, enter kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Warning card ───────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(8.dp),
                color    = AmberSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "Use your portable scale. The facility will re-weigh on arrival — a ±15% difference is normal.",
                    modifier = Modifier.padding(10.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = AmberDark
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Weight fields — one per item ───────────────────
            itemIds.forEachIndexed { index, itemId ->
                Text(
                    text  = "Item ${index + 1} — weight (kg)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value         = weights[itemId] ?: "",
                    onValueChange = { input ->
                        // Only accept valid decimal numbers
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weights[itemId] = input
                        }
                    },
                    placeholder   = { Text("0.00") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Running totals card ────────────────────────────
            Surface(
                shape    = RoundedCornerShape(8.dp),
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
                            text  = "Total logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "%.2f kg".format(totalKg),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Estimated payout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "KES %.0f".format(payoutEstimate),
                            style = MaterialTheme.typography.titleSmall,
                            color = Green
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Facility Selection ─────────────────────────────
            Text(
                text  = "SELECT DESTINATION FACILITY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = facilities.find { it.first == selectedFacilityId }?.second ?: "Select Facility",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    facilities.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedFacilityId = id
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text  = errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Confirm button ─────────────────────────────────
            val allEntered = itemIds.isNotEmpty() &&
                    itemIds.all { (weights[it]?.toDoubleOrNull() ?: 0.0) > 0 } &&
                    selectedFacilityId.isNotEmpty()

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = allEntered && !isSaving,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor   = White
                ),
                onClick  = {
                    isSaving     = true
                    errorMessage = ""

                    db.collection("pickups").document(jobId)
                        .update(
                            mapOf(
                                "collector_logged_kg" to totalKg,
                                "facility_id"         to selectedFacilityId,
                                "status"              to "at_facility"
                            )
                        )
                        .addOnSuccessListener {
                            isSaving = false
                            // Return to job board — collector drives to facility
                            navController.navigate(ROUT_COLLECTOR_HOME) {
                                popUpTo(ROUT_COLLECTOR_HOME) { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            isSaving     = false
                            errorMessage = e.message ?: "Failed to save. Try again."
                        }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Confirm & proceed to facility",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeighItemsScreenPreview() {
    TakaSmartTheme {
        WeighItemsPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighItemsPreviewContent() {
    val dummyItemIds = listOf("Phone / tablet", "Cables / chargers", "Batteries")
    val weights      = remember { mutableStateMapOf(
        "Phone / tablet"    to "0.18",
        "Cables / chargers" to "0.12",
        "Batteries"         to "0.31"
    )}
    val totalKg        = weights.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val payoutEstimate = totalKg * 20

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Log weights", style = MaterialTheme.typography.titleMedium)
                        Text("Weigh each item, enter kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            Surface(shape = RoundedCornerShape(8.dp), color = AmberSurface,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Use your portable scale. The facility will re-weigh on arrival — ±15% is normal.",
                    modifier = Modifier.padding(10.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = AmberDark
                )
            }

            Spacer(Modifier.height(14.dp))

            dummyItemIds.forEachIndexed { z_, label ->
                Text("$label — weight (kg)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value         = weights[label] ?: "",
                    onValueChange = { weights[label] = it },
                    placeholder   = { Text("0.00") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(Modifier.height(10.dp))
            }

            Surface(shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically) {
                        Text("Total logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.2f kg".format(totalKg),
                            style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically) {
                        Text("Estimated payout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("KES %.0f".format(payoutEstimate),
                            style = MaterialTheme.typography.titleSmall,
                            color = Green)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal, contentColor = White),
                onClick  = {}
            ) {
                Text("Confirm & proceed to facility",
                    style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}