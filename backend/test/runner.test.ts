import { env } from "cloudflare:test";
import { describe, it, expect, beforeEach } from "vitest";
import { runIngestion } from "../src/ingest/runner";
import type { IngestSource } from "../src/ingest/source";
import type { CarOffer } from "../src/lib/types";

function offer(id: string, sourceId: string): CarOffer {
  return {
    id, sourceId, title: "Test", make: "VW", model: "Golf", year: 2018,
    mileageKm: 100_000, price: { amount: 50_000, currency: "PLN" },
    fuelType: "DIESEL", transmission: "MANUAL", powerHp: 110,
    location: "Wroclaw, PL", region: "POLAND",
    thumbnailUrl: null, imageUrls: [], listingUrl: `https://x/${id}`,
    postedAtEpochMs: 1, latitude: null, longitude: null,
  };
}

function source(
  sourceId: string,
  fetchImpl: () => Promise<CarOffer[]>,
  enabled = true,
): IngestSource {
  return { sourceId, displayName: sourceId, isEnabled: () => enabled, fetch: fetchImpl };
}

async function runsFor(sourceId: string) {
  const { results } = await env.DB.prepare(
    "SELECT ok, error, offers_upserted FROM ingest_runs WHERE source_id = ? ORDER BY id DESC LIMIT 1",
  ).bind(sourceId).all<{ ok: number; error: string | null; offers_upserted: number }>();
  return results[0];
}

describe("runIngestion isolation", () => {
  beforeEach(async () => {
    await env.DB.prepare("DELETE FROM ingest_runs").run();
  });

  it("a failing source does not stop the others", async () => {
    const results = await runIngestion(env, [
      source("good_a", async () => [offer("good_a:1", "good_a")]),
      source("bad", async () => { throw new Error("feed down"); }),
      source("good_b", async () => [offer("good_b:1", "good_b")]),
    ]);

    const byId = Object.fromEntries(results.map((r) => [r.sourceId, r]));
    expect(byId.good_a).toMatchObject({ ok: true, upserted: 1 });
    expect(byId.good_b).toMatchObject({ ok: true, upserted: 1 });
    expect(byId.bad).toMatchObject({ ok: false, upserted: 0, error: "feed down" });
  });

  it("records an ingest_runs row per source, capturing the error", async () => {
    await runIngestion(env, [
      source("ok_src", async () => [offer("ok_src:1", "ok_src")]),
      source("err_src", async () => { throw new Error("boom"); }),
    ]);

    expect(await runsFor("ok_src")).toMatchObject({ ok: 1, error: null, offers_upserted: 1 });
    expect(await runsFor("err_src")).toMatchObject({ ok: 0, error: "boom", offers_upserted: 0 });
  });

  it("skips disabled sources entirely (no run row)", async () => {
    const results = await runIngestion(env, [
      source("on", async () => [offer("on:1", "on")]),
      source("off", async () => { throw new Error("should never run"); }, false),
    ]);

    expect(results.map((r) => r.sourceId)).toEqual(["on"]);
    expect(await runsFor("off")).toBeUndefined();
  });

  it("coerces non-Error throws into a string message", async () => {
    await runIngestion(env, [
      source("weird", async () => { throw "plain string failure"; }),
    ]);
    expect(await runsFor("weird")).toMatchObject({ ok: 0, error: "plain string failure" });
  });
});
