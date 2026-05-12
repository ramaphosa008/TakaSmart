package com.ramaphosa.takasmart.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.ramaphosa.takasmart.data.AccountViewModel
import com.ramaphosa.takasmart.navigation.ROUT_ROLE_SELECT

@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountViewModel = viewModel()
) {

    val account by viewModel.accountData.collectAsState()
    val colors = MaterialTheme.colorScheme

    val roleColor = when (account.role.lowercase()) {
        "household" -> colors.primary
        "collector" -> colors.tertiary
        "facility" -> colors.tertiaryFixed
        else -> colors.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(24.dp))

        // ── Avatar ───────────────────────────────
        Surface(
            shape = CircleShape,
            color = roleColor.copy(alpha = 0.15f),
            modifier = Modifier
                .size(100.dp)
                .shadow(6.dp, CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = roleColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = account.name.ifBlank { "User" },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        )

        Spacer(Modifier.height(6.dp))

        // ── Role Badge ───────────────────────────
        Surface(
            color = roleColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = account.role.uppercase(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                color = roleColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(26.dp))

        // ── Info Card ───────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = "Account Information",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onSurface
                )

                Spacer(Modifier.height(16.dp))

                InfoRow("Role", account.role, colors)
                Spacer(Modifier.height(10.dp))

                when (account.role.lowercase()) {
                    "household" -> InfoRow("Phone", account.uniqueId, colors)
                    "collector" -> InfoRow("Collector ID", account.uniqueId, colors)
                    "facility" -> InfoRow("Facility ID", account.uniqueId, colors)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Logout Button ───────────────────────
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(ROUT_ROLE_SELECT) {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.error,
                contentColor = colors.onError
            )
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String, colors: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── PREVIEW ─────────────────────────────────────────────
@Composable
@Preview(showBackground = true)
fun AccountScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(100.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("John Doe", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    "COLLECTOR",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Role: collector")
                    Text("ID: COL001")
                }
            }

            Spacer(Modifier.weight(1f))

            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }
}
