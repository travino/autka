package com.autka.feature.external

import com.autka.core.model.Region

/**
 * An import/sourcing company — a business that brings a car into Poland (from US
 * auctions or from elsewhere in Europe), as opposed to a marketplace you search.
 *
 * Crucial distinction from [MarketplaceSearchLinks]: these are NOT searchable inventory
 * and do NOT take a SearchFilter. Most are brochure/service sites (often WordPress) with
 * a quote form and a cost calculator, not a filterable car list. So we surface them as a
 * curated directory on the OFFER DETAIL screen, next to the landed-cost breakdown —
 * "import this car via:" — never as filtered deep-links on the results screen.
 *
 * [origin] is the region the company imports FROM, matched against an offer's region:
 * a USA offer shows US importers, a EUROPE offer shows EU importers, a POLAND offer
 * shows none (it's already here).
 *
 * This built-in list is the offline fallback / source of truth, mirroring the app's
 * offline-first rates pattern (StaticRateProvider). The backend serves the authoritative
 * list at GET /import-services so brokers can be added without an app release; when you
 * wire that fetch in, this list becomes the seed. Keep it in sync with
 * backend/src/data/import-services.ts.
 *
 * ⚠️ Only usaimport.pl's calculator path is verified. Other URLs are homepages; confirm
 * each company's real landing/calculator path before relying on the deep target.
 */
data class ImportService(
    val id: String,
    val displayName: String,
    val origin: Region,
    val url: String,
    val calculatorUrl: String? = null, // direct link to their import-cost calculator, if any
    val note: String? = null,          // short PL descriptor for the UI
)

object ImportServices {

    /** Companies importing from the USA (and Canada). */
    private val usa = listOf(
        ImportService(
            id = "usaimport",
            displayName = "USA Import",
            origin = Region.USA,
            url = "https://usaimport.pl/",
            calculatorUrl = "https://usaimport.pl/koszty-sprowadzenia-auta-z-usa-kalkulator/", // verified
            note = "Import z USA i Kanady, aukcje Copart/IAAI, dostawa pod dom",
        ),
        ImportService(
            id = "usacars",
            displayName = "USACARS",
            origin = Region.USA,
            url = "https://usacars.net.pl/pl",
            note = "Sprowadzanie aut z USA", // TODO(verify): real landing/calculator path
        ),
        ImportService(
            id = "mattyusa",
            displayName = "MattyUSA",
            origin = Region.USA,
            url = "https://mattyusa.pl/",
            note = "Import samochodów z USA", // TODO(verify)
        ),
    )

    /** Companies importing from elsewhere in Europe. */
    private val europe = listOf(
        ImportService(
            id = "autopan",
            displayName = "AutoPan",
            origin = Region.EUROPE,
            url = "https://autopan.pl/",
            note = "Sprowadzanie i import aut z Europy", // TODO(verify)
        ),
    )

    private val all = usa + europe

    /** Importers matching an offer's region. POLAND -> none (already in PL). */
    fun forRegion(region: Region): List<ImportService> = when (region) {
        Region.USA -> usa
        Region.EUROPE -> europe
        Region.POLAND -> emptyList()
    }
}
