package com.carfinder.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carfinder.core.model.ImportCostCalculator
import com.carfinder.core.model.Region
import com.carfinder.data.repository.CarOfferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OfferDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CarOfferRepository,
) : ViewModel() {

    private val offerId: String = checkNotNull(savedStateHandle["offerId"])

    // Default assumption for shipping a US car to Poland; surface as editable in UI later.
    private val defaultUsShippingUsd = 2_400.0

    val uiState: StateFlow<OfferDetailUiState> =
        repository.observeOffer(offerId).map { offer ->
            when {
                offer == null -> OfferDetailUiState.NotFound
                offer.region == Region.USA -> OfferDetailUiState.Success(
                    offer = offer,
                    importEstimate = ImportCostCalculator.estimate(
                        vehiclePriceUsd = offer.price.amount,
                        shippingUsd = defaultUsShippingUsd,
                        engineCapacityCc = null,
                    ),
                )
                else -> OfferDetailUiState.Success(offer, importEstimate = null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OfferDetailUiState.Loading,
        )
}
