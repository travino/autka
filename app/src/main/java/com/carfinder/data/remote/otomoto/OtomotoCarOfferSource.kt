package com.carfinder.data.remote.otomoto

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.SourceId
import javax.inject.Inject

/**
 * Otomoto adapter (OLX Group, Poland's largest car marketplace).
 *
 * DATA SOURCING NOTE
 * ------------------
 * Otomoto does not offer a public, open listings API for third parties. Legitimate
 * access routes are: an official OLX Group / Otomoto partner or dealer-feed agreement,
 * or a licensed data provider. Scraping the public site is restricted by Otomoto's
 * Terms of Service. This class is a documented stub: implement [fetch] against
 * whichever feed you are contractually entitled to use, mapping the response into
 * [CarOffer]. Leave [isEnabled] = false until a real backend/feed is wired in.
 */
class OtomotoCarOfferSource @Inject constructor(
    // inject your configured Retrofit service / backend client here
) : CarOfferSource {
    override val sourceId = SourceId.OTOMOTO
    override val displayName = "Otomoto"
    override val isEnabled = false // flip on once a compliant feed is configured

    override suspend fun fetch(filter: SearchFilter): List<CarOffer> {
        if (!isEnabled) return emptyList()
        TODO("Map your Otomoto partner-feed / backend response into List<CarOffer>")
    }
}
