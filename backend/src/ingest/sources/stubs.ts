import type { IngestSource } from "../source";
import type { CarOffer } from "../../lib/types";

/**
 * Documented stub connectors for the real marketplaces. Each is DISABLED until a
 * compliant data feed is configured (see notes per source). When you implement one,
 * fetch from the permitted feed and map the payload into CarOffer[].
 *
 * IMPORTANT — the legal acquisition path, not the code, is the hard part:
 *  - Otomoto / OLX (OLX Group): no open public listings API. Requires an official
 *    partner / dealer-feed agreement or a licensed data provider. Scraping the public
 *    site is restricted by their Terms of Service.
 *  - Facebook Marketplace: Meta's ToS prohibits scraping and there is no listings API
 *    for third-party aggregation. There is no compliant ingestion path; prefer deep-
 *    linking users into a pre-filled Marketplace search instead. Left unimplemented.
 *  - US auctions (Copart / IAAI ...): require registered membership or a licensed
 *    broker API. Populate import cost on the client (the app already does this).
 */

function disabledStub(sourceId: string, displayName: string): IngestSource {
  return {
    sourceId,
    displayName,
    isEnabled: () => false, // flip on once a compliant feed + credentials are configured
    async fetch(): Promise<CarOffer[]> {
      // No compliant feed wired in yet — return nothing rather than scraping.
      return [];
    },
  };
}

export const otomotoSource = disabledStub("otomoto", "Otomoto");
export const olxSource = disabledStub("olx", "OLX");
export const facebookSource = disabledStub("facebook", "Facebook Marketplace");
export const usAuctionSource = disabledStub("us_auction", "US auctions (import)");
