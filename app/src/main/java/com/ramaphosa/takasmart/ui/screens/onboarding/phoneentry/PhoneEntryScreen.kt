package com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.ui.theme.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneEntryScreen(
    navController : NavController,
    role          : String = "household"  // household | collector | facility
) {

    val context  = LocalContext.current
    val activity = context as Activity
    val db       = FirebaseFirestore.getInstance()

    var phone        by remember { mutableStateOf("") }
    var entityId     by remember { mutableStateOf("") } // collector or facility ID
    var isError      by remember { mutableStateOf(false) }
    var idError      by remember { mutableStateOf(false) }
    var idErrorMsg   by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Label changes based on role
    val idLabel = when (role) {
        "collector" -> "Collector ID"
        "facility"  -> "Facility ID"
        else        -> ""
    }

    val idPlaceholder = when (role) {
        "collector" -> "e.g. COL001"
        "facility"  -> "e.g. FAC001"
        else        -> ""
    }

    val idCollection = when (role) {
        "collector" -> "collectors"
        "facility"  -> "facilities"
        else        -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GraySurface)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(48.dp))

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StepDot(active = false)
                StepDot(active = true)
                StepDot(active = false)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text  = "Sign in",
                style = MaterialTheme.typography.titleLarge,
                color = GrayDark
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = when (role) {
                    "collector" -> "Enter your phone number and Collector ID"
                    "facility"  -> "Enter your phone number and Facility ID"
                    else        -> "Enter your Safaricom or Airtel number"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMid
            )

            Spacer(Modifier.height(28.dp))

            // ── Phone number field ─────────────────────────────
            OutlinedTextField(
                value         = phone,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }
                    phone = filtered
                    isError = false
                    // Auto-focus ID field if phone looks complete
                    if (filtered.length >= 10 && (role == "collector" || role == "facility")) {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                },
                label          = { Text("Phone number") },
                placeholder    = { Text("+254 7__ ___ ___") },
                leadingIcon    = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = GrayMid)
                },
                isError        = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text  = "Enter a valid number e.g. 0712345678",
                            color = ErrorRed
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor,
                    errorBorderColor     = ErrorRed,
                    focusedLabelColor    = Teal,
                    unfocusedLabelColor  = GrayMid
                )
            )

            // ── Collector / Facility ID field ──────────────────
            if (role == "collector" || role == "facility") {

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = entityId,
                    onValueChange = {
                        entityId = it.uppercase().trim()
                        idError  = false
                    },
                    label          = { Text(idLabel) },
                    placeholder    = { Text(idPlaceholder) },
                    leadingIcon    = {
                        Icon(
                            imageVector = if (role == "collector") Icons.Default.Badge else Icons.Default.Business,
                            contentDescription = null,
                            tint = GrayMid
                        )
                    },
                    isError        = idError,
                    supportingText = {
                        if (idError) {
                            Text(
                                text  = idErrorMsg,
                                color = ErrorRed
                            )
                        }
                    },
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = when (role) {
                            "collector" -> Amber
                            "facility"  -> Purple
                            else        -> Teal
                        },
                        unfocusedBorderColor = BorderColor,
                        errorBorderColor     = ErrorRed,
                        focusedLabelColor    = when (role) {
                            "collector" -> Amber
                            "facility"  -> Purple
                            else        -> Teal
                        },
                        unfocusedLabelColor  = GrayMid
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text  = when (role) {
                    "collector" -> "Your Collector ID was given to you when you registered."
                    "facility"  -> "Your Facility ID was given to you when your facility was registered."
                    else        -> "We'll send a 6-digit OTP to this number."
                },
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight
            )

            Spacer(Modifier.weight(1f))

            // ── Send OTP button ────────────────────────────────
            Button(
                onClick = {
                    // Extract only digits to normalize
                    val digits = phone.filter { it.isDigit() }
                    
                    // Robust normalization for Kenyan numbers
                    // We take the last 9 digits and prepend +254
                    val normalized = if (digits.length >= 9) {
                        "+254${digits.takeLast(9)}"
                    } else {
                        ""
                    }

                    // Basic length check for +254XXXXXXXXX
                    if (normalized.length != 13) {
                        isError = true
                        return@Button
                    }

                    // For collector and facility — validate ID first
                    if (role == "collector" || role == "facility") {
                        if (entityId.isEmpty()) {
                            idError    = true
                            idErrorMsg = "$idLabel cannot be empty"
                            return@Button
                        }

                        // Check ID exists in Firestore before sending OTP
                        isValidating = true
                        db.collection(idCollection)
                            .document(entityId)
                            .get()
                            .addOnSuccessListener { document ->
                                isValidating = false

                                if (!document.exists()) {
                                    // ID not found
                                    idError    = true
                                    idErrorMsg = "$idLabel not found. Check and try again."
                                    return@addOnSuccessListener
                                }

                                if (document.getBoolean("active") != true) {
                                    // ID found but account is inactive
                                    idError    = true
                                    idErrorMsg = "This account is inactive. Contact support."
                                    return@addOnSuccessListener
                                }

                                // ID is valid — now send OTP
                                sendOtp(
                                    auth          = FirebaseAuth.getInstance(),
                                    activity      = activity,
                                    normalized    = normalized,
                                    role          = role,
                                    entityId      = entityId,
                                    navController = navController,
                                    onLoading     = { isLoading = it },
                                    onError       = {
                                        // Specific error handled in sendOtp or logcat
                                        // We set isError to true only for phone validation
                                    }
                                )
                            }
                            .addOnFailureListener {
                                isValidating = false
                                idError      = true
                                idErrorMsg   = "Could not verify ID. Check your connection."
                            }

                    } else {
                        // Household — send OTP directly
                        sendOtp(
                            auth          = FirebaseAuth.getInstance(),
                            activity      = activity,
                            normalized    = normalized,
                            role          = role,
                            entityId      = "",
                            navController = navController,
                            onLoading     = { isLoading = it },
                            onError       = { }
                        )
                    }
                },
                enabled  = phone.isNotEmpty() &&
                        (role == "household" || entityId.isNotEmpty()) &&
                        !isLoading && !isValidating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = when (role) {
                        "collector" -> Amber
                        "facility"  -> Purple
                        else        -> Teal
                    },
                    contentColor           = White,
                    disabledContainerColor = GraySurface,
                    disabledContentColor   = GrayLight
                )
            ) {
                if (isLoading || isValidating) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isValidating) "Verifying ID..." else "Sending OTP...",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                } else {
                    Text(
                        text = "Send OTP",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick  = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, BorderColor
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GrayMid
                )
            ) {
                Text(
                    text  = "Back",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── OTP sending helper — called after ID validation passes ─────────────────
private fun sendOtp(
    auth          : FirebaseAuth,
    activity      : Activity,
    normalized    : String,
    role          : String,
    entityId      : String,
    navController : NavController,
    onLoading     : (Boolean) -> Unit,
    onError       : () -> Unit
) {
    onLoading(true)

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(normalized)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onLoading(false)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                android.util.Log.e("OTP_DEBUG", "Failed: ${e.message}")
                onLoading(false)
                onError()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onLoading(false)
                // Pass verificationId, role and entityId to OTP screen
                // Use "none" as placeholder if entityId is empty to satisfy route
                val eid = if (entityId.trim().isEmpty()) "none" else entityId
                navController.navigate(
                    "otp_verify/$verificationId/$role/$eid"
                )
            }
        })
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

@Composable
fun StepDot(active: Boolean) {
    Box(
        modifier = Modifier
            .height(6.dp)
            .width(if (active) 20.dp else 6.dp)
            .background(
                color = if (active) Teal else BorderColor,
                shape = RoundedCornerShape(3.dp)
            )
    )
}

@Preview(showBackground = true)
@Composable
fun PhoneEntryScreenPreview() {
    TakaSmartTheme {
        PhoneEntryScreen(rememberNavController())
    }
}