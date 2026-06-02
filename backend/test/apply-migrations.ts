import { applyD1Migrations, env } from "cloudflare:test";

// Apply all D1 migrations to the test database before the suite runs.
await applyD1Migrations(env.DB, env.TEST_MIGRATIONS);
