import type { CarOffer, SearchFilter } from "../lib/types";

// Row shape as stored in D1 (snake_case columns).
interface OfferRow {
  id: string;
  source_id: string;
  title: string;
  make: string;
  model: string;
  year: number | null;
  mileage_km: number | null;
  price_amount: number;
  price_currency: string;
  fuel_type: string;
  transmission: string;
  power_hp: number | null;
  location: string | null;
  region: string;
  thumbnail_url: string | null;
  image_urls: string;
  listing_url: string;
  posted_at_ms: number | null;
}

function rowToOffer(r: OfferRow): CarOffer {
  return {
    id: r.id,
    sourceId: r.source_id,
    title: r.title,
    make: r.make,
    model: r.model,
    year: r.year,
    mileageKm: r.mileage_km,
    price: { amount: r.price_amount, currency: r.price_currency as CarOffer["price"]["currency"] },
    fuelType: r.fuel_type as CarOffer["fuelType"],
    transmission: r.transmission as CarOffer["transmission"],
    powerHp: r.power_hp,
    location: r.location,
    region: r.region as CarOffer["region"],
    thumbnailUrl: r.thumbnail_url,
    imageUrls: r.image_urls ? r.image_urls.split(";") : [],
    listingUrl: r.listing_url,
    postedAtEpochMs: r.posted_at_ms,
  };
}

const SORT_SQL: Record<NonNullable<SearchFilter["sort"]>, string> = {
  NEWEST: "posted_at_ms DESC",
  PRICE_ASC: "price_amount ASC",
  PRICE_DESC: "price_amount DESC",
  MILEAGE_ASC: "mileage_km ASC",
  YEAR_DESC: "year DESC",
};

/**
 * Query offers with a parameterized filter. Uses plain positional `?` placeholders
 * bound in order — all inputs are bound, never string-interpolated, so this is
 * injection-safe. (LIMIT/OFFSET are clamped numbers, safe to inline.)
 */
export async function queryOffers(db: D1Database, f: SearchFilter): Promise<CarOffer[]> {
  const where: string[] = [];
  const binds: unknown[] = [];

  if (f.query) {
    where.push("(title LIKE ? OR make LIKE ? OR model LIKE ?)");
    const like = `%${f.query}%`;
    binds.push(like, like, like);
  }
  if (f.make) { where.push("make = ?"); binds.push(f.make); }
  if (f.model) { where.push("model = ?"); binds.push(f.model); }
  if (f.minPrice != null) { where.push("price_amount >= ?"); binds.push(f.minPrice); }
  if (f.maxPrice != null) { where.push("price_amount <= ?"); binds.push(f.maxPrice); }
  if (f.minYear != null) { where.push("year >= ?"); binds.push(f.minYear); }
  if (f.maxYear != null) { where.push("year <= ?"); binds.push(f.maxYear); }
  if (f.maxMileageKm != null) { where.push("mileage_km <= ?"); binds.push(f.maxMileageKm); }
  if (f.regions?.length) {
    where.push(`region IN (${f.regions.map(() => "?").join(",")})`);
    binds.push(...f.regions);
  }
  if (f.sourceIds?.length) {
    where.push(`source_id IN (${f.sourceIds.map(() => "?").join(",")})`);
    binds.push(...f.sourceIds);
  }

  const sort = SORT_SQL[f.sort ?? "NEWEST"];
  const limit = Math.min(Math.max(f.limit ?? 50, 1), 200);
  const offset = Math.max(f.offset ?? 0, 0);

  const sql =
    `SELECT * FROM offers` +
    (where.length ? ` WHERE ${where.join(" AND ")}` : "") +
    ` ORDER BY ${sort} LIMIT ${limit} OFFSET ${offset}`;

  const { results } = await db.prepare(sql).bind(...binds).all<OfferRow>();
  return results.map(rowToOffer);
}

export async function getOffer(db: D1Database, id: string): Promise<CarOffer | null> {
  const row = await db.prepare("SELECT * FROM offers WHERE id = ?").bind(id).first<OfferRow>();
  return row ? rowToOffer(row) : null;
}

/** Batch upsert. D1 supports batching prepared statements in one round trip. */
export async function upsertOffers(db: D1Database, offers: CarOffer[]): Promise<number> {
  if (offers.length === 0) return 0;
  const now = Date.now();
  const stmt = db.prepare(
    `INSERT INTO offers (
       id, source_id, title, make, model, year, mileage_km,
       price_amount, price_currency, fuel_type, transmission, power_hp,
       location, region, thumbnail_url, image_urls, listing_url,
       posted_at_ms, fetched_at_ms
     ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
     ON CONFLICT(id) DO UPDATE SET
       title=excluded.title, make=excluded.make, model=excluded.model,
       year=excluded.year, mileage_km=excluded.mileage_km,
       price_amount=excluded.price_amount, price_currency=excluded.price_currency,
       fuel_type=excluded.fuel_type, transmission=excluded.transmission,
       power_hp=excluded.power_hp, location=excluded.location, region=excluded.region,
       thumbnail_url=excluded.thumbnail_url, image_urls=excluded.image_urls,
       listing_url=excluded.listing_url, posted_at_ms=excluded.posted_at_ms,
       fetched_at_ms=excluded.fetched_at_ms`,
  );
  const batch = offers.map((o) =>
    stmt.bind(
      o.id, o.sourceId, o.title, o.make, o.model, o.year, o.mileageKm,
      o.price.amount, o.price.currency, o.fuelType, o.transmission, o.powerHp,
      o.location, o.region, o.thumbnailUrl, o.imageUrls.join(";"), o.listingUrl,
      o.postedAtEpochMs, now,
    ),
  );
  await db.batch(batch);
  return offers.length;
}
