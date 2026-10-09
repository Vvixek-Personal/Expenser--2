package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Dedicated Currency & Exchange Rates Configuration Screen (Rule 6 & 7)
 * - 100+ countries/currencies searchable by Country, Name, Code, and Symbol.
 * - Option A (Keep Existing) vs Option B (Convert Existing with live rate & confirmation).
 * - Default Currency for Statistics selector.
 * - Exchange rate status & local cache management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val currentCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val currentSymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val currentName by viewModel.selectedCurrencyName.collectAsStateWithLifecycle()
    val statsCode by viewModel.statsCurrencyCode.collectAsStateWithLifecycle()
    val statsSymbol by viewModel.statsCurrencySymbol.collectAsStateWithLifecycle()
    val statsName by viewModel.statsCurrencyName.collectAsStateWithLifecycle()
    val isAutoUpdate by viewModel.isAutoExchangeRateUpdateEnabled.collectAsStateWithLifecycle()
    val lastUpdate by viewModel.lastExchangeRateUpdate.collectAsStateWithLifecycle()
    val isUpdatingRates by viewModel.isUpdatingExchangeRates.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    var pendingCurrencyChange by remember { mutableStateOf<CurrencyItem?>(null) }
    var showStatsCurrencyPicker by remember { mutableStateOf(false) }

    val allCurrencies = remember { CurrencyManager.currencies }

    val filteredCurrencies = remember(searchQuery, selectedRegion) {
        allCurrencies.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.country.contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.code.contains(searchQuery, ignoreCase = true) ||
                    item.symbol.contains(searchQuery, ignoreCase = true)

            val matchesRegion = when (selectedRegion) {
                "All" -> true
                "Popular" -> item.code in listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED")
                "Asia" -> item.country in listOf("India", "Japan", "China", "Hong Kong", "Singapore", "South Korea", "Bangladesh", "Pakistan", "Sri Lanka", "Nepal", "Indonesia", "Malaysia", "Thailand", "Philippines", "Vietnam")
                "Europe" -> item.country in listOf("European Union", "United Kingdom", "Switzerland", "Sweden", "Norway", "Denmark", "Poland", "Czech Republic", "Hungary", "Romania", "Croatia", "Iceland")
                "Americas" -> item.country in listOf("United States", "Canada", "Brazil", "Mexico", "Argentina", "Chile", "Colombia", "Peru", "Costa Rica", "Dominican Republic", "Jamaica")
                "Middle East & Africa" -> item.country in listOf("United Arab Emirates", "Saudi Arabia", "South Africa", "Egypt", "Nigeria", "Kenya", "Qatar", "Kuwait", "Morocco", "Ethiopia")
                else -> true
            }

            matchesQuery && matchesRegion
        }
    }

    Scaffold(
        topBar = {
            SettingsHeaderTitle(title = "Currency & Rates", onBack = onBack)
        },
        containerColor = SleekBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header summary cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Active Currency Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Default Transaction Currency Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekSurface),
                            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    color = IncomeGreenBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "TRANSACTIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val currentItem = CurrencyManager.getByCode(currentCode)
                                    Text(text = currentItem.flag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$currentCode ($currentSymbol)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = currentName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SleekTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Statistics Default Currency Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekSurface),
                            border = BorderStroke(1.dp, SleekBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showStatsCurrencyPicker = true }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = SleekPrimaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "STATISTICS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = AppIcons.Edit,
                                        contentDescription = "Change Stats Currency",
                                        tint = SleekTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statsItem = CurrencyManager.getByCode(statsCode)
                                    Text(text = statsItem.flag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$statsCode ($statsSymbol)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = statsName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SleekTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live Rates & Offline Sync section (Rule 7)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AppIcons.Sync,
                                        contentDescription = null,
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Exchange Rates Engine",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Cached offline. Rates auto-refresh every ~10 days.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                                Text(
                                    text = "Last synced: $lastUpdate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.refreshExchangeRates() },
                                enabled = !isUpdatingRates,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isUpdatingRates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = SleekPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = AppIcons.Refresh,
                                        contentDescription = "Refresh",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-update exchange rates",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextPrimary
                            )
                            Switch(
                                checked = isAutoUpdate,
                                onCheckedChange = { viewModel.toggleAutoExchangeRateUpdate(it) }
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Choose Default Currency (100+ Available)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_search_input"),
                        placeholder = { Text("Search by country, name, code, symbol...") },
                        leadingIcon = {
                            Icon(AppIcons.Search, contentDescription = "Search", tint = SleekTextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(AppIcons.Clear, contentDescription = "Clear", tint = SleekTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Region Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Popular", "Asia", "Europe", "Americas", "Middle East & Africa").forEach { region ->
                            val isSelected = selectedRegion == region
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRegion = region },
                                label = { Text(region, fontSize = 12.sp) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SleekSurface,
                                    labelColor = SleekTextPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder)
                            )
                        }
                    }
                }
            }

            // Currencies List
            items(filteredCurrencies, key = { it.code + it.country }) { currency ->
                val isCurrentDefault = currency.code.equals(currentCode, ignoreCase = true)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentDefault) SleekPrimary.copy(alpha = 0.08f) else SleekSurface
                    ),
                    border = BorderStroke(
                        width = if (isCurrentDefault) 1.5.dp else 1.dp,
                        color = if (isCurrentDefault) SleekPrimary else SleekBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            if (!isCurrentDefault) {
                                pendingCurrencyChange = currency
                            }
                        }
                        .testTag("currency_item_${currency.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currency.flag, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currency.country,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SleekTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SleekPrimaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = currency.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPrimary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = currency.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "1 USD = ${String.format(Locale.getDefault(), "%,.2f", currency.rateToUsd)} ${currency.code}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary.copy(alpha = 0.75f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = CircleShape,
                                color = if (isCurrentDefault) SleekPrimary else SleekBorder.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currency.symbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isCurrentDefault) Color.White else SleekTextPrimary
                                    )
                                }
                            }
                            if (isCurrentDefault) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Option A vs Option B Currency Conversion Dialog (Rule 6)
    val pendingChange = pendingCurrencyChange
    if (pendingChange != null) {
        val target = pendingChange
        var selectedOption by remember { mutableStateOf("KEEP") } // "KEEP" or "CONVERT"

        val currentRate = remember(currentCode, target.code) {
            val rate = CurrencyManager.convert(1.0, currentCode, target.code)
            String.format(Locale.getDefault(), "%,.4f", rate)
        }

        AlertDialog(
            onDismissRequest = { pendingCurrencyChange = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = target.flag, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change Currency to ${target.code}?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Current Rate: 1 $currentCode = $currentRate ${target.code}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "How should existing transactions be handled?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Option A: Keep Existing Transactions
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == "KEEP") SleekPrimary.copy(alpha = 0.08f) else SleekSurface
                        ),
                        border = BorderStroke(
                            width = if (selectedOption == "KEEP") 1.5.dp else 1.dp,
                            color = if (selectedOption == "KEEP") SleekPrimary else SleekBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = "KEEP" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = selectedOption == "KEEP",
                                onClick = { selectedOption = "KEEP" },
                                colors = RadioButtonDefaults.colors(selectedColor = SleekPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Option A — Keep Existing Values",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Existing transactions keep their original numbers and history. Only new transactions will use ${target.code} (${target.symbol}).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option B: Convert Existing Transactions
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == "CONVERT") SleekPrimary.copy(alpha = 0.08f) else SleekSurface
                        ),
                        border = BorderStroke(
                            width = if (selectedOption == "CONVERT") 1.5.dp else 1.dp,
                            color = if (selectedOption == "CONVERT") SleekPrimary else SleekBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = "CONVERT" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = selectedOption == "CONVERT",
                                onClick = { selectedOption = "CONVERT" },
                                colors = RadioButtonDefaults.colors(selectedColor = SleekPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Option B — Convert Transactions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Recalculates past amounts to ${target.code} using exchange rate (1 $currentCode = $currentRate ${target.code}). Recommended for accurate net balances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val convert = (selectedOption == "CONVERT")
                        viewModel.updateDefaultCurrency(
                            code = target.code,
                            symbol = target.symbol,
                            name = target.name,
                            convertExisting = convert
                        )
                        pendingCurrencyChange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Apply & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCurrencyChange = null }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Default Currency for Statistics Picker Dialog
    if (showStatsCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showStatsCurrencyPicker = false },
            title = {
                Text(
                    text = "Default Currency for Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select the currency used when aggregating total expenses, category charts, and trend metrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val quickCurrencies = remember {
                        allCurrencies.filter { it.code in listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED", "SAR") }
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(quickCurrencies, key = { it.code }) { item ->
                            val isSelected = item.code.equals(statsCode, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateStatsCurrency(item.code, item.symbol, item.name)
                                        showStatsCurrencyPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${item.code} (${item.symbol}) - ${item.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SleekPrimary else SleekTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = AppIcons.Check,
                                            contentDescription = null,
                                            tint = SleekPrimary,
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
                TextButton(onClick = { showStatsCurrencyPicker = false }) {
                    Text("Close", color = SleekPrimary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Dedicated Financial Calculations Screen (Rule 12)
 * Includes:
 * 1. Currency Converter (Money-to-Money across 100+ currencies)
 * 2. Split Bill & Tip Calculator (with direct option to record share as expense)
 * 3. Percentage & Discount Calculator (X% of Y, % Change, Discount savings)
 * 4. Average & Run-rate Calculator (daily allowance, projected monthly spend)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Currency Converter", "Split Bill & Tip", "Percentage", "Run-Rate", "Loan & EMI")

    Scaffold(
        topBar = {
            SettingsHeaderTitle(title = "Financial Calculators", onBack = onBack)
        },
        containerColor = SleekBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Calculator Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SleekSurface,
                contentColor = SleekPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = SleekBorder) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) SleekPrimary else SleekTextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> CurrencyConverterTool(viewModel)
                    1 -> SplitBillCalculatorTool(viewModel)
                    2 -> PercentageCalculatorTool()
                    3 -> RunRateCalculatorTool(viewModel)
                    4 -> LoanEmiCalculatorTool(viewModel)
                }
            }
        }
    }
}

@Composable
private fun CurrencyConverterTool(viewModel: FinanceViewModel) {
    val defaultCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val allCurrencies = remember { CurrencyManager.currencies }

    var fromCurrency by remember { mutableStateOf(CurrencyManager.getByCode(defaultCode)) }
    var toCurrency by remember { mutableStateOf(CurrencyManager.getByCode("USD")) }
    var inputAmountText by remember { mutableStateOf("100") }
    val context = LocalContext.current

    val inputAmount = inputAmountText.toDoubleOrNull() ?: 0.0
    val convertedResult = remember(inputAmount, fromCurrency.code, toCurrency.code) {
        CurrencyManager.convert(inputAmount, fromCurrency.code, toCurrency.code)
    }

    val exchangeRateSingle = remember(fromCurrency.code, toCurrency.code) {
        CurrencyManager.convert(1.0, fromCurrency.code, toCurrency.code)
    }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Result Display Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Converted Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${toCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", convertedResult)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
                Text(
                    text = "${fromCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", inputAmount)} ${fromCurrency.code} = ${toCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", convertedResult)} ${toCurrency.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SleekPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "1 ${fromCurrency.code} = ${String.format(Locale.getDefault(), "%,.4f", exchangeRateSingle)} ${toCurrency.code}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Amount Input Field
        OutlinedTextField(
            value = inputAmountText,
            onValueChange = { inputAmountText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Amount to Convert") },
            leadingIcon = {
                Text(
                    text = fromCurrency.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface
            )
        )

        // Quick Amount Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("10", "50", "100", "500", "1000").forEach { preset ->
                FilledTonalButton(
                    onClick = { inputAmountText = preset },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(preset, fontSize = 12.sp)
                }
            }
        }

        // Currency Selectors with Swap Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // From Currency Button
            OutlinedCard(
                onClick = { showFromPicker = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = SleekSurface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fromCurrency.flag, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("From", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(fromCurrency.code, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    }
                }
            }

            // Swap Button
            IconButton(
                onClick = {
                    val temp = fromCurrency
                    fromCurrency = toCurrency
                    toCurrency = temp
                },
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(SleekPrimary, CircleShape)
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = AppIcons.SwapHoriz,
                    contentDescription = "Swap Currencies",
                    tint = Color.White
                )
            }

            // To Currency Button
            OutlinedCard(
                onClick = { showToPicker = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = SleekSurface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(toCurrency.flag, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("To", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(toCurrency.code, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    }
                }
            }
        }

        // Copy Result Button
        Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Currency Result", "${toCurrency.symbol}${String.format(Locale.getDefault(), "%,.2f", convertedResult)} ${toCurrency.code}")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied result to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
        ) {
            Icon(AppIcons.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Conversion Result")
        }
    }

    // Currency Pickers
    if (showFromPicker) {
        CurrencyPickerDialog(
            title = "Select Source Currency",
            currencies = allCurrencies,
            selectedCode = fromCurrency.code,
            onSelect = {
                fromCurrency = it
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        CurrencyPickerDialog(
            title = "Select Target Currency",
            currencies = allCurrencies,
            selectedCode = toCurrency.code,
            onSelect = {
                toCurrency = it
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun SplitBillCalculatorTool(viewModel: FinanceViewModel) {
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    var billAmountText by remember { mutableStateOf("1500") }
    var tipPercent by remember { mutableIntStateOf(10) }
    var peopleCount by remember { mutableIntStateOf(3) }
    val context = LocalContext.current

    val billAmount = billAmountText.toDoubleOrNull() ?: 0.0
    val tipAmount = (billAmount * tipPercent) / 100.0
    val totalWithTip = billAmount + tipAmount
    val perPersonAmount = if (peopleCount > 0) totalWithTip / peopleCount else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Result Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Each Person Pays", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", perPersonAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SleekBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Bill", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", billAmount)}",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tip ($tipPercent%)", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", tipAmount)}",
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total With Tip", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", totalWithTip)}",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                }
            }
        }

        // Bill Amount Input
        OutlinedTextField(
            value = billAmountText,
            onValueChange = { billAmountText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Total Bill Amount") },
            leadingIcon = {
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface
            )
        )

        // Tip Percentage Selector
        Column {
            Text("Tip Percentage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 5, 10, 15, 20).forEach { pct ->
                    val isSelected = tipPercent == pct
                    FilledTonalButton(
                        onClick = { tipPercent = pct },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected) SleekPrimary else SleekSurface,
                            contentColor = if (isSelected) Color.White else SleekTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("$pct%", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // People Stepper
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Number of People", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("$peopleCount person${if (peopleCount > 1) "s" else ""} splitting", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (peopleCount > 1) peopleCount-- },
                        modifier = Modifier
                            .background(SleekPrimaryContainer, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(AppIcons.Remove, contentDescription = "Decrease", tint = SleekPrimary)
                    }

                    Text(
                        text = "$peopleCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = { if (peopleCount < 50) peopleCount++ },
                        modifier = Modifier
                            .background(SleekPrimaryContainer, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(AppIcons.Add, contentDescription = "Increase", tint = SleekPrimary)
                    }
                }
            }
        }

        // Action: Record my share directly into transactions
        Button(
            onClick = {
                if (perPersonAmount > 0) {
                    viewModel.addExpense(
                        amount = perPersonAmount,
                        category = "Food & Dining",
                        date = System.currentTimeMillis(),
                        note = "Split bill ($peopleCount people) total: $currencySymbol$totalWithTip",
                        type = "EXPENSE",
                        currencyCode = viewModel.selectedCurrencyCode.value
                    )
                    Toast.makeText(context, "Recorded my share ($currencySymbol${String.format(Locale.getDefault(), "%,.2f", perPersonAmount)}) as Expense", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
        ) {
            Icon(AppIcons.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record My Share as Expense")
        }
    }
}

@Composable
private fun PercentageCalculatorTool() {
    var subMode by remember { mutableIntStateOf(0) } // 0: X% of Y, 1: % Change, 2: Discount
    val subModes = listOf("X% of Y", "% Change", "Discount")

    var val1Text by remember { mutableStateOf("18") }
    var val2Text by remember { mutableStateOf("2500") }

    val val1 = val1Text.toDoubleOrNull() ?: 0.0
    val val2 = val2Text.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Sub Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subModes.forEachIndexed { index, mode ->
                val isSelected = subMode == index
                FilledTonalButton(
                    onClick = { subMode = index },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) SleekPrimary else SleekSurface,
                        contentColor = if (isSelected) Color.White else SleekTextPrimary
                    ),
                    border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder)
                ) {
                    Text(mode, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Calculation result
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (subMode) {
                    0 -> { // X% of Y
                        val result = (val1 * val2) / 100.0
                        val total = val2 + result
                        Text("$val1% of $val2 is", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%,.2f", result),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Total (Original + Percentage) = ${String.format(Locale.getDefault(), "%,.2f", total)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                    1 -> { // % Change from val1 to val2
                        val diff = val2 - val1
                        val pctChange = if (val1 != 0.0) (diff / val1) * 100.0 else 0.0
                        val isIncrease = diff >= 0
                        Text("Change from $val1 to $val2", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (isIncrease) "+" else ""}${String.format(Locale.getDefault(), "%,.2f", pctChange)}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncrease) IncomeGreen else ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Absolute Difference: ${if (diff >= 0) "+" else ""}${String.format(Locale.getDefault(), "%,.2f", diff)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                    2 -> { // Discount: Original val2 with val1% off
                        val saved = (val2 * val1) / 100.0
                        val finalPrice = (val2 - saved).coerceAtLeast(0.0)
                        Text("Final Price After $val1% Discount", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%,.2f", finalPrice),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You save: ${String.format(Locale.getDefault(), "%,.2f", saved)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                    }
                }
            }
        }

        // Inputs
        OutlinedTextField(
            value = val1Text,
            onValueChange = { val1Text = it.filter { char -> char.isDigit() || char == '.' } },
            label = {
                Text(
                    when (subMode) {
                        0 -> "Percentage (%)"
                        1 -> "Initial Amount"
                        else -> "Discount (%)"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface
            )
        )

        OutlinedTextField(
            value = val2Text,
            onValueChange = { val2Text = it.filter { char -> char.isDigit() || char == '.' } },
            label = {
                Text(
                    when (subMode) {
                        0 -> "Total Value (Y)"
                        1 -> "New Amount"
                        else -> "Original Price"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface
            )
        )
    }
}

@Composable
private fun RunRateCalculatorTool(viewModel: FinanceViewModel) {
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()

    var spentSoFarText by remember { mutableStateOf("8500") }
    var daysElapsedText by remember { mutableStateOf("10") }
    var totalDaysInMonthText by remember { mutableStateOf("30") }

    val spent = spentSoFarText.toDoubleOrNull() ?: 0.0
    val daysElapsed = (daysElapsedText.toIntOrNull() ?: 1).coerceAtLeast(1)
    val totalDays = (totalDaysInMonthText.toIntOrNull() ?: 30).coerceAtLeast(daysElapsed)
    val daysRemaining = totalDays - daysElapsed

    val dailyAverage = spent / daysElapsed
    val projectedTotal = dailyAverage * totalDays
    val remainingBudget = (monthlyBudget - spent).coerceAtLeast(0.0)
    val safeDailyAllowance = if (daysRemaining > 0) remainingBudget / daysRemaining else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Overview Summary
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Safe Daily Allowance for Rest of Month", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", safeDailyAllowance)} / day",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (safeDailyAllowance >= dailyAverage) IncomeGreen else ExpenseRed
                )
                Text(
                    text = "$daysRemaining days remaining in period",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SleekBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Daily Average", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", dailyAverage)}",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Projected Total", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", projectedTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = if (projectedTotal > monthlyBudget) ExpenseRed else IncomeGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monthly Limit", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", monthlyBudget)}",
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                }
            }
        }

        // Inputs
        OutlinedTextField(
            value = spentSoFarText,
            onValueChange = { spentSoFarText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Amount Spent So Far ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekSurface,
                unfocusedContainerColor = SleekSurface
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = daysElapsedText,
                onValueChange = { daysElapsedText = it.filter { char -> char.isDigit() } },
                label = { Text("Days Elapsed") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                )
            )

            OutlinedTextField(
                value = totalDaysInMonthText,
                onValueChange = { totalDaysInMonthText = it.filter { char -> char.isDigit() } },
                label = { Text("Days in Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                )
            )
        }
    }
}

/**
 * 🏦 Loan EMI & Mortgage Calculator Tool
 * Calculates monthly EMI, total interest, and total payment with interactive visual breakdown
 */
@Composable
private fun LoanEmiCalculatorTool(viewModel: FinanceViewModel) {
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()

    var principalText by remember { mutableStateOf("100000") }
    var interestRateText by remember { mutableStateOf("8.5") }
    var tenureYearsText by remember { mutableStateOf("5") }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val annualRate = interestRateText.toDoubleOrNull() ?: 0.0
    val tenureYears = tenureYearsText.toDoubleOrNull() ?: 1.0

    val (monthlyEmi, totalInterest, totalPayment) = remember(principal, annualRate, tenureYears) {
        if (principal <= 0 || annualRate <= 0 || tenureYears <= 0) {
            Triple(0.0, 0.0, 0.0)
        } else {
            val monthlyRate = annualRate / (12.0 * 100.0)
            val totalMonths = (tenureYears * 12.0).toInt().coerceAtLeast(1)
            val mathPow = Math.pow(1.0 + monthlyRate, totalMonths.toDouble())
            val emi = (principal * monthlyRate * mathPow) / (mathPow - 1.0)
            val totalPay = emi * totalMonths
            val interest = totalPay - principal
            Triple(emi, interest, totalPay)
        }
    }

    val principalRatio = if (totalPayment > 0) (principal / totalPayment).toFloat().coerceIn(0f, 1f) else 0.7f
    val interestRatio = 1f - principalRatio

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Output Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monthly EMI Payment",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", monthlyEmi)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SleekPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Stacked Amortization Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(principalRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFF10B981))
                        )
                        Box(
                            modifier = Modifier
                                .weight(interestRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFFF59E0B))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Principal: ${(principalRatio * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Interest: ${(interestRatio * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SleekBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Interest Payable", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", totalInterest)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Amount Payable", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                        Text(
                            "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", totalPayment)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                }
            }
        }

        // Input Fields Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Loan Parameters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                OutlinedTextField(
                    value = principalText,
                    onValueChange = { principalText = it },
                    label = { Text("Loan Principal ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = interestRateText,
                        onValueChange = { interestRateText = it },
                        label = { Text("Interest Rate (% p.a.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        )
                    )

                    OutlinedTextField(
                        value = tenureYearsText,
                        onValueChange = { tenureYearsText = it },
                        label = { Text("Tenure (Years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        )
                    )
                }

                // Quick Tenure Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1 Year" to "1", "3 Years" to "3", "5 Years" to "5", "10 Years" to "10", "20 Years" to "20").forEach { (label, years) ->
                        val isSelected = tenureYearsText == years
                        FilterChip(
                            selected = isSelected,
                            onClick = { tenureYearsText = years },
                            label = { Text(label, fontSize = 11.sp) },
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
}

/**
 * Dedicated Savings Goals Settings Screen (Rule 11)
 */
@Composable
fun SavingsGoalsSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val viewMode by viewModel.goalViewMode.collectAsStateWithLifecycle()
    val progressStyle by viewModel.goalProgressStyle.collectAsStateWithLifecycle()

    var autoGapCalculation by remember { mutableStateOf(true) }
    var celebrateMilestones by remember { mutableStateOf(true) }
    var autoArchiveCompleted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SettingsHeaderTitle(title = "Savings Goals Settings", onBack = onBack)
        },
        containerColor = SleekBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display Preferences Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Display & Visualization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // View Mode
                    Text("Card Layout", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Grid", "List").forEach { mode ->
                            val isSelected = viewMode == mode
                            FilledTonalButton(
                                onClick = { viewModel.updateGoalViewMode(mode) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) SleekPrimary else SleekBg,
                                    contentColor = if (isSelected) Color.White else SleekTextPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder)
                            ) {
                                Icon(
                                    imageVector = if (mode == "Grid") AppIcons.GridView else AppIcons.ViewAgenda,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(mode)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Style
                    Text("Progress Indicator Style", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Circle", "Bar").forEach { style ->
                            val isSelected = progressStyle == style
                            FilledTonalButton(
                                onClick = { viewModel.updateGoalProgressStyle(style) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) SleekPrimary else SleekBg,
                                    contentColor = if (isSelected) Color.White else SleekTextPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder)
                            ) {
                                Icon(
                                    imageVector = if (style == "Circle") AppIcons.PieChart else AppIcons.LinearScale,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (style == "Circle") "Circular Arc" else "Horizontal Bar")
                            }
                        }
                    }
                }
            }

            // Calculation & Goal Rules Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Behavior & Calculations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Gap Calculation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Automatically calculates suggested weekly contribution based on target date", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = autoGapCalculation, onCheckedChange = { autoGapCalculation = it })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Milestone Celebrations", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Celebrate when reaching 25%, 50%, 75% and 100% of target amount", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = celebrateMilestones, onCheckedChange = { celebrateMilestones = it })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Archive Completed Goals", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Hide 100% reached goals from active list into completed archive", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = autoArchiveCompleted, onCheckedChange = { autoArchiveCompleted = it })
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Transaction Settings Screen (Rule 13)
 */
@Composable
fun TransactionSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val defaultTxType by viewModel.defaultTxType.collectAsStateWithLifecycle()
    val rememberLastCategory by viewModel.rememberLastCategory.collectAsStateWithLifecycle()
    val confirmDelete by viewModel.confirmTxDelete.collectAsStateWithLifecycle()
    val groupByDate by viewModel.groupByDate.collectAsStateWithLifecycle()
    val defaultPaymentMode by viewModel.defaultPaymentMode.collectAsStateWithLifecycle()
    val highAmountEnabled by viewModel.highAmountWarningEnabled.collectAsStateWithLifecycle()
    val highAmountThreshold by viewModel.highAmountThreshold.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()

    var thresholdInput by remember(highAmountThreshold) { mutableStateOf(highAmountThreshold.toInt().toString()) }

    Scaffold(
        topBar = {
            SettingsHeaderTitle(title = "Transaction Preferences", onBack = onBack)
        },
        containerColor = SleekBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Defaults Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Input Defaults",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Default Transaction Type", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EXPENSE" to "Expense", "INCOME" to "Income").forEach { (typeKey, label) ->
                            val isSelected = defaultTxType.equals(typeKey, ignoreCase = true)
                            FilledTonalButton(
                                onClick = { viewModel.updateDefaultTxType(typeKey) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) {
                                        if (typeKey == "EXPENSE") ExpenseRed else IncomeGreen
                                    } else SleekBg,
                                    contentColor = if (isSelected) Color.White else SleekTextPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else SleekBorder)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Default Payment Method", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI", "Cash", "Card", "NetBank").forEach { mode ->
                            val isSelected = defaultPaymentMode.equals(mode, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateDefaultPaymentMode(mode) },
                                label = { Text(mode, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Remember Last Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Automatically select last-used category for faster entry", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = rememberLastCategory,
                            onCheckedChange = { viewModel.toggleRememberLastCategory(it) }
                        )
                    }
                }
            }

            // Organization & Safety Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Safety & Organization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Group Transactions by Date", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Organize records into Today, Yesterday and monthly section headers", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = groupByDate,
                            onCheckedChange = { viewModel.toggleGroupByDate(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Confirm Before Deleting", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Show confirmation dialog to prevent accidental deletion", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = confirmDelete,
                            onCheckedChange = { viewModel.toggleConfirmTxDelete(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Amount Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
                            Text("Requires confirmation for amounts over $currencySymbol$thresholdInput", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(
                            checked = highAmountEnabled,
                            onCheckedChange = { viewModel.updateHighAmountWarning(it, thresholdInput.toDoubleOrNull() ?: 5000.0) }
                        )
                    }

                    if (highAmountEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = thresholdInput,
                            onValueChange = {
                                thresholdInput = it.filter { char -> char.isDigit() }
                                val d = thresholdInput.toDoubleOrNull()
                                if (d != null && d > 0) {
                                    viewModel.updateHighAmountWarning(true, d)
                                }
                            },
                            label = { Text("Warning Threshold ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    title: String,
    currencies: List<CurrencyItem>,
    selectedCode: String,
    onSelect: (CurrencyItem) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        currencies.filter {
            query.isBlank() ||
                    it.country.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.code.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search currency...") },
                    leadingIcon = { Icon(AppIcons.Search, contentDescription = null, tint = SleekTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { it.code + it.country }) { item ->
                        val isSelected = item.code.equals(selectedCode, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.code} (${item.symbol}) - ${item.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SleekPrimary else SleekTextPrimary
                                    )
                                    Text(item.country, style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                                }
                                if (isSelected) {
                                    Icon(AppIcons.Check, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SleekPrimary)
            }
        },
        containerColor = SleekSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
