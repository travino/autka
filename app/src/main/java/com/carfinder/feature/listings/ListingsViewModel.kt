package com.carfinder.feature.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.repository.CarOfferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val repository: CarOfferRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ListingsUiState())

    val uiState: StateFlow<ListingsUiState> =
        combine(repository.observeOffers(), mutableState) { offers, state ->
            state.copy(offers = offers)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListingsUiState(),
        )

    init {
        refresh()
    }

    fun onQueryChange(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isRefreshing = true, errorMessage = null)
            val filter = SearchFilter(query = mutableState.value.query)
            val failed = runCatching { repository.refresh(filter) }
                .getOrElse {
                    mutableState.value = mutableState.value.copy(
                        isRefreshing = false,
                        errorMessage = it.message ?: "Failed to refresh",
                    )
                    return@launch
                }
            mutableState.value = mutableState.value.copy(
                isRefreshing = false,
                failedSources = failed,
            )
        }
    }
}
