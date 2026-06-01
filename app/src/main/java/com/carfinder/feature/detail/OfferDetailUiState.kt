package com.carfinder.feature.detail

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.ImportCostEstimate

sealed interface OfferDetailUiState {
    data object Loading : OfferDetailUiState
    data class Success(
        val offer: CarOffer,
        val importEstimate: ImportCostEstimate?,
    ) : OfferDetailUiState
    data object NotFound : OfferDetailUiState
}
