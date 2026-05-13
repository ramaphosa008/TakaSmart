package com.ramaphosa.takasmart.ui.screens.onboarding.roleselection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.navigation.ROUT_ADMIN_LOGIN
import com.ramaphosa.takasmart.ui.screens.onboarding.phoneentry.StepDot
import com.ramaphosa.takasmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectScreen(navController: NavController) {

    var selectedRole by remember { mutableStateOf("household") }

    Scaffold(

        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal,
                    titleContentColor = White,
                    navigationIconContentColor = White,
                    actionIconContentColor = White
                ),

                title = {
                    Column {
                        Text(
                            text = "TakaSmart App",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },

                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(ROUT_ADMIN_LOGIN)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Admin"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GraySurface)
                .padding(paddingValues)
                .padding(24.dp)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(modifier = Modifier.height(20.dp))

                // Step indicator
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StepDot(active = true)
                    StepDot(active = false)
                    StepDot(active = false)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text  = "Welcome to Taka Smart",
                    style = MaterialTheme.typography.titleLarge,
                    color = GrayDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text  = "Who are you?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayMid
                )

                Spacer(modifier = Modifier.height(32.dp))

                RoleCard(
                    letter       = "H",
                    title        = "Household",
                    subtitle     = "Log e-waste, schedule pickups, earn rewards",
                    selected     = selectedRole == "household",
                    iconBg       = TealSurface,
                    iconColor    = Teal,
                    selectedColor= Teal
                ) { selectedRole = "household" }

                Spacer(modifier = Modifier.height(12.dp))

                RoleCard(
                    letter       = "C",
                    title        = "Collector / rider",
                    subtitle     = "Pick up e-waste, earn per kilogram",
                    selected     = selectedRole == "collector",
                    iconBg       = AmberSurface,
                    iconColor    = Amber,
                    selectedColor= Amber
                ) { selectedRole = "collector" }

                Spacer(modifier = Modifier.height(12.dp))

                RoleCard(
                    letter       = "F",
                    title        = "Recycling facility",
                    subtitle     = "Receive loads, issue certificates",
                    selected     = selectedRole == "facility",
                    iconBg       = PurpleSurface,
                    iconColor    = Purple,
                    selectedColor= Purple
                ) { selectedRole = "facility" }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick  = {
                        // Navigate to the phone entry screen with the selected role
                        navController.navigate("phone_entry/$selectedRole")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Teal,
                        contentColor   = White
                    )
                ) {
                    Text(
                        text  = "Continue as $selectedRole",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}




@Composable
fun RoleCard(
    letter        : String,
    title         : String,
    subtitle      : String,
    selected      : Boolean,
    iconBg        : Color,
    iconColor     : Color,
    selectedColor : Color,
    onClick       : () -> Unit
) {
    val borderWidth = if (selected) 2.dp else 0.5.dp
    val borderColor = if (selected) selectedColor else BorderColor
    val cardBg      = if (selected) White else White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(borderWidth, borderColor),
        colors   = CardDefaults.cardColors(containerColor = cardBg),
        elevation= CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier          = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment  = Alignment.Center
            ) {
                Text(
                    text      = letter,
                    fontSize  = 17.sp,
                    color     = iconColor,
                    style     = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = GrayDark
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayMid
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectScreenPreview() {
    TakaSmartTheme {
        RoleSelectScreen(rememberNavController())
    }
}