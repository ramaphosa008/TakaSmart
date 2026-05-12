package com.ramaphosa.takasmart.ui.screens.collector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    val otpDigits    = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var otpError     by remember { mutableStateOf("") }
    var isVerifying  by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) {
        db.collection("pickups").document(jobId)
            .addSnapshotListener { snap, _ ->
                address   = snap?.getString("address") ?: ""
                itemIds   = (snap?.get("item_ids") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                otpFromDb = snap?.getString("otp") ?: ""
            }
        db.collection("pickups").document(jobId)
            .update("status", "en_route")
    }

    val checkedCount  = checkedItems.size
    val totalItems    = itemIds.size
    val progressFloat = if (totalItems > 0) checkedCount.toFloat() / totalItems else 0f

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
                            text  = "Job #${jobId.take(8).uppercase()} · Step 1 of 3",
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
                        shape = RoundedCornerShape(20.dp),
                        color = TealSurface
                    ) {
                        Text(
                            text     = "En route",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color    = TealDark,
                            style    = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = GraySurface
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Progress bar ───────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(4.dp),
                color    = TealSurface,
                modifier = Modifier.fillMaxWidth().height(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFloat)
                        .background(Teal)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Map card ───────────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = TealSurface,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                border   = BorderStroke(0.5.dp, TealMid)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📍", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text       = address.ifEmpty { "Loading address..." },
                            style      = MaterialTheme.typography.titleSmall,
                            color      = TealDark,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text  = "1.2 km away · ETA ~8 min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Teal
                        )
                    }
                    Surface(
                        shape    = RoundedCornerShape(8.dp),
                        color    = Teal,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text     = "Navigate",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = White
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── OTP section ────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "HOUSEHOLD OTP",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = GrayMid,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = "6-digit code from their app",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayLight
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    val isFilled = digit.isNotEmpty()
                    OutlinedTextField(
                        value         = digit,
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                otpDigits[index] = input
                                otpError = ""
                                if (input.isNotEmpty() && index < 5)
                                    focusRequesters[index + 1].requestFocus()
                            }
                            if (input.isEmpty() && index > 0) {
                                otpDigits[index] = ""
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier        = Modifier
                            .width(46.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle       = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontSize  = 20.sp,
                            color     = if (isFilled) Teal else GrayDark,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        shape  = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = Teal,
                            unfocusedBorderColor    = if (isFilled) TealMid else BorderColor,
                            focusedContainerColor   = TealSurface,
                            unfocusedContainerColor = if (isFilled) TealSurface
                            else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            if (otpError.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = otpError,
                        modifier = Modifier.padding(10.dp),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = ErrorRed
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Items checklist ────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "ITEMS TO COLLECT",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = GrayMid,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (checkedCount == totalItems && totalItems > 0)
                        GreenSurface else GraySurface
                ) {
                    Text(
                        text     = "$checkedCount / $totalItems",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (checkedCount == totalItems && totalItems > 0)
                            GreenDark else GrayMid,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            itemIds.forEachIndexed { index, itemId ->
                val isChecked = itemId in checkedItems
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = if (isChecked) TealSurface
                    else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    border   = BorderStroke(
                        width = if (isChecked) 1.dp else 0.5.dp,
                        color = if (isChecked) TealMid else BorderColor
                    )
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) checkedItems.add(itemId)
                                else checkedItems.remove(itemId)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor   = Teal,
                                uncheckedColor = BorderColor
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text       = "Item ${index + 1}",
                                style      = MaterialTheme.typography.bodyMedium,
                                color      = if (isChecked) TealDark else GrayDark,
                                fontWeight = if (isChecked) FontWeight.Medium
                                else FontWeight.Normal
                            )
                        }
                        if (isChecked) {
                            Spacer(Modifier.weight(1f))
                            Surface(shape = CircleShape, color = Teal) {
                                Text(
                                    text     = "✓",
                                    modifier = Modifier.padding(4.dp),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Proceed button ─────────────────────────────────
            val enteredOtp  = otpDigits.joinToString("")
            val allChecked  = checkedItems.size == itemIds.size && itemIds.isNotEmpty()
            val otpComplete = enteredOtp.length == 6

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = otpComplete && allChecked && !isVerifying,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Teal,
                    contentColor           = White,
                    disabledContainerColor = GraySurface,
                    disabledContentColor   = GrayLight
                ),
                shape   = RoundedCornerShape(12.dp),
                onClick = {
                    isVerifying = true
                    if (otpFromDb.isNotEmpty() && enteredOtp != otpFromDb) {
                        otpError    = "Wrong code. Ask the household to check their app."
                        isVerifying = false
                        return@Button
                    }
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
                        text  = "Proceed to weigh items",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
fun ActivePickupScreenPreview() {
    TakaSmartTheme {
        ActivePickupPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePickupPreviewContent() {
    val otpDigits       = remember { mutableStateListOf("3", "7", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val checkedItems    = remember { mutableStateListOf("item1") }
    val dummyItemIds    = listOf("item1", "item2", "item3")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Active pickup", style = MaterialTheme.typography.titleMedium)
                        Text("Job #PKP001 · Step 1 of 3",
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
                    Surface(shape = RoundedCornerShape(20.dp), color = TealSurface) {
                        Text("En route",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = TealDark, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        containerColor = GraySurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = TealSurface,
                modifier = Modifier.fillMaxWidth().height(110.dp),
                border = BorderStroke(0.5.dp, TealMid)) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", style = MaterialTheme.typography.titleLarge)
                        Text("14 Gitanga Rd, Lavington",
                            style = MaterialTheme.typography.titleSmall,
                            color = TealDark, fontWeight = FontWeight.Medium)
                        Text("1.2 km away · ETA ~8 min",
                            style = MaterialTheme.typography.bodySmall, color = Teal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("HOUSEHOLD OTP", style = MaterialTheme.typography.labelSmall,
                color = GrayMid, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                otpDigits.forEach { digit ->
                    val filled = digit.isNotEmpty()
                    OutlinedTextField(
                        value = digit, onValueChange = {},
                        modifier = Modifier.width(46.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center, fontSize = 20.sp,
                            color = if (filled) Teal else GrayDark,
                            fontWeight = FontWeight.Medium),
                        singleLine = true,
                        shape  = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = if (filled) TealMid else BorderColor,
                            focusedContainerColor = TealSurface,
                            unfocusedContainerColor = if (filled) TealSurface else White))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("ITEMS TO COLLECT", style = MaterialTheme.typography.labelSmall,
                color = GrayMid, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            dummyItemIds.forEachIndexed { index, itemId ->
                val isChecked = itemId in checkedItems
                Surface(shape = RoundedCornerShape(10.dp),
                    color = if (isChecked) TealSurface else White,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    border = BorderStroke(
                        if (isChecked) 1.dp else 0.5.dp,
                        if (isChecked) TealMid else BorderColor)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isChecked, onCheckedChange = {},
                            colors = CheckboxDefaults.colors(
                                checkedColor = Teal, uncheckedColor = BorderColor))
                        Spacer(Modifier.width(8.dp))
                        Text("Item ${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isChecked) TealDark else GrayDark,
                            fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = White),
                shape = RoundedCornerShape(12.dp), onClick = {}) {
                Text("Proceed to weigh items →", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}