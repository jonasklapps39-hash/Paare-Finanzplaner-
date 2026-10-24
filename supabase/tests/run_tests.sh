#!/usr/bin/env bash
# Spielt Supabase-Stub, Migrationen und Tests in eine leere Postgres-Datenbank (ab Version 15) ein.
# Nutzung: DATABASE_URL=postgres://user:pass@localhost:5432/db supabase/tests/run_tests.sh
set -euo pipefail

cd "$(dirname "$0")/.."
: "${DATABASE_URL:?DATABASE_URL muss gesetzt sein}"

run() { psql "$DATABASE_URL" -v ON_ERROR_STOP=1 --quiet -f "$1"; }

run tests/00_supabase_stub.sql
for migration in migrations/*.sql; do
    echo "Migration: $migration"
    run "$migration"
done
for test in tests/[1-9]*.sql; do
    echo "Test: $test"
    run "$test"
done
