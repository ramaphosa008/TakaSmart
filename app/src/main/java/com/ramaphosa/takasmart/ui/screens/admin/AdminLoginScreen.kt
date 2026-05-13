package com.ramaphosa.takasmart.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramaphosa.takasmart.navigation.ROUT_ADMIN_HOME
import com.ramaphosa.takasmart.navigation.ROUT_ROLE_SELECT
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db   = FirebaseFirestore.getInstance()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Admin Login",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GrayDark
                        )
                        Text(
                            text  = "Restricted access",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMid
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GrayDark)
                    }
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            // ── Admin badge ────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TealSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text       = "Administrator",
                style      = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color      = GrayDark
            )
            Text(
                text  = "Sign in to access the admin panel",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMid
            )

            Spacer(Modifier.height(36.dp))

            // ── Email field ────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = {
                    email        = it
                    errorMessage = ""
                },
                label           = { Text("Admin email") },
                placeholder     = { Text("admin@takasmart.com") },
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape  = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor    = Teal
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Password field ─────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = {
                    password     = it
                    errorMessage = ""
                },
                label           = { Text("Password") },
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword)
                                "Hide password" else "Show password",
                            tint = GrayMid
                        )
                    }
                },
                shape  = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor    = Teal
                )
            )

            // ── Error message ──────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = ErrorSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = errorMessage,
                        modifier = Modifier.padding(12.dp),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = ErrorRed
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Login button ───────────────────────────────────
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled  = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Teal,
                    contentColor           = White,
                    disabledContainerColor = GraySurface,
                    disabledContentColor   = GrayLight
                ),
                shape   = RoundedCornerShape(12.dp),
                onClick = {
                    isLoading    = true
                    errorMessage = ""

                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: ""

                            // Verify this user has role == admin in Firestore
                            db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener { snap ->
                                    val role = snap.getString("role") ?: ""
                                    if (role == "admin") {
                                        isLoading = false
                                        navController.navigate(ROUT_ADMIN_HOME) {
                                            popUpTo(ROUT_ROLE_SELECT) { inclusive = true }
                                        }
                                    } else {
                                        // Signed in but not an admin — sign out immediately
                                        auth.signOut()
                                        isLoading    = false
                                        errorMessage = "Access denied. This account is not an admin."
                                    }
                                }
                                .addOnFailureListener {
                                    auth.signOut()
                                    isLoading    = false
                                    errorMessage = "Could not verify admin access. Try again."
                                }
                        }
                        .addOnFailureListener { e ->
                            isLoading    = false
                            errorMessage = when {
                                e.message?.contains("password") == true ->
                                    "Wrong password. Try again."
                                e.message?.contains("user") == true ->
                                    "No account found with this email."
                                else ->
                                    "Login failed. Check your credentials."
                            }
                        }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "Sign in as Admin",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text  = "This login is restricted to authorised administrators only.",
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
fun AdminLoginScreenPreview() {
    TakaSmartTheme {
        AdminLoginScreen(rememberNavController())
    }
}
