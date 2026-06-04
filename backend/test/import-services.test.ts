import { env, createExecutionContext, waitOnExecutionContext } from "cloudflare:test";
import { describe, it, expect } from "vitest";
import worker from "../src/index";
import { IMPORT_SERVICES } from "../src/data/import-services";

async function call(path: string, init?: RequestInit) {
  const ctx = createExecutionContext();
  const res = await worker.fetch(new Request(`https://x${path}`, init), env, ctx);
  await waitOnExecutionContext(ctx);
  return res;
}

type Body = {
  services: { id: string; displayName: string; origin: string; url: string }[];
  count: number;
};

describe("import-services", () => {
  it("returns the full directory", async () => {
    const res = await call("/import-services");
    expect(res.status).toBe(200);
    const body = (await res.json()) as Body;
    expect(body.count).toBe(IMPORT_SERVICES.length);
    expect(body.services).toHaveLength(body.count);
    // shape check on a known, verified entry
    const usa = body.services.find((s) => s.id === "usaimport");
    expect(usa).toMatchObject({ origin: "USA", url: "https://usaimport.pl/" });
  });

  it("filters by region=USA", async () => {
    const res = await call("/import-services?region=USA");
    const body = (await res.json()) as Body;
    expect(body.count).toBeGreaterThan(0);
    expect(body.services.every((s) => s.origin === "USA")).toBe(true);
  });

  it("filters by region=EUROPE", async () => {
    const res = await call("/import-services?region=EUROPE");
    const body = (await res.json()) as Body;
    expect(body.services.every((s) => s.origin === "EUROPE")).toBe(true);
  });

  it("returns none for region=POLAND (already in PL)", async () => {
    const res = await call("/import-services?region=POLAND");
    const body = (await res.json()) as Body;
    expect(body.count).toBe(0);
  });

  it("ignores an invalid region and returns all (validate, don't assert)", async () => {
    const res = await call("/import-services?region=NOPE");
    expect(res.status).toBe(200);
    const body = (await res.json()) as Body;
    expect(body.count).toBe(IMPORT_SERVICES.length);
  });
});
