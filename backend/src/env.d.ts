// Secrets are not emitted by `wrangler types` (set via `wrangler secret put`), so we
// add them here. Augments the generated top-level `Env` interface used as Hono Bindings.
interface Env {
  /** Bearer token guarding POST /admin/ingest. Set via: wrangler secret put ADMIN_TOKEN */
  ADMIN_TOKEN?: string;
}
