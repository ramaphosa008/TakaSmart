package com.ramaphosa.takasmart.ui.screens.household

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ramaphosa.takasmart.data.repository.CloudinaryRepository
import com.ramaphosa.takasmart.ui.theme.*
import com.ramaphosa.takasmart.data.HouseholdViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogItemScreen(navController: NavController) {

    val context = LocalContext.current
    val vm      : HouseholdViewModel = viewModel()
    val scope   = rememberCoroutineScope()
    val cloudinary = remember { CloudinaryRepository(context) }

    var selectedCategory  by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("Broken / not working") }
    var model             by remember { mutableStateOf("") }
    var selectedImageUri  by remember { mutableStateOf<Uri?>(null) }
    var uploadedPhotoUrl  by remember { mutableStateOf("") }
    var isUploading       by remember { mutableStateOf(false) }
    var isSaving          by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf("") }
    var conditionExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Phone / tablet", "Laptop / PC",
        "Cables / chargers", "Batteries",
        "TV / monitor", "Other"
    )

    val conditions = listOf(
        "Broken / not working",
        "Working but old",
        "Parts only"
    )

    // Image picker launcher — opens device gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploading      = true
            errorMessage     = ""

            // Upload to Cloudinary as soon as image is picked
            scope.launch {
                try {
                    uploadedPhotoUrl = cloudinary.uploadPhoto(it)
                    isUploading      = false
                } catch (e: Exception) {
                    isUploading  = false
                    errorMessage = "Photo upload failed. You can still save without a photo."
                    uploadedPhotoUrl = ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Log item",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "What are you recycling?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        =Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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

            // ── Category grid ──────────────────────────────────
            Text(
                text  = "Category",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            categories.chunked(2).forEach { rowItems ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        OutlinedCard(
                            onClick  = { selectedCategory = cat },
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 8.dp),
                            shape  = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Teal else BorderColor
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) TealSurface else White
                            )
                        ) {
                            Text(
                                text      = cat,
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                textAlign = TextAlign.Center,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = if (isSelected) TealDark else GrayMid
                            )
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Model field ────────────────────────────────────
            Text(
                text  = "Brand & model (optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value         = model,
                onValueChange = { model = it },
                placeholder   = { Text("e.g. Samsung Galaxy S9") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Teal,
                    unfocusedBorderColor = BorderColor
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Condition dropdown ─────────────────────────────
            Text(
                text  = "Condition",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded         = conditionExpanded,
                onExpandedChange = { conditionExpanded = !conditionExpanded }
            ) {
                OutlinedTextField(
                    value         = selectedCondition,
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = conditionExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        ),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = BorderColor
                    )
                )
                ExposedDropdownMenu(
                    expanded         = conditionExpanded,
                    onDismissRequest = { conditionExpanded = false }
                ) {
                    conditions.forEach { condition ->
                        DropdownMenuItem(
                            text    = { Text(condition) },
                            onClick = {
                                selectedCondition = condition
                                conditionExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Photo upload section ───────────────────────────
            Text(
                text  = "Photo (optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            if (selectedImageUri != null) {
                // Show the selected image
                Box(
                    modifier        = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model             = selectedImageUri,
                        contentDescription = "Selected photo",
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )

                    // Uploading overlay
                    if (isUploading) {
                        Surface(
                            color    = GrayDark.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text  = "Uploading...",
                                        color = White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Uploaded success indicator
                    if (!isUploading && uploadedPhotoUrl.isNotEmpty()) {
                        Box(
                            modifier        = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GreenSurface
                            ) {
                                Text(
                                    text     = "✓ Uploaded",
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp, vertical = 4.dp
                                    ),
                                    color    = GreenDark,
                                    style    = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Change photo button
                TextButton(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Text(
                        text  = "Change photo",
                        color = Teal,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            } else {
                // Photo picker placeholder
                OutlinedCard(
                    onClick  = { imagePickerLauncher.launch("image/*") },
                    shape    = RoundedCornerShape(8.dp),
                    border   = BorderStroke(0.5.dp, BorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = "📷",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "Tap to add a photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

            // ── Save button ────────────────────────────────────
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = selectedCategory.isNotEmpty() && !isSaving && !isUploading,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor   = White
                ),
                onClick  = {
                    isSaving     = true
                    errorMessage = ""

                    scope.launch {
                        try {
                            vm.addItem(
                                category  = selectedCategory,
                                condition = selectedCondition,
                                model     = model,
                                photoUrl  = uploadedPhotoUrl
                            )
                            isSaving = false
                            navController.popBackStack()
                        } catch (e: Exception) {
                            isSaving     = false
                            errorMessage = e.message ?: "Failed to save. Try again."
                        }
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
                    Text("Add to my list", style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogItemScreenPreview() {
    TakaSmartTheme {
        LogItemScreen(rememberNavController())
    }
}