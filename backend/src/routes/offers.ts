import { Hono } from "hono";
import type { SearchFilter, FuelType, Region } from "../lib/types";
import { queryOffers, getOffer } from "../db/offers";
import { ALL_SOURCES } from "../ingest/runner";

export const offersRouter = new Hono<{ Bindings: Env }>();

// GET /offers — search with query params mirroring the app's SearchFilter.
offersRouter.get("/offers", async (c) => {
  const q = c.req.query();
  const num = (v: string | undefined) => (v != null && v !== "" ? Number(v) : undefined);
  const list = (v: string | undefined) => (v ? v.split(",").filter(Boolean) : undefined);

  const filter: SearchFilter = {
    query: q.query || undefined,
    make: q.make || undefined,
    model: q.model || undefined,
    minPrice: num(q.minPrice),
    maxPrice: num(q.maxPrice),
    minYear: num(q.minYear),
    maxYear: num(q.maxYear),
    maxMileageKm: num(q.maxMileageKm),
    fuelTypes: list(q.fuelTypes) as FuelType[] | undefined,
    regions: list(q.regions) as Region[] | undefined,
    sourceIds: list(q.sources),
    sort: (q.sort as SearchFilter["sort"]) || "NEWEST",
    limit: num(q.limit),
    offset: num(q.offset),
  };

  const offers = await queryOffers(c.env.DB, filter);
  return c.json({ offers, count: offers.length });
});

// GET /offers/:id — single offer.
offersRouter.get("/offers/:id", async (c) => {
  const offer = await getOffer(c.env.DB, c.req.param("id"));
  if (!offer) return c.json({ error: "not_found" }, 404);
  return c.json(offer);
});

// GET /sources — which sources exist and whether they're enabled (for app UI toggles).
offersRouter.get("/sources", (c) => {
  const sources = ALL_SOURCES.map((s) => ({
    id: s.sourceId,
    displayName: s.displayName,
    enabled: s.isEnabled(c.env),
  }));
  return c.json({ sources });
});
