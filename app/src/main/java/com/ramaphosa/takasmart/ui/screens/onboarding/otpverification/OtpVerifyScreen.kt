package com.ramaphosa.takasmart.ui.screens.onboarding.otpverification

import androidx.compose.foundation.layout.*
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
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_COLLECTOR_HOME
import com.ramaphosa.takasmart.navigation.ROUT_FACILITY_HOME
import com.ramaphosa.takasmart.navigation.ROUT_HOUSEHOLD_HOME
import com.ramaphosa.takasmart.navigation.ROUT_SPLASH
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerifyScreen(
    navController  : NavController,
    verificationId : String,
    role           : String = "household",  // household | collector | facility
    entityId       : String = ""            // COL001 or FAC001 — empty for household
) {
    val auth = FirebaseAuth.getInstance()

    val digits          = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Verify number",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Enter the 6-digit code we sent you",
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
                }
            )
        }
    ) { padding ->

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(48.dp))

            // ── Step indicator ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry.StepDot(active = false)
                com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry.StepDot(active = false)
                com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry.StepDot(active = true)
            }

            Spacer(Modifier.height(28.dp))

            // ── Role indicator badge ───────────────────────────
            // Shows the user which role they are signing in as
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                color = when (role) {
                    "collector" -> AmberSurface
                    "facility"  -> PurpleSurface
                    else        -> TealSurface
                }
            ) {
                Text(
                    text     = when (role) {
                        "collector" -> "Signing in as Collector" + 
                            if (entityId != "none" && entityId.isNotEmpty()) " · $entityId" else ""
                        "facility"  -> "Signing in as Facility" + 
                            if (entityId != "none" && entityId.isNotEmpty()) " · $entityId" else ""
                        else        -> "Signing in as Household"
                    },
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical   = 6.dp
                    ),
                    color = when (role) {
                        "collector" -> AmberDark
                        "facility"  -> PurpleDark
                        else        -> TealDark
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── 6 digit boxes ──────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                digits.forEachIndexed { index, digit ->
                    val isFilled = digit.isNotEmpty()

                    OutlinedTextField(
                        value         = digit,
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                digits[index] = input
                                if (input.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                                if (index == 5 && input.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            }
                            if (input.isEmpty() && index > 0) {
                                digits[index] = ""
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier        = Modifier
                            .width(48.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle       = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center,
                            fontSize  = 16.sp,
                            color     = if (isFilled) Teal else GrayDark
                        ),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = Teal,
                            unfocusedBorderColor    = if (isFilled) TealMid else BorderColor,
                            focusedContainerColor   = TealSurface,
                            unfocusedContainerColor = if (isFilled) TealSurface else White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Error message ──────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Text(
                    text  = errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Resend hint ────────────────────────────────────
            Text(
                text      = "Didn't receive a code? Go back and resend.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Verify button ──────────────────────────────────
            val allFilled = digits.all { it.isNotEmpty() }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = allFilled && !isLoading,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = when (role) {
                        "collector" -> Amber
                        "facility"  -> Purple
                        else        -> Teal
                    },
                    contentColor = White
                ),
                onClick = {
                    isLoading    = true
                    errorMessage = ""

                    val code       = digits.joinToString("")
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)

                    signInWithCredential(
                        auth          = auth,
                        credential    = credential,
                        role          = role,
                        entityId      = entityId,
                        navController = navController,
                        onLoading     = { isLoading = it },
                        onError       = { message ->
                            errorMessage = message
                            digits.forEachIndexed { i, _ -> digits[i] = "" }
                            focusRequesters[0].requestFocus()
                        }
                    )
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

// ── Firebase sign-in + user document creation ──────────────────────────────
private fun signInWithCredential(
    auth          : FirebaseAuth,
    credential    : PhoneAuthCredential,
    role          : String,
    entityId      : String,
    navController : NavController,
    onLoading     : (Boolean) -> Unit,
    onError       : (String) -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnSuccessListener { result ->
            val user = result.user ?: return@addOnSuccessListener
            val db   = FirebaseFirestore.getInstance()

            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { snap ->
                    if (!snap.exists()) {
                        // First login — build user document
                        val userData = mutableMapOf<String, Any>(
                            "uid"            to user.uid,
                            "phone"          to (user.phoneNumber ?: ""),
                            "role"           to role,
                            "points_balance" to 0,
                            "recycled_kg"    to 0.0,
                            "pickups_done"   to 0,
                            "created_at"     to FieldValue.serverTimestamp()
                        )

                        // Store entity ID for collector and facility
                        if (entityId.isNotEmpty() && entityId != "none") {
                            userData["entity_id"] = entityId
                            
                            // Link the UID back to the collector/facility record for easier lookups
                            val collection = if (role == "collector") "collectors" else "facilities"
                            db.collection(collection).document(entityId)
                                .update("uid", user.uid)
                        }

                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                onLoading(false)
                                navigateByRole(role, navController)
                            }
                            .addOnFailureListener { e ->
                                onLoading(false)
                                onError(e.message ?: "Failed to create account.")
                            }
                    } else {
                        // Returning user — use stored role
                        onLoading(false)
                        val storedRole = snap.getString("role") ?: role
                        navigateByRole(storedRole, navController)
                    }
                }
                .addOnFailureListener { e ->
                    onLoading(false)
                    onError(e.message ?: "Failed to load account.")
                }
        }
        .addOnFailureListener { e ->
            onLoading(false)
            onError(e.message ?: "Wrong code. Please try again.")
        }
}

// ── Navigate to the correct home screen based on role ─────────────────────
private fun navigateByRole(role: String, navController: NavController) {
    val destination = when (role) {
        "collector" -> ROUT_COLLECTOR_HOME
        "facility"  -> ROUT_FACILITY_HOME
        else        -> ROUT_HOUSEHOLD_HOME
    }
    navController.navigate(destination) {
        popUpTo(ROUT_SPLASH) { inclusive = true }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun OtpVerifyScreenPreview() {
    TakaSmartTheme {
        OtpVerifyScreen(
            navController  = rememberNavController(),
            verificationId = "fake_id_for_preview",
            role           = "household",
            entityId       = ""
        )
    }
}

@Preview(showBackground = true, name = "Collector OTP")
@Composable
fun OtpVerifyCollectorPreview() {
    TakaSmartTheme {
        OtpVerifyScreen(
            navController  = rememberNavController(),
            verificationId = "fake_id_for_preview",
            role           = "collector",
            entityId       = "COL001"
        )
    }
}

@Preview(showBackground = true, name = "Facility OTP")
@Composable
fun OtpVerifyFacilityPreview() {
    TakaSmartTheme {
        OtpVerifyScreen(
            navController  = rememberNavController(),
            verificationId = "fake_id_for_preview",
            role           = "facility",
            entityId       = "FAC001"
        )
    }
}