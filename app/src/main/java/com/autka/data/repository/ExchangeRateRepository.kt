package com.autka.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.autka.core.model.Currency
import com.autka.core.model.ExchangeRates
import com.autka.data.remote.rates.NbpRateProvider
import com.autka.data.remote.rates.StaticRateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface ExchangeRateRepository {
    /** Latest rates; always has a value (seeded with offline fallback). */
    fun rates(): StateFlow<ExchangeRates>

    /** Try to refresh from the live source; silently keeps current rates on failure. */
    suspend fun refresh()
}

@Singleton
class DefaultExchangeRateRepository @Inject constructor(
    private val live: NbpRateProvider,
    staticRates: StaticRateProvider,
    private val dataStore: DataStore<Preferences>,
) : ExchangeRateRepository {

    // Static fallback is the immediate initial value so rates() never blocks.
    private val state = MutableStateFlow(staticRates.snapshot())
    private var hydrated = false

    override fun rates(): StateFlow<ExchangeRates> = state.asStateFlow()

    override suspend fun refresh() {
        hydrateFromCacheOnce()
        runCatching { live.latest() }.onSuccess { fresh ->
            state.value = fresh
            persist(fresh)
        }
        // on failure: keep current rates (cached-on-launch, else static fallback)
    }

    /**
     * Seed from the last persisted snapshot exactly once, so a cold start shows the
     * most recent real rates instead of the static seed while the network call is in
     * flight (or if it never succeeds). Guarded + lazy, so no startup call site needs
     * to change and repeated refresh() calls don't re-read the store.
     */
    private suspend fun hydrateFromCacheOnce() {
        if (hydrated) return
        hydrated = true
        readCached()?.let { cached -> state.value = cached }
    }

    private suspend fun readCached(): ExchangeRates? {
        val prefs = dataStore.data.first()
        val base = prefs[KEY_BASE]
            ?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
            ?: return null
        val perUnit = prefs[KEY_PER_UNIT]?.let(::decodeRates).orEmpty()
        if (perUnit.isEmpty()) return null
        return ExchangeRates(
            base = base,
            perUnit = perUnit,
            asOfEpochMs = prefs[KEY_AS_OF] ?: 0L,
            isStale = false, // it was a real fetch last session
        )
    }

    private suspend fun persist(rates: ExchangeRates) {
        dataStore.edit { prefs ->
            prefs[KEY_BASE] = rates.base.name
            prefs[KEY_PER_UNIT] = encodeRates(rates.perUnit)
            prefs[KEY_AS_OF] = rates.asOfEpochMs
        }
    }

    private companion object {
        val KEY_BASE = stringPreferencesKey("rates_base")
        val KEY_PER_UNIT = stringPreferencesKey("rates_per_unit")
        val KEY_AS_OF = longPreferencesKey("rates_as_of_epoch_ms")

        // "PLN:1.0;EUR:4.3;USD:4.0" — dependency-free, no serialization lib needed.
        fun encodeRates(perUnit: Map<Currency, Double>): String =
            perUnit.entries.joinToString(";") { (c, v) -> "${c.name}:$v" }

        fun decodeRates(encoded: String): Map<Currency, Double> =
            encoded.split(";").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 2) return@mapNotNull null
                val currency = runCatching { Currency.valueOf(parts[0]) }.getOrNull()
                    ?: return@mapNotNull null
                val rate = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                currency to rate
            }.toMap()
    }
}
