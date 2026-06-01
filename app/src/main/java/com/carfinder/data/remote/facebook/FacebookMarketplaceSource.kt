package com.carfinder.data.remote.facebook

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.SourceId
import javax.inject.Inject

/**
 * Facebook Marketplace adapter.
 *
 * IMPORTANT -- READ BEFORE ENABLING
 * ---------------------------------
 * Meta's Terms of Service prohibit automated scraping of Facebook/Marketplace, and
 * Marketplace has no public listings API for vehicle aggregation. There is no
 * compliant programmatic path for a third party to pull general Marketplace car
 * listings today. This adapter is intentionally left disabled and unimplemented.
 *
 * Practical compliant alternative: let the user open Marketplace via deep link with a
 * pre-filled search built from their [SearchFilter], rather than ingesting listings.
 */
class FacebookMarketplaceSource @Inject constructor() : CarOfferSource {
    override val sourceId = SourceId.FACEBOOK
    override val displayName = "Facebook Marketplace"
    override val isEnabled = false

    override suspend fun fetch(filter: SearchFilter): List<CarOffer> {
        // No compliant ingestion path. Returns nothing by design.
        return emptyList()
    }
}
