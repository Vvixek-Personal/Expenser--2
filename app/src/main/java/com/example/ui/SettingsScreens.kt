package com.example.ui

import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.*

enum class SettingsSubScreen {
    PersonalData,
    BadgesAndMilestones,
    Appearance,
    Language,
    Currency,
    DateTime,
    Bills,
    CategoriesTags,
    Budgets,
    SavingsGoals,
    Calculations,
    Transactions,
    BackupRestore,
    DataManagement,
    LocalRecovery,
    Security,
    Privacy,
    AboutApp,
    HelpSupport,
    DataAndStorage,
    ThemeAndLanguage,
    Export,
    FaqAndHelp
}

@Composable
fun SettingsHeaderTitle(title: String, onBack: () -> Unit) {
    Surface(
        color = SleekSurface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SleekTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
        }
    }
}

// ==========================================
// 0️⃣ PERSONAL DATA SCREEN
// ==========================================
@Composable
fun PersonalDataScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val currentName by viewModel.userName.collectAsStateWithLifecycle()
    val currentDob by viewModel.userDob.collectAsStateWithLifecycle()
    val currentJob by viewModel.userJob.collectAsStateWithLifecycle()
    val currentIncome by viewModel.userMonthlyIncome.collectAsStateWithLifecycle()
    val currentGender by viewModel.userGender.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()

    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
    val googleProfileName by viewModel.googleProfileName.collectAsStateWithLifecycle()
    val googleProfilePhotoUrl by viewModel.googleProfilePhotoUrl.collectAsStateWithLifecycle()

    var nameText by remember { mutableStateOf(currentName ?: "William John Malik") }
    LaunchedEffect(currentName) {
        currentName?.let {
            if (it.isNotBlank()) {
                nameText = it
            }
        }
    }
    var dobText by remember { mutableStateOf(currentDob) }
    var jobText by remember { mutableStateOf(currentJob) }
    var incomeText by remember { mutableStateOf(currentIncome) }
    var genderOption by remember { mutableStateOf(currentGender) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPhotoOptionSheet by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateUserProfileImageFromUri(context, uri)
            Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Personal Data", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Picture Circle
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable { showPhotoOptionSheet = true }
            ) {
                if (!profileImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = SleekPrimary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = jobText,
                onValueChange = { jobText = it },
                label = { Text("Occupation / Job") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dobText,
                onValueChange = { dobText = it },
                label = { Text("Date of Birth") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it },
                label = { Text("Income Range") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveUserProfile(nameText, dobText, jobText, incomeText, genderOption)
                    Toast.makeText(context, "Personal data saved successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Badges & Milestones Section in Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Badges and Milestone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            ComingSoonConstructionBanner()

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showPhotoOptionSheet) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionSheet = false },
            title = { Text("Change Profile Photo") },
            text = { Text("Select photo from device gallery or remove current photo.") },
            confirmButton = {
                val suppressAutoLock = LocalAutoLockSuppressor.current
                TextButton(onClick = {
                    showPhotoOptionSheet = false
                    suppressAutoLock()
                    imagePickerLauncher.launch("image/*")
                }) { Text("Choose from Gallery") }
            },
            dismissButton = {
                Row {
                    if (!profileImageUri.isNullOrBlank()) {
                        TextButton(onClick = {
                            showPhotoOptionSheet = false
                            viewModel.removeUserProfileImage(context)
                            Toast.makeText(context, "Profile photo removed", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Remove", color = Color(0xFFDC2626))
                        }
                    }
                    TextButton(onClick = { showPhotoOptionSheet = false }) { Text("Cancel") }
                }
            }
        )
    }
}

// ==========================================
// 0️⃣-B BADGES AND MILESTONES SCREEN
// ==========================================
@Composable
fun BadgesAndMilestonesScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Badges and Milestone", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ComingSoonConstructionBanner()
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ==========================================
// 🏗️ COMING SOON CONSTRUCTION BANNER
// ==========================================
@Composable
fun ComingSoonConstructionBanner(
    modifier: Modifier = Modifier
) {
    val swingAngle = rememberLoopFloat(-12f, 12f, 2200, "swing", reverse = true, rest = 0f)
    val craneHookOffsetY = rememberLoopFloat(0f, 14f, 1800, "craneY", reverse = true, rest = 0f)

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.5.dp, SleekBorder),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Construction Graphic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E293B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Background City Outline
                    val buildingColor = Color(0xFF334155)
                    val windowColor = Color(0xFF94A3B8).copy(alpha = 0.4f)

                    // Building 1
                    drawRect(
                        color = buildingColor,
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.45f),
                        size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.55f)
                    )
                    for (r in 0..3) {
                        for (c in 0..1) {
                            drawRect(
                                color = windowColor,
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.08f + c * 20f, h * 0.5f + r * 22f),
                                size = androidx.compose.ui.geometry.Size(12f, 14f)
                            )
                        }
                    }

                    // Building 2
                    drawRect(
                        color = buildingColor,
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.65f)
                    )
                    for (r in 0..4) {
                        for (c in 0..1) {
                            drawRect(
                                color = windowColor,
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.28f + c * 24f, h * 0.4f + r * 22f),
                                size = androidx.compose.ui.geometry.Size(14f, 14f)
                            )
                        }
                    }

                    // Building 3 (Right building under construction)
                    drawRect(
                        color = buildingColor,
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.5f),
                        size = androidx.compose.ui.geometry.Size(w * 0.23f, h * 0.5f)
                    )
                    val scaffoldColor = Color(0xFFF59E0B).copy(alpha = 0.6f)
                    drawLine(
                        color = scaffoldColor,
                        start = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.4f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.95f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = scaffoldColor,
                        start = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.4f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.95f),
                        strokeWidth = 3f
                    )
                    for (yStep in 0..3) {
                        val yPos = h * 0.4f + yStep * 20f
                        drawLine(
                            color = scaffoldColor,
                            start = androidx.compose.ui.geometry.Offset(w * 0.72f, yPos),
                            end = androidx.compose.ui.geometry.Offset(w * 0.95f, yPos),
                            strokeWidth = 2f
                        )
                    }

                    // 2. Construction Crane
                    val craneColor = Color(0xFFF59E0B)
                    val craneX = w * 0.55f
                    val craneBaseY = h * 0.95f
                    val craneTopY = h * 0.15f

                    drawLine(
                        color = craneColor,
                        start = androidx.compose.ui.geometry.Offset(craneX - 6f, craneBaseY),
                        end = androidx.compose.ui.geometry.Offset(craneX - 6f, craneTopY),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = craneColor,
                        start = androidx.compose.ui.geometry.Offset(craneX + 6f, craneBaseY),
                        end = androidx.compose.ui.geometry.Offset(craneX + 6f, craneTopY),
                        strokeWidth = 4f
                    )
                    for (step in 0..6) {
                        val y1 = craneTopY + step * 20f
                        val y2 = y1 + 20f
                        drawLine(craneColor, androidx.compose.ui.geometry.Offset(craneX - 6f, y1), androidx.compose.ui.geometry.Offset(craneX + 6f, y2), 2f)
                    }

                    // Horizontal Jib
                    val jibLeft = craneX - w * 0.2f
                    val jibRight = craneX + w * 0.35f
                    drawLine(
                        color = craneColor,
                        start = androidx.compose.ui.geometry.Offset(jibLeft, craneTopY),
                        end = androidx.compose.ui.geometry.Offset(jibRight, craneTopY),
                        strokeWidth = 5f
                    )
                    drawRect(
                        color = Color(0xFF64748B),
                        topLeft = androidx.compose.ui.geometry.Offset(jibLeft, craneTopY - 6f),
                        size = androidx.compose.ui.geometry.Size(24f, 16f)
                    )

                    // Cable & Swinging Hook
                    val cableHookX = craneX + w * 0.2f
                    val cableStartY = craneTopY
                    val cableLength = h * 0.35f + craneHookOffsetY

                    val swingRad = Math.toRadians(swingAngle.toDouble())
                    val endHookX = (cableHookX + Math.sin(swingRad) * cableLength).toFloat()
                    val endHookY = (cableStartY + Math.cos(swingRad) * cableLength).toFloat()

                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = androidx.compose.ui.geometry.Offset(cableHookX, cableStartY),
                        end = androidx.compose.ui.geometry.Offset(endHookX, endHookY),
                        strokeWidth = 2f
                    )

                    // Hook Badge
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 12f,
                        center = androidx.compose.ui.geometry.Offset(endHookX, endHookY + 12f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(endHookX, endHookY + 12f)
                    )
                }

                // Overlay Text
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coming soon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Badges and Milestone",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We are currently constructing your Badges and Milestones hub! Soon you will earn unlockable badges for daily savings streaks, financial milestones, and smart budgeting achievements.",
                fontSize = 13.sp,
                color = SleekTextSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// ==========================================
// 1️⃣ APPEARANCE SCREEN
// ==========================================
@Composable
fun AppearanceScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isFollowDeviceColors by viewModel.isFollowDeviceColors.collectAsStateWithLifecycle()
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val customThemeHue by viewModel.customThemeHue.collectAsStateWithLifecycle()
    val textSize by viewModel.textSizeOption.collectAsStateWithLifecycle()
    val isCompact by viewModel.isCompactLayout.collectAsStateWithLifecycle()
    val isAnim by viewModel.isAnimationEnabled.collectAsStateWithLifecycle()
    val fontFamilyChoice by viewModel.fontFamily.collectAsStateWithLifecycle()
    val isHaptics by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val isHighContrast by viewModel.highContrastMode.collectAsStateWithLifecycle()
    val isCardGlow by viewModel.cardGlowAnimation.collectAsStateWithLifecycle()

    var showCustomHuePicker by remember { mutableStateOf(false) }
    var tempHue by remember(customThemeHue) { mutableStateOf(customThemeHue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Appearance Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 1. Top Mode Selector Segmented Control (Light / Dark / Device)
            Surface(
                shape = CircleShape,
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("light", "Light", Icons.Default.LightMode),
                        Triple("dark", "Dark", Icons.Default.DarkMode),
                        Triple("device", "Device", Icons.Default.Tv)
                    ).forEach { (modeKey, label, icon) ->
                        val isSelected = themeMode == modeKey
                        Surface(
                            onClick = { viewModel.updateThemeMode(modeKey) },
                            shape = CircleShape,
                            color = if (isSelected) SleekPrimary else Color.Transparent,
                            contentColor = if (isSelected) Color.White else SleekTextPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 4x4 Theme Swatches Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 4) {
                            val index = row * 4 + col
                            val isSelected = themeIndex == index
                            val preview = getThemePalettePreview(index, customThemeHue)

                            Card(
                                onClick = {
                                    viewModel.updateTheme(index)
                                    if (index == 15) {
                                        showCustomHuePicker = true
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (com.example.ui.theme.isDarkModeActive) Color(0xFF23252B) else Color(0xFFF0F2F5)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) SleekPrimary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (index < 15) {
                                        Box(
                                            modifier = Modifier.size(46.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawArc(color = preview.topLeft, startAngle = 180f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.topRight, startAngle = 270f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.bottomRight, startAngle = 0f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.bottomLeft, startAngle = 90f, sweepAngle = 90f, useCenter = true)
                                            }

                                             if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(18.dp)
                                                        .background(SleekPrimary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.size(46.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Custom Hue",
                                                tint = SleekTextPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(18.dp)
                                                        .background(SleekPrimary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SleekBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Follow Device Colors Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Follow device colors",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleekTextPrimary
                )
                Switch(
                    checked = isFollowDeviceColors,
                    onCheckedChange = { viewModel.toggleFollowDeviceColors(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1967D2)
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("TEXT SIZE PREFERENCE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Small", "Normal", "Large", "Extra Large").forEach { option ->
                    val selected = textSize == option || (textSize == "Medium" && option == "Normal")
                    OutlinedButton(
                        onClick = {
                            val targetOption = if (option == "Medium") "Normal" else option
                            if (textSize != targetOption) {
                                viewModel.updateTextSizeOption(targetOption)
                                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                                if (context is android.app.Activity) {
                                    context.finish()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) SleekPrimary.copy(alpha = 0.15f) else SleekSurface,
                            contentColor = if (selected) SleekPrimary else SleekTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder)
                    ) {
                        Text(
                            text = option, 
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("LAYOUT & ANIMATIONS", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Compact Dashboard Layout", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Displays tighter card padding for more data visibility", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isCompact, onCheckedChange = { viewModel.toggleCompactLayout(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Fluid Animations", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Smooth transitions and fluid budget indicators", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isAnim, onCheckedChange = { viewModel.toggleAnimationEnabled(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Card Glow & Shimmer Effects", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Lively glowing borders on cards and active milestones", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isCardGlow, onCheckedChange = { viewModel.toggleCardGlow(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Touch Feedback", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Tactile micro-vibrations on button taps & operations", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isHaptics, onCheckedChange = { viewModel.toggleHaptics(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("High Contrast Mode", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Sharpened borders and high-visibility content edges", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isHighContrast, onCheckedChange = { viewModel.toggleHighContrast(it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("TYPOGRAPHY & FONT STYLE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Sans Serif" to "Modern Crisp (Default)",
                        "Rounded / Casual" to "Warm & Friendly",
                        "Tech Monospace" to "Fintech & Terminal",
                        "Classic Serif" to "Editorial & Elegant"
                    ).forEach { (fontKey, fontDesc) ->
                        val isSelected = fontFamilyChoice == fontKey || (fontFamilyChoice == "Default" && fontKey == "Sans Serif")
                        Surface(
                            onClick = { viewModel.updateFontFamily(fontKey) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = fontKey,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SleekPrimary else SleekTextPrimary,
                                        fontFamily = com.example.ui.theme.getFontFamilyForChoice(fontKey),
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = fontDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekTextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showCustomHuePicker) {
        AlertDialog(
            onDismissRequest = { showCustomHuePicker = false },
            title = { Text("Custom Theme Color Accent", color = SleekTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.hsv(tempHue, 0.75f, 0.65f), CircleShape)
                            .border(1.dp, SleekBorder, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hue: ${tempHue.toInt()}°", color = SleekTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = tempHue,
                        onValueChange = {
                            tempHue = it
                            viewModel.updateCustomThemeHue(it)
                        },
                        valueRange = 0f..360f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomHuePicker = false }) {
                    Text("Done", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// 2️⃣ LANGUAGE SCREEN
// ==========================================
@Composable
fun LanguageScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var searchLanguage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredLangs = remember(searchLanguage) {
        LanguageManager.supportedLanguages.filter {
            it.nativeName.contains(searchLanguage, ignoreCase = true) ||
            it.engName.contains(searchLanguage, ignoreCase = true) ||
            it.code.contains(searchLanguage, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Language Settings", onBack)

        Column(modifier = Modifier.padding(20.dp)) {
            OutlinedTextField(
                value = searchLanguage,
                onValueChange = { searchLanguage = it },
                placeholder = { Text("Search language") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredLangs, key = { it.code }) { lang ->
                    val isSelected = selectedLanguage.equals(lang.nativeName, ignoreCase = true) || selectedLanguage.equals(lang.engName, ignoreCase = true)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                        border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLanguage(lang.nativeName)
                                Toast.makeText(context, "Language changed to ${lang.nativeName}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(lang.nativeName, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                                    Text(lang.engName, style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                                }
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateLanguage(lang.nativeName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4️⃣ DATE & TIME SCREEN
// ==========================================
@Composable
fun DateTimeScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val firstDay by viewModel.firstDayOfWeek.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Date & Time Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("DATE FORMAT", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd").forEach { fmt ->
                val selected = dateFormat == fmt
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateDateFormat(fmt) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fmt, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateDateFormat(fmt) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("FIRST DAY OF THE WEEK", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("Monday", "Sunday", "Saturday").forEach { day ->
                val selected = firstDay == day
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateFirstDayOfWeek(day) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateFirstDayOfWeek(day) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 5️⃣ BILLS & REMINDERS SCREEN
// ==========================================
@Composable
fun BillsSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val defaultRecurrence by viewModel.billDefaultRecurrence.collectAsStateWithLifecycle()
    val recurringEnd by viewModel.billRecurringEnd.collectAsStateWithLifecycle()
    val defaultCategory by viewModel.billDefaultCategory.collectAsStateWithLifecycle()
    val autoMarkPaid by viewModel.billAutoMarkPaid.collectAsStateWithLifecycle()
    val archiveDays by viewModel.billArchiveDays.collectAsStateWithLifecycle()

    val showUpcomingDashboard by viewModel.billShowUpcomingDashboard.collectAsStateWithLifecycle()
    val upcomingDays by viewModel.billUpcomingDays.collectAsStateWithLifecycle()
    val sortOrder by viewModel.billSortOrder.collectAsStateWithLifecycle()

    val defaultFilter by viewModel.billDefaultFilter.collectAsStateWithLifecycle()
    val overdueAlert by viewModel.billOverdueAlert.collectAsStateWithLifecycle()
    val showNotes by viewModel.billShowNotes.collectAsStateWithLifecycle()

    var activePickerTitle by remember { mutableStateOf<String?>(null) }
    var pickerOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSelectedValue by remember { mutableStateOf("") }
    var onOptionSelected by remember { mutableStateOf<((String) -> Unit)?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Custom Header matching reference image
        Surface(
            color = SleekSurface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Bills & Reminders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Manage your bills and reminder preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= SECTION 1: Bills Preferences =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.AccountBalanceWallet,
                sectionIconBg = Color(0xFFEFF0FE),
                sectionIconTint = Color(0xFF4F46E5),
                title = "Bills Preferences",
                subtitle = "Configure how bills are handled in the app"
            ) {
                // 1. Default Recurrence
                BillsSettingItemRow(
                    icon = Icons.Outlined.CalendarToday,
                    iconBg = Color(0xFFEBF5FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Default Recurrence",
                    subtitle = "Set default recurrence for new bills",
                    valueText = defaultRecurrence,
                    onClick = {
                        activePickerTitle = "Default Recurrence"
                        pickerOptions = listOf("Daily", "Weekly", "Monthly", "Yearly")
                        currentSelectedValue = defaultRecurrence
                        onOptionSelected = { viewModel.updateBillDefaultRecurrence(it) }
                    }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 2. Recurring End
                BillsSettingItemRow(
                    icon = Icons.Outlined.CalendarMonth,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Recurring End",
                    subtitle = "When should recurring bills stop",
                    valueText = recurringEnd,
                    onClick = {
                        activePickerTitle = "Recurring End"
                        pickerOptions = listOf("Never", "After 6 Months", "After 12 Months", "After 24 Months")
                        currentSelectedValue = recurringEnd
                        onOptionSelected = { viewModel.updateBillRecurringEnd(it) }
                    }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 3. Default Bill Category
                BillsSettingItemRow(
                    icon = Icons.Outlined.Sell,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Default Bill Category",
                    subtitle = "Category selected for new bills",
                    valueText = defaultCategory,
                    onClick = {
                        activePickerTitle = "Default Bill Category"
                        pickerOptions = listOf("Utilities", "Rent", "Subscriptions", "Credit Card", "Insurance", "Education", "Other")
                        currentSelectedValue = defaultCategory
                        onOptionSelected = { viewModel.updateBillDefaultCategory(it) }
                    }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 4. Mark as Paid Automatically
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.TaskAlt,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Mark as Paid Automatically",
                    subtitle = "Auto mark recurring bills as paid on due date",
                    checked = autoMarkPaid,
                    onCheckedChange = { viewModel.toggleBillAutoMarkPaid(it) }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 5. Archive Paid Bills
                BillsSettingItemRow(
                    icon = Icons.Outlined.Archive,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626),
                    title = "Archive Paid Bills",
                    subtitle = "Move paid bills to archive after",
                    valueText = archiveDays,
                    onClick = {
                        activePickerTitle = "Archive Paid Bills"
                        pickerOptions = listOf("Immediately", "7 Days", "30 Days", "60 Days", "90 Days", "Never")
                        currentSelectedValue = archiveDays
                        onOptionSelected = { viewModel.updateBillArchiveDays(it) }
                    }
                )
            }

            // ================= SECTION 2: Upcoming Bills =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.FormatListBulleted,
                sectionIconBg = Color(0xFFE0F2FE),
                sectionIconTint = Color(0xFF0284C7),
                title = "Upcoming Bills",
                subtitle = "Control how upcoming bills are shown"
            ) {
                // 1. Show Upcoming Bills on Dashboard
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.GridView,
                    iconBg = Color(0xFFFFEDD5),
                    iconTint = Color(0xFFEA580C),
                    title = "Show Upcoming Bills on Dashboard",
                    subtitle = "Display upcoming bills on home dashboard",
                    checked = showUpcomingDashboard,
                    onCheckedChange = { viewModel.toggleBillShowUpcomingDashboard(it) }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 2. Upcoming Days to Show
                BillsSettingItemRow(
                    icon = Icons.Outlined.DateRange,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Upcoming Days to Show",
                    subtitle = "Number of days to show upcoming bills",
                    valueText = upcomingDays,
                    onClick = {
                        activePickerTitle = "Upcoming Days to Show"
                        pickerOptions = listOf("3 Days", "7 Days", "14 Days", "30 Days")
                        currentSelectedValue = upcomingDays
                        onOptionSelected = { viewModel.updateBillUpcomingDays(it) }
                    }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 3. Sort Upcoming Bills By
                BillsSettingItemRow(
                    icon = Icons.Outlined.SwapVert,
                    iconBg = Color(0xFFEBF5FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Sort Upcoming Bills By",
                    subtitle = "Choose the default sorting order",
                    valueText = sortOrder,
                    onClick = {
                        activePickerTitle = "Sort Upcoming Bills By"
                        pickerOptions = listOf("Due Date (Nearest)", "Amount (High to Low)", "Amount (Low to High)", "Bill Name")
                        currentSelectedValue = sortOrder
                        onOptionSelected = { viewModel.updateBillSortOrder(it) }
                    }
                )
            }

            // ================= SECTION 3: Bill Management =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.SnippetFolder,
                sectionIconBg = Color(0xFFDCFCE7),
                sectionIconTint = Color(0xFF16A34A),
                title = "Bill Management",
                subtitle = "Manage how bills are organized and displayed"
            ) {
                // 1. Default Filter
                BillsSettingItemRow(
                    icon = Icons.Outlined.FilterList,
                    iconBg = Color(0xFFCFFAFE),
                    iconTint = Color(0xFF0891B2),
                    title = "Default Filter",
                    subtitle = "Default filter when opening bills",
                    valueText = defaultFilter,
                    onClick = {
                        activePickerTitle = "Default Filter"
                        pickerOptions = listOf("All Bills", "Unpaid", "Paid", "Overdue")
                        currentSelectedValue = defaultFilter
                        onOptionSelected = { viewModel.updateBillDefaultFilter(it) }
                    }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 2. Overdue Bills
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.AccessTime,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626),
                    title = "Overdue Bills",
                    subtitle = "Move overdue bills to top",
                    checked = overdueAlert,
                    onCheckedChange = { viewModel.toggleBillOverdueAlert(it) }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // 3. Show Notes on Bill List
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.StickyNote2,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Show Notes on Bill List",
                    subtitle = "Display bill notes in the list",
                    checked = showNotes,
                    onCheckedChange = { viewModel.toggleBillShowNotes(it) }
                )
            }

            // ================= SECTION 4: About Bills & Reminders =================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FE)),
                border = BorderStroke(1.dp, Color(0xFFE2E4FA)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color(0xFF4F46E5)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "i",
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "About Bills & Reminders",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3544C4),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "These settings only affect how bills are managed and displayed in the app.\nNo alerts, notifications or external reminders are sent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4B5563),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal picker dialog when an option row is clicked
    if (activePickerTitle != null) {
        AlertDialog(
            onDismissRequest = { activePickerTitle = null },
            title = {
                Text(
                    activePickerTitle ?: "",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column {
                    pickerOptions.forEach { opt ->
                        val selected = opt == currentSelectedValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected?.invoke(opt)
                                    activePickerTitle = null
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                opt,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) SleekPrimary else SleekTextPrimary
                            )
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SleekPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activePickerTitle = null }) {
                    Text("Cancel", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun BillsSettingSectionCard(
    sectionIcon: ImageVector,
    sectionIconBg: Color,
    sectionIconTint: Color,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = sectionIconBg,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = sectionIcon,
                            contentDescription = null,
                            tint = sectionIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

            content()
        }
    }
}

@Composable
private fun BillsSettingItemRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SleekPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SleekPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BillsSettingSwitchRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekPrimary
            )
        )
    }
}

// ==========================================
// 6️⃣ CATEGORIES & TAGS SCREEN
// ==========================================
@Composable
fun CategoriesTagsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val goalCategories by viewModel.goalCategories.collectAsStateWithLifecycle()
    val budgetCategories by viewModel.budgetCategories.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    val preventDeleteUsed by viewModel.preventDeleteUsedCategories.collectAsStateWithLifecycle()
    val showCategoryInList by viewModel.showCategoryInTransactionList.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Dialog state variables
    var activeCategoryTypeDialog by remember { mutableStateOf<String?>(null) } // "Expense", "Income", "Savings", "Budget"
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoriesDialog by remember { mutableStateOf(false) }
    var showDeleteCategoriesDialog by remember { mutableStateOf(false) }
    var showReorderCategoriesDialog by remember { mutableStateOf(false) }
    var showCategoryColorsDialog by remember { mutableStateOf(false) }

    var showCreateTagDialog by remember { mutableStateOf(false) }
    var showEditTagsDialog by remember { mutableStateOf(false) }
    var showDeleteTagsDialog by remember { mutableStateOf(false) }
    var showReorderTagsDialog by remember { mutableStateOf(false) }
    var showTagColorsDialog by remember { mutableStateOf(false) }

    var showInfoNoteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Custom Header matching reference image
        Surface(
            color = SleekSurface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Categories & Tags",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Manage your categories and tags for better organization",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { showInfoNoteDialog = true }) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color(0xFF4F46E5)),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "i",
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= SECTION 1: Categories =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.AccountBalanceWallet,
                sectionIconBg = Color(0xFFEFF0FE),
                sectionIconTint = Color(0xFF4F46E5),
                title = "Categories",
                subtitle = "Create, edit and manage your income & expense categories"
            ) {
                // Top Category Pills/Cards (Grid style: Expense, Income, Savings, Budget)
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Expense Categories Card
                        CategoryPillCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ArrowDownward,
                            iconBg = Color(0xFFDCFCE7),
                            iconTint = Color(0xFF16A34A),
                            title = "Expense Categories",
                            subtitle = "Manage expense categories",
                            count = expenseCategories.size,
                            onClick = { activeCategoryTypeDialog = "Expense" }
                        )

                        // Income Categories Card
                        CategoryPillCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ArrowUpward,
                            iconBg = Color(0xFFDBEAFE),
                            iconTint = Color(0xFF2563EB),
                            title = "Income Categories",
                            subtitle = "Manage income categories",
                            count = incomeCategories.size,
                            onClick = { activeCategoryTypeDialog = "Income" }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Savings Categories Card
                        CategoryPillCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Savings,
                            iconBg = Color(0xFFFFEDD5),
                            iconTint = Color(0xFFEA580C),
                            title = "Savings Categories",
                            subtitle = "Manage savings goals",
                            count = goalCategories.size,
                            onClick = { activeCategoryTypeDialog = "Savings" }
                        )

                        // Budget Categories Card
                        CategoryPillCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.PieChart,
                            iconBg = Color(0xFFF3E8FF),
                            iconTint = Color(0xFF9333EA),
                            title = "Budget Categories",
                            subtitle = "Manage budget limits",
                            count = budgetCategories.size,
                            onClick = { activeCategoryTypeDialog = "Budget" }
                        )
                    }
                }

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Create Category Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Add,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Create Category",
                    subtitle = "Add a new category",
                    valueText = "",
                    onClick = { showCreateCategoryDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Edit Categories Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Edit,
                    iconBg = Color(0xFFFFEDD5),
                    iconTint = Color(0xFFEA580C),
                    title = "Edit Categories",
                    subtitle = "Modify or rename existing categories",
                    valueText = "",
                    onClick = { showEditCategoriesDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Delete Categories Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Delete,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626),
                    title = "Delete Categories",
                    subtitle = "Delete categories you no longer use",
                    valueText = "",
                    onClick = { showDeleteCategoriesDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Reorder Categories Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.SwapVert,
                    iconBg = Color(0xFFEBF5FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Reorder Categories",
                    subtitle = "Change the order of categories",
                    valueText = "",
                    onClick = { showReorderCategoriesDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Category Colors Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Palette,
                    iconBg = Color(0xFFCFFAFE),
                    iconTint = Color(0xFF0891B2),
                    title = "Category Colors",
                    subtitle = "Customize colors for your categories",
                    valueText = "",
                    onClick = { showCategoryColorsDialog = true }
                )
            }

            // ================= SECTION 2: Tags =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.Sell,
                sectionIconBg = Color(0xFFEFF0FE),
                sectionIconTint = Color(0xFF4F46E5),
                title = "Tags",
                subtitle = "Create, edit and manage tags to label your transactions"
            ) {
                // Create Tag Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Add,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Create Tag",
                    subtitle = "Add a new tag",
                    valueText = "",
                    onClick = { showCreateTagDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Edit Tags Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Edit,
                    iconBg = Color(0xFFFFEDD5),
                    iconTint = Color(0xFFEA580C),
                    title = "Edit Tags",
                    subtitle = "Modify or rename existing tags",
                    valueText = "",
                    onClick = { showEditTagsDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Delete Tags Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Delete,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFDC2626),
                    title = "Delete Tags",
                    subtitle = "Delete tags you no longer use",
                    valueText = "",
                    onClick = { showDeleteTagsDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Reorder Tags Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.SwapVert,
                    iconBg = Color(0xFFEBF5FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Reorder Tags",
                    subtitle = "Change the order of tags",
                    valueText = "",
                    onClick = { showReorderTagsDialog = true }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Tag Colors Row
                BillsSettingItemRow(
                    icon = Icons.Outlined.Palette,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    title = "Tag Colors",
                    subtitle = "Customize colors for your tags",
                    valueText = "",
                    onClick = { showTagColorsDialog = true }
                )
            }

            // ================= SECTION 3: General Preferences =================
            BillsSettingSectionCard(
                sectionIcon = Icons.Outlined.Settings,
                sectionIconBg = Color(0xFFEFF0FE),
                sectionIconTint = Color(0xFF4F46E5),
                title = "General Preferences",
                subtitle = "Configure how categories & tags behave in the app"
            ) {
                // Prevent Deleting Used Categories
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.Link,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Prevent Deleting Used Categories",
                    subtitle = "Protect categories that are used in transactions",
                    checked = preventDeleteUsed,
                    onCheckedChange = { viewModel.togglePreventDeleteUsedCategories(it) }
                )

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                // Show Category in Transaction List
                BillsSettingSwitchRow(
                    icon = Icons.Outlined.Sell,
                    iconBg = Color(0xFFEBF5FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Show Category in Transaction List",
                    subtitle = "Display category name in transaction list",
                    checked = showCategoryInList,
                    onCheckedChange = { viewModel.toggleShowCategoryInTransactionList(it) }
                )
            }

            // ================= SECTION 4: Note / Info Banner =================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FE)),
                border = BorderStroke(1.dp, Color(0xFFE2E4FA)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInfoNoteDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color(0xFF4F46E5)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "i",
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Note",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3544C4),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Deleting a category will not delete your transactions. Transactions will be moved to \"Uncategorized\".",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4B5563),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // ==========================================
    // INTERACTIVE DIALOGS FOR CATEGORIES & TAGS
    // ==========================================

    // 1. Manage specific Category Type Dialog
    val activeType = activeCategoryTypeDialog
    if (activeType != null) {
        val type = activeType
        val categoryList = when (type) {
            "Expense" -> expenseCategories
            "Income" -> incomeCategories
            "Savings" -> goalCategories
            "Budget" -> budgetCategories
            else -> expenseCategories
        }
        var newCatInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeCategoryTypeDialog = null },
            title = {
                Text(
                    "$type Categories",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCatInput,
                            onValueChange = { newCatInput = it },
                            placeholder = { Text("New $type category name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCatInput.isNotBlank()) {
                                    viewModel.addCategoryWithType(newCatInput, type)
                                    newCatInput = ""
                                    Toast.makeText(context, "Added to $type categories!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                        ) {
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryList.forEach { cat ->
                            key(cat) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SleekBg, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        cat,
                                        fontWeight = FontWeight.Medium,
                                        color = SleekTextPrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteAnyCategory(cat)
                                            Toast.makeText(context, "Deleted '$cat'", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeCategoryTypeDialog = null }) {
                    Text("Done", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Create Category Dialog
    if (showCreateCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("Expense") }

        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = {
                Text("Create Category", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category Name", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        placeholder = { Text("e.g. Groceries, Gym, Freelance") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category Type", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Expense", "Income", "Savings", "Budget").forEach { t ->
                            val selected = t == selectedType
                            FilterChip(
                                selected = selected,
                                onClick = { selectedType = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.addCategoryWithType(catName, selectedType)
                            showCreateCategoryDialog = false
                            Toast.makeText(context, "Created '$catName' category!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCategoryDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 3. Edit Categories Dialog
    if (showEditCategoriesDialog) {
        var editingCat by remember { mutableStateOf<String?>(null) }
        var newCatName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditCategoriesDialog = false },
            title = {
                Text("Edit Categories", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                if (editingCat == null) {
                    val allCats = (expenseCategories + incomeCategories + goalCategories + budgetCategories).distinct()
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCats.size, key = { allCats[it] }) { idx ->
                            val cat = allCats[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingCat = cat
                                        newCatName = cat
                                    }
                                    .background(SleekBg, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat, fontWeight = FontWeight.Medium, color = SleekTextPrimary)
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Rename '${editingCat}'", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { editingCat = null }) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    val cat = editingCat
                                    if (newCatName.isNotBlank() && cat != null) {
                                        viewModel.renameAnyCategory(cat, newCatName)
                                        editingCat = null
                                        Toast.makeText(context, "Renamed category!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditCategoriesDialog = false }) {
                    Text("Close", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 4. Delete Categories Dialog
    if (showDeleteCategoriesDialog) {
        val allCats = (expenseCategories + incomeCategories + goalCategories + budgetCategories).distinct()

        AlertDialog(
            onDismissRequest = { showDeleteCategoriesDialog = false },
            title = {
                Text("Delete Categories", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Column {
                    Text(
                        "Every category is deletable. Transactions in deleted categories will be reassigned to Uncategorized.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCats.size, key = { allCats[it] }) { idx ->
                            val cat = allCats[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SleekBg, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat, fontWeight = FontWeight.Medium, color = SleekTextPrimary)
                                IconButton(
                                    onClick = {
                                        viewModel.deleteAnyCategory(cat)
                                        Toast.makeText(context, "Deleted '$cat'", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeleteCategoriesDialog = false }) {
                    Text("Done", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 5. Reorder Categories Dialog
    if (showReorderCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showReorderCategoriesDialog = false },
            title = {
                Text("Reorder Categories", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Categories are automatically sorted alphabetically and organized by type for quick access throughout transaction entry and budgeting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showReorderCategoriesDialog = false }) {
                    Text("OK", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 6. Category Colors Dialog
    if (showCategoryColorsDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryColorsDialog = false },
            title = {
                Text("Category Colors", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Category colors are automatically assigned vibrant, distinct palette colors across all charts, pie graphs, and transaction cards for optimal visual scanning.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showCategoryColorsDialog = false }) {
                    Text("OK", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 7. Create Tag Dialog
    if (showCreateTagDialog) {
        var tagNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateTagDialog = false },
            title = {
                Text("Create Tag", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tag Name", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                    OutlinedTextField(
                        value = tagNameInput,
                        onValueChange = { tagNameInput = it },
                        placeholder = { Text("e.g. TaxDeductible, Urgent, Vacation") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagNameInput.isNotBlank()) {
                            viewModel.addTag(tagNameInput)
                            showCreateTagDialog = false
                            Toast.makeText(context, "Created tag '$tagNameInput'", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTagDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 8. Edit Tags Dialog
    if (showEditTagsDialog) {
        var editingTag by remember { mutableStateOf<String?>(null) }
        var newTagName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditTagsDialog = false },
            title = {
                Text("Edit Tags", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                if (editingTag == null) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allTags.size, key = { allTags[it] }) { idx ->
                            val tag = allTags[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingTag = tag
                                        newTagName = tag
                                    }
                                    .background(SleekBg, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tag, fontWeight = FontWeight.Medium, color = SleekTextPrimary)
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Rename '${editingTag}'", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        OutlinedTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { editingTag = null }) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    val tag = editingTag
                                    if (newTagName.isNotBlank() && tag != null) {
                                        viewModel.renameTag(tag, newTagName)
                                        editingTag = null
                                        Toast.makeText(context, "Renamed tag!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditTagsDialog = false }) {
                    Text("Close", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 9. Delete Tags Dialog
    if (showDeleteTagsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTagsDialog = false },
            title = {
                Text("Delete Tags", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allTags.size, key = { allTags[it] }) { idx ->
                        val tag = allTags[idx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SleekBg, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag, fontWeight = FontWeight.Medium, color = SleekTextPrimary)
                            IconButton(
                                onClick = {
                                    viewModel.deleteTag(tag)
                                    Toast.makeText(context, "Deleted tag '$tag'", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeleteTagsDialog = false }) {
                    Text("Done", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 10. Reorder Tags Dialog
    if (showReorderTagsDialog) {
        AlertDialog(
            onDismissRequest = { showReorderTagsDialog = false },
            title = {
                Text("Reorder Tags", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Tags are sorted alphabetically to help you search and organize transaction labels quickly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showReorderTagsDialog = false }) {
                    Text("OK", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 11. Tag Colors Dialog
    if (showTagColorsDialog) {
        AlertDialog(
            onDismissRequest = { showTagColorsDialog = false },
            title = {
                Text("Tag Colors", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Tags are automatically styled with subtle primary theme accents and badges across transaction notes and filter lists.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showTagColorsDialog = false }) {
                    Text("OK", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 12. Info / Note Dialog
    if (showInfoNoteDialog) {
        AlertDialog(
            onDismissRequest = { showInfoNoteDialog = false },
            title = {
                Text("Categories & Tags Info", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Deleting a category will not delete your existing transactions or budgets. All associated transactions will safely remain intact and be moved to \"Uncategorized\".\n\nAll categories—including system defaults, expense, income, savings, and budget categories—are completely editable and deletable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextPrimary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoNoteDialog = false }) {
                    Text("Got it", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CategoryPillCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekBg),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SleekPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 12.sp,
                softWrap = true
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary,
                fontSize = 10.sp,
                softWrap = true,
                lineHeight = 13.sp
            )
        }
    }
}

// ==========================================
// 7️⃣ BUDGET SETTINGS SCREEN
// ==========================================
@Composable
fun BudgetSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val currencyCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val includeRecurringBills by viewModel.budgetIncludeRecurringBills.collectAsStateWithLifecycle()

    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()

    val warn80 by viewModel.budgetWarning80.collectAsStateWithLifecycle()
    val warn90 by viewModel.budgetWarning90.collectAsStateWithLifecycle()
    val warn100 by viewModel.budgetWarning100.collectAsStateWithLifecycle()

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var editBudgetInput by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showColorPaletteDialog by remember { mutableStateOf(false) }

    var showCategoryBudgetDialog by remember { mutableStateOf(false) }
    var targetCategoryForBudget by remember { mutableStateOf("Food") }
    var categoryLimitInput by remember { mutableStateOf("") }

    val masterWarnings = warn80 || warn90 || warn100

    val currentMonthCategorySpent = remember(allExpenses, currencyCode) {
        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        allExpenses.filter {
            it.type != "INCOME" &&
            Calendar.getInstance().apply { timeInMillis = it.date }.run {
                get(Calendar.MONTH) == m && get(Calendar.YEAR) == y
            }
        }.groupBy { it.category }
            .mapValues { entry -> entry.value.realExpense(currencyCode) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header matching reference layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(SleekSurface, CircleShape)
                    .border(1.dp, SleekBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SleekTextPrimary
                )
            }

            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(SleekSurface, CircleShape)
                    .border(1.dp, SleekBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = SleekTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Screen Header Title & Subtitle
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 28.sp
            )
            Text(
                text = "Manage your monthly spending limits & warnings.",
                style = MaterialTheme.typography.bodyMedium,
                color = SleekTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. GENERAL SETTINGS CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // General Settings Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SleekPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "General Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 16.sp
                        )
                    }

                    // Monthly Budget Limit Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editBudgetInput = if (monthlyBudget % 1.0 == 0.0) {
                                    monthlyBudget.toLong().toString()
                                } else {
                                    monthlyBudget.toString()
                                }
                                showEditBudgetDialog = true
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Monthly Budget Limit",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Configure your total monthly spending ceiling",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$currencySymbol${String.format(java.util.Locale.US, "%,.2f", monthlyBudget)}",
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))

                    // Currency Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Currency",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Current budget currency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "$currencyCode ($currencySymbol)",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 13.sp
                        )
                    }

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))

                    // Recurring Bills Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Recurring Bills",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Include recurring bills in budget tracking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = includeRecurringBills,
                            onCheckedChange = { viewModel.toggleBudgetIncludeRecurringBills(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SleekPrimary,
                                uncheckedThumbColor = SleekTextSecondary,
                                uncheckedTrackColor = SleekBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. BUDGET WARNINGS CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Budget Warnings Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Budget Warnings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 16.sp
                        )
                    }

                    // Warning Thresholds Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReportProblem,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Warning Thresholds",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Manage cautionary spend notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = masterWarnings,
                                onCheckedChange = { enable ->
                                    viewModel.toggleBudgetWarning(80, enable)
                                    viewModel.toggleBudgetWarning(90, enable)
                                    viewModel.toggleBudgetWarning(100, enable)
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))

                    // 80% Caution Indicator Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "80% Caution Indicator",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Warn when spending reaches 80%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = warn80,
                                onCheckedChange = { viewModel.toggleBudgetWarning(80, it) }
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))

                    // 90% High Alert Indicator Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF97316).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "90% High Alert Indicator",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Warn when spending reaches 90%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = warn90,
                                onCheckedChange = { viewModel.toggleBudgetWarning(90, it) }
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))

                    // Exceeded Indicator Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDC2626).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Exceeded Indicator",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Alert when over budget",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = warn100,
                                onCheckedChange = { viewModel.toggleBudgetWarning(100, it) }
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2.5 CATEGORY SPENDING LIMITS CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Category Spending Limits",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Set individual monthly limits per category",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                targetCategoryForBudget = "Food"
                                categoryLimitInput = ""
                                showCategoryBudgetDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category Budget", tint = SleekPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (budgets.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SleekPrimaryContainer.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, SleekBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("No category budgets set", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextPrimary)
                                    Text("Tap '+' above to set a spending limit for Food, Transport, Shopping, etc.", fontSize = 11.sp, color = SleekTextSecondary)
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (b in budgets) {
                                val spent = currentMonthCategorySpent[b.category] ?: 0.0
                                val limit = b.amountLimit.coerceAtLeast(1.0)
                                val progress = (spent / limit).coerceIn(0.0, 1.2).toFloat()
                                val isOver = spent > limit
                                val isNear = progress >= 0.8f && !isOver

                                val barColor = when {
                                    isOver -> Color(0xFFEF4444)
                                    isNear -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SleekPrimaryContainer.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, SleekBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(b.category, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextPrimary)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = barColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (isOver) "Exceeded" else if (isNear) "Near Limit" else "On Track",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = barColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        targetCategoryForBudget = b.category
                                                        categoryLimitInput = if (b.amountLimit % 1.0 == 0.0) b.amountLimit.toLong().toString() else b.amountLimit.toString()
                                                        showCategoryBudgetDialog = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteCategoryBudget(b.category) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Progress Bar
                                        LinearProgressIndicator(
                                            progress = { (progress / 1.0f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = barColor,
                                            trackColor = SleekBorder.copy(alpha = 0.6f)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Spent: $currencySymbol${String.format(Locale.US, "%,.2f", spent)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SleekTextSecondary
                                            )
                                            Text(
                                                text = "Limit: $currencySymbol${String.format(Locale.US, "%,.2f", b.amountLimit)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. APPEARANCE CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Appearance Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF06B6D4).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 16.sp
                        )
                    }

                    // Indicator Colors Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showColorPaletteDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF14B8A6).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = Color(0xFF14B8A6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Indicator Colors",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Customize warning colors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color Spectrum Bar Matching Reference
                    val rainbowBrush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFEF4444),
                            Color(0xFFF97316),
                            Color(0xFFF59E0B),
                            Color(0xFFEAB308),
                            Color(0xFF84CC16),
                            Color(0xFF10B981),
                            Color(0xFF06B6D4),
                            Color(0xFF3B82F6),
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFFEF4444)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(rainbowBrush)
                    ) {
                        // Pointer diamond accent matching image
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .size(10.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. NOTE / INFO BANNER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekPrimary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Note:",
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Use existing budget logic. Warning thresholds are based on your transactions and budget limits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // EDIT BUDGET DIALOG
    if (showEditBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = {
                Text(
                    text = "Monthly Budget Limit",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set your total monthly spending ceiling ($currencyCode):",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editBudgetInput,
                        onValueChange = { editBudgetInput = it },
                        prefix = { Text("$currencySymbol ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = editBudgetInput.toDoubleOrNull()
                        if (num != null && num > 0) {
                            viewModel.updateMonthlyBudget(num)
                        }
                        showEditBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // INFO DIALOG
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text("About Budgets & Warnings", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "Budgets help you keep track of your overall monthly spending. Your spending progress is updated in real-time as transactions are recorded. Warning thresholds automatically alert you when you reach 80%, 90%, or exceed 100% of your ceiling.",
                    fontSize = 13.sp,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Got It")
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // INDICATOR COLOR CUSTOMIZER DIALOG
    if (showColorPaletteDialog) {
        AlertDialog(
            onDismissRequest = { showColorPaletteDialog = false },
            title = {
                Text("Indicator Colors", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Warning indicator color palette is synced with your active theme system accent palette.", fontSize = 12.sp, color = SleekTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF6366F1)).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(1.5.dp, SleekBorder, CircleShape)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showColorPaletteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Done")
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // CATEGORY BUDGET DIALOG
    if (showCategoryBudgetDialog) {
        val expenseCatList = listOf("Food", "Shopping", "Entertainment", "Transport", "Bills", "Utilities", "Housing", "Health", "Education", "Other")

        AlertDialog(
            onDismissRequest = { showCategoryBudgetDialog = false },
            title = {
                Text(
                    text = "Set Category Spending Limit",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose a category and enter your monthly spending limit:",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        expenseCatList.forEach { cat ->
                            val selected = targetCategoryForBudget.equals(cat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) SleekPrimary else SleekSurface,
                                border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                                modifier = Modifier.clickable {
                                    targetCategoryForBudget = cat
                                }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else SleekTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = categoryLimitInput,
                        onValueChange = { categoryLimitInput = it },
                        label = { Text("Limit Amount ($currencySymbol)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = categoryLimitInput.toDoubleOrNull()
                        if (limitVal != null && limitVal > 0 && targetCategoryForBudget.isNotBlank()) {
                            viewModel.setCategoryBudget(targetCategoryForBudget, limitVal)
                            showCategoryBudgetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Limit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryBudgetDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==========================================
// 1️⃣1️⃣ BACKUP & RESTORE SCREEN (WITH PORTABLE CODE SYNC)
// ==========================================
@Composable
fun BackupRestoreScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val autoBackup by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val lastBackup by viewModel.lastBackupTimestamp.collectAsStateWithLifecycle()
    val syncMode by viewModel.syncMode.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val lastCloudSync by viewModel.lastCloudSyncTimestamp.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var generatedCode by remember { mutableStateOf<String?>(null) }
    var inputCode by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showCloudRestoreConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Backup & Restore", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 🛡️ SECTION 0: STORAGE & SYNC ENGINE MODE SELECTOR
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                    colors = if (syncMode == "ONLINE_SYNC") {
                        listOf(Color(0xFF0284C7).copy(alpha = 0.8f), Color(0xFF38BDF8).copy(alpha = 0.8f))
                    } else {
                        listOf(Color(0xFF10B981).copy(alpha = 0.8f), Color(0xFF34D399).copy(alpha = 0.8f))
                    }
                )),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7).copy(alpha = 0.15f)
                                    else Color(0xFF10B981).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (syncMode == "ONLINE_SYNC") Icons.Default.CloudSync else Icons.Default.Security,
                                contentDescription = "Storage Mode",
                                tint = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7) else Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Storage & Sync Destination",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (syncMode == "ONLINE_SYNC") "Cloud Sync Enabled (Firebase Vault)" else "Local Only Storage (100% Offline & Private)",
                                fontSize = 12.sp,
                                color = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7) else Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Choose whether financial records stay exclusively on this device or synchronize to your secure cloud database:",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 🔲 OPTION A: LOCAL ONLY
                    Surface(
                        onClick = {
                            viewModel.setSyncMode("LOCAL_ONLY")
                            Toast.makeText(context, "Switched to Local Only Mode (Offline)", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (syncMode == "LOCAL_ONLY") Color(0xFF10B981).copy(alpha = 0.12f) else SleekSurfaceElevated,
                        border = BorderStroke(
                            width = if (syncMode == "LOCAL_ONLY") 1.8.dp else 1.dp,
                            color = if (syncMode == "LOCAL_ONLY") Color(0xFF10B981) else SleekBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = syncMode == "LOCAL_ONLY",
                                onClick = {
                                    viewModel.setSyncMode("LOCAL_ONLY")
                                    Toast.makeText(context, "Switched to Local Only Mode", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Local Storage Only",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SleekTextPrimary
                                    )
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "100% PRIVATE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "All accounts, transactions and budgets stay in SQLite Room on device. Zero external data transmission.",
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ☁️ OPTION B: ONLINE CLOUD SYNC
                    Surface(
                        onClick = {
                            viewModel.setSyncMode("ONLINE_SYNC")
                            Toast.makeText(context, "Switched to Online Cloud Sync Mode", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7).copy(alpha = 0.12f) else SleekSurfaceElevated,
                        border = BorderStroke(
                            width = if (syncMode == "ONLINE_SYNC") 1.8.dp else 1.dp,
                            color = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7) else SleekBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = syncMode == "ONLINE_SYNC",
                                onClick = {
                                    viewModel.setSyncMode("ONLINE_SYNC")
                                    Toast.makeText(context, "Switched to Online Cloud Sync Mode", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Online Cloud Sync",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SleekTextPrimary
                                    )
                                    Surface(
                                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "FIREBASE VAULT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0284C7),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Securely backup and synchronize accounts, transactions, budgets & goals to Cloud Firestore.",
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🚀 ACTIVE MODE CONTROLS & PROPER BUTTONS
                    if (syncMode == "ONLINE_SYNC") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SleekSurfaceElevated)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Cloud Sync Status",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "State: $cloudSyncStatus • Last synced: $lastCloudSync",
                                        fontSize = 11.sp,
                                        color = SleekTextSecondary
                                    )
                                }
                                if (isCloudSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 🔘 PROPER BUTTON: SYNC NOW
                            Button(
                                onClick = {
                                    viewModel.performCloudSync { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isCloudSyncing,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (isCloudSyncing) Icons.Default.Sync else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isCloudSyncing) "Syncing in Progress..." else "Sync Now to Cloud",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 🔘 PROPER BUTTON: RESTORE FROM CLOUD
                            OutlinedButton(
                                onClick = { showCloudRestoreConfirmDialog = true },
                                enabled = !isCloudSyncing,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Restore Latest From Cloud Vault",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // LOCAL ONLY HELPER BANNER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Local Mode Active: Zero network traffic. Your finances are strictly offline and encrypted on this device.",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            // 📦 SECTION 1: EXPORT BACKUP CODE CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                    colors = listOf(
                        SleekPrimary.copy(alpha = 0.6f),
                        Color(0xFF8B5CF6).copy(alpha = 0.6f)
                    )
                )),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Backup Code",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Full Portable Data Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Export 100% of your transactions, profile & settings into text code",
                                fontSize = 12.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedCode == null) {
                        Button(
                            onClick = {
                                isExporting = true
                                scope.launch {
                                    val code = viewModel.generateFullBackupCode()
                                    generatedCode = code
                                    isExporting = false
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating Encrypted Code...")
                            } else {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate My Backup Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        val code = generatedCode ?: ""
                        // Display Generated Code Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SleekBg)
                                .border(1.dp, SleekBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = code,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SleekTextPrimary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "Backup code copied to clipboard!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isExporting = true
                                    scope.launch {
                                        val code = viewModel.generateFullBackupCode()
                                        generatedCode = code
                                        isExporting = false
                                        Toast.makeText(context, "Fresh backup code regenerated!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Ready to paste! Includes Profile, 2-Year Transactions, Calendar, Savings & Settings.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📥 SECTION 2: IMPORT / RESTORE FROM CODE
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Restore Code",
                                tint = SleekPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Restore Data From Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Paste your backup code string below to restore all details",
                                fontSize = 12.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        placeholder = { Text("Paste AISTUDIO_BACKUP_V1:... code string here", fontSize = 12.sp, color = SleekTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = SleekBg,
                            unfocusedContainerColor = SleekBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    inputCode = clip
                                    Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste")
                        }

                        Button(
                            onClick = {
                                if (inputCode.isBlank()) {
                                    Toast.makeText(context, "Please paste a backup code first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    showRestoreConfirmDialog = true
                                }
                            },
                            enabled = inputCode.isNotBlank() && !isRestoring,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restoring...")
                            } else {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Data Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 💾 SECTION 3: LOCAL OFFLINE MANUAL BACKUP
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL STORAGE BACKUP", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Your database is also snapshot-backed on device storage.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Last Local Snapshot: $lastBackup", style = MaterialTheme.typography.bodySmall, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.markBackupPerformed()
                            Toast.makeText(context, "Local backup snapshot updated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Manual Local Snapshot")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Automatic Daily Sync Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Daily Snapshot", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text("Saves local database snapshots daily without cloud dependency", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    }
                    Switch(checked = autoBackup, onCheckedChange = { viewModel.toggleAutoBackupEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(150.dp))
        }
    }

    // 🚨 RESTORE CONFIRMATION DIALOG
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restore From Backup Code?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This action will import all 2-year transactions, calendar logs, savings goals, budgets, profile details, and settings from the backup code. Existing database records will be synced and updated. Proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        isRestoring = true
                        scope.launch {
                            val res = viewModel.restoreFromBackupCode(inputCode)
                            isRestoring = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "🎉 ${res.getOrNull()}", Toast.LENGTH_LONG).show()
                                inputCode = ""
                            } else {
                                Toast.makeText(context, "❌ Error: ${res.exceptionOrNull()?.localizedMessage ?: "Invalid code format"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Yes, Restore Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ☁️ CLOUD RESTORE CONFIRMATION DIALOG
    if (showCloudRestoreConfirmDialog) {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val isUserLoggedIn = authUser != null
        AlertDialog(
            onDismissRequest = { showCloudRestoreConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF0284C7))
                    Text("Restore from Cloud Vault?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (!isUserLoggedIn) {
                    Text("You must be signed in to access and restore your cloud vault. Please sign in via the Account tab first.")
                } else {
                    Text("This will download your latest cloud backup and restore your accounts, transactions, budgets, and savings goals into the local database. An emergency safety backup of your current local database will be created automatically before restoring. Proceed?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloudRestoreConfirmDialog = false
                        viewModel.restoreFromCloud { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = isUserLoggedIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Restore from Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 🚚 Delivery Truck Loading Dialog for Export & Restore Operations
    TruckLoadingDialog(
        isOpen = isExporting,
        message = "Generating Encrypted Backup Code...",
        subtitle = "Packing transactions, budgets & settings securely"
    )

    TruckLoadingDialog(
        isOpen = isRestoring,
        message = "Restoring Financial Records...",
        subtitle = "Decrypting vault and syncing local database"
    )
}

// ==========================================
// 1️⃣2️⃣ DATA MANAGEMENT SCREEN
// ==========================================
@Composable
fun DataManagementScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToLocalRecovery: () -> Unit = {}
) {
    val storageSize by viewModel.storageSize.collectAsStateWithLifecycle()
    val syncMode by viewModel.syncMode.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val lastCloudSync by viewModel.lastCloudSyncTimestamp.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Data Management", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL RECOVERY SYSTEM", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Versioned rolling snapshots with SHA-256 integrity checks and automated rollback protection.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToLocalRecovery,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Local Recovery", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 💾 STORAGE & SYNC ENGINE CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (syncMode == "ONLINE_SYNC") Icons.Default.CloudSync else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7) else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Text("STORAGE & SYNC DESTINATION", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        }
                        Surface(
                            color = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (syncMode == "ONLINE_SYNC") "CLOUD SYNC" else "LOCAL ONLY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (syncMode == "ONLINE_SYNC") Color(0xFF0284C7) else Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (syncMode == "ONLINE_SYNC") {
                            "Encrypted cloud synchronization active with Firebase Vault. Status: $cloudSyncStatus • Last synced: $lastCloudSync"
                        } else {
                            "100% Offline & Private: Records stay solely in on-device SQLite database. No data is sent to external servers."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val target = if (syncMode == "ONLINE_SYNC") "LOCAL_ONLY" else "ONLINE_SYNC"
                                viewModel.setSyncMode(target)
                                Toast.makeText(
                                    context,
                                    if (target == "ONLINE_SYNC") "Switched to Online Cloud Sync" else "Switched to Local Storage Only",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (syncMode == "ONLINE_SYNC") Icons.Default.Security else Icons.Default.CloudSync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (syncMode == "ONLINE_SYNC") "Use Local Only" else "Enable Cloud Sync", fontSize = 12.sp)
                        }

                        if (syncMode == "ONLINE_SYNC") {
                            Button(
                                onClick = {
                                    viewModel.performCloudSync { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isCloudSyncing,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                if (isCloudSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCloudSyncing) "Syncing..." else "Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL STORAGE USAGE", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Total database size: $storageSize", style = MaterialTheme.typography.bodyMedium, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.refreshUsageData()
                            Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Temporary Cache")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DESTRUCTIVE ZONE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Resetting data will clear custom categories and settings.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Application Preferences", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Preferences?") },
            text = { Text("Are you sure you want to reset all custom app settings to default?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Preferences reset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 1️⃣3️⃣ SECURITY SETTINGS SCREEN
// ==========================================
@Composable
fun SecuritySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val lockOnRestart by viewModel.lockOnRestart.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val hideAmounts by viewModel.hideSensitiveAmounts.collectAsStateWithLifecycle()
    val isScreenshotProtected by viewModel.screenshotProtection.collectAsStateWithLifecycle()
    val autoLockDuration by viewModel.autoLockDuration.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Security Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PASSCODE PIN LOCK", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text(if (!appPin.isNullOrBlank()) "Passcode PIN Status: ACTIVE 🔒" else "Passcode PIN Status: DISABLED 🔓", style = MaterialTheme.typography.bodySmall, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (appPin.isNullOrBlank()) "Set PIN" else "Change PIN")
                        }

                        if (!appPin.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.setAppPin(null)
                                    Toast.makeText(context, "PIN Passcode removed.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove PIN")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Lock Immediately on App Restart", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Requires PIN whenever application reopens", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = lockOnRestart, onCheckedChange = { viewModel.toggleLockOnRestart(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    val canUseBiometric = remember(context) {
                        try {
                            androidx.biometric.BiometricManager.from(context)
                                .canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                        } catch (_: Exception) {
                            false
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Biometric / Fingerprint Unlock", fontWeight = FontWeight.Bold, color = if (canUseBiometric) SleekTextPrimary else SleekTextSecondary)
                            Text(
                                if (!canUseBiometric) "No strong biometric enrolled or supported on device" else "Use system biometric authentication",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                        Switch(
                            checked = biometricEnabled && canUseBiometric,
                            enabled = canUseBiometric && !appPin.isNullOrBlank(),
                            onCheckedChange = { viewModel.toggleBiometricEnabled(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hide Sensitive Balances on Launch", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Blurs total balance card by default", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = hideAmounts, onCheckedChange = { viewModel.toggleHideSensitiveAmounts(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Screenshot & Capture Protection", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Blocks Android system screenshots & screen recordings (FLAG_SECURE) for banking privacy", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = isScreenshotProtected,
                            onCheckedChange = { viewModel.toggleScreenshotProtection(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Inactivity Auto-Lock Duration", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text("Re-locks the app after being placed in background", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        val currentAutoLock = com.example.data.AutoLockDuration.fromString(autoLockDuration)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            com.example.data.AutoLockDuration.values().forEach { option ->
                                val isSelected = currentAutoLock == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateAutoLockDuration(option.name) },
                                    label = { Text(option.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SleekPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("Enter 4 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (pinInput.length == 4) {
                        viewModel.setAppPin(pinInput)
                        showPinDialog = false
                        pinInput = ""
                        Toast.makeText(context, "PIN Passcode set successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save PIN") }
            },
            dismissButton = { OutlinedButton(onClick = { showPinDialog = false }) { Text("Cancel") } }
        )
    }
}

// ==========================================
// 1️⃣4️⃣ PRIVACY SETTINGS SCREEN
// ==========================================
@Composable
fun PrivacySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val privacyMode by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()
    val aiConsent by viewModel.aiConsentEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Privacy Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DATA STORAGE & PRIVACY ARCHITECTURE", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Core Storage: All transaction entries, receipts, accounts, and local backup files remain strictly on your local device inside Room SQLite database. Backups to cloud are disabled.\n" +
                        "• Online Features: Exchange rate updates (open.er-api.com) and optional AI Advisor (Google Gemini via Firebase AI Logic) require internet. AI features are disabled by default and require explicit consent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Financial Advisor", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text(
                                "Enable Gemini AI insights and daily budget recommendations",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = aiConsent,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.promptAiConsentDialog()
                                } else {
                                    viewModel.setAiConsent(false)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "When enabled, high-level monthly aggregates (total income, expenses, net balance) and questions are transmitted securely to Google Gemini servers via Firebase. Transaction notes, account numbers, and merchant names are NEVER shared.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Privacy Blur Mode", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text("Mask financial numbers when sharing screens", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    }
                    Switch(checked = privacyMode, onCheckedChange = { viewModel.togglePrivacyModeEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 1️⃣5️⃣ ABOUT APP & UPDATES TIMELINE SCREEN
// ==========================================
@Composable
fun AboutAppScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("What's New & About App", onBack)

        // Segmented Tabs: Updates Timeline vs App Specs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SleekSurface,
            contentColor = SleekPrimary,
            divider = { HorizontalDivider(color = SleekBorder) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Updates Timeline", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("About & System", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                item {
                    UpdatesTimelineView()
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val logoPulse = rememberLoopFloat(0.96f, 1.04f, 2200, "app_logo_pulse", FastOutSlowInEasing, reverse = true, rest = 1f)
                Box(
                    modifier = Modifier
                        .scale(logoPulse)
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(BorderStroke(2.dp, SleekPrimary.copy(alpha = 0.5f)), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_modern),
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Finance Tracker Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Text("Version 1.28 • Build 2026.09 (v28)", style = MaterialTheme.typography.bodyMedium, color = SleekTextSecondary)

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("System Architecture & Specifications", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        HorizontalDivider(color = SleekBorder)
                        DetailSpecRow("Platform", "Android (Jetpack Compose & Kotlin)")
                        DetailSpecRow("Database", "Dual-Tier: 100% Offline Room + Optional Firebase Cloud Sync")
                        DetailSpecRow("Screen Protection", "Android FLAG_SECURE Banking Privacy Shield")
                        DetailSpecRow("Diagnostics", "AI Financial Health Score & Loan EMI Engine")
                        DetailSpecRow("Localization", "9 Built-in Languages with native RTL")
                        DetailSpecRow("Currencies", "100+ Global fiat currencies catalog with live forex")
                        DetailSpecRow("Data Security", "Local Encrypted File Storage, Biometric & PIN Lock")
                        DetailSpecRow("Target SDK", "Android 15 (API 36)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Privacy Guarantee", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text(
                            "This application operates 100% locally on your device. None of your financial transactions, account balances, bills, or personal notes ever leave your hardware.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🚚 UIVERSE.IO DELIVERY TRUCK LOADER PREVIEW CARD
                var showTruckPreviewDialog by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Delivery Truck Loading Engine",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            "Interactive high-fidelity physics loader (Uiverse.io by vinodjangid07) used across app startup, cloud sync, backups and data operations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        DeliveryTruckLoader(
                            scale = 1.0f,
                            caption = "Loading Assets & Data...",
                            subCaption = "1s suspension bobbing • 1.4s road animation"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(
                            onClick = { showTruckPreviewDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Fullscreen Loading Screen")
                        }
                    }
                }
                if (showTruckPreviewDialog) {
                    TruckLoadingDialog(
                        isOpen = true,
                        message = "Synchronizing Financial Vault...",
                        subtitle = "Testing Uiverse.io delivery truck loading screen",
                        onDismissRequest = { showTruckPreviewDialog = false }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
private fun DetailSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = SleekTextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
    }
}

// ==========================================
// 1️⃣6️⃣ HELP & SUPPORT SCREEN
// ==========================================
@Composable
fun HelpSupportScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val faqs = remember {
        listOf(
            "How do I use Quick Shortcuts and the All Shortcuts Launcher?" to "You can access primary shortcuts directly from your Home dashboard. Tap the 'More' button to open the full-screen App Launcher with all 16+ tools, search filter, and category pills.",
            "How do I view App Update details and release specifications?" to "Navigate to Sidebar -> What's New & About App -> Updates Timeline to inspect each release, its realtime date, and categorized specifications.",
            "How does the Daily Streak work?" to "Every day you open the app and review or record financial activity, your streak advances! Celebrate milestones with dynamic flame bursts, unlock higher tier badges (Starter -> Bronze -> Silver -> Gold -> Platinum -> Diamond), and tap anywhere on screen to smoothly dismiss the celebration.",
            "How do I use the interactive Calendar tab?" to "The Calendar tab provides a visual breakdown of your income and expenses day by day. Tap any day to inspect transactions, toggle month/week navigation, and use filters to analyze your cash flow across time.",
            "How do I change the default currency?" to "Go to Sidebar -> Currency, search for your currency, and choose Option A (Keep existing) or Option B (Convert existing transactions using real rates).",
            "Are my financial transactions safe?" to "Yes! Your entire financial database is stored 100% locally on your device with local backup & restore capabilities.",
            "How do I manage my monthly budgets?" to "Go to Sidebar -> Budgets or the Home screen budget card to configure monthly limits and warning thresholds.",
            "How do I lock the app with a PIN?" to "Navigate to Sidebar -> Security and tap 'Set PIN' to create a 4-digit security code.",
            "How do I switch the application language?" to "Navigate to Sidebar -> Language and select your language. It instantly updates text across all screens."
        )
    }

    val filtered = remember(searchQuery) {
        faqs.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Help & Support Center", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search help topics") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            filtered.forEach { (question, answer) ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(question, fontWeight = FontWeight.Bold, color = SleekTextPrimary, modifier = Modifier.weight(1f))
                            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SleekPrimary)
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(answer, style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// Dedicated Export Statements & Reports Screen
@Composable
fun ExportDataScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val currencyCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var dateRangeFilter by remember { mutableStateOf("All Time") }
    var typeFilter by remember { mutableStateOf("ALL") }
    var categoryFilter by remember { mutableStateOf("ALL") }
    var includeDetailedTxns by remember { mutableStateOf(true) }

    var showCategoryDropdown by remember { mutableStateOf(false) }

    val allCategories = remember(expenses) {
        listOf("ALL") + expenses.map { it.category }.distinct().sorted()
    }

    val filteredExpenses = remember(expenses, dateRangeFilter, typeFilter, categoryFilter) {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        expenses.filter { exp ->
            val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
            val matchesDate = when (dateRangeFilter) {
                "This Month" -> expCal.get(Calendar.MONTH) == currentMonth && expCal.get(Calendar.YEAR) == currentYear
                "Last Month" -> {
                    val lastMonthCal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -1)
                    }
                    expCal.get(Calendar.MONTH) == lastMonthCal.get(Calendar.MONTH) && expCal.get(Calendar.YEAR) == lastMonthCal.get(Calendar.YEAR)
                }
                "This Year" -> expCal.get(Calendar.YEAR) == currentYear
                else -> true
            }

            val matchesType = when (typeFilter) {
                "EXPENSE" -> exp.type != "INCOME"
                "INCOME" -> exp.type == "INCOME"
                else -> true
            }

            val matchesCategory = if (categoryFilter == "ALL") true else exp.category == categoryFilter

            matchesDate && matchesType && matchesCategory
        }
    }

    val totalIncome = remember(filteredExpenses, currencyCode) { filteredExpenses.realIncome(currencyCode) }
    val totalExpense = remember(filteredExpenses, currencyCode) { filteredExpenses.realExpense(currencyCode) }
    val netBalance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Export Statements & Reports", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STATEMENT FILTERS CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = SleekPrimary)
                        Text(
                            text = "Statement Configuration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SleekTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Date Range Filter:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All Time", "This Month", "Last Month", "This Year").forEach { option ->
                            val isSelected = dateRangeFilter == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SleekPrimary else SleekNeutralLight)
                                    .border(1.dp, if (isSelected) SleekPrimary else SleekBorder, RoundedCornerShape(12.dp))
                                    .clickable { dateRangeFilter = option }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else SleekTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Transaction Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL" to "All Types", "EXPENSE" to "Expenses", "INCOME" to "Income").forEach { (typeKey, label) ->
                            val isSelected = typeFilter == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SleekPrimaryContainer else SleekNeutralLight)
                                    .border(1.dp, if (isSelected) SleekPrimary else SleekBorder, RoundedCornerShape(12.dp))
                                    .clickable { typeFilter = typeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SleekPrimary else SleekTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)

                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekNeutralLight,
                                border = BorderStroke(1.dp, SleekBorder),
                                modifier = Modifier.clickable { showCategoryDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (categoryFilter == "ALL") "All Categories" else categoryFilter,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SleekTextSecondary)
                                }
                            }

                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                allCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(if (cat == "ALL") "All Categories" else cat) },
                                        onClick = {
                                            categoryFilter = cat
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Include Itemized Transaction Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Include full line-item transaction table in PDF/CSV exports", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = includeDetailedTxns,
                            onCheckedChange = { includeDetailedTxns = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SleekPrimary)
                        )
                    }
                }
            }

            // SUMMARY PREVIEW BADGE
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredExpenses.size} Records Selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekPrimary
                        )
                        Text(
                            text = "Filter: $dateRangeFilter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SleekTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Income", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Text("₹%,.0f".format(totalIncome), fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(ExpenseRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Expense", fontSize = 9.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                Text("₹%,.0f".format(totalExpense), fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SleekNeutralLight, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Net Flow", fontSize = 9.sp, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
                                Text("₹%,.0f".format(netBalance), fontSize = 12.sp, color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // EXPORT FORMAT 1: PDF REPORT
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("PDF Financial Ledger Statement", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SleekTextPrimary)
                            Text("Formatted document with executive KPI summary and transaction ledger table.", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (filteredExpenses.isEmpty()) {
                                Toast.makeText(context, "No transactions found matching the selected filters.", Toast.LENGTH_SHORT).show()
                            } else {
                                DataExporter.sharePdfReport(
                                    context = context,
                                    expenses = filteredExpenses,
                                    dateRangeStr = dateRangeFilter,
                                    typeFilterStr = typeFilter,
                                    categoryFilterStr = categoryFilter,
                                    includeDetailedTxns = includeDetailedTxns
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF Document", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // EXPORT FORMAT 2: CSV SPREADSHEET
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("CSV Spreadsheet File", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SleekTextPrimary)
                            Text("Raw structured table data ready for Microsoft Excel, Google Sheets, or accounting tools.", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            if (filteredExpenses.isEmpty()) {
                                Toast.makeText(context, "No transactions found matching the selected filters.", Toast.LENGTH_SHORT).show()
                            } else {
                                DataExporter.exportToCSV(
                                    context = context,
                                    expenses = filteredExpenses,
                                    dateRangeStr = dateRangeFilter,
                                    typeFilterStr = typeFilter,
                                    categoryFilterStr = categoryFilter
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV Spreadsheet", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // EXPORT FORMAT 3: JSON STRUCTURED DATA
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("JSON Data Export", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SleekTextPrimary)
                            Text("Standard developer & backup JSON schema with itemized ledger array and metrics summary.", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            if (filteredExpenses.isEmpty()) {
                                Toast.makeText(context, "No transactions found matching the selected filters.", Toast.LENGTH_SHORT).show()
                            } else {
                                DataExporter.exportToJson(
                                    context = context,
                                    expenses = filteredExpenses,
                                    dateRangeStr = dateRangeFilter,
                                    typeFilterStr = typeFilter,
                                    categoryFilterStr = categoryFilter
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export JSON Document", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // EXPORT FORMAT 4: GRAPHIC SUMMARY REPORT
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Graphic Summary Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SleekTextPrimary)
                            Text("High-resolution graphic image visual summarizing key financial metrics.", fontSize = 11.sp, color = SleekTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            if (filteredExpenses.isEmpty()) {
                                Toast.makeText(context, "No transactions found matching the selected filters.", Toast.LENGTH_SHORT).show()
                            } else {
                                DataExporter.shareImageReport(
                                    context = context,
                                    expenses = filteredExpenses,
                                    dateRangeStr = dateRangeFilter,
                                    typeFilterStr = typeFilter,
                                    categoryFilterStr = categoryFilter
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, SleekPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Graphic Summary", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// Keep legacy screen aliases for backwards compatibility
@Composable fun DataAndStorageScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { DataManagementScreen(viewModel, onBack) }
@Composable fun ThemeAndLanguageScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { AppearanceScreen(viewModel, onBack) }
@Composable fun FaqAndHelpScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { HelpSupportScreen(viewModel, onBack) }
