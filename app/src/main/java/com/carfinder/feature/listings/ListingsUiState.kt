package com.carfinder.feature.listings

import com.carfinder.core.model.CarOffer

data class ListingsUiState(
    val isRefreshing: Boolean = false,
    val offers: List<CarOffer> = emptyList(),
    val query: String = "",
    val failedSources: List<String> = emptyList(),
    val errorMessage: String? = null,
)
