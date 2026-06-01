package com.carfinder.data.remote.olx

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.SourceId
import javax.inject.Inject

/**
 * OLX adapter (Poland + several European markets).
 *
 * DATA SOURCING NOTE
 * ------------------
 * OLX exposes a partner/affiliate API only under agreement; general listing scraping
 * is restricted by its Terms of Service. Wire [fetch] to a compliant feed and map
 * into [CarOffer]. Disabled until configured.
 */
class OlxCarOfferSource @Inject constructor() : CarOfferSource {
    override val sourceId = SourceId.OLX
    override val displayName = "OLX"
    override val isEnabled = false

    override suspend fun fetch(filter: SearchFilter): List<CarOffer> {
        if (!isEnabled) return emptyList()
        TODO("Map your OLX feed response into List<CarOffer>")
    }
}
