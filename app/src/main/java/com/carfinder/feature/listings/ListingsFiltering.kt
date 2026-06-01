package com.carfinder.feature.listings

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.Region
import com.carfinder.core.model.SearchFilter
import com.carfinder.core.model.SortOrder

/**
 * Client-side application of a [SearchFilter] over the locally cached offers, so the
 * list reacts instantly to filter changes. (A network refresh runs in parallel to pull
 * fresh matches from the sources.)
 */
fun List<CarOffer>.applyFilter(f: SearchFilter): List<CarOffer> = filter { o ->
    (f.query.isBlank() ||
        o.title.contains(f.query, ignoreCase = true) ||
        o.make.contains(f.query, ignoreCase = true) ||
        o.model.contains(f.query, ignoreCase = true)) &&
        (f.make == null || o.make.equals(f.make, ignoreCase = true)) &&
        (f.model == null || o.model.equals(f.model, ignoreCase = true)) &&
        (f.minPrice == null || o.price.amount >= f.minPrice) &&
        (f.maxPrice == null || o.price.amount <= f.maxPrice) &&
        (f.minYear == null || (o.year ?: Int.MIN_VALUE) >= f.minYear) &&
        (f.maxYear == null || (o.year ?: Int.MAX_VALUE) <= f.maxYear) &&
        (f.maxMileageKm == null || (o.mileageKm ?: Int.MAX_VALUE) <= f.maxMileageKm) &&
        (f.fuelTypes.isEmpty() || o.fuelType in f.fuelTypes) &&
        (o.region in f.regions) &&
        (f.sourceIds.isEmpty() || o.sourceId in f.sourceIds)
}

fun sortComparator(sort: SortOrder): Comparator<CarOffer> = when (sort) {
    SortOrder.NEWEST -> compareByDescending { it.postedAtEpochMs ?: 0L }
    SortOrder.PRICE_ASC -> compareBy { it.price.amount }
    SortOrder.PRICE_DESC -> compareByDescending { it.price.amount }
    SortOrder.MILEAGE_ASC -> compareBy { it.mileageKm ?: Int.MAX_VALUE }
    SortOrder.YEAR_DESC -> compareByDescending { it.year ?: 0 }
}

/** Number of non-default filter facets, for the toolbar badge. */
fun SearchFilter.activeCount(): Int {
    var n = 0
    if (make != null) n++
    if (model != null) n++
    if (minPrice != null) n++
    if (maxPrice != null) n++
    if (minYear != null) n++
    if (maxYear != null) n++
    if (maxMileageKm != null) n++
    if (fuelTypes.isNotEmpty()) n++
    if (regions.size != Region.entries.size) n++
    if (sourceIds.isNotEmpty()) n++
    if (sort != SortOrder.NEWEST) n++
    return n
}
