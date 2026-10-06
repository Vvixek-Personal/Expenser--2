package com.example.ui

import com.example.data.*
import com.example.R
import java.util.*
import java.text.*
import kotlin.math.roundToInt

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import android.content.Context
import kotlin.math.roundToInt
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Expenses : Screen("expenses", "Finance", Icons.Default.ReceiptLong)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.PieChart)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
}

fun getCategoryEmoji(category: String, customMap: Map<String, String> = emptyMap()): String {
    val resolved = (customMap[category] ?: category).lowercase(java.util.Locale.getDefault())
    return when (resolved) {
        "food", "dining", "restaurant", "baking", "fastfood" -> "🍔"
        "travel", "flight", "aviation", "taxi", "transportation", "flighttakeoff" -> "✈️"
        "rent", "home", "housing", "real estate" -> "🏠"
        "utilities", "electricity", "water", "gas", "electricbolt", "bolt" -> "💡"
        "entertainment", "gaming", "movies", "cinema", "sportsesports", "movie" -> "🎮"
        "shopping", "clothing", "gear", "shoppingcart" -> "🛍️"
        "persons", "friends", "family", "dogs", "cats", "pets", "person" -> "👥"
        "salary", "income", "finance", "monetizationon" -> "💵"
        "freelance", "work" -> "💼"
        "investments", "crypto" -> "🪙"
        "gifts", "gift", "cardgiftcard" -> "🎁"
        "art" -> "🎨"
        "botany" -> "🌱"
        "cars", "car", "directionscar" -> "🚗"
        "technology", "programming" -> "💻"
        "fashion" -> "👗"
        "birds" -> "🐦"
        "health care", "healthcare", "medical", "localhospital", "healing" -> "🏥"
        "geography" -> "🗺️"
        "lgbtq" -> "🏳️‍🌈"
        "mental health" -> "🧠"
        "sports", "fitness", "fitnesscenter" -> "⚽"
        "photography" -> "📷"
        "design" -> "🖋️"
        "ufo" -> "🛸"
        "music" -> "🎶"
        "school" -> "🏫"
        "settings" -> "⚙️"
        "star" -> "⭐️"
        "construction" -> "🔨"
        "coffee" -> "☕"
        "waterdrop" -> "💧"
        "checkroom" -> "🧥"
        "directionsbus" -> "🚌"
        "localgasstation" -> "⛽"
        "event" -> "📅"
        "spa" -> "🧖"
        "pending" -> "⏳"
        else -> "📦"
    }
}

// Category palette helper
val categoryColors = mapOf(
    "Food" to Color(0xFFF97316),        // Orange
    "Travel" to Color(0xFF0D9488),      // Teal
    "Rent" to Color(0xFF2563EB),        // Blue
    "Utilities" to Color(0xFF16A34A),   // Green
    "Entertainment" to Color(0xFFE11D48),// Rose
    "Shopping" to Color(0xFF9333EA),     // Purple
    "Persons" to Color(0xFF0EA5E9),      // Sky Blue (Persons Category)
    "Others" to Color(0xFF64748B)        // Slate
)

fun getCategoryIcon(category: String, customMap: Map<String, String> = emptyMap()): ImageVector {
    val resolved = customMap[category] ?: category
    return when (resolved) {
        "Food", "Restaurant" -> Icons.Default.Restaurant
        "Travel", "DirectionsCar" -> Icons.Default.DirectionsCar
        "Rent", "Home" -> Icons.Default.Home
        "Utilities", "Bolt", "ElectricBolt" -> Icons.Default.Bolt
        "Entertainment", "Movie" -> Icons.Default.Movie
        "Shopping", "ShoppingCart" -> Icons.Default.ShoppingCart
        "Persons", "Person" -> Icons.Default.Person
        "LocalHospital", "Healing" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "Work" -> Icons.Default.Work
        "Flight", "FlightTakeoff" -> Icons.Default.Flight
        "SportsEsports" -> Icons.Default.SportsEsports
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "MonetizationOn" -> Icons.Default.MonetizationOn
        "Settings" -> Icons.Default.Settings
        "Pets" -> Icons.Default.Pets
        "Star" -> Icons.Default.Star
        "Construction" -> Icons.Default.Construction
        "Fastfood" -> Icons.Default.Fastfood
        "Coffee" -> Icons.Default.Coffee
        "WaterDrop" -> Icons.Default.WaterDrop
        "Checkroom" -> Icons.Default.Checkroom
        "DirectionsBus" -> Icons.Default.DirectionsBus
        "LocalGasStation" -> Icons.Default.LocalGasStation
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Event" -> Icons.Default.Event
        "Spa" -> Icons.Default.Spa
        "Pending" -> Icons.Default.Pending
        else -> Icons.Default.Category
    }
}

val LocalAutoLockSuppressor = compositionLocalOf<() -> Unit> { {} }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    val animPref by viewModel.isAnimationEnabled.collectAsStateWithLifecycle()
    val animEnabled = animPref && systemAnimationsAllowed()
    CompositionLocalProvider(
        LocalAutoLockSuppressor provides { viewModel.suppressNextAutoLock() },
        LocalAnimationsEnabled provides animEnabled
    ) {
        val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val categoryIcons by viewModel.categoryIcons.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()

    val globalLoadingMessage by viewModel.appLoadingMessage.collectAsStateWithLifecycle()
    var isInitialAppLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(950)
        isInitialAppLoading = false
    }

    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val hasPromptedFirstRunPin by viewModel.hasPromptedFirstRunPin.collectAsStateWithLifecycle()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var customTxTypeForAddDialog by remember { mutableStateOf<String?>(null) }
    var prefilledDateForAddDialog by remember { mutableStateOf<Long?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var viewingDetailExpense by remember { mutableStateOf<Expense?>(null) }
    var recordedTransactionInfo by remember { mutableStateOf<RecordedTransactionInfo?>(null) }

    var activeSettingsSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var drawerFullscreen by remember { mutableStateOf(false) }

    // Always reopen at normal width
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) drawerFullscreen = false
    }

    val navigateToScreen: (Screen) -> Unit = { selected ->
        activeSettingsSubScreen = null
        if (currentScreen != selected) {
            backStack.add(selected)
            currentScreen = selected
        }
    }

    // Close drawer automatically if app gets locked
    LaunchedEffect(isAppLocked, appPin) {
        if (isAppLocked && !appPin.isNullOrBlank()) {
            drawerState.close()
        }
    }

    // Re-lock app according to Inactivity Auto-Lock duration when foregrounded
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, appPin) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    viewModel.onAppBackgrounded()
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    viewModel.onAppForegrounded()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back button when locked
    BackHandler(enabled = isAppLocked && !appPin.isNullOrBlank()) {
        // Do nothing on back button during lock screen
    }

    // SYSTEM BACK BUTTON HANDLER (Pops navigation stack, closes drawer/settings/dialogs)
    val canHandleBack = (!isAppLocked || appPin.isNullOrBlank()) && (
            drawerFullscreen ||
            drawerState.isOpen ||
            activeSettingsSubScreen != null ||
            viewingDetailExpense != null ||
            editingExpense != null ||
            showAddExpenseDialog ||
            backStack.size > 1 ||
            currentScreen != Screen.Dashboard
    )

    val locked = isAppLocked && !appPin.isNullOrBlank()

    BackHandler(enabled = canHandleBack) {
        when {
            drawerFullscreen -> drawerFullscreen = false
            drawerState.isOpen -> scope.launch { drawerState.close() }
            activeSettingsSubScreen != null -> activeSettingsSubScreen = null
            viewingDetailExpense != null -> viewingDetailExpense = null
            editingExpense != null -> editingExpense = null
            showAddExpenseDialog -> showAddExpenseDialog = false
            backStack.size > 1 -> {
                backStack.removeAt(backStack.lastIndex)
                currentScreen = backStack.last()
            }
            currentScreen != Screen.Dashboard -> {
                backStack.clear()
                backStack.add(Screen.Dashboard)
                currentScreen = Screen.Dashboard
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            modifier = if (locked) Modifier.clearAndSetSemantics { } else Modifier,
            drawerState = drawerState,
            gesturesEnabled = !locked && activeSettingsSubScreen == null,
            drawerContent = {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val sheetSpec = spring<androidx.compose.ui.unit.Dp>(stiffness = Spring.StiffnessMediumLow)
                val sheetWidth by animateDpAsState(if (drawerFullscreen) screenWidth else 330.dp, sheetSpec, label = "sheetWidth")
                val padTop by animateDpAsState(if (drawerFullscreen) 0.dp else 44.dp, sheetSpec, label = "sheetPadTop")
                val padBottom by animateDpAsState(if (drawerFullscreen) 0.dp else 44.dp, sheetSpec, label = "sheetPadBottom")
                val padEnd by animateDpAsState(if (drawerFullscreen) 0.dp else 12.dp, sheetSpec, label = "sheetPadEnd")
                val corner by animateDpAsState(if (drawerFullscreen) 0.dp else 24.dp, sheetSpec, label = "sheetCorner")

                ModalDrawerSheet(
                    drawerContainerColor = Color.Transparent,
                    drawerTonalElevation = 0.dp,
                    drawerShape = RectangleShape,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(sheetWidth)
                        .padding(top = padTop, bottom = padBottom, end = padEnd)
                ) {
                    Surface(
                        shape = RoundedCornerShape(corner),
                        color = SleekBg,
                        border = BorderStroke(1.dp, SleekBorder),
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(drawerState.currentValue, drawerFullscreen) {
                                if (drawerState.currentValue != DrawerValue.Open) return@pointerInput
                                val trigger = 48.dp.toPx()
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val first = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                                        // Only claim the swipe we care about; everything else
                                        // passes through so normal swipe-to-close still works.
                                        if ((!drawerFullscreen && overSlop > 0f) ||
                                            (drawerFullscreen && overSlop < 0f)) change.consume()
                                    }
                                    if (first != null) {
                                        var total = 0f
                                        horizontalDrag(first.id) { c ->
                                            total += c.positionChange().x
                                            c.consume()
                                        }
                                        if (!drawerFullscreen && total > trigger) drawerFullscreen = true
                                        else if (drawerFullscreen && total < -trigger) drawerFullscreen = false
                                    }
                                }
                            }
                    ) {
                        SidebarDrawerContent(
                            viewModel = viewModel,
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                            onOpenSettingsScreen = { subScreen ->
                                scope.launch { drawerState.close() }
                                activeSettingsSubScreen = subScreen
                            },
                            onChangePasswordClick = {
                                showChangePinDialog = true
                            }
                        )
                    }
                }
            }
        ) {
        Scaffold(
        bottomBar = {
            if (!isAppLocked || appPin.isNullOrBlank()) {
                FloatingDockBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { selected -> navigateToScreen(selected) },
                    selectedLanguage = viewModel.selectedLanguage.collectAsStateWithLifecycle().value
                )
            }
        },
        containerColor = SleekBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isAnimEnabled = LocalAnimationsEnabled.current
            // Screen Switcher with liquid-glass shared animations
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = appSpring(isAnimEnabled, dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
                     scaleIn(initialScale = 0.94f, animationSpec = appSpring(isAnimEnabled, dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))) togetherWith
                    (fadeOut(animationSpec = appSpring(isAnimEnabled, stiffness = Spring.StiffnessMedium)) +
                     scaleOut(targetScale = 1.04f, animationSpec = appSpring(isAnimEnabled, stiffness = Spring.StiffnessMedium)))
                },
                label = "LiquidGlassScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardTab(
                        expenses = filteredExpenses,
                        userName = userName,
                        monthlyBudget = monthlyBudget,
                        categoryIcons = categoryIcons,
                        onUpdateBudget = { viewModel.updateMonthlyBudget(it) },
                        onUpdateName = { viewModel.saveUserName(it) },
                        onAddExpenseClick = {
                            customTxTypeForAddDialog = "EXPENSE"
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onAddIncomeClick = {
                            customTxTypeForAddDialog = "INCOME"
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onNavigateToExpenses = { navigateToScreen(Screen.Expenses) },
                        onNavigateToAnalytics = { navigateToScreen(Screen.Analytics) },
                        onProfileClick = { scope.launch { drawerState.open() } },
                        onEditExpenseClick = { viewingDetailExpense = it },
                        onOpenSettingsSubScreen = { activeSettingsSubScreen = it },
                        viewModel = viewModel
                    )
                    Screen.Expenses -> ExpensesTab(
                        viewModel = viewModel,
                        onAddExpenseClick = {
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onEditExpenseClick = { viewingDetailExpense = it }
                    )
                    Screen.Analytics -> AnalyticsTab(
                        viewModel = viewModel,
                        onAddClick = {
                            activeSettingsSubScreen = null
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onProfileClick = { scope.launch { drawerState.open() } },
                        onExpenseClick = { viewingDetailExpense = it }
                    )
                    Screen.Calendar -> EnhancedCalendarTab(
                        expenses = expenses,
                        categoryIcons = categoryIcons,
                        currencySymbol = currencySymbol,
                        currencyCode = selectedCurrencyCode,
                        billsList = viewModel.billsList,
                        onAddExpenseForDate = { date, type ->
                            prefilledDateForAddDialog = date
                            customTxTypeForAddDialog = type
                            showAddExpenseDialog = true
                        },
                        onEditExpense = { viewingDetailExpense = it },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }
            }

            if (showAddExpenseDialog) {
                val defaultTxType by viewModel.defaultTxType.collectAsStateWithLifecycle()
                val rememberLastCategory by viewModel.rememberLastCategory.collectAsStateWithLifecycle()
                val lastUsedExpenseCategory by viewModel.lastUsedExpenseCategory.collectAsStateWithLifecycle()
                val lastUsedIncomeCategory by viewModel.lastUsedIncomeCategory.collectAsStateWithLifecycle()
                val isGstEnabledForDialog by viewModel.isGstEnabled.collectAsStateWithLifecycle()
                val gstRateForDialog by viewModel.gstRatePercent.collectAsStateWithLifecycle()
                val isMonthlySafeEnabledForDialog by viewModel.isMonthlySafeEnabled.collectAsStateWithLifecycle()
                val monthlySafeAmountForDialog by viewModel.monthlySafeAmount.collectAsStateWithLifecycle()
                AddExpenseDialog(
                    prefilledDate = prefilledDateForAddDialog,
                    categories = allCategories,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    categoryIcons = categoryIcons,
                    expenses = expenses,
                    defaultTxType = customTxTypeForAddDialog ?: defaultTxType,
                    rememberLastCategory = rememberLastCategory,
                    lastUsedExpenseCategory = lastUsedExpenseCategory,
                    lastUsedIncomeCategory = lastUsedIncomeCategory,
                    gstReserveAmount = if (isGstEnabledForDialog) viewModel.getGstReserveAmount() else 0.0,
                    monthlySafeAmount = if (isMonthlySafeEnabledForDialog) monthlySafeAmountForDialog else 0.0,
                    currencySymbol = currencySymbol,
                    currencyCode = selectedCurrencyCode,
                    onAddCategory = { name, catType -> viewModel.addCustomCategory(name, catType) },
                    onDeleteCategory = { viewModel.deleteCustomCategory(it) },
                    onEditCategory = { old, new -> viewModel.renameCustomCategory(old, new) },
                    onDismiss = {
                        showAddExpenseDialog = false
                        customTxTypeForAddDialog = null
                    },
                    onConfirm = { amount, category, date, note, imagePath, type ->
                        viewModel.addExpense(amount, category, date, note, imagePath, type, selectedCurrencyCode)
                        viewModel.refreshUsageData()
                        showAddExpenseDialog = false
                        customTxTypeForAddDialog = null
                        recordedTransactionInfo = RecordedTransactionInfo(
                            amount = amount,
                            category = category,
                            type = type,
                            note = note
                        )
                    }
                )
            }

            if (recordedTransactionInfo != null) {
                TransactionSuccessDialog(
                    info = recordedTransactionInfo!!,
                    onDismiss = { recordedTransactionInfo = null }
                )
            }

            if (editingExpense != null) {
                EditExpenseDialog(
                    expense = editingExpense!!,
                    categories = allCategories,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    categoryIcons = categoryIcons,
                    currencySymbol = currencySymbol,
                    currencyCode = selectedCurrencyCode,
                    expenses = expenses,
                    onAddCategory = { name, catType -> viewModel.addCustomCategory(name, catType) },
                    onDeleteCategory = { viewModel.deleteCustomCategory(it) },
                    onEditCategory = { old, new -> viewModel.renameCustomCategory(old, new) },
                    onDismiss = { editingExpense = null },
                    onConfirm = { updatedExpense ->
                        viewModel.updateExpense(updatedExpense)
                        editingExpense = null
                    }
                )
            }

            if (viewingDetailExpense != null) {
                ExpenseDetailDialog(
                    expense = viewingDetailExpense!!,
                    viewModel = viewModel,
                    onDismiss = { viewingDetailExpense = null },
                    onEditClick = {
                        val target = viewingDetailExpense
                        viewingDetailExpense = null
                        if (target != null) {
                            editingExpense = target
                        }
                    },
                    onDeleteClick = {
                        val target = viewingDetailExpense
                        viewingDetailExpense = null
                        if (target != null) {
                            viewModel.deleteExpense(target)
                        }
                    }
                )
            }
        }

        AnimatedContent(
            targetState = activeSettingsSubScreen,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn()).togetherWith(
                        slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
                    )
                } else {
                    fadeIn().togetherWith(
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    )
                }
            },
            label = "settings_subscreen_transition"
        ) { targetScreen ->
            if (targetScreen != null) {
                when (targetScreen) {
                    SettingsSubScreen.PersonalData -> PersonalDataScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.BadgesAndMilestones -> BadgesAndMilestonesScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Appearance -> AppearanceScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Language -> LanguageScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Currency -> CurrencySettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.DateTime -> DateTimeScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Bills -> BillsSettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.CategoriesTags -> CategoriesTagsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Budgets -> BudgetSettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.SavingsGoals -> SavingsGoalsSettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Calculations -> CalculationsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Transactions -> TransactionSettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.BackupRestore -> BackupRestoreScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.DataManagement -> DataManagementScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null },
                        onNavigateToLocalRecovery = { activeSettingsSubScreen = SettingsSubScreen.LocalRecovery }
                    )
                    SettingsSubScreen.LocalRecovery -> LocalRecoveryScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = SettingsSubScreen.DataManagement }
                    )
                    SettingsSubScreen.Security -> SecuritySettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Privacy -> PrivacySettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.AboutApp -> AboutAppScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.HelpSupport -> HelpSupportScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.DataAndStorage -> DataAndStorageScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.ThemeAndLanguage -> ThemeAndLanguageScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.Export -> ExportDataScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                    SettingsSubScreen.FaqAndHelp -> FaqAndHelpScreen(
                        viewModel = viewModel,
                        onBack = { activeSettingsSubScreen = null }
                    )
                }
            }
        }

        // Full Screen Lock Overlay if app is locked with a PIN
        if (locked) {
            PinLockScreen(viewModel = viewModel)
        }
    }

    // First Run Optional PIN Setup Prompt
    if (!locked && appPin.isNullOrBlank() && !hasPromptedFirstRunPin) {
        FirstRunPinSetupDialog(
            onSetPin = { newPin ->
                viewModel.setAppPin(newPin)
                viewModel.markFirstRunPinPrompted()
            },
            onMaybeLater = {
                viewModel.markFirstRunPinPrompted()
            }
        )
    }

    // Change Password / PIN Dialog
    if (showChangePinDialog && !locked) {
        ChangePinDialog(
            currentAppPin = appPin,
            onDismiss = { showChangePinDialog = false },
            onSavePin = { newPin ->
                viewModel.setAppPin(newPin)
            }
        )
    }

    if (!locked && userName.isNullOrBlank()) {
        OnboardingNameDialog(onSave = { viewModel.saveUserName(it) })
    }

    // 🚚 Fullscreen Delivery Truck Loader for app startup
    if (isInitialAppLoading && !locked) {
        TruckLoadingScreen(
            message = "Loading Your Financial Workspace...",
            subtitle = "Preparing secure offline ledgers, accounts & charts"
        )
    }

    // 🚚 Global Truck Loading Dialog for asynchronous tasks
    TruckLoadingDialog(
        isOpen = globalLoadingMessage != null && !locked,
        message = globalLoadingMessage ?: "Processing...",
        subtitle = "Please wait while your task completes safely",
        onDismissRequest = { viewModel.hideAppLoading() }
    )
    }
}
}
}

// ==========================================
// 1️⃣ DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTab(
    expenses: List<Expense>,
    userName: String?,
    monthlyBudget: Double,
    categoryIcons: Map<String, String> = emptyMap(),
    onUpdateBudget: (Double) -> Unit,
    onUpdateName: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit = {},
    onNavigateToExpenses: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onProfileClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit,
    onOpenSettingsSubScreen: (SettingsSubScreen) -> Unit = {},
    viewModel: FinanceViewModel
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val billShowUpcomingDashboard by viewModel.billShowUpcomingDashboard.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val showStreakDialog by viewModel.showStreakDialog.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val isLocked = isAppLocked && !appPin.isNullOrBlank()
    val isPrivacyMode by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()
    val isPrivacyRevealed by viewModel.privacyRevealOverride.collectAsStateWithLifecycle()
    val shouldHideBalance = isPrivacyMode && !isPrivacyRevealed

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val todayExpense = remember(expenses) {
        expenses.filter { it.date in todayStart..todayEnd && it.type != "INCOME" && it.category != "Locked Savings" }
            .sumOf { it.amount }
    }

    var showQuickDepositDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var showQuickSplitDialog by remember { mutableStateOf(false) }
    var showQuickConvertDialog by remember { mutableStateOf(false) }
    var showAdjustBudgetDialog by remember { mutableStateOf(false) }

    val showAiConsentDialog by viewModel.showAiConsentDialog.collectAsStateWithLifecycle()
    if (showAiConsentDialog && !isLocked) {
        AiConsentDialog(
            onConsentAccepted = { viewModel.setAiConsent(true) },
            onConsentDeclined = { viewModel.setAiConsent(false) },
            onDismiss = { viewModel.dismissAiConsentDialog() }
        )
    }

    if (showStreakDialog && !isLocked) {
        DailyStreakCelebrationDialog(
            streakCount = currentStreak,
            onDismiss = { viewModel.dismissStreakDialog() }
        )
    }

    val (thisMonthRange, lastMonthRange) = remember {
        val cal = Calendar.getInstance()
        val cMonth = cal.get(Calendar.MONTH)
        val cYear = cal.get(Calendar.YEAR)

        val calStart = Calendar.getInstance().apply {
            set(cYear, cMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = Calendar.getInstance().apply {
            set(cYear, cMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val lmCal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val lmMonth = lmCal.get(Calendar.MONTH)
        val lmYear = lmCal.get(Calendar.YEAR)

        val lmStart = Calendar.getInstance().apply {
            set(lmYear, lmMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lmEnd = Calendar.getInstance().apply {
            set(lmYear, lmMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        (calStart.timeInMillis..calEnd.timeInMillis) to (lmStart.timeInMillis..lmEnd.timeInMillis)
    }

    // All-time income, expense, and savings goals totals for Total Net Balance
    val totalAllTimeIncome = remember(expenses, selectedCurrencyCode) { expenses.realIncome(selectedCurrencyCode) }
    val totalAllTimeExpense = remember(expenses, selectedCurrencyCode) { expenses.realExpense(selectedCurrencyCode) }
    val totalSavingsGoalsMoney = remember(savingsGoals) { savingsGoals.totalSavings() }
    val overallTotalNetBalance = remember(expenses, savingsGoals, selectedCurrencyCode) { expenses.netWorth(savingsGoals, selectedCurrencyCode) }

    // Filter current month expenses
    val thisMonthExpenses = remember(expenses, thisMonthRange) {
        expenses.filter {
            it.date in thisMonthRange && it.type != "INCOME" && it.category != "Locked Savings"
        }
    }
    val thisMonthTotal = remember(thisMonthExpenses, selectedCurrencyCode) {
        thisMonthExpenses.realExpense(selectedCurrencyCode)
    }

    // Filter current month incomes
    val thisMonthIncomes = remember(expenses, thisMonthRange) {
        expenses.filter {
            it.date in thisMonthRange && it.type == "INCOME" && it.category != "Goal Withdrawal"
        }
    }
    val thisMonthIncomeTotal = remember(thisMonthIncomes, selectedCurrencyCode) {
        thisMonthIncomes.realIncome(selectedCurrencyCode)
    }

    // Last month expenses
    val lastMonthExpenses = remember(expenses, lastMonthRange) {
        expenses.filter {
            it.date in lastMonthRange && it.type != "INCOME" && it.category != "Locked Savings"
        }
    }
    val lastMonthTotal = remember(lastMonthExpenses, selectedCurrencyCode) {
        lastMonthExpenses.realExpense(selectedCurrencyCode)
    }

    // Difference Calculation
    val diffPct = remember(thisMonthTotal, lastMonthTotal) {
        if (lastMonthTotal > 0) {
            ((thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100
        } else {
            0.0
        }
    }

    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showBillsScreen by remember { mutableStateOf(false) }
    var showRemindersScreen by remember { mutableStateOf(false) }
    var showSavingGoalsScreen by remember { mutableStateOf(false) }
    var showAllShortcutsScreen by remember { mutableStateOf(false) }
    var showStartupReminder by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        showStartupReminder = false
    }

    if (showAllShortcutsScreen) {
        val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
        val hasBillDueToday = viewModel.billsList.any { it.dueDate == todayStr }

        AllShortcutsTabScreen(
            onBack = { showAllShortcutsScreen = false },
            onAddExpense = {
                showAllShortcutsScreen = false
                onAddExpenseClick()
            },
            onAddIncome = {
                showAllShortcutsScreen = false
                onAddIncomeClick()
            },
            onQuickSplit = {
                showAllShortcutsScreen = false
                showQuickSplitDialog = true
            },
            onQuickConvert = {
                showAllShortcutsScreen = false
                showQuickConvertDialog = true
            },
            onViewGoals = {
                showAllShortcutsScreen = false
                showSavingGoalsScreen = true
            },
            onNavigateToExpenses = {
                showAllShortcutsScreen = false
                onNavigateToExpenses()
            },
            onNavigateToAnalytics = {
                showAllShortcutsScreen = false
                onNavigateToAnalytics()
            },
            onBillsClick = {
                showAllShortcutsScreen = false
                showBillsScreen = true
            },
            onReminderClick = {
                showAllShortcutsScreen = false
                showRemindersScreen = true
            },
            onAdjustBudget = {
                showAllShortcutsScreen = false
                showAdjustBudgetDialog = true
            },
            onOpenStreak = {
                showAllShortcutsScreen = false
                viewModel.triggerShowStreakDialog()
            },
            onOpenSettingsSubScreen = { subScreen ->
                showAllShortcutsScreen = false
                onOpenSettingsSubScreen(subScreen)
            },
            hasBillDueToday = hasBillDueToday,
            currentStreak = currentStreak,
            selectedLanguage = selectedLanguage
        )
        return
    }

    // Startup reminder alert dialog removed - shown on-screen in dashboard feed

    if (showSavingGoalsScreen) {
        SavingGoalsFullScreen(
            viewModel = viewModel,
            onBack = { showSavingGoalsScreen = false }
        )
        return
    }

    if (showBillsScreen) {
        BillsFullScreen(
            viewModel = viewModel,
            onBack = { showBillsScreen = false },
            onPayBill = { title, amt ->
                viewModel.addExpense(
                    amount = amt,
                    category = "Bills",
                    date = System.currentTimeMillis(),
                    note = "Paid $title",
                    type = "EXPENSE",
                    currencyCode = selectedCurrencyCode
                )
            }
        )
        return
    }

    if (showRemindersScreen) {
        RemindersFullScreen(
            viewModel = viewModel,
            onBack = { showRemindersScreen = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header & User Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                val initials = if (!userName.isNullOrBlank()) {
                    userName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
                } else "U"

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column {
                    val timeOfDay = remember {
                        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                            in 0..11 -> "Good Morning,"
                            in 12..16 -> "Good Afternoon,"
                            else -> "Good Evening,"
                        }
                    }
                    Text(
                        text = timeOfDay,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    Text(
                        text = userName ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Streak Badge beside profile & Quick Add Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🔥 Streak Flame Logo (replaces text pill as requested)
                StreakFlameLogo(
                    streakCount = currentStreak,
                    onClick = { viewModel.triggerShowStreakDialog() },
                    modifier = Modifier.testTag("dashboard_streak_flame_badge")
                )

                IconButton(
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SleekPrimary)
                        .bouncyPress()
                        .testTag("dashboard_add_expense_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Balance Card (Bank Account Balance Style)
        val animatedBalance = animateAmountFloat(overallTotalNetBalance.toFloat())
        val animatedIncome = animateAmountFloat(totalAllTimeIncome.toFloat())
        val animatedExpense = animateAmountFloat(totalAllTimeExpense.toFloat())
        val heroGlowBorder = rememberAnimatedGlowBrush(Color.White.copy(alpha = 0.15f), SleekPrimary.copy(alpha = 0.5f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .bouncyPress(scaleDown = 0.985f)
                .clip(RoundedCornerShape(28.dp))
                .background(getHeroCardGradient())
                .border(
                    BorderStroke(1.2.dp, heroGlowBorder),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            // Canvas decorative overlapping ambient circles for fintech polish
            Box(modifier = Modifier.matchParentSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.07f),
                        radius = 120.dp.toPx(),
                        center = Offset(size.width - 15.dp.toPx(), -15.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = 175.dp.toPx(),
                        center = Offset(size.width - 5.dp.toPx(), 25.dp.toPx())
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "TOTAL NET BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        if (isPrivacyMode) {
                            IconButton(
                                onClick = { viewModel.togglePrivacyReveal() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (shouldHideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle privacy reveal",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (overallTotalNetBalance >= 0) Color(0xFF10B981).copy(alpha = 0.28f)
                                else Color(0xFFEF4444).copy(alpha = 0.28f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (overallTotalNetBalance >= 0) Color(0xFF10B981).copy(alpha = 0.6f)
                                    else Color(0xFFEF4444).copy(alpha = 0.6f)
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (overallTotalNetBalance >= 0) "Safe Balance" else "Overdrawn",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (shouldHideBalance) "$currencySymbol ••••••" else String.format("%s%s%,.2f", if (animatedBalance >= 0) "" else "-", currencySymbol, Math.abs(animatedBalance)),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )

                if (totalSavingsGoalsMoney > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (shouldHideBalance) "Includes $currencySymbol•••• in Savings Goals" else String.format("Includes %s%,.0f in Savings Goals", currencySymbol, totalSavingsGoalsMoney),
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Breakdown row: Inflow vs Outflow with frosted capsules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Income Capsule
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "INCOME",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (shouldHideBalance) "$currencySymbol••••" else String.format("%s%,.0f", currencySymbol, animatedIncome),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Expense Capsule
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EXPENSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (shouldHideBalance) "$currencySymbol••••" else String.format("%s%,.0f", currencySymbol, animatedExpense),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isDecrease = thisMonthTotal <= lastMonthTotal
                    val pillBg = if (isDecrease) Color(0xFF10B981).copy(alpha = 0.28f) else Color(0xFFEF4444).copy(alpha = 0.28f)
                    val pillTextColor = Color.White
                    val prefixSign = if (isDecrease) "-" else "+"

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(pillBg)
                            .border(
                                BorderStroke(1.dp, if (isDecrease) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)),
                                CircleShape
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = String.format("%s%.1f%%", prefixSign, Math.abs(diffPct)),
                            color = pillTextColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = String.format("vs last month (%s%,.0f)", currencySymbol, lastMonthTotal),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 💡 1. Daily Smart Financial Insight
        DailyFinancialInsightWidget(
            thisMonthIncome = thisMonthIncomeTotal,
            thisMonthExpense = thisMonthTotal,
            monthlyBudget = monthlyBudget,
            streakCount = currentStreak,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ⚡ 2. Quick Shortcuts Feed (Non-repeating 1-tap actions + "More" to open All Shortcuts Launcher)
        QuickServicesCategorySection(
            viewModel = viewModel,
            selectedLanguage = selectedLanguage,
            onAddExpense = onAddExpenseClick,
            onAddIncome = onAddIncomeClick,
            onQuickSplit = { showQuickSplitDialog = true },
            onQuickConvert = { showQuickConvertDialog = true },
            onMoreClick = { showAllShortcutsScreen = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 🧾 4. Upcoming Bills Widget with inline "Pay Now"
        if (billShowUpcomingDashboard) {
            UpcomingBillsDashboardWidget(
                bills = viewModel.billsList,
                currencySymbol = currencySymbol,
                onPayBill = { bill ->
                    val totalBal = accounts.sumOf { it.balance }
                    if (bill.amount > totalBal && totalBal > 0) {
                        Toast.makeText(context, "Cannot pay: amount exceeds total balance!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addExpense(
                            amount = bill.amount,
                            category = "Bills",
                            date = System.currentTimeMillis(),
                            note = "Paid ${bill.title}",
                            type = "EXPENSE",
                            currencyCode = selectedCurrencyCode
                        )
                        viewModel.billsList.remove(bill)
                        Toast.makeText(context, "Paid ${bill.title} successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                onViewAllBills = { showBillsScreen = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. Smart Budget Health Gauge Ring
        if (monthlyBudget > 0) {
            SmartBudgetHealthGaugeWidget(
                monthlyBudget = monthlyBudget,
                currentMonthExpense = thisMonthTotal,
                currencySymbol = currencySymbol,
                onUpdateBudget = { showAdjustBudgetDialog = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
        } else {
            MonthlyBudgetSnapshotCard(
                monthlyBudget = monthlyBudget,
                currentMonthExpense = thisMonthTotal,
                currencySymbol = currencySymbol,
                onUpdateBudget = onUpdateBudget
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 🌟 Dynamic Financial Health Score & Diagnostics Widget
        val overdueBillsCount = remember(viewModel.billsList.toList()) {
            val nowTime = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            viewModel.billsList.count { bill ->
                try {
                    val parsed = sdf.parse(bill.dueDate)
                    parsed != null && parsed.time < nowTime
                } catch (e: Exception) {
                    false
                }
            }
        }
        FinancialHealthScoreWidget(
            incomeThisMonth = thisMonthIncomeTotal,
            expenseThisMonth = thisMonthTotal,
            monthlyBudget = monthlyBudget,
            overdueBillsCount = overdueBillsCount,
            currencySymbol = currencySymbol,
            onExploreBudget = { showAdjustBudgetDialog = true }
        )
        Spacer(modifier = Modifier.height(14.dp))

        // 🎯 7. Interactive Savings Goal Mini-Carousel
        if (savingsGoals.isNotEmpty()) {
            SavingsGoalsMiniCarouselWidget(
                goals = savingsGoals,
                currencySymbol = currencySymbol,
                onQuickDeposit = { goal, amount ->
                    viewModel.quickDepositToGoal(goal, amount)
                    Toast.makeText(context, "Deposited \$%.0f to \${goal.name}!".format(amount), Toast.LENGTH_SHORT).show()
                },
                onViewAllGoals = { showSavingGoalsScreen = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 📊 8. Monthly Top Spending Categories Widget
        if (thisMonthExpenses.isNotEmpty()) {
            TopSpendingCategoriesWidget(
                expensesThisMonth = thisMonthExpenses,
                categoryIcons = categoryIcons,
                currencySymbol = currencySymbol,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Recent Activity List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = SleekPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToExpenses() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activities recorded yet. Tap + to add!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary
                )
            }
        } else {
            // Show top 3 recent items
            expenses.take(3).forEach { expense ->
                RecentExpenseRow(
                    expense = expense,
                    categoryIcons = categoryIcons,
                    currencySymbol = currencySymbol,
                    onClick = { onEditExpenseClick(expense) }
                )
            }
        }
        Spacer(modifier = Modifier.height(90.dp))
    }

    if (showQuickDepositDialog != null) {
        val targetGoal = showQuickDepositDialog!!
        QuickGoalDepositDialog(
            goal = targetGoal,
            currencySymbol = currencySymbol,
            onDismiss = { showQuickDepositDialog = null },
            onConfirmDeposit = { amt ->
                viewModel.quickDepositToGoal(targetGoal, amt)
                showQuickDepositDialog = null
                Toast.makeText(context, "Deposited to ${targetGoal.name} successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showQuickSplitDialog) {
        QuickSplitBillDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showQuickSplitDialog = false },
            onRecordExpense = { amt, note ->
                viewModel.addExpense(
                    amount = amt,
                    category = "Food & Dining",
                    date = System.currentTimeMillis(),
                    note = note,
                    type = "EXPENSE",
                    currencyCode = selectedCurrencyCode
                )
                Toast.makeText(context, "Recorded split expense of $currencySymbol${String.format(Locale.getDefault(), "%,.2f", amt)}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showQuickConvertDialog) {
        QuickCurrencyConvertDialog(
            currencyCode = selectedCurrencyCode,
            onDismiss = { showQuickConvertDialog = false },
            onOpenFullSettings = {
                showQuickConvertDialog = false
                onOpenSettingsSubScreen(SettingsSubScreen.Currency)
            }
        )
    }

    if (showAdjustBudgetDialog) {
        var budgetInput by remember { mutableStateOf(if (monthlyBudget > 0) monthlyBudget.toInt().toString() else "") }
        Dialog(onDismissRequest = { showAdjustBudgetDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Set Monthly Budget Target",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Your daily spending allowance will automatically adapt based on the days remaining in the month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Budget Target ($currencySymbol)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAdjustBudgetDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }
                        Button(
                            onClick = {
                                val amt = budgetInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onUpdateBudget(amt)
                                    showAdjustBudgetDialog = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showChangeNameDialog) {
        Dialog(onDismissRequest = { showChangeNameDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                var newName by remember { mutableStateOf(userName ?: "") }
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Update Your Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Your Name", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedLabelColor = SleekPrimary,
                            unfocusedLabelColor = SleekTextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangeNameDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newName.trim().isNotEmpty()) {
                                    onUpdateName(newName.trim())
                                    showChangeNameDialog = false
                                }
                            },
                            enabled = newName.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(110.dp))
    }
}

// ==========================================
// 🎨 QUICK SHORTCUTS & CATEGORY FEED
// ==========================================
@Composable
fun QuickServicesCategorySection(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    selectedLanguage: String = "English",
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onQuickSplit: () -> Unit,
    onQuickConvert: () -> Unit,
    onMoreClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_services_category_feed")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = LanguageManager.tr("Quick Shortcuts", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onMoreClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "All Tools",
                        style = MaterialTheme.typography.labelMedium,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryFeedTile(
                    icon = Icons.Rounded.TrendingDown,
                    label = "+ Expense",
                    tileColor = Color(0xFFEF4444),
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.TrendingUp,
                    label = "+ Income",
                    tileColor = Color(0xFF10B981),
                    onClick = onAddIncome,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.CallSplit,
                    label = "Split Bill",
                    tileColor = Color(0xFF6366F1),
                    onClick = onQuickSplit,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.CurrencyExchange,
                    label = "Convert",
                    tileColor = Color(0xFF0EA5E9),
                    onClick = onQuickConvert,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.GridView,
                    label = "More",
                    tileColor = Color(0xFFF59E0B),
                    onClick = onMoreClick,
                    modifier = Modifier.weight(1f),
                    hasBadge = true
                )
            }
        }
    }
}

@Composable
fun CategoryFeedTile(
    icon: ImageVector,
    label: String,
    tileColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBadge: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tile_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(tileColor.copy(alpha = 0.14f))
                .border(BorderStroke(1.dp, tileColor.copy(alpha = 0.22f)), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tileColor,
                modifier = Modifier.size(26.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(1.5.dp, SleekSurface, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = SleekTextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MonthlyBudgetSnapshotCard(
    monthlyBudget: Double,
    currentMonthExpense: Double,
    currencySymbol: String = "₹",
    onUpdateBudget: (Double) -> Unit
) {
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_monthly_budget_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (monthlyBudget > 0) "Current month burn rate" else "No target configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = { showEditBudgetDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (monthlyBudget > 0) "Adjust" else "Set Target",
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (monthlyBudget > 0) {
                val spentPct = ((currentMonthExpense / monthlyBudget) * 100).coerceAtLeast(0.0)
                val remaining = monthlyBudget - currentMonthExpense
                val isExceeded = remaining < 0
                val progressFraction = (currentMonthExpense / monthlyBudget).toFloat().coerceIn(0f, 1f)

                val statusColor = when {
                    isExceeded -> Color(0xFFEF4444)
                    spentPct >= 80 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Spent this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%s%,.0f", currencySymbol, currentMonthExpense),
                            style = MaterialTheme.typography.titleLarge,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isExceeded) "Exceeded by ${String.format("%s%,.0f", currencySymbol, Math.abs(remaining))}"
                                else String.format("%.0f%% used", spentPct),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (!isExceeded) String.format("%s%,.0f remaining", currencySymbol, remaining) else "Limit: ${String.format("%s%,.0f", currencySymbol, monthlyBudget)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Sleek progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(SleekBorder.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(statusColor.copy(alpha = 0.8f), statusColor)
                                )
                            )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SleekBorder.copy(alpha = 0.25f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set a monthly target to visualize burn rate and receive pace alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showEditBudgetDialog) {
        var inputBudget by remember { mutableStateOf(if (monthlyBudget > 0) String.format(Locale.getDefault(), "%.0f", monthlyBudget) else "") }
        Dialog(onDismissRequest = { showEditBudgetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputBudget,
                        onValueChange = { inputBudget = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Budget Cap ($currencySymbol)", color = SleekTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditBudgetDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }
                        Button(
                            onClick = {
                                val value = inputBudget.toDoubleOrNull() ?: 0.0
                                onUpdateBudget(value)
                                showEditBudgetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentExpenseRow(
    expense: Expense,
    categoryIcons: Map<String, String> = emptyMap(),
    currencySymbol: String = "₹",
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    val isIncome = expense.type == "INCOME"
    val catColor = if (isIncome) {
        when (expense.category) {
            "Salary" -> Color(0xFF10B981)
            "Freelance" -> Color(0xFF0D9488)
            "Investments" -> Color(0xFF3B82F6)
            "Gifts" -> Color(0xFFEC4899)
            else -> Color(0xFF10B981)
        }
    } else {
        categoryColors[expense.category] ?: SleekPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(catColor.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, catColor.copy(alpha = 0.25f)), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category, categoryIcons),
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: (if (isIncome) "Income" else "Expense"),
                    style = MaterialTheme.typography.titleSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }

            Text(
                text = String.format("%s%s%,.2f", if (isIncome) "+" else "-", currencySymbol, expense.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIncome) Color(0xFF10B981) else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 🎯 TRANSACTIONS TAB (IMAGE 1 DESIGN)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpensesTab(
    viewModel: FinanceViewModel,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit
) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val categoryIcons by viewModel.categoryIcons.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedAccountIdFilter by remember { mutableStateOf<Int?>(null) }
    
    val rawCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val categories = remember(rawCategories) { listOf("All") + rawCategories }

    var selectedExpenseIds by remember { mutableStateOf(setOf<Long>()) }
    var showDateRangePickerDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }

    // Filtered expenses list
    val filteredExpenses = expenses.filter {
        val matchesSearch = it.note?.contains(searchQuery, ignoreCase = true) == true ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    // Date grouping logic for Image 1 (Today, Yesterday, 19 November, etc.)
    val groupedExpenses = remember(filteredExpenses) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calYesterday.time)

        filteredExpenses.groupBy { expense ->
            val expenseDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expense.date))
            when (expenseDateStr) {
                todayStr -> "Today"
                yesterdayStr -> "Yesterday"
                else -> SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(expense.date))
            }
        }
    }

    // Use standard sleek background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Image 1 Top Navigation Header: [<] Transactions [Search]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Icon Button / Clear Filter
                IconButton(
                    onClick = {
                        if (selectedDateRange != null || selectedAccountIdFilter != null || searchQuery.isNotEmpty()) {
                            viewModel.setDateRange(null, null)
                            selectedAccountIdFilter = null
                            searchQuery = ""
                            isSearchActive = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekSurface.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                // Search Icon Button
                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekSurface.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Transactions",
                        tint = SleekTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))



            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar Input (Animated Toggle)
            AnimatedVisibility(visible = isSearchActive) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search merchant, recipient, or category...", color = SleekTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SleekTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val selected = selectedCategoryFilter == cat
                    val chipBg = if (selected) SleekPrimary else SleekSurface
                    val chipText = if (selected) Color.White else SleekTextSecondary
                    val chipBorder = if (!selected) BorderStroke(1.dp, SleekBorder) else null

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(chipBg)
                            .then(if (chipBorder != null) Modifier.border(chipBorder, RoundedCornerShape(14.dp)) else Modifier)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            color = chipText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        color = SleekTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedExpenses.forEach { (dateHeader, txList) ->
                        item {
                            // Section Date Header (Image 1 Style: "Today", "Yesterday", "19 November")
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = SleekTextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        items(txList, key = { it.id }) { expense ->
                            val isSelected = selectedExpenseIds.contains(expense.id)
                            Image1TransactionRow(
                                expense = expense,
                                isSelected = isSelected,
                                categoryIcons = categoryIcons,
                                currencySymbol = currencySymbol,
                                onLongClick = {
                                    selectedExpenseIds = if (isSelected) {
                                        selectedExpenseIds - expense.id
                                    } else {
                                        selectedExpenseIds + expense.id
                                    }
                                },
                                onClick = {
                                    if (selectedExpenseIds.isNotEmpty()) {
                                        selectedExpenseIds = if (isSelected) {
                                            selectedExpenseIds - expense.id
                                        } else {
                                            selectedExpenseIds + expense.id
                                        }
                                    } else {
                                        onEditExpenseClick(expense)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Selected Expense Action Bar (Multi-Select & Delete/Edit)
        if (selectedExpenseIds.isNotEmpty()) {
            val selectedExpenses = filteredExpenses.filter { selectedExpenseIds.contains(it.id) }
            if (selectedExpenses.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedExpenses.size} items selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedExpenses.size == 1) {
                                IconButton(
                                    onClick = {
                                        onEditExpenseClick(selectedExpenses.first())
                                        selectedExpenseIds = emptySet()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteExpenses(selectedExpenses)
                                    selectedExpenseIds = emptySet()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ExpenseRed.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { selectedExpenseIds = emptySet() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deselect All", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showDateRangePickerDialog) {
            DateRangePickerDialog(
                onDismiss = { showDateRangePickerDialog = false },
                onSelectRange = { start, end ->
                    viewModel.setDateRange(start, end)
                    showDateRangePickerDialog = false
                }
            )
        }
    }
}

/**
 * Image 1 Inspired Transaction Row Card:
 * Soft white squircle, circular avatar/logo on left, title & Received/Paid status in middle,
 * bright green (+$5,710.20) or dark red (-$124.55) amount on right.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Image1TransactionRow(
    expense: Expense,
    isSelected: Boolean,
    categoryIcons: Map<String, String> = emptyMap(),
    currencySymbol: String = "₹",
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val isIncome = expense.type == "INCOME"
    val displayName = expense.note?.takeIf { it.isNotBlank() } ?: expense.category

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(2.dp, SleekPrimary) else BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Avatar / Brand Circle (Image 1 visual)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) IncomeGreenBg else SleekBg
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Initial character or category icon
                val firstChar = displayName.trim().firstOrNull()?.uppercaseChar() ?: 'T'
                if (displayName.contains("Eva", ignoreCase = true) ||
                    displayName.contains("Henrik", ignoreCase = true) ||
                    displayName.contains("Matteo", ignoreCase = true) ||
                    displayName.contains("Emilia", ignoreCase = true)) {
                    Text(
                        text = firstChar.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIcon(expense.category, categoryIcons),
                        contentDescription = expense.category,
                        tint = if (isIncome) IncomeGreen else SleekTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center: Title + "Received ⏱" / "Paid ⏱"
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isIncome) "Received" else "Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Right: Amount +₹5,710.20 (Green) / -₹124.55 (Red)
            Text(
                text = String.format("%s%s%,.2f", if (isIncome) "+" else "-", currencySymbol, expense.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onSelectRange: (Long, Long) -> Unit
) {
    var startCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }
    var endCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        })
    }

    val sFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Quick Presets",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        "Today" to {
                            val s = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "This Week" to {
                            val s = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "This Month" to {
                            val s = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "Last 30 Days" to {
                            val e = Calendar.getInstance()
                            val s = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "Last 90 Days" to {
                            val e = Calendar.getInstance()
                            val s = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        }
                    )

                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowPresets.forEach { preset ->
                                OutlinedButton(
                                    onClick = preset.second,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                                    border = BorderStroke(1.dp, SleekBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(preset.first, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = SleekBorder)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Custom Range Selection",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start Date:", style = MaterialTheme.typography.bodyMedium, color = SleekTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = startCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            startCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = SleekPrimary)
                        }
                        Text(
                            text = sFormatter.format(Date(startCalendar.timeInMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = startCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            startCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = SleekPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End Date:", style = MaterialTheme.typography.bodyMedium, color = SleekTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = endCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            endCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = SleekPrimary)
                        }
                        Text(
                            text = sFormatter.format(Date(endCalendar.timeInMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = endCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            endCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = SleekPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSelectRange(startCalendar.timeInMillis, endCalendar.timeInMillis)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color = SleekSurface,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==========================================
// 3️⃣ ANALYTICS TAB (REWORKED WITH DEFAULT APP STYLE & ANIMATED CHARTS)
// ==========================================
@Composable
fun AnalyticsTab(
    viewModel: FinanceViewModel,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onExpenseClick: (Expense) -> Unit = {}
) {
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val categoryIcons by viewModel.categoryIcons.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val statsCurrencyCode by viewModel.statsCurrencyCode.collectAsStateWithLifecycle()
    val statsCurrencySymbol by viewModel.statsCurrencySymbol.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences(com.example.data.AppSettingsManager.PREFS_NAME, Context.MODE_PRIVATE)
    }
    // Filter presets: 7D, 1M, 6M, 1Y, All
    val timeFilters = listOf("7D", "1M", "6M", "1Y", "All")
    var selectedTimeFilter by remember {
        val savedPreset = sharedPrefs.getString("analytics_time_filter", "7D") ?: "7D"
        val effectivePreset = if (savedPreset == "30D") "1M" else savedPreset
        mutableStateOf(if (effectivePreset in timeFilters || effectivePreset == "Custom") effectivePreset else "7D")
    }
    var customStartDateMs by remember { mutableStateOf<Long?>(null) }
    var customEndDateMs by remember { mutableStateOf<Long?>(null) }
    var showCustomDateRangePicker by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var analyticsMode by rememberSaveable { mutableStateOf(AnalyticsViewMode.QUICK_STATS) }

    var inspectionMetric by remember { mutableStateOf<MetricInspectionData?>(null) }
    var activeCategoryDrillDown by remember { mutableStateOf<CategoryTrendSummary?>(null) }

    val initials = remember(userName) {
        if (!userName.isNullOrBlank()) {
            userName!!.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        } else "U"
    }

    // Aligned date range calculation for each filter preset
    val periodDateRange = remember(selectedTimeFilter, allExpenses, customStartDateMs, customEndDateMs) {
        if (selectedTimeFilter == "Custom" && customStartDateMs != null && customEndDateMs != null) {
            Pair(customStartDateMs!!, customEndDateMs!!)
        } else {
            val endCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val end = endCal.timeInMillis
            val startCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = when (selectedTimeFilter) {
                "7D" -> startCal.apply { add(Calendar.DAY_OF_YEAR, -6) }.timeInMillis
                "1M" -> startCal.apply { add(Calendar.MONTH, -1) }.timeInMillis
                "6M" -> startCal.apply { add(Calendar.MONTH, -6) }.timeInMillis
                "1Y" -> startCal.apply { add(Calendar.YEAR, -1) }.timeInMillis
                else -> {
                    val minExpenseDate = allExpenses.minOfOrNull { it.date } ?: startCal.timeInMillis
                    minOf(minExpenseDate, startCal.timeInMillis)
                }
            }
            Pair(start, end)
        }
    }

    // Filter expenses based on selected time filter's strictly aligned date boundaries
    val filteredPeriodExpenses = remember(allExpenses, periodDateRange) {
        val (start, end) = periodDateRange
        allExpenses.filter { it.date in start..end }
    }

    // Immediately preceding comparative period of identical length
    val prevDateRange = remember(periodDateRange) {
        AnalyticsCalculator.computePreviousDateRange(periodDateRange.first, periodDateRange.second)
    }

    val previousPeriodExpenses = remember(allExpenses, prevDateRange) {
        val (pStart, pEnd) = prevDateRange
        allExpenses.filter { it.date in pStart..pEnd }
    }

    val prevPeriodLabel = remember(selectedTimeFilter, prevDateRange) {
        when (selectedTimeFilter) {
            "7D" -> "prev 7 days"
            "1M" -> "prev month"
            "6M" -> "prev 6 mos"
            "1Y" -> "prev year"
            else -> "prev period"
        }
    }

    val comparisonSummary = remember(filteredPeriodExpenses, previousPeriodExpenses, statsCurrencyCode, prevPeriodLabel) {
        AnalyticsCalculator.computeComparison(filteredPeriodExpenses, previousPeriodExpenses, statsCurrencyCode, prevPeriodLabel)
    }

    val categoryTrends = remember(filteredPeriodExpenses, previousPeriodExpenses, statsCurrencyCode) {
        AnalyticsCalculator.computeCategoryTrends(filteredPeriodExpenses, previousPeriodExpenses, statsCurrencyCode)
    }

    val spendingPatterns = remember(filteredPeriodExpenses, statsCurrencyCode) {
        AnalyticsCalculator.computeSpendingPatterns(filteredPeriodExpenses, statsCurrencyCode)
    }

    val budgetPerformance = remember(filteredPeriodExpenses, budgets, statsCurrencyCode) {
        AnalyticsCalculator.computeBudgetPerformance(filteredPeriodExpenses, budgets, statsCurrencyCode)
    }

    val periodDays = remember(periodDateRange) {
        val (start, end) = periodDateRange
        val diffMs = (end - start).coerceAtLeast(86400000L)
        (diffMs / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
    }
    val dailyAvg = if (periodDays > 0) comparisonSummary.currentSpent / periodDays else 0.0

    val dateRangeLabel = remember(periodDateRange, selectedTimeFilter) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val startStr = sdf.format(Date(periodDateRange.first))
        val endStr = sdf.format(Date(periodDateRange.second))
        if (selectedTimeFilter == "All") "All Time" else "$startStr – $endStr"
    }

    val categoryColors = remember {
        mapOf(
            "Food" to Color(0xFFF97316),
            "Travel" to Color(0xFF06B6D4),
            "Rent" to Color(0xFF8B5CF6),
            "Utilities" to Color(0xFFEC4899),
            "Entertainment" to Color(0xFF3B82F6),
            "Shopping" to Color(0xFF10B981),
            "Home" to Color(0xFFF59E0B),
            "Bills" to Color(0xFFEF4444),
            "Persons" to Color(0xFF6366F1),
            "Salary" to Color(0xFF10B981),
            "Freelance" to Color(0xFF06B6D4),
            "Investments" to Color(0xFF8B5CF6),
            "Gifts" to Color(0xFFEC4899),
            "Others" to Color(0xFF64748B)
        )
    }

    if (showCustomDateRangePicker) {
        CustomDateRangePickerDialog(
            initialStartMs = periodDateRange.first,
            initialEndMs = periodDateRange.second,
            onDismiss = { showCustomDateRangePicker = false },
            onApplyRange = { start, end ->
                customStartDateMs = start
                customEndDateMs = end
                selectedTimeFilter = "Custom"
                showCustomDateRangePicker = false
            }
        )
    }

    if (inspectionMetric != null) {
        MetricInspectionModal(
            data = inspectionMetric!!,
            onDismiss = { inspectionMetric = null },
            onExpenseClick = onExpenseClick
        )
    }

    if (activeCategoryDrillDown != null) {
        CategoryDrillDownDialog(
            item = activeCategoryDrillDown!!,
            currencySymbol = currencySymbol,
            onDismiss = { activeCategoryDrillDown = null },
            onExpenseClick = onExpenseClick
        )
    }

    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.5.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                            }
                            Text("Export Report", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SleekTextPrimary)
                        }
                        IconButton(onClick = { showExportDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Text("Export analytics data for $selectedTimeFilter ($dateRangeLabel):", fontSize = 13.sp, color = SleekTextSecondary)

                    Button(
                        onClick = {
                            DataExporter.sharePdfReport(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "$selectedTimeFilter ($dateRangeLabel)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories",
                                amountSaved = (comparisonSummary.currentIncome - comparisonSummary.currentSpent).coerceAtLeast(0.0),
                                monthsOverBudget = 0,
                                includeDetailedTxns = true
                            )
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF Document", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            DataExporter.exportToCSV(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "$selectedTimeFilter ($dateRangeLabel)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories"
                            )
                            showExportDialog = false
                        },
                        border = BorderStroke(1.dp, SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV Spreadsheet", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            DataExporter.exportToJson(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "$selectedTimeFilter ($dateRangeLabel)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories"
                            )
                            showExportDialog = false
                        },
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export JSON Document", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            DataExporter.shareImageReport(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "Period ($selectedTimeFilter)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories"
                            )
                            showExportDialog = false
                        },
                        border = BorderStroke(1.dp, SleekBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, tint = SleekTextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Graphic Summary", color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // App Default Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!profileImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary,
                        fontSize = 15.sp
                    )
                }
            }

            Text(
                text = "Analytics",
                style = MaterialTheme.typography.titleLarge,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Report",
                    tint = SleekTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Mode & Filter Switcher
        InteractiveAnalyticsHeader(
            currentMode = analyticsMode,
            onModeChange = { analyticsMode = it },
            selectedTimeFilter = selectedTimeFilter,
            onTimeFilterChange = { tf ->
                selectedTimeFilter = tf
                if (tf != "Custom") {
                    sharedPrefs.edit().putString("analytics_time_filter", tf).apply()
                }
            },
            timeFilterPresets = timeFilters,
            onCustomDateRangeClick = { showCustomDateRangePicker = true },
            onExportClick = { showExportDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Aligned Date Range Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = dateRangeLabel,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${filteredPeriodExpenses.size} transactions",
                fontSize = 11.sp,
                color = SleekTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-currency normalization banner if currencies differ
        val hasMultiCurrency = remember(filteredPeriodExpenses, statsCurrencyCode) {
            filteredPeriodExpenses.any { !it.currencyCode.equals(statsCurrencyCode, ignoreCase = true) }
        }
        if (hasMultiCurrency) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SleekPrimaryContainer.copy(alpha = 0.25f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "💱 Multi-currency values converted & normalized to $statsCurrencyCode for exact calculations.",
                        fontSize = 10.5.sp,
                        color = SleekTextPrimary
                    )
                }
            }
        }

        // Render according to selected mode with fluid animated transitions
        AnimatedContent(
            targetState = analyticsMode,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                 slideInVertically(initialOffsetY = { 20 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                (fadeOut(animationSpec = tween(150)))
            },
            label = "AnalyticsModeTransition"
        ) { mode ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (mode) {
                    AnalyticsViewMode.QUICK_STATS -> {
                        // 1. Interactive Scorecards with Enlarge-On-Tap
                        InteractiveScorecardRow(
                            comparison = comparisonSummary,
                            currencySymbol = currencySymbol,
                            onMetricClick = { inspectionMetric = it },
                            currentExpenses = filteredPeriodExpenses,
                            previousExpenses = previousPeriodExpenses,
                            dateRangeLabel = dateRangeLabel
                        )

                        // 2. Secondary Metrics Bar (Activity count, Daily Average, Savings Rate)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekSurface)
                                .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Activity", fontSize = 10.sp, color = SleekTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${filteredPeriodExpenses.size} txs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(SleekBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily Avg", fontSize = 10.sp, color = SleekTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currencySymbol%,.0f".format(dailyAvg),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(SleekBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Savings Rate", fontSize = 10.sp, color = SleekTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${comparisonSummary.currentSavingsRate.roundToInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (comparisonSummary.currentSavingsRate >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }

                        // 3. Cash Flow Waterfall Analysis
                        CashFlowWaterfallCard(
                            comparison = comparisonSummary,
                            currencySymbol = currencySymbol
                        )

                        // 4. Top Category Trends & Velocity (tap to drill down)
                        CategoryTrendsSection(
                            categoryTrends = categoryTrends.take(5),
                            currencySymbol = currencySymbol,
                            categoryColors = categoryColors,
                            onCategoryClick = { activeCategoryDrillDown = it }
                        )

                        // 5. Net Worth Over Time Chart Card
                        NetWorthOverTimeChartCard(
                            allExpenses = filteredPeriodExpenses,
                            accounts = accounts,
                            selectedTimeFilter = selectedTimeFilter,
                            currencySymbol = currencySymbol
                        )

                        // 6. Period Transactions Card
                        AnalyticsPeriodTransactionsCard(
                            expenses = filteredPeriodExpenses,
                            selectedTimeFilter = selectedTimeFilter,
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol,
                            categoryIcons = categoryIcons,
                            onExpenseClick = onExpenseClick
                        )
                    }

                    AnalyticsViewMode.DEEP_ANALYTICS -> {
                        // 1. Full Category Trends & Velocity (tap to drill down)
                        CategoryTrendsSection(
                            categoryTrends = categoryTrends,
                            currencySymbol = currencySymbol,
                            categoryColors = categoryColors,
                            onCategoryClick = { activeCategoryDrillDown = it }
                        )

                        // 2. Budget Performance & Variance
                        BudgetPerformanceSection(
                            budgetPerformance = budgetPerformance,
                            currencySymbol = currencySymbol
                        )

                        // 3. Account-wise Analytics & Distribution
                        AccountWiseAnalyticsSection(
                            accounts = accounts,
                            expenses = filteredPeriodExpenses,
                            currencySymbol = currencySymbol
                        )

                        // 4. Spending Rhythm & Spike Detection
                        SpendingPatternDetectionSection(
                            pattern = spendingPatterns,
                            currencySymbol = currencySymbol
                        )

                        // 5. Tag & Note Spending Bar Chart
                        TagSpendingBarChart(
                            expenses = filteredPeriodExpenses,
                            categoryColors = categoryColors,
                            periodLabel = "$selectedTimeFilter ($dateRangeLabel)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 6. Period Transactions
                        AnalyticsPeriodTransactionsCard(
                            expenses = filteredPeriodExpenses,
                            selectedTimeFilter = selectedTimeFilter,
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol,
                            categoryIcons = categoryIcons,
                            onExpenseClick = onExpenseClick
                        )
                    }

                    AnalyticsViewMode.TRENDS_CASHFLOW -> {
                        // 1. Interactive Spline/Bar Spending Trends
                        SpendingTrendsScreen(
                            expenses = allExpenses,
                            currencySymbol = currencySymbol,
                            categoryColors = categoryColors,
                            onExpenseClick = onExpenseClick
                        )

                        // 2. Cash Flow Waterfall
                        CashFlowWaterfallCard(
                            comparison = comparisonSummary,
                            currencySymbol = currencySymbol
                        )

                        // 3. Net Worth Over Time Chart Card
                        NetWorthOverTimeChartCard(
                            allExpenses = filteredPeriodExpenses,
                            accounts = accounts,
                            selectedTimeFilter = selectedTimeFilter,
                            currencySymbol = currencySymbol
                        )

                        // 4. Period Transactions
                        AnalyticsPeriodTransactionsCard(
                            expenses = filteredPeriodExpenses,
                            selectedTimeFilter = selectedTimeFilter,
                            dateRangeLabel = dateRangeLabel,
                            currencySymbol = currencySymbol,
                            categoryIcons = categoryIcons,
                            onExpenseClick = onExpenseClick
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

// ==========================================
// 📋 ANALYTICS PERIOD TRANSACTIONS CARD
// ==========================================
@Composable
fun AnalyticsPeriodTransactionsCard(
    expenses: List<Expense>,
    selectedTimeFilter: String,
    dateRangeLabel: String = "",
    currencySymbol: String,
    categoryIcons: Map<String, String>,
    onExpenseClick: (Expense) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember(selectedTimeFilter) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_period_transactions_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions in Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = if (dateRangeLabel.isNotBlank()) "$selectedTimeFilter • $dateRangeLabel • ${expenses.size} records" else "$selectedTimeFilter • ${expenses.size} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${expenses.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found for $selectedTimeFilter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )
                }
            } else {
                val sorted = remember(expenses) { expenses.sortedByDescending { it.date } }
                val displayCount = if (isExpanded) sorted.size else minOf(6, sorted.size)
                val visibleItems = remember(sorted, displayCount) { sorted.take(displayCount) }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleItems.forEach { exp ->
                        val isExpense = exp.type != "INCOME" && exp.category != "Locked Savings"
                        val amountColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
                        val sign = if (isExpense) "-" else "+"
                        val emoji = getCategoryEmoji(exp.category, categoryIcons)
                        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(exp.date))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SleekNeutralLight.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, SleekBorder.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onExpenseClick(exp)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(amountColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji.ifBlank { if (isExpense) "💸" else "💰" }, fontSize = 16.sp)
                                    }
                                    Column {
                                        Text(
                                            text = if (!exp.note.isNullOrBlank()) exp.note else exp.category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SleekTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${exp.category} • $dateStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SleekTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "$sign$currencySymbol%,.2f".format(exp.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor
                                )
                            }
                        }
                    }

                    if (sorted.size > 6) {
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isExpanded = !isExpanded
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (isExpanded) "Show Less" else "View All (${sorted.size})",
                                color = SleekPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 💎 NET WORTH OVER TIME CHART CARD
// ==========================================
@Composable
fun NetWorthOverTimeChartCard(
    allExpenses: List<Expense>,
    accounts: List<Account>,
    selectedTimeFilter: String,
    currencySymbol: String,
    currencyCode: String = "INR"
) {
    val totalAccountAssets = remember(accounts) {
        accounts.sumOf { if (it.type == "CREDIT") -it.balance else it.balance }
    }

    val sortedExpenses = remember(allExpenses) { allExpenses.sortedBy { it.date } }

    val dataPoints = remember(sortedExpenses, totalAccountAssets, selectedTimeFilter, currencyCode) {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val periodStartMs = when (selectedTimeFilter) {
            "7D" -> now - 7 * dayMs
            "1M" -> now - 30 * dayMs
            "6M" -> now - 180 * dayMs
            "1Y" -> now - 365 * dayMs
            else -> sortedExpenses.firstOrNull()?.date ?: (now - 30 * dayMs)
        }
        if (sortedExpenses.isEmpty()) {
            listOf(
                Pair(periodStartMs, totalAccountAssets),
                Pair(now, totalAccountAssets)
            )
        } else {
            val points = mutableListOf<Pair<Long, Double>>()
            val totalIncome = sortedExpenses.realIncome(currencyCode)
            val totalExpense = sortedExpenses.realExpense(currencyCode)
            val initialNetWorth = if (accounts.isNotEmpty()) {
                totalAccountAssets - (totalIncome - totalExpense)
            } else {
                0.0
            }

            var runningFlow = initialNetWorth
            val firstDate = minOf(periodStartMs, (sortedExpenses.firstOrNull()?.date ?: now) - dayMs)
            points.add(Pair(firstDate, initialNetWorth))

            val step = (sortedExpenses.size / 7).coerceAtLeast(1)
            sortedExpenses.chunked(step).take(7).forEach { chunk ->
                val date = chunk.last().date
                chunk.forEach { e ->
                    val converted = CurrencyManager.convert(e.amount, e.currencyCode, currencyCode)
                    if (e.type == "INCOME") runningFlow += converted else runningFlow -= converted
                }
                points.add(Pair(date, runningFlow))
            }
            if (points.size < 2) {
                points.add(0, Pair(periodStartMs, initialNetWorth))
            }
            points
        }
    }

    val currentNetWorth = dataPoints.lastOrNull()?.second ?: totalAccountAssets
    val startingNetWorth = dataPoints.firstOrNull()?.second ?: (currentNetWorth * 0.9)
    val changeAmount = currentNetWorth - startingNetWorth
    val changePct = if (startingNetWorth != 0.0) (changeAmount / startingNetWorth) * 100 else 0.0

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val activePoint = selectedPointIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SleekBorder),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Net Worth Over Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Accumulated assets minus liabilities",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (changeAmount >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (changeAmount >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (changeAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (changeAmount >= 0) "+" else ""}${String.format(Locale.US, "%.1f", changePct)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (changeAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = activePoint?.let {
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            sdf.format(Date(it.first))
                        } ?: "Current Value",
                        fontSize = 11.sp,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", activePoint?.second ?: currentNetWorth)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekTextPrimary
                    )
                }

                if (selectedPointIndex != null) {
                    TextButton(
                        onClick = { selectedPointIndex = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset view", fontSize = 11.sp, color = SleekPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val lineGraphColor = Color(0xFF10B981)
            val values = dataPoints.map { it.second }
            val minVal = (values.minOrNull() ?: 0.0) * 0.95
            val maxVal = ((values.maxOrNull() ?: 1000.0) * 1.05).coerceAtLeast(minVal + 100.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, dataPoints.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val pointsCount = dataPoints.size

                    if (pointsCount < 2) return@Canvas

                    val stepX = w / (pointsCount - 1)
                    val coords = dataPoints.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        val normalizedY = ((pt.second - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
                        val y = h - (normalizedY * (h - 20.dp.toPx())) - 10.dp.toPx()
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p1 = coords[i]
                            val p2 = coords[i + 1]
                            val cx = (p1.x + p2.x) / 2f
                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineGraphColor.copy(alpha = 0.35f),
                                lineGraphColor.copy(alpha = 0.02f)
                            )
                        )
                    )

                    drawPath(
                        path = path,
                        color = lineGraphColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    coords.forEachIndexed { idx, point ->
                        val isSelected = selectedPointIndex == idx || (selectedPointIndex == null && idx == coords.size - 1)
                        if (isSelected) {
                            drawCircle(
                                color = lineGraphColor.copy(alpha = 0.25f),
                                radius = 10.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineGraphColor,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekPrimaryContainer.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Net Worth", fontSize = 10.sp, color = SleekTextSecondary)
                    Text("$currencySymbol${String.format(Locale.US, "%,.0f", maxVal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(SleekBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Assets", fontSize = 10.sp, color = SleekTextSecondary)
                    Text("$currencySymbol${String.format(Locale.US, "%,.0f", totalAccountAssets)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

private data class GraphPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val timestamp: Long
)

// ==========================================
// 4️⃣.5️⃣ CATEGORY SELECTOR COMPONENTS
// ==========================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CategorySelectorGrid(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    categoryIcons: Map<String, String> = emptyMap(),
    onAddCustomCategoryClick: () -> Unit,
    onDeleteCustomCategory: ((String) -> Unit)? = null,
    onEditCustomCategory: ((String, String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            val emoji = getCategoryEmoji(cat, categoryIcons)
            
            val isDefault = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Persons", "Others", "Salary", "Freelance", "Investments", "Gifts").contains(cat)
            
            var showEditDeleteDialog by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCategorySelected(cat)
                        },
                        onLongClick = {
                            if (!isDefault && (onDeleteCustomCategory != null || onEditCustomCategory != null)) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showEditDeleteDialog = true
                            }
                        }
                    ),
                shape = CircleShape,
                color = if (isSelected) SleekPrimary.copy(alpha = 0.2f) else SleekNeutralLight,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) SleekPrimary else SleekBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(emoji, fontSize = 16.sp)
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) SleekPrimary else SleekTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    
                    if (!isDefault && isSelected) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            tint = SleekTextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { showEditDeleteDialog = true }
                        )
                    }
                }
            }
            
            if (showEditDeleteDialog) {
                ManageCategoryDialog(
                    categoryName = cat,
                    onDismiss = { showEditDeleteDialog = false },
                    onDelete = {
                        if (onDeleteCustomCategory != null) {
                            onDeleteCustomCategory(cat)
                        }
                        showEditDeleteDialog = false
                    },
                    onRename = { newName ->
                        if (onEditCustomCategory != null) {
                            onEditCustomCategory(cat, newName)
                        }
                        showEditDeleteDialog = false
                    }
                )
            }
        }
        
        if (onAddCustomCategoryClick != null) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onAddCustomCategoryClick() },
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Category",
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Add Custom",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ManageCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(categoryName) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showConfirmDelete) {
                    Text(
                        text = "Delete Category?",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Are you sure you want to delete \"$categoryName\"? Associated expenses will revert to \"Others\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmDelete = false },
                            border = BorderStroke(1.dp, SleekBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = "Manage Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Category Name", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showConfirmDelete = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (newName.trim().isNotEmpty() && newName.trim() != categoryName) {
                                    onRename(newName.trim())
                                }
                                onDismiss()
                            },
                            enabled = newName.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rename", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5️⃣ ADD EXPENSE DIALOG
// Defined in AddTransactionFlowComponents.kt
// ==========================================
@Composable
private fun AddExpenseDialogOld(
    prefilledDate: Long?,
    categories: List<String>,
    categoryIcons: Map<String, String> = emptyMap(),
    expenses: List<Expense> = emptyList(),
    currencyCode: String = "INR",
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onEditCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, date: Long, note: String, imagePath: String?, type: String) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountStr by remember { mutableStateOf("") }

    val totalIncome = remember(expenses, currencyCode) {
        expenses.realIncome(currencyCode)
    }
    val totalExpenses = remember(expenses, currencyCode) {
        expenses.realExpense(currencyCode)
    }
    val availableBalance = (totalIncome - totalExpenses).coerceAtLeast(0.0)

    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val isExceedingIncome = type == "EXPENSE" && (enteredAmount > availableBalance || totalExpenses + enteredAmount > totalIncome)
    
    val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Others")
    val currentCategoriesList = if (type == "INCOME") {
        incomeCategories
    } else {
        categories
    }
    
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Food") }
    var note by remember { mutableStateOf("") }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Automatically set default category when switching type
    LaunchedEffect(type) {
        category = if (type == "INCOME") "Salary" else (categories.firstOrNull() ?: "Food")
    }

    // Image/receipt selection states
    val context = LocalContext.current
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var editingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropperDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadFullResolutionBitmap(context, uri)
            if (bitmap != null) {
                editingBitmap = bitmap
                showCropperDialog = true
            } else {
                Toast.makeText(context, "Error loading full-resolution image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            editingBitmap = bitmap
            showCropperDialog = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (type == "INCOME") "Add New Income" else "Add New Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Segmented Switcher (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBorder.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "EXPENSE") SleekPrimary else Color.Transparent)
                            .clickable { type = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (type == "EXPENSE") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "INCOME") Color(0xFF10B981) else Color.Transparent)
                            .clickable { type = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (type == "INCOME") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prefilled Date Status Info
                if (prefilledDate != null) {
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(prefilledDate))
                    Text(
                        text = "Adding for: $dateFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimary.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Amount Input Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                if (type == "EXPENSE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Balance: ₹${String.format(Locale.getDefault(), "%,.2f", availableBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExceedingIncome) ExpenseRed else SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalIncome > 0) {
                            Text(
                                text = "Total Income: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    if (isExceedingIncome) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalIncome <= 0) 
                                        "Expense cannot exceed total income. Income is ₹0.00." 
                                    else 
                                        "Expense (₹${String.format(Locale.getDefault(), "%,.2f", enteredAmount)}) exceeds available bank balance (₹${String.format(Locale.getDefault(), "%,.2f", availableBalance)}).",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Total Income so far: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategorySelectorGrid(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = currentCategoriesList,
                    categoryIcons = categoryIcons,
                    onAddCustomCategoryClick = { showCreateCategoryDialog = true },
                    onDeleteCustomCategory = if (type == "EXPENSE") onDeleteCategory else null,
                    onEditCustomCategory = if (type == "EXPENSE") onEditCategory else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input")
                )
                if (note.trim().isEmpty()) {
                    Text(
                        "Description is required",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Attached image section in Add Dialog
                Text(
                    text = "Receipt Photo (Optional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (!attachedImagePath.isNullOrBlank() && File(attachedImagePath!!).exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(attachedImagePath!!),
                            contentDescription = "Attached Receipt",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { attachedImagePath = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Image", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val suppressAutoLock = LocalAutoLockSuppressor.current
                        Button(
                            onClick = {
                                suppressAutoLock()
                                cameraLauncher.launch()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", color = SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                suppressAutoLock()
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", color = SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && note.trim().isNotEmpty()) {
                                onConfirm(
                                    amount,
                                    category,
                                    prefilledDate ?: System.currentTimeMillis(),
                                    note.trim(),
                                    attachedImagePath,
                                    type
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && enteredAmount > 0.0 && !isExceedingIncome,
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCropperDialog && editingBitmap != null) {
        ImageEditDialog(
            initialBitmap = editingBitmap!!,
            onDismiss = { showCropperDialog = false },
            onSave = { savedPath ->
                showCropperDialog = false
                attachedImagePath = savedPath
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { newCat ->
                onAddCategory(newCat)
                category = newCat
                showCreateCategoryDialog = false
            }
        )
    }
}

// ==========================================
// 6️⃣ EDIT EXPENSE DIALOG
// ==========================================
@Composable
fun EditExpenseDialog(
    expense: Expense,
    categories: List<String>,
    expenseCategories: List<String> = emptyList(),
    incomeCategories: List<String> = emptyList(),
    categoryIcons: Map<String, String> = emptyMap(),
    currencySymbol: String = "₹",
    currencyCode: String = "INR",
    expenses: List<Expense> = emptyList(),
    onAddCategory: (name: String, categoryType: String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit,
    onEditCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var type by rememberSaveable { mutableStateOf(expense.type) }
    var amountStr by rememberSaveable { mutableStateOf(expense.amount.toString()) }

    val otherExpenses = remember(expenses, expense, currencyCode) {
        expenses.filter { it.id != expense.id }.realExpense(currencyCode)
    }
    val totalIncome = remember(expenses, expense, amountStr, type, currencyCode) {
        if (expense.type == "INCOME") {
            expenses.filter { it.id != expense.id }.realIncome(currencyCode) + (if (type == "INCOME") (amountStr.toDoubleOrNull() ?: 0.0) else 0.0)
        } else {
            expenses.realIncome(currencyCode)
        }
    }

    val availableBalanceForEdit = (totalIncome - otherExpenses).coerceAtLeast(0.0)
    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val isExceedingIncome = type == "EXPENSE" && (enteredAmount > availableBalanceForEdit || otherExpenses + enteredAmount > totalIncome)

    val defaultExpensePreset = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Home", "Others")
    val defaultIncomePreset = listOf("Salary", "Freelance", "Investments", "Gifts", "Others")
    val currentCategoriesList = if (type == "INCOME") {
        (defaultIncomePreset + (if (incomeCategories.isNotEmpty()) incomeCategories else categories.filter { defaultIncomePreset.contains(it) })).distinct()
    } else {
        (defaultExpensePreset + (if (expenseCategories.isNotEmpty()) expenseCategories else categories.filter { !defaultIncomePreset.contains(it) })).distinct()
    }
    
    var category by rememberSaveable { mutableStateOf(expense.category) }
    var note by rememberSaveable { mutableStateOf(expense.note ?: "") }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    var firstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(type) {
        if (firstLoad) {
            firstLoad = false
        } else {
            category = if (type == "INCOME") (currentCategoriesList.firstOrNull() ?: "Salary") else (currentCategoriesList.firstOrNull() ?: "Food")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (type == "INCOME") "Edit Income" else "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Segmented Switcher (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBorder.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "EXPENSE") SleekPrimary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                type = "EXPENSE"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (type == "EXPENSE") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "INCOME") Color(0xFF10B981) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                type = "INCOME"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (type == "INCOME") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == "EXPENSE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Balance: $currencySymbol${String.format(Locale.getDefault(), "%,.2f", availableBalanceForEdit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExceedingIncome) ExpenseRed else SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalIncome > 0) {
                            Text(
                                text = "Total Income: $currencySymbol${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    if (isExceedingIncome) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalIncome <= 0) 
                                        "Expense cannot exceed total income. Income is ₹0.00." 
                                    else 
                                        "Expense (₹${String.format(Locale.getDefault(), "%,.2f", enteredAmount)}) exceeds available balance (₹${String.format(Locale.getDefault(), "%,.2f", availableBalanceForEdit)}).",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Total Income: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategorySelectorGrid(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = currentCategoriesList,
                    categoryIcons = categoryIcons,
                    onAddCustomCategoryClick = { showCreateCategoryDialog = true },
                    onDeleteCustomCategory = if (type == "EXPENSE") onDeleteCategory else null,
                    onEditCustomCategory = if (type == "EXPENSE") onEditCategory else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (note.trim().isEmpty()) {
                    Text(
                        "Description is required",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && note.trim().isNotEmpty()) {
                                onConfirm(
                                    expense.copy(
                                        amountMinor = Money.fromDouble(amount, expense.currencyCode),
                                        category = category,
                                        note = note.trim(),
                                        type = type
                                    )
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && enteredAmount > 0.0 && !isExceedingIncome,
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            initialType = type,
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { newCat, catType ->
                onAddCategory(newCat, catType)
                category = newCat
                showCreateCategoryDialog = false
            }
        )
    }
}

// ==========================================
// 7️⃣ LIVE CALENDAR TAB
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarTab(
    expenses: List<Expense>,
    categoryIcons: Map<String, String> = emptyMap(),
    currencyCode: String = "INR",
    onAddExpenseForDate: (Long) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val today = Calendar.getInstance()
    
    val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
    val calendarAspectRatio = if (fontScale > 1.3f) 0.7f else if (fontScale > 1.1f) 0.85f else 1f

    // Baseline: March 2027. +1 month on each 7th of the month.
    val maxCalendarLimit = remember(today.get(Calendar.DAY_OF_MONTH)) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 31)
            if (today.get(Calendar.DAY_OF_MONTH) >= 7) {
                add(Calendar.MONTH, 1)
            }
        }
    }

    val initialYear = 2000
    val currentMonthIndex = (today.get(Calendar.YEAR) - initialYear) * 12 + today.get(Calendar.MONTH)
    val maxMonthIndex = (maxCalendarLimit.get(Calendar.YEAR) - initialYear) * 12 + maxCalendarLimit.get(Calendar.MONTH)

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = currentMonthIndex,
        pageCount = { maxMonthIndex + 1 }
    )

    val activeYear = initialYear + pagerState.currentPage / 12
    val activeMonth = pagerState.currentPage % 12
    val canGoForward = pagerState.currentPage < maxMonthIndex
    
    val coroutineScope = rememberCoroutineScope()

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selectedDayOfMonth by remember { mutableStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    val selectedDateMillis = remember(activeYear, activeMonth, selectedDayOfMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, activeYear)
            set(Calendar.MONTH, activeMonth)
            set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val selectedDayExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.YEAR) == activeYear &&
                cal.get(Calendar.MONTH) == activeMonth &&
                cal.get(Calendar.DAY_OF_MONTH) == selectedDayOfMonth
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaction Calendar",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Double-tap day to add. Valid until ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(maxCalendarLimit.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                                selectedDayOfMonth = 1
                            }
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = SleekPrimary)
                    }
                    val currentDisplayCal = remember(activeYear, activeMonth) {
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, activeYear)
                            set(Calendar.MONTH, activeMonth)
                        }
                    }
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDisplayCal.time),
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (canGoForward) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                                selectedDayOfMonth = 1
                            }
                        },
                        enabled = canGoForward
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = if (canGoForward) SleekPrimary else SleekTextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pageYear = initialYear + page / 12
                    val pageMonth = page % 12
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SleekTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val firstDayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, pageYear)
                            set(Calendar.MONTH, pageMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
                        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val dayOffset = firstDayOfWeek - 1

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (row in 0 until 6) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (col in 0 until 7) {
                                        val slotIndex = row * 7 + col
                                        val dayNum = slotIndex - dayOffset + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val isSelected = selectedDayOfMonth == dayNum && pageYear == activeYear && pageMonth == activeMonth
                                            val isToday = today.get(Calendar.YEAR) == pageYear &&
                                                    today.get(Calendar.MONTH) == pageMonth &&
                                                    today.get(Calendar.DAY_OF_MONTH) == dayNum

                                            val dayExpenses = expenses.filter {
                                                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                                                c.get(Calendar.YEAR) == pageYear &&
                                                        c.get(Calendar.MONTH) == pageMonth &&
                                                        c.get(Calendar.DAY_OF_MONTH) == dayNum
                                            }
                                            val dayIncome = dayExpenses.realIncome(currencyCode)
                                            val dayExpense = dayExpenses.realExpense(currencyCode)
                                            val hasTransactions = dayExpenses.isNotEmpty()
                                            val isProfit = hasTransactions && dayIncome >= dayExpense
                                            val isLoss = hasTransactions && dayExpense > dayIncome

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(calendarAspectRatio)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> SleekPrimary
                                                            isToday -> SleekPrimaryContainer.copy(alpha = 0.5f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = when {
                                                            isSelected -> Color.Transparent
                                                            isToday -> SleekPrimary
                                                            else -> Color.Transparent
                                                        },
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .combinedClickable(
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            selectedDayOfMonth = dayNum
                                                            if (page != pagerState.currentPage) {
                                                                coroutineScope.launch {
                                                                    pagerState.animateScrollToPage(page)
                                                                }
                                                            }
                                                        },
                                                        onDoubleClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            selectedDayOfMonth = dayNum
                                                            val clickedDate = Calendar.getInstance().apply {
                                                                set(Calendar.YEAR, pageYear)
                                                                set(Calendar.MONTH, pageMonth)
                                                                set(Calendar.DAY_OF_MONTH, dayNum)
                                                            }.timeInMillis
                                                            onAddExpenseForDate(clickedDate)
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = dayNum.toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> SleekPrimary
                                                            else -> SleekTextPrimary
                                                        }
                                                    )
                                                    if (hasTransactions) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(top = 2.dp)
                                                                .size(5.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    when {
                                                                        isSelected -> Color.White
                                                                        isProfit -> Color(0xFF10B981)
                                                                        else -> Color(0xFFEF4444)
                                                                    }
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(modifier = Modifier.weight(1f).aspectRatio(calendarAspectRatio))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
            Text(
                text = "Transactions on $selectedDateStr",
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { onAddExpenseForDate(selectedDateMillis) },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedDayExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions logged for this day.",
                    color = SleekTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedDayExpenses.forEach { expense ->
                    val isIncome = expense.type == "INCOME"
                    val catColor = if (isIncome) {
                        when (expense.category) {
                            "Salary" -> Color(0xFF10B981)
                            "Freelance" -> Color(0xFF0D9488)
                            "Investments" -> Color(0xFF3B82F6)
                            "Gifts" -> Color(0xFFEC4899)
                            else -> Color(0xFF10B981)
                        }
                    } else {
                        categoryColors[expense.category] ?: SleekPrimary
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                            .clickable { onEditExpense(expense) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getCategoryEmoji(expense.category, categoryIcons),
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.note ?: (if (isIncome) "Income" else "Expense"),
                                style = MaterialTheme.typography.titleSmall,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = expense.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }

                        Text(
                            text = String.format("%s₹%,.2f", if (isIncome) "+" else "-", expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isIncome) Color(0xFF10B981) else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = { onDeleteExpense(expense) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ExpenseRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun OnboardingNameDialog(
    onSave: (String) -> Unit
) {
    var nameStr by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { /* Prevent dismiss to force name entry */ }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekBg),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Welcome User",
                        tint = SleekPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Finance",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please enter your name to personalize your offline ledgers and insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nameStr,
                    onValueChange = { nameStr = it },
                    label = { Text("Your Name", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (nameStr.trim().isNotEmpty()) {
                            onSave(nameStr.trim())
                        }
                    },
                    enabled = nameStr.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_save_button")
                ) {
                    Text(
                        "Get Started",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit = { _, _ -> },
    onConfirmSingle: ((String) -> Unit)? = null
) {
    var newCatName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(if (initialType == "INCOME") "INCOME" else "EXPENSE") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().testTag("create_category_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Add New Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Category Type Selector (Expense vs Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExp = selectedType == "EXPENSE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExp) Color(0xFFEF5350) else Color.Transparent)
                            .clickable { selectedType = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExp) Color.White else SleekTextSecondary
                        )
                    }

                    val isInc = selectedType == "INCOME"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isInc) Color(0xFF10B981) else Color.Transparent)
                            .clickable { selectedType = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInc) Color.White else SleekTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Category Name", color = SleekTextSecondary) },
                    placeholder = { Text(if (selectedType == "INCOME") "e.g., Freelance, Bonus..." else "e.g., Gym, Subscriptions...", color = SleekTextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350),
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350),
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (newCatName.trim().isNotEmpty()) {
                                onConfirm(newCatName.trim(), selectedType)
                                onConfirmSingle?.invoke(newCatName.trim())
                            }
                        },
                        enabled = newCatName.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    CreateCategoryDialog(
        initialType = "EXPENSE",
        onDismiss = onDismiss,
        onConfirmSingle = onConfirm
    )
}

@Composable
fun BillsFullScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onPayBill: (String, Double) -> Unit
) {
    val context = LocalContext.current
    val billsList = viewModel.billsList
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalBalance = remember(accounts) { accounts.sumOf { it.balance } }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<BillEntry?>(null) }
    var billTitle by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var billDueDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var billErrorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (editingBill == null) "Add New Bill" else "Edit Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Title", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("Amount (₹)", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    billDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                },
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH),
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Due Date: $billDueDate", color = SleekTextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEditDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }

                        Button(
                            onClick = {
                                val amt = billAmount.toDoubleOrNull() ?: 0.0
                                if (billTitle.isNotBlank() && amt > 0) {
                                    val currentEditing = editingBill
                                    if (currentEditing == null) {
                                        billsList.add(BillEntry(System.currentTimeMillis().toString(), billTitle.trim(), amt, billDueDate))
                                    } else {
                                        val idx = billsList.indexOfFirst { it.id == currentEditing.id }
                                        if (idx != -1) {
                                            billsList[idx] = currentEditing.copy(title = billTitle.trim(), amount = amt, dueDate = billDueDate)
                                        }
                                    }
                                    showAddEditDialog = false
                                    Toast.makeText(context, "Saved bill successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SleekSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Bills & Utilities",
                        style = MaterialTheme.typography.titleLarge,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingBill = null
                        billTitle = ""
                        billAmount = ""
                        billDueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        showAddEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bill", tint = SleekPrimary)
                }
            }

            billErrorMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                            Text(msg, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { billErrorMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = "Pay bills directly on screen or tap edit/delete icons.",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary
            )

            if (billsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(48.dp))
                        Text("No upcoming bills", fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 16.sp)
                        Text("Tap + to add a bill", color = SleekTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(billsList, key = { it.id }) { bill ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBorder.copy(alpha = 0.25f))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bill.title, fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Due: ${bill.dueDate}", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                        Text("•", fontSize = 12.sp, color = SleekTextSecondary)
                                        Text("₹%,.0f".format(bill.amount), fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 14.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (bill.amount > totalBalance) {
                                                billErrorMessage = "Cannot pay ₹%.0f for %s: Amount exceeds available balance (₹%.0f available).".format(bill.amount, bill.title, totalBalance)
                                            } else {
                                                onPayBill(bill.title, bill.amount)
                                                billsList.remove(bill)
                                                billErrorMessage = null
                                                Toast.makeText(context, "Paid ${bill.title} successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Pay Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = {
                                            editingBill = bill
                                            billTitle = bill.title
                                            billAmount = bill.amount.toString()
                                            billDueDate = bill.dueDate
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            billsList.remove(bill)
                                            Toast.makeText(context, "Deleted ${bill.title}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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

@Composable
fun RemindersFullScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var reminderText by remember { mutableStateOf("") }
    var reminderDueDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    BackHandler { onBack() }

    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (editingReminder == null) "Add New Reminder" else "Edit Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        label = { Text("Reminder Note", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    reminderDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                },
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH),
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Due Date: $reminderDueDate", color = SleekTextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEditDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }

                        Button(
                            onClick = {
                                if (reminderText.isNotBlank()) {
                                    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    val dueDateLong = try { df.parse(reminderDueDate)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }

                                    val currentEditing = editingReminder
                                    if (currentEditing == null) {
                                        viewModel.insertReminder(reminderText.trim(), dueDateLong, true)
                                    } else {
                                        viewModel.updateReminder(currentEditing.copy(text = reminderText.trim(), dueDate = dueDateLong))
                                    }
                                    showAddEditDialog = false
                                    Toast.makeText(context, "Saved reminder to Room DB!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SleekSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Payment Reminders",
                        style = MaterialTheme.typography.titleLarge,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingReminder = null
                        reminderText = ""
                        reminderDueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        showAddEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = SleekPrimary)
                }
            }

            Text(
                text = "Room-backed reminders with push notifications & active state.",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary
            )

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(48.dp))
                        Text("No active reminders", fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 16.sp)
                        Text("Tap + to add a reminder", color = SleekTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reminders, key = { it.id }) { rem ->
                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(rem.dueDate))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBorder.copy(alpha = 0.25f))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = if (rem.isEnabled) Color(0xFFEC4899) else SleekTextSecondary, modifier = Modifier.size(22.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rem.text, fontSize = 14.sp, color = SleekTextPrimary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Due: $dateStr • ${if (rem.isEnabled) "Active" else "Stopped"}", fontSize = 11.sp, color = if (rem.isEnabled) Color(0xFFEC4899) else SleekTextSecondary, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Switch(
                                        checked = rem.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            viewModel.updateReminder(rem.copy(isEnabled = isChecked))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFEC4899),
                                            checkedTrackColor = Color(0xFFEC4899).copy(alpha = 0.5f)
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            viewModel.updateReminder(rem.copy(isCompleted = true, isEnabled = false))
                                            Toast.makeText(context, "Marked reminder as complete!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            editingReminder = rem
                                            reminderText = rem.text
                                            reminderDueDate = dateStr
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteReminder(rem)
                                            Toast.makeText(context, "Deleted reminder", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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

@Composable
fun AdjustBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetStr by remember { mutableStateOf(currentBudget.toInt().toString()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("adjust_budget_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Adjust Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Budget Cap (₹)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("budget_input")
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val budgetVal = budgetStr.toDoubleOrNull() ?: 0.0
                            if (budgetVal > 0) {
                                onConfirm(budgetVal)
                            }
                        },
                        enabled = (budgetStr.toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DateRangeReportModalDialog(
    initialStartDate: Long,
    initialEndDate: Long,
    onDismiss: () -> Unit,
    onConfirm: (startDate: Long, endDate: Long) -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    val context = LocalContext.current
    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("date_range_picker_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Select Report Period",
                                style = MaterialTheme.typography.titleMedium,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pick Start & End Date for report",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Start Date Picker Field
                Text(
                    "Start Date",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = if (startDate == 0L) System.currentTimeMillis() else startDate }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    startDate = selCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (startDate == 0L) "All Time / Beginning" else dateSdf.format(Date(startDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                        Text("Pick Date", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // End Date Picker Field
                Text(
                    "End Date",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = endDate }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(year, month, day, 23, 59, 59)
                                        set(Calendar.MILLISECOND, 999)
                                    }
                                    endDate = selCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dateSdf.format(Date(endDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                        Text("Pick Date", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                Text("Quick Selection", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf("7 Days", "30 Days", "This Month", "All Time")
                    presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = Calendar.getInstance()
                                when (preset) {
                                    "7 Days" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_MONTH, -7)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "30 Days" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_MONTH, -30)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "This Month" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            set(Calendar.DAY_OF_MONTH, 1)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "All Time" -> {
                                        endDate = now.timeInMillis
                                        startDate = 0L
                                    }
                                }
                            },
                            label = { Text(preset, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekBorder)
                    ) {
                        Text("Cancel", color = SleekTextPrimary)
                    }

                    Button(
                        onClick = { onConfirm(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Apply Range", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarSettingsTile(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SleekSurface,
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SleekTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

data class SidebarMenuItemData(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val titleKey: String,
    val subtitleKey: String,
    val onClick: () -> Unit
)

@Composable
fun SidebarGroupCard(
    sectionTitle: String,
    items: List<SidebarMenuItemData>,
    selectedLanguage: String
) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp, top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SleekPrimary)
            )
            Text(
                text = LanguageManager.tr(sectionTitle, selectedLanguage).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SleekTextSecondary,
                letterSpacing = 1.1.sp
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.98f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh),
                        label = "sidebarPressScale"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable { item.onClick() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(item.iconBgColor)
                                .border(1.dp, item.iconColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.titleKey,
                                tint = item.iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LanguageManager.tr(item.titleKey, selectedLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextPrimary,
                                softWrap = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageManager.tr(item.subtitleKey, selectedLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                softWrap = true
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SleekBorder.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = SleekBorder.copy(alpha = 0.45f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SidebarDrawerContent(
    viewModel: FinanceViewModel,
    onCloseDrawer: () -> Unit,
    onOpenSettingsScreen: (SettingsSubScreen) -> Unit,
    onChangePasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val allSections = remember(selectedLanguage) {
        listOf(
            "ACCOUNT & PROFILE" to listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Person,
                    iconColor = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFEFF6FF),
                    titleKey = "Profile",
                    subtitleKey = "View and edit personal profile details",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.PersonalData)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFEA580C),
                    iconBgColor = Color(0xFFFFEDD5),
                    titleKey = "Badges and Milestone",
                    subtitleKey = "Daily streaks, badges and achievements",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.BadgesAndMilestones)
                    }
                )
            ),
            "PREFERENCES & APPEARANCE" to listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Palette,
                    iconColor = Color(0xFF8B5CF6),
                    iconBgColor = Color(0xFFF3E8FF),
                    titleKey = "Appearance & Theme",
                    subtitleKey = "Light/Dark mode, color palettes & custom accent",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Appearance)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Translate,
                    iconColor = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFEFF6FF),
                    titleKey = "Language",
                    subtitleKey = "App localization, Hindi, English & 9+ languages",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Language)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Payments,
                    iconColor = Color(0xFF10B981),
                    iconBgColor = Color(0xFFD1FAE5),
                    titleKey = "Currency & Rates",
                    subtitleKey = "100+ currencies, live conversion & stats currency",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Currency)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFF0D9488),
                    iconBgColor = Color(0xFFCCFBF1),
                    titleKey = "Transaction Preferences",
                    subtitleKey = "Input defaults, remember category & safe delete",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Transactions)
                    }
                )
            ),
            "FINANCIAL MANAGEMENT" to listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.ReceiptLong,
                    iconColor = Color(0xFFD97706),
                    iconBgColor = Color(0xFFFEF3C7),
                    titleKey = "Bills & Reminders",
                    subtitleKey = "Configure recurring bills, alerts & due dates",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Bills)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.PieChart,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Budgets",
                    subtitleKey = "Monthly limits, warning thresholds & indicators",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Budgets)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Savings,
                    iconColor = Color(0xFFF59E0B),
                    iconBgColor = Color(0xFFFEF3C7),
                    titleKey = "Savings Goals",
                    subtitleKey = "Goal layouts, milestone celebrations & auto-gap",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.SavingsGoals)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Calculate,
                    iconColor = Color(0xFF6366F1),
                    iconBgColor = Color(0xFFEEF2FF),
                    titleKey = "Financial Calculators",
                    subtitleKey = "Currency converter, split bill, tip & percentage tools",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Calculations)
                    }
                )
            ),
            "CATEGORIES & DATA" to listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Category,
                    iconColor = Color(0xFF16A34A),
                    iconBgColor = Color(0xFFDCFCE7),
                    titleKey = "Categories & Tags",
                    subtitleKey = "Manage categories, tags and custom groups",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.CategoriesTags)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.FileDownload,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Export Statements",
                    subtitleKey = "Export analytics, stats, CSV & PDF reports",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Export)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Storage,
                    iconColor = Color(0xFF10B981),
                    iconBgColor = Color(0xFFD1FAE5),
                    titleKey = "Data Management",
                    subtitleKey = "Storage usage, clear cache, reset app data",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.DataManagement)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.CloudSync,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Backup & Restore",
                    subtitleKey = "Local Only or Online Cloud Sync, 1-click restore",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.BackupRestore)
                    }
                )
            ),
            "SECURITY & ABOUT" to listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF8B5CF6),
                    iconBgColor = Color(0xFFF3E8FF),
                    titleKey = "Password & Security",
                    subtitleKey = "App lock, passcode, biometric security",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Security)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.VisibilityOff,
                    iconColor = Color(0xFF6366F1),
                    iconBgColor = Color(0xFFE0E7FF),
                    titleKey = "Privacy Settings",
                    subtitleKey = "Blur sensitive amounts, hide balances & privacy mode",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Privacy)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Help,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Help & Support FAQ",
                    subtitleKey = "Categorized answers for app features & troubleshooting",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.HelpSupport)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFFDB2777),
                    iconBgColor = Color(0xFFFCE7F3),
                    titleKey = "What's New & About",
                    subtitleKey = "Version info, new features and updates",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.AboutApp)
                    }
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
        // Top Close & App Brand Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.5f)), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_modern),
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = "Finance Tracker Pro",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = LanguageManager.tr("Settings", selectedLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary
                    )
                }
            }

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SleekBorder.copy(alpha = 0.4f))
                    .bouncyPress()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Drawer",
                    tint = SleekTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))

        // 1. User Profile Top Section Hero Card
        val initials = if (!userName.isNullOrBlank()) {
            userName!!.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        } else "U"

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(26.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.PersonalData)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(SleekPrimary, SleekPrimaryContainer, SleekPrimary)
                            )
                        )
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            color = SleekOnPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!userName.isNullOrBlank()) userName!! else "Aarav Sharma",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "🔥 $currentStreak Day Streak",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🛡️ 100% Offline",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SleekBorder.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Profile Page",
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar for Quick Filtering Settings
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = LanguageManager.tr("Search settings...", selectedLanguage),
                    fontSize = 13.5.sp,
                    color = SleekTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SleekTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface,
                focusedBorderColor = SleekPrimary,
                unfocusedBorderColor = SleekBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = LanguageManager.tr("QUICK ACTIONS", selectedLanguage),
            style = MaterialTheme.typography.labelSmall,
            color = SleekTextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.Currency)
                },
                shape = RoundedCornerShape(12.dp),
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Text(LanguageManager.tr("Currency", selectedLanguage), style = MaterialTheme.typography.labelMedium, color = SleekTextPrimary)
                }
            }
            Surface(
                onClick = {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.Export)
                },
                shape = RoundedCornerShape(12.dp),
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    Text(LanguageManager.tr("Export", selectedLanguage), style = MaterialTheme.typography.labelMedium, color = SleekTextPrimary)
                }
            }
            Surface(
                onClick = {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.Security)
                },
                shape = RoundedCornerShape(12.dp),
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                    Text(LanguageManager.tr("Security", selectedLanguage), style = MaterialTheme.typography.labelMedium, color = SleekTextPrimary)
                }
            }
            Surface(
                onClick = {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.Calculations)
                },
                shape = RoundedCornerShape(12.dp),
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                    Text(LanguageManager.tr("Calculators", selectedLanguage), style = MaterialTheme.typography.labelMedium, color = SleekTextPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Render Filtered Sections
        allSections.forEach { (sectionTitle, items) ->
            val filteredItems = if (searchQuery.isBlank()) {
                items
            } else {
                items.filter {
                    val title = LanguageManager.tr(it.titleKey, selectedLanguage)
                    val sub = LanguageManager.tr(it.subtitleKey, selectedLanguage)
                    title.contains(searchQuery, ignoreCase = true) ||
                    sub.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filteredItems.isNotEmpty()) {
                SidebarGroupCard(
                    sectionTitle = sectionTitle,
                    items = filteredItems,
                    selectedLanguage = selectedLanguage
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer: Version & Credits
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Version 1.28 (Stable & Secure)",
                style = MaterialTheme.typography.labelMedium,
                color = SleekTextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Created with",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Love",
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "by Vivek",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun ThemeCirclePreview(index: Int, customHue: Float, modifier: Modifier = Modifier) {
    val (primary, primaryContainer, onPrimaryContainer) = com.example.ui.theme.getPresetThemeColors(index, customHue)
    Canvas(modifier = modifier) {
        // Top half
        drawArc(
            color = primary,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true
        )
        // Bottom-left quadrant
        drawArc(
            color = primaryContainer,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Bottom-right quadrant
        drawArc(
            color = onPrimaryContainer,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true
        )
    }
}

@Composable
fun ColorThemeGrid(
    selectedThemeIndex: Int,
    customHue: Float,
    onThemeSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 4) {
                    val index = row * 4 + col
                    val isSelected = selectedThemeIndex == index
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekBorder.copy(alpha = 0.3f))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekPrimary else SleekBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onThemeSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == 15) {
                            // Eyedropper custom picker
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Colorize,
                                    contentDescription = "Custom Theme",
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Standard preset theme circle
                            ThemeCirclePreview(
                                index = index,
                                customHue = customHue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        // Checkmark Overlay
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqAccordion(viewModel: FinanceViewModel) {
    val faqs = listOf(
        "Is my financial data secure?" to "Yes, all data is stored offline locally on your device and never uploaded to any servers.",
        "How do I set a monthly budget?" to "Click the pencil icon on the monthly card on the Dashboard to set your budget limit.",
        "What are custom categories?" to "Select '+ Add Custom' in the Category dropdown when adding/editing an expense to add new categories.",
        "How do I delete or edit transactions?" to "On the Transactions tab, long press or tap on any expense row to select it, then use the floating actions bar to edit or delete."
    )
    
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        faqs.forEachIndexed { index, (question, answer) ->
            val isExpanded = expandedIndex == index
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) null else index }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // 🧠 AI BASED ASK
        var aiQuestion by remember { mutableStateOf("") }
        val aiAnswer by viewModel.aiCardAnswer.collectAsStateWithLifecycle()
        val isQuerying by viewModel.isAiCardLoading.collectAsStateWithLifecycle()
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ask AI Financial Guide",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Have a question about your personal budget, tax, or saving tips? Ask our AI Financial Guide for practical insights grounded in your aggregated financial totals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = aiQuestion,
                    onValueChange = { 
                        if (it.length <= 1000) {
                            aiQuestion = it 
                        }
                    },
                    placeholder = { Text("How can I start investing ₹2000/month?", fontSize = 13.sp, color = SleekTextSecondary) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔒 AI queries are processed online via Google Gemini. For your security, never enter account numbers, passwords, or personal credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (aiQuestion.isNotBlank()) {
                            viewModel.askAiCardQuestion(aiQuestion)
                        }
                    },
                    enabled = !isQuerying && aiQuestion.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isQuerying) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Ask AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                if (aiAnswer != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = aiAnswer!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextPrimary,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Version Footer
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expense Tracker",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "100% Offline • Secure Room DB",
                        fontSize = 10.sp,
                        color = SleekTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SleekPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "v1.29",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SleekPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameStr by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Edit Your Name",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = nameStr,
                    onValueChange = { nameStr = it },
                    label = { Text("Name", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (nameStr.trim().isNotEmpty()) {
                                onConfirm(nameStr.trim())
                            }
                        },
                        enabled = nameStr.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// TRANSACTION RECORDED SUCCESS ANIMATED DIALOG
// ==========================================
data class RecordedTransactionInfo(
    val amount: Double,
    val category: String,
    val type: String, // "EXPENSE" or "INCOME"
    val note: String? = null
)

@Composable
fun TransactionSuccessDialog(
    info: RecordedTransactionInfo,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var animPhase by remember { mutableIntStateOf(0) } // 0: rotating red swoosh in navy circle, 1: white checkmark draw, 2: card reveal
    val scaleAnim = remember { Animatable(0.35f) }
    val checkmarkProgress = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardTranslationY = remember { Animatable(35f) }

    val swooshRotation = rememberLoopFloat(0f, 360f, 650, "swoosh_rotation", LinearEasing, reverse = false, rest = 0f)

    LaunchedEffect(Unit) {
        // Step 1: Scale up dark blue circle with bounce
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Hold rotating red swoosh briefly
        kotlinx.coroutines.delay(650)
        animPhase = 1

        // Step 2: Animate checkmark draw
        checkmarkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(450, easing = FastOutSlowInEasing)
        )
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
        animPhase = 2

        // Step 3: Reveal text card
        launch {
            cardAlpha.animateTo(1f, tween(300))
        }
        launch {
            cardTranslationY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .clickable(enabled = false) {}
            ) {
                // Navy Blue Badge Container from Video (Color(0xFF0B2E4E))
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF0B2E4E))
                        .border(4.dp, Color(0xFF1E40AF).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerPx = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        if (animPhase == 0) {
                            // Red Swoosh Shape from Frame 0 & Frame 1 of video
                            rotate(swooshRotation, pivot = centerPx) {
                                val redPath = Path().apply {
                                    moveTo(centerPx.x - radius * 0.4f, centerPx.y + radius * 0.1f)
                                    cubicTo(
                                        centerPx.x - radius * 0.2f, centerPx.y - radius * 0.45f,
                                        centerPx.x + radius * 0.4f, centerPx.y - radius * 0.35f,
                                        centerPx.x + radius * 0.35f, centerPx.y + radius * 0.2f
                                    )
                                    cubicTo(
                                        centerPx.x + radius * 0.15f, centerPx.y + radius * 0.5f,
                                        centerPx.x - radius * 0.35f, centerPx.y + radius * 0.4f,
                                        centerPx.x - radius * 0.4f, centerPx.y + radius * 0.1f
                                    )
                                    close()
                                }
                                drawPath(
                                    path = redPath,
                                    color = Color(0xFFE53935)
                                )
                            }
                        } else {
                            // White Checkmark (✓) from Frame 2 & Frame 3 of video
                            val p = checkmarkProgress.value
                            val checkPath = Path().apply {
                                val start = Offset(size.width * 0.30f, size.height * 0.52f)
                                val mid = Offset(size.width * 0.44f, size.height * 0.66f)
                                val end = Offset(size.width * 0.72f, size.height * 0.38f)

                                moveTo(start.x, start.y)
                                if (p <= 0.5f) {
                                    val localP = p / 0.5f
                                    lineTo(
                                        start.x + (mid.x - start.x) * localP,
                                        start.y + (mid.y - start.y) * localP
                                    )
                                } else {
                                    lineTo(mid.x, mid.y)
                                    val localP = (p - 0.5f) / 0.5f
                                    lineTo(
                                        mid.x + (end.x - mid.x) * localP,
                                        mid.y + (end.y - mid.y) * localP
                                    )
                                }
                            }

                            drawPath(
                                path = checkPath,
                                color = Color.White,
                                style = Stroke(
                                    width = size.width * 0.12f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Card below badge
                if (cardAlpha.value > 0.01f) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .graphicsLayer {
                                alpha = cardAlpha.value
                                translationY = cardTranslationY.value
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            val isIncome = info.type.equals("INCOME", ignoreCase = true)
                            val accentColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isIncome) "INCOME RECORDED" else "EXPENSE RECORDED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", info.amount)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = info.category + if (!info.note.isNullOrBlank()) " • ${info.note}" else "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2E4E)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
