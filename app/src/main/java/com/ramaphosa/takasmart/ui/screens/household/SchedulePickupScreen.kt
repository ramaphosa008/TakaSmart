package com.ramaphosa.takasmart.ui.screens.household

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.HouseholdViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.ramaphosa.takasmart.ui.screens.shared.DummyItem
import com.ramaphosa.takasmart.ui.screens.shared.DummyPickup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePickupScreen(navController: NavController) {

    val db  = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val vm    : HouseholdViewModel = viewModel()
    val wasteItems by vm.items.collectAsState()

    var address      by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Track which item IDs the user has checked
    val selectedItemIds = remember { mutableStateListOf<String>() }

    // Time slot dropdown
    val timeSlots = listOf(
        "9:00am – 11:00am",
        "11:00am – 1:00pm",
        "2:00pm – 4:00pm"
    )
    var selectedSlot    by remember { mutableStateOf(timeSlots[0]) }
    var slotExpanded    by remember { mutableStateOf(false) }

    // Date picker
    val datePickerState = rememberDatePickerState()
    var showDatePicker  by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = java.text.SimpleDateFormat(
                            "EEE d MMM yyyy", java.util.Locale.getDefault()
                        )
                        selectedDate = sdf.format(java.util.Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Schedule pickup",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "${wasteItems.size} item${if (wasteItems.size != 1) "s" else ""} ready",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Address field ──────────────────────────────────
            Text(
                text  = "Pickup address",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value         = address,
                onValueChange = { address = it },
                placeholder   = { Text("e.g. 14 Gitanga Rd, Lavington") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Date picker ────────────────────────────────────
            Text(
                text  = "Preferred date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape   = RoundedCornerShape(8.dp),
                border  = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor)
            ) {
                Text(
                    text  = selectedDate.ifEmpty { "Tap to select a date" },
                    color = if (selectedDate.isEmpty()) GrayLight else GrayDark,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Time slot dropdown ─────────────────────────────
            Text(
                text  = "Time window",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded         = slotExpanded,
                onExpandedChange = { slotExpanded = !slotExpanded }
            ) {
                OutlinedTextField(
                    value         = selectedSlot,
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded)
                    },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                ExposedDropdownMenu(
                    expanded         = slotExpanded,
                    onDismissRequest = { slotExpanded = false }
                ) {
                    timeSlots.forEach { slot ->
                        DropdownMenuItem(
                            text    = { Text(slot) },
                            onClick = {
                                selectedSlot = slot
                                slotExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Items checklist ────────────────────────────────
            Text(
                text  = "Items to collect",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            if (wasteItems.isEmpty()) {
                Text(
                    text  = "No pending items. Go back and log some items first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
            } else {
                wasteItems.forEach { item ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = item.id in selectedItemIds,
                            onCheckedChange = { checked ->
                                if (checked) selectedItemIds.add(item.id)
                                else selectedItemIds.remove(item.id)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Teal
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
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
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Reward estimate ────────────────────────────────
            if (selectedItemIds.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "Estimated reward: ${selectedItemIds.size * 10}–${selectedItemIds.size * 20} pts after verified delivery",
                        modifier = Modifier.padding(10.dp),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = GreenDark
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Error ──────────────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Text(
                    text  = errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Confirm button ─────────────────────────────────
            val canSubmit = address.isNotEmpty() &&
                    selectedDate.isNotEmpty() &&
                    selectedItemIds.isNotEmpty() &&
                    !isSaving

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = canSubmit,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor   = White
                ),
                onClick  = {
                    isSaving     = true
                    errorMessage = ""

                    db.collection("pickups").add(
                        mapOf(
                            "household_id"  to uid,
                            "address"       to address,
                            "scheduled_at"  to "$selectedDate · $selectedSlot",
                            "item_ids"      to selectedItemIds.toList(),
                            "status"        to "requested",
                            "created_at"    to FieldValue.serverTimestamp()
                        )
                    )
                        .addOnSuccessListener {
                            isSaving = false
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            isSaving     = false
                            errorMessage = e.message ?: "Failed to schedule. Try again."
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
                    Text("Confirm pickup request", style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulePickupScreenPreview() {
    TakaSmartTheme {
        SchedulePickupPreviewContent()
    }
}

// Standalone preview content — no ViewModel, no Firebase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePickupPreviewContent() {
    var address      by remember { mutableStateOf("14 Gitanga Rd, Lavington") }
    var selectedDate by remember { mutableStateOf("Sat 26 Apr 2025") }
    var selectedSlot by remember { mutableStateOf("9:00am – 11:00am") }
    var slotExpanded by remember { mutableStateOf(false) }

    val dummyItems = listOf(
        DummyItem("1", "Phone / tablet",    "Samsung Galaxy S9", "pending", ""),
        DummyItem("2", "Cables / chargers", "Dell charger",      "pending", ""),
        DummyItem("3", "Batteries",         "HP battery",        "pending", "")
    )
    val selectedItemIds = remember { mutableStateListOf("1", "2") }
    val timeSlots = listOf("9:00am – 11:00am", "11:00am – 1:00pm", "2:00pm – 4:00pm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Schedule pickup",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "${dummyItems.size} items ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            Text("Pickup address", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value         = address,
                onValueChange = { address = it },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor
                )
            )

            Spacer(Modifier.height(12.dp))

            Text("Preferred date", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick  = {},
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(8.dp),
                border   = BorderStroke(0.5.dp, BorderColor)
            ) {
                Text(selectedDate, color = GrayDark,
                    style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            Text("Time window", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded         = slotExpanded,
                onExpandedChange = { slotExpanded = !slotExpanded }
            ) {
                OutlinedTextField(
                    value         = selectedSlot,
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                ExposedDropdownMenu(
                    expanded         = slotExpanded,
                    onDismissRequest = { slotExpanded = false }
                ) {
                    timeSlots.forEach { slot ->
                        DropdownMenuItem(
                            text    = { Text(slot) },
                            onClick = { selectedSlot = slot; slotExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Items to collect", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))

            dummyItems.forEach { item ->
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = item.id in selectedItemIds,
                        onCheckedChange = { checked ->
                            if (checked) selectedItemIds.add(item.id)
                            else selectedItemIds.remove(item.id)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Teal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(item.category, style = MaterialTheme.typography.bodyMedium)
                        Text(item.model, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(shape = RoundedCornerShape(8.dp), color = GreenSurface,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    text     = "Estimated reward: ${selectedItemIds.size * 10}–${selectedItemIds.size * 20} pts after verified delivery",
                    modifier = Modifier.padding(10.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = GreenDark
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal, contentColor = White),
                onClick  = {}
            ) {
                Text("Confirm pickup request",
                    style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}