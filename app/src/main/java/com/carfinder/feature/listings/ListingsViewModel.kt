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

    private val filter = MutableStateFlow(SearchFilter())
    private val transient = MutableStateFlow(TransientState())

    val uiState: StateFlow<ListingsUiState> =
        combine(repository.observeOffers(), filter, transient) { offers, f, t ->
            ListingsUiState(
                isRefreshing = t.isRefreshing,
                offers = offers.applyFilter(f).sortedWith(sortComparator(f.sort)),
                filter = f,
                availableMakes = offers.map { it.make }.distinct().sorted(),
                availableSources = repository.availableSources(),
                failedSources = t.failedSources,
                errorMessage = t.errorMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListingsUiState(availableSources = repository.availableSources()),
        )

    init {
        refresh()
    }

    fun onQueryChange(query: String) {
        filter.value = filter.value.copy(query = query)
    }

    /** Apply a new filter from the filter sheet and pull matching results. */
    fun onApplyFilter(newFilter: SearchFilter) {
        filter.value = newFilter
        refresh()
    }

    /** Clear everything except the current free-text query. */
    fun onResetFilter() {
        filter.value = SearchFilter(query = filter.value.query)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            transient.value = transient.value.copy(isRefreshing = true, errorMessage = null)
            val failed = runCatching { repository.refresh(filter.value) }
                .getOrElse {
                    transient.value = transient.value.copy(
                        isRefreshing = false,
                        errorMessage = it.message ?: "Failed to refresh",
                    )
                    return@launch
                }
            transient.value = transient.value.copy(isRefreshing = false, failedSources = failed)
        }
    }
}

private data class TransientState(
    val isRefreshing: Boolean = false,
    val failedSources: List<String> = emptyList(),
    val errorMessage: String? = null,
)
