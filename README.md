# Paarfinanz

Android-App, mit der Paare ihre gemeinsamen Finanzen fair managen: gemeinsame Ausgaben im Überblick,
aufgeteilt 50/50, nach Einkommen oder mit festem Prozentsatz, dazu Budgets, Fixkosten und Sparziele.

Der vollständige Produktplan steht in [`docs/PLAN.md`](docs/PLAN.md).

## Stand

| Phase | Inhalt | Status |
|------:|--------|--------|
| 1 | Fundament: Projektstruktur, Theme, Supabase-Schema + RLS, Login | ✅ |
| 2 | Haushalt anlegen, Partner einladen (Code, Link, QR), Einkommen hinterlegen | ✅ |
| 3 | Ausgaben, Aufteilung, Saldo & Ausgleich | ⏳ |
| 4 | Kategorien, Budgets, Fixkosten | ⏳ |
| 5 | Dashboard, Statistiken, Gemeinschaftskonto, private Ausgaben, Sparziele | ⏳ |
| 6 | CSV-Import, Beleg-Scan, Benachrichtigungen, Export | ⏳ |

## Aufbau

```
app/            App-Einstieg, Navigation, Tabs, Einstellungen
core/domain/    Reine Kotlin-Logik: Geldbeträge, faire Aufteilung (ohne Android-Abhängigkeiten)
core/data/      Supabase-Anbindung (Auth, Postgrest, Realtime), Repositories
core/ui/        Theme (Material 3, Partnerfarben, Dark Mode) und gemeinsame Komponenten
feature/auth/   Anmeldung & Registrierung (E-Mail, optional Google)
feature/household/  Onboarding, Einladen/Beitreten, Einkommen, Haushalts-Einstellungen
supabase/       Datenbank-Migrationen, Sicherheitsregeln (RLS) und deren Tests
```

**Stack:** Kotlin · Jetpack Compose · Material 3 · Hilt · Coroutines/Flow · Supabase (Postgres, Auth, Realtime)

**Grundregeln:** Beträge immer als Cent (`Long`), Anteile in Basispunkten (10 000 = 100 %).
Rundungsreste werden deterministisch verteilt, sodass beide Teile immer exakt den Gesamtbetrag ergeben.

## Einrichten

### 1. Supabase-Projekt

1. Auf [supabase.com](https://supabase.com) ein Projekt anlegen (Region **Frankfurt / eu-central-1**).
2. Migrationen einspielen – entweder mit der [Supabase CLI](https://supabase.com/docs/guides/cli):
   ```bash
   supabase link --project-ref <projekt-ref>
   supabase db push
   ```
   oder die Dateien aus `supabase/migrations/` der Reihe nach im SQL-Editor ausführen.
3. Unter **Authentication → URL Configuration** `paarfinanz://login` als Redirect-URL eintragen,
   damit Bestätigungs-Mails zurück in die App führen.

### 2. App

1. Android Studio (aktuelle stabile Version) und JDK 17.
2. `local.properties.example` nach `local.properties` kopieren und `SUPABASE_URL` sowie
   `SUPABASE_ANON_KEY` eintragen (Dashboard → Project Settings → API).
3. Optional für „Weiter mit Google“: OAuth-Client (Typ *Web*) in der Google Cloud Console anlegen,
   als `GOOGLE_WEB_CLIENT_ID` eintragen und den Google-Provider in Supabase aktivieren.
4. Starten: `./gradlew installDebug` oder über Android Studio.

Ohne Zugangsdaten startet die App trotzdem und zeigt auf dem Login-Screen einen Hinweis.

## Tests

```bash
./gradlew :core:domain:test testDebugUnitTest   # Kotlin-Unit-Tests
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/postgres supabase/tests/run_tests.sh  # Datenbank & RLS
```

Die Datenbanktests laufen gegen ein leeres Postgres ab Version 15 und simulieren die Supabase-Rollen.
Sie prüfen unter anderem, dass Fremde keine Haushaltsdaten sehen, private Ausgaben nur als Summe sichtbar
sind und ein Haushalt höchstens zwei Mitglieder hat. Beide Testarten laufen bei jedem Push in GitHub Actions,
dort entsteht auch eine Debug-APK als Download.
