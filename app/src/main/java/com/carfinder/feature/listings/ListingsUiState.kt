package com.carfinder.feature.listings

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.Currency
import com.carfinder.core.model.ExchangeRates
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.repository.SourceInfo

data class ListingsUiState(
    val isRefreshing: Boolean = false,
    val offers: List<CarOffer> = emptyList(),
    val filter: SearchFilter = SearchFilter(),
    val availableMakes: List<String> = emptyList(),
    val availableSources: List<SourceInfo> = emptyList(),
    val failedSources: List<String> = emptyList(),
    val errorMessage: String? = null,
    val displayCurrency: Currency = Currency.PLN,
    val exchangeRates: ExchangeRates? = null,
) {
    val activeFilterCount: Int get() = filter.activeCount()
    val ratesAreStale: Boolean get() = exchangeRates?.isStale ?: true
}
