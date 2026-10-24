# Paarfinanz

App für Android und iPhone, mit der Paare ihre gemeinsamen Finanzen fair managen: gemeinsame Ausgaben im Überblick,
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

Eine Codebasis für beide Plattformen mit **Kotlin Multiplatform** und **Compose Multiplatform**:

```
core/domain/    Reine Kotlin-Logik: Geldbeträge, faire Aufteilung (Android + iOS, ohne UI)
shared/         Gemeinsame App: Supabase-Anbindung, Oberfläche, Login, Haushalt, Navigation
  commonMain/     Code für beide Plattformen (inkl. Texte in composeResources)
  androidMain/    Android-Besonderheiten (QR-Scanner, Teilen, Google-Login)
  iosMain/        iPhone-Besonderheiten (Teilen, Einstieg für Swift)
app/            Android-App (startet nur shared)
iosApp/         iPhone-App: kleine SwiftUI-Hülle, Xcode-Projekt aus project.yml
supabase/       Datenbank-Migrationen, Sicherheitsregeln (RLS) und deren Tests
```

**Stack:** Kotlin · Compose Multiplatform · Material 3 · Koin · Coroutines/Flow · kotlinx-datetime · Supabase (Postgres, Auth, Realtime)

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

### 2. Android-App

1. Android Studio (aktuelle stabile Version) und JDK 17.
2. Die App ist über `supabase.properties` bereits mit dem Projekt „Paarfinanz“ (Frankfurt) verbunden.
   Für ein eigenes Projekt `SUPABASE_URL` und `SUPABASE_ANON_KEY` in `local.properties` eintragen
   (Vorlage: `local.properties.example`); diese Werte haben Vorrang.
3. Optional für „Weiter mit Google“: OAuth-Client (Typ *Web*) in der Google Cloud Console anlegen,
   als `GOOGLE_WEB_CLIENT_ID` eintragen und den Google-Provider in Supabase aktivieren.
4. Starten: `./gradlew installDebug` oder über Android Studio.

### 3. iPhone-App (Mac nötig)

1. Xcode (aktuelle Version), JDK 17 und [XcodeGen](https://github.com/yonaskolb/XcodeGen) installieren:
   `brew install xcodegen openjdk@17`
2. Xcode-Projekt erzeugen: `cd iosApp && xcodegen generate`
3. `iosApp/iosApp.xcodeproj` in Xcode öffnen, unter *Signing & Capabilities* dein Apple-Team wählen
   (eine kostenlose Apple-ID reicht für das eigene iPhone; die App läuft dann 7 Tage).
4. iPhone per Kabel verbinden, als Ziel wählen und auf ▶ drücken. Beim ersten Mal auf dem iPhone unter
   *Einstellungen → Allgemein → VPN & Geräteverwaltung* dem Entwickler vertrauen.

Der erste Build dauert einige Minuten, weil Xcode dabei den Kotlin-Code (`shared`) kompiliert.
Für dauerhafte Installation ohne Kabel: Apple Developer Program (99 €/Jahr) und TestFlight.

**Einladung annehmen auf dem iPhone:** Den QR-Code einfach mit der Kamera-App scannen – Paarfinanz öffnet sich mit dem Code.


## Tests

```bash
./gradlew :core:domain:jvmTest :shared:testDebugUnitTest   # Kotlin-Tests (Android/JVM)
./gradlew :shared:iosSimulatorArm64Test                       # Kotlin-Tests im iOS-Simulator (Mac)
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/postgres supabase/tests/run_tests.sh  # Datenbank & RLS
```

Die Datenbanktests laufen gegen ein leeres Postgres ab Version 15 und simulieren die Supabase-Rollen.
Sie prüfen unter anderem, dass Fremde keine Haushaltsdaten sehen, private Ausgaben nur als Summe sichtbar
sind und ein Haushalt höchstens zwei Mitglieder hat. Beide Testarten laufen bei jedem Push in GitHub Actions,
dort entsteht auch eine Debug-APK als Download, und die iPhone-App wird für den Simulator gebaut.
