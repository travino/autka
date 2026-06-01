# CarFinder

An Android app that aggregates used-car offers from multiple marketplaces across
**Poland, the rest of Europe, and US import sources** into one searchable list, with
landed-cost estimation for vehicles imported from the USA.

## Status

Runnable scaffold. The app builds and runs today against a built-in **sample data
source**, so you can see the full flow (search -> filter -> list -> detail -> import
cost breakdown) immediately. Filtering by make, price, year, mileage, fuel, region,
source and sort order is wired end-to-end (live local filtering over the cache plus a
network refresh). Real marketplace adapters are present as documented stubs.

## Architecture

Single-module app organized by Google's recommended layers (UI / Data), Kotlin +
Jetpack Compose, Hilt DI, Room (offline-first), Kotlin Flow.

```
core/model        Normalized domain model (CarOffer, Money, SearchFilter, ImportCostEstimate)
data/local        Room database, DAO, entity + mappers (local cache = source of truth)
data/remote       CarOfferSource adapter interface + one adapter per marketplace
data/repository   OfflineFirstCarOfferRepository: fans queries to all sources, merges, caches
feature/listings  Search + results screen (ViewModel + Compose)
feature/detail    Offer detail + US import cost breakdown
di                Hilt modules (database, repository, sources multibinding)
ui                Theme, navigation host, shared components
```

The key design point: **every marketplace is a `CarOfferSource` adapter** contributed
into a multibound `Set` in `di/SourcesModule.kt`. Adding a marketplace is one `@Binds
@IntoSet` line; the repository merges whatever is enabled. A source failing (network,
auth) is isolated so the others still return.

## Data sourcing — read this

The app's architecture is the easy part; lawfully obtaining listing data is the real
work, and it's your responsibility. Summary of each adapter's status:

| Source | Adapter | Status | Notes |
|--------|---------|--------|-------|
| Sample | `MockCarOfferSource` | Enabled | Built-in demo data, zero config |
| Otomoto | `OtomotoCarOfferSource` | Stub (disabled) | OLX Group; no open public API. Needs a partner/dealer-feed agreement or licensed data. Scraping restricted by ToS. |
| OLX | `OlxCarOfferSource` | Stub (disabled) | Partner/affiliate API under agreement only. |
| Facebook Marketplace | `FacebookMarketplaceSource` | Disabled by design | Meta ToS prohibits scraping; no listings API. Consider deep-linking the user into a pre-filled Marketplace search instead of ingesting. |
| US auctions (import) | `UsAuctionCarOfferSource` | Stub (disabled) | Copart/IAAI etc. require membership or a licensed broker API. |

The realistic production shape is a **backend** that holds the compliant feeds,
normalizes them, and serves one clean API the app consumes — not direct scraping from
the device. The adapter interface is designed so each adapter can simply call your
backend endpoint for that source.

## US import cost

`core/model/ImportCostCalculator.kt` estimates landed cost into Poland (shipping +
EU customs duty + PL excise/akcyza + 23% VAT). Rates are indicative constants —
externalize and verify them before relying on the numbers. The detail screen shows the
full breakdown for any USA-region offer.

## Build & run

Requires Android Studio (Ladybug or newer) and JDK 17.

```bash
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # install on a connected device/emulator
```

Or open the folder in Android Studio and hit Run. First sync downloads dependencies.

## Versions

Kotlin 2.0.21, AGP 8.7.3, Gradle 8.11.1, Compose BOM 2024.12.01, Hilt 2.52, Room 2.6.1,
compileSdk 35, minSdk 26. Bump via the version catalog at `gradle/libs.versions.toml`.

## Next steps

- Stand up the aggregation backend and point the stub adapters at it.
- Add currency conversion so mixed PLN/EUR/USD results sort and compare correctly
  (filtering/sorting currently compares raw amounts across currencies).
- Make US import shipping/engine-capacity inputs editable in the detail screen.
- Tests: repository merge/failure-isolation, import calculator, mapper round-trips,
  `applyFilter`/`sortComparator`.
