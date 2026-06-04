package com.autka.data.imports

import com.autka.core.model.ImportService
import com.autka.core.model.Region

/**
 * Compiled-in import-company directory. Acts as the offline fallback and initial seed
 * for [ImportServicesRepository]; the backend's GET /import-services overrides it at
 * runtime so brokers can be added without an app release. Keep in sync with
 * backend/src/data/import-services.ts.
 *
 * ⚠️ Only usaimport.pl's calculator path is verified; other URLs are homepages —
 * confirm real landing/calculator paths before relying on the deep target.
 */
object DefaultImportServices {
    val ALL: List<ImportService> = listOf(
        ImportService(
            id = "usaimport", displayName = "USA Import", origin = Region.USA,
            url = "https://usaimport.pl/",
            calculatorUrl = "https://usaimport.pl/koszty-sprowadzenia-auta-z-usa-kalkulator/",
            note = "Import z USA i Kanady, aukcje Copart/IAAI, dostawa pod dom",
        ),
        ImportService(
            id = "usacars", displayName = "USACARS", origin = Region.USA,
            url = "https://usacars.net.pl/pl",
            note = "Sprowadzanie aut z USA", // TODO(verify): landing/calculator path
        ),
        ImportService(
            id = "mattyusa", displayName = "MattyUSA", origin = Region.USA,
            url = "https://mattyusa.pl/",
            note = "Import samochodów z USA", // TODO(verify)
        ),
        ImportService(
            id = "autopan", displayName = "AutoPan", origin = Region.EUROPE,
            url = "https://autopan.pl/",
            note = "Sprowadzanie i import aut z Europy", // TODO(verify)
        ),
    )
}
