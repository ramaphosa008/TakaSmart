package com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.ramaphosa.takasmart.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun PhoneEntryScreen(navController: NavController) {

    val context  = LocalContext.current
    val activity = context as Activity

    var phone       by remember { mutableStateOf("") }
    var isError     by remember { mutableStateOf(false) }
    var isLoading   by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GraySurface)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(48.dp))

            // Step indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StepDot(active = false)
                StepDot(active = true)
                StepDot(active = false)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text  = "Sign in",
                style = MaterialTheme.typography.titleLarge,
                color = GrayDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = "Enter your Safaricom or Airtel number",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMid
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value         = phone,
                onValueChange = {
                    phone   = it
                    isError = false
                },
                label         = { Text("Phone number") },
                placeholder   = { Text("+254 7__ ___ ___") },
                isError       = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text  = "Enter a valid Kenyan number e.g. 0712345678",
                            color = ErrorRed
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor,
                    errorBorderColor     = ErrorRed,
                    focusedLabelColor    = Teal,
                    unfocusedLabelColor  = GrayMid,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = "We'll send a 4-digit OTP to this number.",
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val cleaned = phone.trim()
                    val stripped = cleaned.filter { it.isDigit() || it == '+' }

                    android.util.Log.d("OTP_DEBUG", "=== SEND OTP TAPPED ===")
                    android.util.Log.d("OTP_DEBUG", "Raw input: '$cleaned'")
                    android.util.Log.d("OTP_DEBUG", "Stripped: '$stripped'")

                    val normalized = when {
                        stripped.startsWith("+254") -> stripped
                        stripped.startsWith("254")  -> "+$stripped"
                        stripped.startsWith("0")    -> "+254${stripped.removePrefix("0")}"
                        else                        -> "+254$stripped"
                    }

                    android.util.Log.d("OTP_DEBUG", "Normalized: '$normalized'")

                    val digitsAfterCode = normalized.removePrefix("+254")
                    android.util.Log.d("OTP_DEBUG", "Digits after code: '$digitsAfterCode'")
                    android.util.Log.d("OTP_DEBUG", "Digit count: ${digitsAfterCode.length}")

                    if (digitsAfterCode.length != 9 || !digitsAfterCode.all { it.isDigit() }) {
                        android.util.Log.d("OTP_DEBUG", "VALIDATION FAILED")
                        isError = true
                    } else {
                        android.util.Log.d("OTP_DEBUG", "VALIDATION PASSED — calling Firebase")
                        isLoading = true
                        isError   = false

                        val auth = FirebaseAuth.getInstance()
                        android.util.Log.d("OTP_DEBUG", "Auth instance: $auth")
                        android.util.Log.d("OTP_DEBUG", "Current user before OTP: ${auth.currentUser}")

                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(normalized)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    android.util.Log.d("OTP_DEBUG", "onVerificationCompleted fired")
                                    isLoading = false
                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    android.util.Log.e("OTP_DEBUG", "onVerificationFailed: ${e.message}")
                                    android.util.Log.e("OTP_DEBUG", "Exception class: ${e.javaClass.simpleName}")
                                    isLoading = false
                                    isError   = true
                                }

                                override fun onCodeSent(
                                    verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken
                                ) {
                                    android.util.Log.d("OTP_DEBUG", "onCodeSent fired — verificationId: $verificationId")
                                    isLoading = false
                                    navController.navigate("otp_verify/$verificationId")
                                }
                            })
                            .build()

                        android.util.Log.d("OTP_DEBUG", "Calling verifyPhoneNumber with: $normalized")
                        PhoneAuthProvider.verifyPhoneNumber(options)
                        android.util.Log.d("OTP_DEBUG", "verifyPhoneNumber called — waiting for callback")
                    }
                },
                enabled  = phone.isNotEmpty() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Teal,
                    contentColor           = White,
                    disabledContainerColor = TealSurface,
                    disabledContentColor   = TealMid
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "Send OTP",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick  = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = true
                ).copy(
                    width = 0.5.dp
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
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