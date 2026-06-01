package com.carfinder.di

import com.carfinder.data.remote.CarOfferSource
import com.carfinder.data.remote.facebook.FacebookMarketplaceSource
import com.carfinder.data.remote.mock.MockCarOfferSource
import com.carfinder.data.remote.olx.OlxCarOfferSource
import com.carfinder.data.remote.otomoto.OtomotoCarOfferSource
import com.carfinder.data.remote.usimport.UsAuctionCarOfferSource
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Every source is contributed into a Set<CarOfferSource>. The repository iterates the
 * set, so adding a new marketplace is a one-line @Binds @IntoSet here -- nothing else
 * in the app needs to change.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SourcesModule {

    @Binds @IntoSet
    abstract fun bindMock(source: MockCarOfferSource): CarOfferSource

    @Binds @IntoSet
    abstract fun bindOtomoto(source: OtomotoCarOfferSource): CarOfferSource

    @Binds @IntoSet
    abstract fun bindOlx(source: OlxCarOfferSource): CarOfferSource

    @Binds @IntoSet
    abstract fun bindFacebook(source: FacebookMarketplaceSource): CarOfferSource

    @Binds @IntoSet
    abstract fun bindUsAuction(source: UsAuctionCarOfferSource): CarOfferSource
}
