package com.carfinder.data.remote.usimport

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.SourceId
import javax.inject.Inject

/**
 * US auction / import adapter (e.g. Copart, IAAI via a licensed broker API).
 *
 * DATA SOURCING NOTE
 * ------------------
 * US salvage/auction platforms require registered membership or a licensed broker
 * API to access listings; direct scraping is restricted. When wired up, populate
 * each [CarOffer.importEstimate] using
 * [com.carfinder.core.model.ImportCostCalculator] so users compare true landed cost
 * into Poland, not just the auction price. Disabled until a broker feed is configured.
 */
class UsAuctionCarOfferSource @Inject constructor() : CarOfferSource {
    override val sourceId = SourceId.US_AUCTION
    override val displayName = "US auctions (import)"
    override val isEnabled = false

    override suspend fun fetch(filter: SearchFilter): List<CarOffer> {
        if (!isEnabled) return emptyList()
        TODO("Map broker auction feed into List<CarOffer> with importEstimate populated")
    }
}
