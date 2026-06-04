package com.autka.feature.detail

import com.autka.core.model.CarOffer
import com.autka.core.model.Currency
import com.autka.core.model.ExchangeRates
import com.autka.core.model.ImportCostEstimate
import com.autka.core.model.ImportService

sealed interface OfferDetailUiState {
    data object Loading : OfferDetailUiState
    data class Success(
        val offer: CarOffer,
        val importEstimate: ImportCostEstimate?,
        val displayCurrency: Currency,
        val exchangeRates: ExchangeRates?,
        // Editable import-calculator inputs (relevant when importEstimate != null):
        val shippingUsd: Double = 0.0,
        val engineCapacityCc: Int? = null,
        // Region-matched import/sourcing companies (USA/EUROPE offers). Empty -> section hides.
        val importServices: List<ImportService> = emptyList(),
    ) : OfferDetailUiState
    data object NotFound : OfferDetailUiState
}
