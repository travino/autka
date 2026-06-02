import { env, createExecutionContext, waitOnExecutionContext } from "cloudflare:test";
import { describe, it, expect } from "vitest";
import worker from "../src/index";


async function call(path: string, init?: RequestInit) {
  const ctx = createExecutionContext();
  const res = await worker.fetch(new Request(`https://x${path}`, init), env, ctx);
  await waitOnExecutionContext(ctx);
  return res;
}

describe("backend", () => {
  it("health responds ok", async () => {
    const res = await call("/health");
    expect(res.status).toBe(200);
    expect(await res.json()).toMatchObject({ status: "ok" });
  });

  it("lists sources with mock enabled", async () => {
    const res = await call("/sources");
    const body = await res.json() as { sources: { id: string; enabled: boolean }[] };
    const mock = body.sources.find((s) => s.id === "mock");
    expect(mock?.enabled).toBe(true);
  });

  it("returns empty before ingestion, populated after", async () => {
    const before = await call("/offers");
    expect((await before.json() as { count: number }).count).toBe(0);

    // run ingestion via admin endpoint (needs token)
    (env as unknown as { ADMIN_TOKEN: string }).ADMIN_TOKEN = "test-token";
    const ingest = await call("/admin/ingest", {
      method: "POST",
      headers: { authorization: "Bearer test-token" },
    });
    expect(ingest.status).toBe(200);

    const after = await call("/offers?make=BMW");
    const body = await after.json() as { offers: { make: string }[]; count: number };
    expect(body.count).toBeGreaterThan(0);
    expect(body.offers.every((o) => o.make === "BMW")).toBe(true);
  });

  it("rejects unauthorized ingest", async () => {
    const res = await call("/admin/ingest", { method: "POST" });
    expect(res.status).toBe(401);
  });
});
