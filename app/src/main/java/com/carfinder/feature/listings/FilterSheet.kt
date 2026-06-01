package com.carfinder.feature.listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carfinder.core.model.FuelType
import com.carfinder.core.model.Region
import com.carfinder.core.model.SearchFilter
import com.carfinder.core.model.SortOrder
import com.carfinder.data.repository.SourceInfo

private val FUEL_CHOICES = listOf(
    FuelType.PETROL, FuelType.DIESEL, FuelType.HYBRID,
    FuelType.PLUGIN_HYBRID, FuelType.ELECTRIC, FuelType.LPG,
)

private const val MIN_YEAR = 1990f
private const val MAX_YEAR = 2026f
private const val MAX_MILEAGE = 300_000f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filter: SearchFilter,
    availableMakes: List<String>,
    availableSources: List<SourceInfo>,
    onApply: (SearchFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(filter) { mutableStateOf(filter) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Filters", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

            if (availableMakes.isNotEmpty()) {
                Section("Make") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableMakes.forEach { make ->
                            FilterChip(
                                selected = draft.make == make,
                                onClick = { draft = draft.copy(make = if (draft.make == make) null else make) },
                                label = { Text(make) },
                            )
                        }
                    }
                }
            }

            Section("Price range") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.minPrice?.toLong()?.toString() ?: "",
                        onValueChange = { draft = draft.copy(minPrice = it.toDoubleOrNull()) },
                        label = { Text("Min") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = draft.maxPrice?.toLong()?.toString() ?: "",
                        onValueChange = { draft = draft.copy(maxPrice = it.toDoubleOrNull()) },
                        label = { Text("Max") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Section("Min year: ${draft.minYear ?: "Any"}") {
                Slider(
                    value = (draft.minYear ?: MIN_YEAR.toInt()).toFloat(),
                    onValueChange = { v ->
                        val y = v.toInt()
                        draft = draft.copy(minYear = if (y <= MIN_YEAR.toInt()) null else y)
                    },
                    valueRange = MIN_YEAR..MAX_YEAR,
                    steps = (MAX_YEAR - MIN_YEAR).toInt() - 1,
                )
            }

            Section("Max mileage: ${draft.maxMileageKm?.let { "${it / 1000}k km" } ?: "Any"}") {
                Slider(
                    value = (draft.maxMileageKm ?: MAX_MILEAGE.toInt()).toFloat(),
                    onValueChange = { v ->
                        val km = (v / 5_000).toInt() * 5_000
                        draft = draft.copy(maxMileageKm = if (km >= MAX_MILEAGE.toInt()) null else km)
                    },
                    valueRange = 0f..MAX_MILEAGE,
                    steps = (MAX_MILEAGE / 5_000).toInt() - 1,
                )
            }

            Section("Fuel") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FUEL_CHOICES.forEach { fuel ->
                        FilterChip(
                            selected = fuel in draft.fuelTypes,
                            onClick = {
                                draft = draft.copy(
                                    fuelTypes = draft.fuelTypes.toMutableSet().apply {
                                        if (!add(fuel)) remove(fuel)
                                    },
                                )
                            },
                            label = { Text(fuel.label()) },
                        )
                    }
                }
            }

            Section("Region") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Region.entries.forEach { region ->
                        FilterChip(
                            selected = region in draft.regions,
                            onClick = {
                                val next = draft.regions.toMutableSet().apply {
                                    if (!add(region)) remove(region)
                                }
                                // never allow zero regions -> treat empty as "all"
                                draft = draft.copy(regions = if (next.isEmpty()) Region.entries.toSet() else next)
                            },
                            label = { Text(region.label()) },
                        )
                    }
                }
            }

            if (availableSources.isNotEmpty()) {
                Section("Sources") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableSources.forEach { source ->
                            FilterChip(
                                enabled = source.enabled,
                                selected = source.id in draft.sourceIds,
                                onClick = {
                                    draft = draft.copy(
                                        sourceIds = draft.sourceIds.toMutableSet().apply {
                                            if (!add(source.id)) remove(source.id)
                                        },
                                    )
                                },
                                label = { Text(source.displayName) },
                            )
                        }
                    }
                }
            }

            Section("Sort by") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.entries.forEach { sort ->
                        FilterChip(
                            selected = draft.sort == sort,
                            onClick = { draft = draft.copy(sort = sort) },
                            label = { Text(sort.label()) },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                ) { Text("Reset") }
                Button(
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f),
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        content()
    }
}

private fun FuelType.label() = when (this) {
    FuelType.PETROL -> "Petrol"
    FuelType.DIESEL -> "Diesel"
    FuelType.HYBRID -> "Hybrid"
    FuelType.PLUGIN_HYBRID -> "Plug-in"
    FuelType.ELECTRIC -> "Electric"
    FuelType.LPG -> "LPG"
    FuelType.OTHER -> "Other"
    FuelType.UNKNOWN -> "Unknown"
}

private fun Region.label() = when (this) {
    Region.POLAND -> "Poland"
    Region.EUROPE -> "Europe"
    Region.USA -> "US import"
}

private fun SortOrder.label() = when (this) {
    SortOrder.NEWEST -> "Newest"
    SortOrder.PRICE_ASC -> "Price up"
    SortOrder.PRICE_DESC -> "Price down"
    SortOrder.MILEAGE_ASC -> "Mileage"
    SortOrder.YEAR_DESC -> "Year"
}
