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
import androidx.compose.ui.platform.LocalContext
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
import com.ramaphosa.takasmart.navigation.ROUT_HOUSEHOLD_HOME
import com.ramaphosa.takasmart.navigation.ROUT_SPLASH
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerifyScreen(
    navController: NavController,
    verificationId: String  // passed from PhoneEntryScreen after Firebase sends the OTP
) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()

    // One string per digit box
    val digits = remember { mutableStateListOf("", "", "", "", "", "") }

    // A FocusRequester for each box so the cursor jumps forward automatically
    val focusRequesters = remember { List(6) { FocusRequester() } }

    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    // Auto-request focus on the first box when the screen opens
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
                            text  = "Enter the 4-digit code we sent you",
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(48.dp))

            // ── 4 digit boxes ──────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                digits.forEachIndexed { index, digit ->

                    val isFilled = digit.isNotEmpty()

                    OutlinedTextField(
                        value         = digit,
                        onValueChange = { input ->
                            // Only accept a single digit
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                digits[index] = input

                                // Jump to next box if a digit was typed
                                if (input.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }

                                // If last box filled, clear error
                                if (index == 5 && input.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            }

                            // Handle backspace — go to previous box
                            if (input.isEmpty() && index > 0) {
                                digits[index] = ""
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier      = Modifier
                            .width(64.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle     = MaterialTheme.typography.titleLarge.copy(
                            textAlign  = TextAlign.Center,
                            fontSize   = 24.sp,
                            color      = if (isFilled) Teal else GrayDark
                        ),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Teal,
                            unfocusedBorderColor = if (isFilled) TealMid else BorderColor,
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
                text  = "Didn't receive a code? Go back and resend.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    containerColor = Teal,
                    contentColor   = White
                ),
                onClick  = {
                    isLoading = true
                    errorMessage = ""

                    val code       = digits.joinToString("")
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)

                    signInWithCredential(
                        auth        = auth,
                        credential  = credential,
                        onSuccess   = {
                            isLoading = false
                            // Clear the entire back stack so the user
                            // cannot press back to return to login
                            navController.navigate(ROUT_HOUSEHOLD_HOME) {
                                popUpTo(ROUT_SPLASH) { inclusive = true }
                            }
                        },
                        onError     = { message ->
                            isLoading = false
                            errorMessage = message
                            // Clear all boxes so user retypes
                            digits.forEachIndexed { i, _ -> digits[i] = "" }
                            focusRequesters[0].requestFocus()
                        }
                    )
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

// ── Firebase sign-in helper ────────────────────────────────────────────────
private fun signInWithCredential(
    auth       : FirebaseAuth,
    credential : PhoneAuthCredential,
    onSuccess  : () -> Unit,
    onError    : (String) -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnSuccessListener { result ->
            val user = result.user ?: return@addOnSuccessListener
            val db   = FirebaseFirestore.getInstance()

            // Check if user document already exists
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { snap ->
                    if (!snap.exists()) {
                        // First login — create the document
                        db.collection("users").document(user.uid).set(
                            mapOf(
                                "uid"            to user.uid,
                                "phone"          to (user.phoneNumber ?: ""),
                                "role"           to "household",
                                "points_balance" to 0,
                                "recycled_kg"    to 0.0,
                                "pickups_done"   to 0,
                                "created_at"     to FieldValue.serverTimestamp()
                            )
                        ).addOnSuccessListener { onSuccess() }
                    } else {
                        // Returning user — go straight in
                        onSuccess()
                    }
                }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Wrong code. Please try again.")
        }
}

@Preview(showBackground = true)
@Composable
fun OtpVerifyScreenPreview() {
    TakaSmartTheme {
        OtpVerifyScreen(
            navController    = rememberNavController(),
            verificationId   = "fake_verification_id_for_preview"
        )
    }
}