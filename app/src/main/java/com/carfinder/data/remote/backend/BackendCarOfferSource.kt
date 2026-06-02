package com.carfinder.data.remote.backend

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.SourceId
import javax.inject.Inject

/**
 * Single source that delegates aggregation to the backend. Replaces the per-marketplace
 * client adapters: the backend merges Otomoto/OLX/US-auction/... server-side, so the app
 * no longer holds marketplace credentials or scraping logic.
 */
class BackendCarOfferSource @Inject constructor(
    private val api: BackendApi,
) : CarOfferSource {
    override val sourceId = SourceId.BACKEND
    override val displayName = "CarGate backend"
    override val isEnabled = true

    override suspend fun fetch(filter: SearchFilter): List<CarOffer> {
        val resp = api.offers(
            query = filter.query.ifBlank { null },
            make = filter.make,
            model = filter.model,
            minPrice = filter.minPrice,
            maxPrice = filter.maxPrice,
            minYear = filter.minYear,
            maxYear = filter.maxYear,
            maxMileageKm = filter.maxMileageKm,
            fuelTypes = filter.fuelTypes.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name },
            regions = filter.regions.joinToString(",") { it.name },
            sources = filter.sourceIds.takeIf { it.isNotEmpty() }?.joinToString(","),
            sort = filter.sort.name,
        )
        return resp.offers.map { it.toModel() }
    }
}
