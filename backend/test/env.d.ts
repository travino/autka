/// <reference types="@cloudflare/vitest-plugin/types" />
import type { D1Migration } from "@cloudflare/vitest-plugin";

// The Cloudflare Vitest plugin types `env` (from "cloudflare:test") as Cloudflare.Env,
// so test-only bindings are added by augmenting that interface rather than ProvidedEnv.
declare global {
  namespace Cloudflare {
    interface Env {
      TEST_MIGRATIONS: D1Migration[];
      ADMIN_TOKEN: string;
    }
  }
}
