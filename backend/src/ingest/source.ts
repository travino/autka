import type { CarOffer } from "../lib/types";

/**
 * Server-side aggregation source. Each marketplace implements this; the runner calls
 * every enabled source and upserts the merged result into D1. This mirrors the app's
 * CarOfferSource adapter pattern — but here is where the compliant feeds actually live,
 * so credentials and any fetching stay server-side, never on the device.
 */
export interface IngestSource {
  readonly sourceId: string;
  readonly displayName: string;
  /** Whether this source is configured & permitted to run (e.g. has credentials). */
  isEnabled(env: Env): boolean;
  /** Fetch + normalize current offers into the canonical CarOffer shape. */
  fetch(env: Env): Promise<CarOffer[]>;
}
