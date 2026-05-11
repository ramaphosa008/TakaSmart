package com.ramaphosa.takasmart.ui.screens.collector


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_WEIGH_ITEMS
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePickupScreen(navController: NavController, jobId: String) {

    val db = FirebaseFirestore.getInstance()

    var address      by remember { mutableStateOf("") }
    var itemIds      by remember { mutableStateOf<List<String>>(emptyList()) }
    var otpFromDb    by remember { mutableStateOf("") }
    val checkedItems = remember { mutableStateListOf<String>() }
    val otpDigits    = remember { mutableStateListOf("", "", "", "") }
    val focusRequesters = remember { List(4) { FocusRequester() } }

    var otpError     by remember { mutableStateOf("") }
    var isVerifying  by remember { mutableStateOf(false) }

    // Load pickup details
    LaunchedEffect(jobId) {
        db.collection("pickups").document(jobId)
            .addSnapshotListener { snap, _ ->
                address   = snap?.getString("address") ?: ""
                itemIds   = (snap?.get("item_ids") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                otpFromDb = snap?.getString("otp") ?: ""
            }

        // Update status to en_route
        db.collection("pickups").document(jobId)
            .update("status", "en_route")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Active pickup",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Job #${jobId.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text(
                            text     = "En route",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color    = TealDark,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Map placeholder ────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = TealSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text  = "📍 ${address.ifEmpty { "Loading address..." }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealDark
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── OTP entry ──────────────────────────────────────
            Text(
                text  = "Ask the household for their OTP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value         = digit,
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                otpDigits[index] = input
                                otpError = ""
                                if (input.isNotEmpty() && index < 3)
                                    focusRequesters[index + 1].requestFocus()
                            }
                            if (input.isEmpty() && index > 0) {
                                otpDigits[index] = ""
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .width(60.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center,
                            fontSize  = 22.sp
                        ),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal,
                            unfocusedBorderColor = if (digit.isNotEmpty()) TealMid else BorderColor
                        )
                    )
                }
            }

            if (otpError.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = otpError,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Item checklist ─────────────────────────────────
            Text(
                text  = "Items to collect",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            itemIds.forEach { itemId ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = itemId in checkedItems,
                        onCheckedChange = { checked ->
                            if (checked) checkedItems.add(itemId)
                            else checkedItems.remove(itemId)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Teal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Item ${itemIds.indexOf(itemId) + 1}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Proceed button ─────────────────────────────────
            val enteredOtp  = otpDigits.joinToString("")
            val allChecked  = checkedItems.size == itemIds.size && itemIds.isNotEmpty()
            val otpComplete = enteredOtp.length == 4

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = otpComplete && allChecked && !isVerifying,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor   = White
                ),
                onClick  = {
                    isVerifying = true

                    // Verify OTP against what's stored in Firestore
                    if (otpFromDb.isNotEmpty() && enteredOtp != otpFromDb) {
                        otpError    = "Wrong code. Ask the household to check their app."
                        isVerifying = false
                        return@Button
                    }

                    // Mark as at_household and proceed to weighing
                    db.collection("pickups").document(jobId)
                        .update("status", "at_household")
                        .addOnSuccessListener {
                            isVerifying = false
                            navController.navigate(
                                ROUT_WEIGH_ITEMS.replace("{jobId}", jobId)
                            )
                        }
                }
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Proceed to weigh items",
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
fun ActivePickupScreenPreview() {
    TakaSmartTheme {
        ActivePickupPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePickupPreviewContent() {
    val otpDigits       = remember { mutableStateListOf("3", "7", "", "") }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val checkedItems    = remember { mutableStateListOf("item1") }
    val dummyItemIds    = listOf("item1", "item2", "item3")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Active pickup",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Job #PKP001",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = TealSurface) {
                        Text("En route",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Map placeholder
            Surface(shape = RoundedCornerShape(10.dp), color = TealSurface,
                modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📍 14 Gitanga Rd, Lavington",
                        style = MaterialTheme.typography.bodySmall, color = TealDark)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Ask the household for their OTP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                otpDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value         = digit,
                        onValueChange = {},
                        modifier      = Modifier.width(60.dp),
                        textStyle     = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center, fontSize = 22.sp),
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal,
                            unfocusedBorderColor = if (digit.isNotEmpty()) TealMid
                            else BorderColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Items to collect", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))

            dummyItemIds.forEachIndexed { index, itemId ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked         = itemId in checkedItems,
                        onCheckedChange = { checked ->
                            if (checked) checkedItems.add(itemId)
                            else checkedItems.remove(itemId)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Teal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Item ${index + 1}",
                        style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp)
            }

            Spacer(Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal, contentColor = White),
                onClick  = {}
            ) {
                Text("Proceed to weigh items",
                    style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}