package com.carfinder.feature.listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.Region
import com.carfinder.ui.components.EmptyState
import com.carfinder.ui.components.LoadingIndicator
import com.carfinder.ui.components.formatted
import com.carfinder.ui.components.kmOrDash

@Composable
fun ListingsRoute(
    onOfferClick: (String) -> Unit,
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ListingsScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::refresh,
        onOfferClick = onOfferClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    uiState: ListingsUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOfferClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("CarFinder") }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search make, model...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )

            when {
                uiState.isRefreshing && uiState.offers.isEmpty() -> LoadingIndicator()
                uiState.offers.isEmpty() -> EmptyState("No offers yet. Pull a search to begin.")
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.offers, key = { it.id }) { offer ->
                        OfferCard(offer = offer, onClick = { onOfferClick(offer.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: CarOffer, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(offer.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(offer.price.formatted(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text("${offer.year ?: "--"} | ${offer.mileageKm.kmOrDash()}", style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(offer.location ?: "--", style = MaterialTheme.typography.bodySmall)
                RegionBadge(offer.region)
            }
            offer.importEstimate?.let {
                Text(
                    "Est. landed cost: ${it.total.formatted()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun RegionBadge(region: Region) {
    val label = when (region) {
        Region.POLAND -> "PL"
        Region.EUROPE -> "EU"
        Region.USA -> "US import"
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
}
