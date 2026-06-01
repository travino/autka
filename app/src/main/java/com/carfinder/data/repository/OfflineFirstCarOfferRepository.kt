package com.carfinder.data.repository

import com.carfinder.core.model.CarOffer
import com.carfinder.core.model.SearchFilter
import com.carfinder.data.local.CarOfferDao
import com.carfinder.data.local.toEntity
import com.carfinder.data.local.toModel
import com.carfinder.data.remote.CarOfferSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstCarOfferRepository @Inject constructor(
    private val dao: CarOfferDao,
    private val sources: Set<@JvmSuppressWildcards CarOfferSource>,
) : CarOfferRepository {

    override fun observeOffers(): Flow<List<CarOffer>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeOffer(id: String): Flow<CarOffer?> =
        dao.observeById(id).map { it?.toModel() }

    override suspend fun refresh(filter: SearchFilter): List<String> = coroutineScope {
        val active = sources.filter { source ->
            source.isEnabled &&
                (filter.sourceIds.isEmpty() || source.sourceId in filter.sourceIds)
        }
        val failed = mutableListOf<String>()
        val results = active.map { source ->
            async {
                runCatching { source.fetch(filter) }
                    .getOrElse { failed += source.sourceId; emptyList() }
            }
        }.flatMap { it.await() }

        if (results.isNotEmpty()) {
            dao.upsertAll(results.map { it.toEntity() })
        }
        failed
    }

    override fun availableSources(): List<SourceInfo> =
        sources.map { SourceInfo(it.sourceId, it.displayName, it.isEnabled) }
            .sortedBy { it.displayName }
}
