# Paare-Finanzplaner – Android Full-Stack App (Plan)

## Context
Das Repo `Paare-Finanzplaner-` ist leer (Branch `claude/clever-tesla-813ufq`, noch kein Commit). Ziel: eine Android-App, mit der ein Paar seine gemeinsamen Finanzen managt – gemeinsame Ausgaben im Überblick, fair aufgeteilt (einkommensproportional **oder** 50/50), dazu die üblichen Budget-Funktionen, in einem modernen, ruhigen, nicht überladenen Design. Zwei Geräte, eine gemeinsame Datenbasis → Backend mit Echtzeit-Sync nötig.

## Entscheidungen
- Backend: **Supabase** (EU-Region Frankfurt).
- Geldmodell: **Beides kombiniert** – Ausgaben + Saldo, plus optionaler Gemeinschaftskonto-Modus mit berechneter Monatsüberweisung je Person.
- Fairness: **wählbar** – Nettogehalt (Standard) oder „frei verfügbares Einkommen“ (Netto minus private Fixkosten).
- MVP-Extras: **Sparziele, CSV-Bankimport, Beleg-Scan (OCR), private Ausgaben**.
  → Hinweis: Das ist ein großes MVP. Ich baue OCR und CSV-Import deshalb in den späteren Phasen 5–6 ein, damit der Kern (Split + Saldo) zuerst solide steht.

## Produktkern (MVP)
1. **Haushalt & Partner-Einladung** – Account anlegen, Haushalt erstellen, Partner per Link/QR-Code einladen.
2. **Aufteilungsmodell** pro Haushalt, pro Kategorie überschreibbar, pro Ausgabe nochmal anpassbar:
   - 50/50
   - Einkommensproportional (Netto A / (Netto A + Netto B)) – automatisch neu berechnet, wenn sich ein Gehalt ändert
   - Fester Prozentsatz (z. B. 60/40)
   - „Gehört nur mir“ (privat, für Partner unsichtbar oder nur als Summe sichtbar)
3. **Ausgaben erfassen** – Betrag, Kategorie, wer bezahlt hat, Aufteilung, Datum, Notiz; Schnell-Eingabe in ≤ 3 Taps.
4. **Ausgleichskonto („Wer schuldet wem?“)** – laufender Saldo + „Ausgleichen“-Button, der eine Ausgleichsbuchung erzeugt.
5. **Fixkosten / wiederkehrende Buchungen** – Miete, Strom, Abos, Versicherungen; automatisch monatlich gebucht.
6. **Budgets pro Kategorie** – Monatslimit, Fortschrittsbalken, Warnung bei 80 % / 100 %.
7. **Dashboard** – Monat auf einen Blick: Gemeinsam ausgegeben, Saldo zwischen euch, Budgetstatus, verbleibendes „frei verfügbares“ Geld je Person.
8. **Sparziele** – gemeinsame Ziele (Urlaub, Notgroschen) mit Beiträgen proportional oder fix.
9. **Statistiken** – Kategorien-Donut, Monatsverlauf, Vergleich zum Vormonat.
10. **Gemeinschaftskonto-Modus** – Topf „Unseres“: App berechnet monatlich, wie viel jede Person fair einzahlt (Fixkosten + Budgets + Sparrate × Anteil); Ausgaben vom Gemeinschaftskonto beeinflussen den Saldo nicht.
11. **CSV-Import** – Kontoauszug-CSV (Sparkasse, ING, DKB, N26 …) mit Spalten-Mapping, Duplikaterkennung, Kategorie-Vorschläge per Regeln („REWE“ → Lebensmittel).
12. **Beleg-Scan** – CameraX + ML Kit Text Recognition (on-device, keine Bilder in der Cloud), erkennt Gesamtbetrag, Datum, Händler; Nutzer bestätigt.

## Technischer Stack (Empfehlung)
- **App:** Kotlin, Jetpack Compose, Material 3 (Dynamic Color), MVVM + Clean Architecture, Hilt, Room (Offline-first-Cache), Kotlin Coroutines/Flow, Navigation Compose, Vico (Charts).
- **Backend:** Supabase (Postgres + Auth + Realtime + Row Level Security + Edge Functions). Begründung: relationale Daten (Haushalte, Splits, Salden) passen besser zu SQL als zu Firestore; RLS sichert ab, dass nur Haushaltsmitglieder Daten sehen; EU-Region (Frankfurt) für DSGVO.
  - Alternative: eigenes Ktor-Backend + Postgres, falls volle Kontrolle gewünscht.
- **Sync:** Room als lokale Quelle der Wahrheit, Outbox-Queue für Offline-Änderungen, Supabase Realtime für Live-Updates vom Partner.
- **Geld:** Beträge immer als `Long` in Cent speichern, nie `Double`. Rundungsrest deterministisch einer Person zuordnen (z. B. dem Zahler).

## Datenmodell (Kern)
- `users` (id, name, avatar)
- `households` (id, name, currency, default_split_mode)
- `household_members` (household_id, user_id, net_income_cents, private_fixed_costs_cents, income_valid_from)
- `households.fairness_basis` (`NET` | `DISPOSABLE`), `households.joint_account_enabled`
- `expenses.source` (`MANUAL` | `CSV` | `OCR`), `expenses.paid_from` (`PERSON` | `JOINT_ACCOUNT`), `expenses.import_hash` (Duplikate)
- `category_rules` (household_id, pattern, category_id) für CSV-Auto-Kategorisierung
- `categories` (id, household_id, name, icon, color, split_override)
- `expenses` (id, household_id, paid_by, amount_cents, category_id, date, note, split_mode, share_a_bps, is_private, recurring_id)
- `recurring_rules` (id, household_id, template expense fields, rrule, next_run)
- `budgets` (household_id, category_id, month, limit_cents)
- `settlements` (id, household_id, from_user, to_user, amount_cents, date)
- `savings_goals` / `goal_contributions`
- Gehälter **historisiert** (`income_valid_from`), damit alte Monate mit damaligem Verhältnis korrekt bleiben.

## Projektstruktur
```
/android        – Compose-App (Module: app, core:data, core:domain, core:ui, feature:*)
/supabase       – migrations/, seed.sql, functions/ (Edge Functions: recurring-runner, invite)
/docs           – Architektur, Datenmodell, Design-Tokens
```

## Design-Leitlinien („clean, nicht überladen“)
- 4 Tabs max.: **Übersicht · Ausgaben · Budgets · Ziele** (+ Einstellungen über Avatar).
- Großer FAB „+ Ausgabe“, Bottom-Sheet statt neuer Screens.
- Jede Person hat eine eigene Akzentfarbe → sofort erkennbar, wer was bezahlt hat.
- Viel Weißraum, eine Kennzahl pro Karte, Dark Mode von Anfang an.

## Umsetzungsphasen
1. **Fundament:** Gradle-Projekt, Modulstruktur, Theme, Supabase-Projekt + Migrationen + RLS-Policies, Auth (E-Mail + Google).
2. **Haushalt & Einladung**, Einkommen hinterlegen.
3. **Ausgaben + Split-Engine + Saldo/Ausgleich** (Herzstück, mit Unit-Tests).
4. **Kategorien, Budgets, Fixkosten** (Edge Function / WorkManager für wiederkehrende Buchungen).
5. **Dashboard, Statistiken, Gemeinschaftskonto-Modus, private Ausgaben, Sparziele.**
6. **CSV-Import + Kategorie-Regeln, Beleg-Scan (OCR), Benachrichtigungen, Export, Feinschliff.**

## Verifikation
- Unit-Tests für Split-Engine (50/50, proportional, Rundung auf Cent, Gehaltsänderung mitten im Monat) und Saldo-Berechnung (`./gradlew test`).
- Supabase-RLS-Tests (pgTAP oder SQL-Tests): Nutzer aus Haushalt B sieht keine Daten von A.
- Instrumented/Compose-UI-Tests für Ausgabe-erfassen-Flow.
- Manuell: zwei Emulatoren mit zwei Accounts im selben Haushalt → Ausgabe auf Gerät A erscheint live auf B; Offline-Eingabe synchronisiert nach Reconnect.

## Vorschläge & Denkanstöße (zur Diskussion)
- **Fairness ist mehr als Gehalt:** Proportional nach *Netto* oder nach *frei verfügbarem Einkommen* (Netto minus private Fixkosten wie Kredit, Unterhalt)? Letzteres ist oft fairer.
- **Drei-Töpfe-Modell:** „Meins · Deins · Unseres“ – jeder überweist monatlich seinen fairen Anteil auf ein Gemeinschaftskonto; die App berechnet den Überweisungsbetrag. Viele Paare leben genau so – könnte das zentrale Feature werden statt reinem Nachtragen.
- **Taschengeld-Ansicht:** Nach Fixkosten + Sparrate – wie viel bleibt jedem *gleich viel* oder *proportional* übrig?
- **Unsichtbare Arbeit / Care-Arbeit:** Option, Teilzeit wegen Kinderbetreuung im Split auszugleichen (z. B. Rentenausgleich-Hinweis). Sehr wertvoll, aber sensibel.
- **Privatsphäre:** Private Ausgaben nur als Summe sichtbar – verhindert „Kontrolle“, ein häufiger Konfliktpunkt.
- **Bank-Anbindung (später):** PSD2 via GoCardless Bank Account Data / finAPI – großer Mehrwert, aber Aufwand + Kosten + Regulatorik. Für MVP: manuell + CSV-Import.
- **Beleg-Scan per Kamera (ML Kit OCR)** für Betrag & Händler – nette Erleichterung, Phase 2.
- **Ausgleich per PayPal-/Bank-Link** (EPC-QR-Code „GiroCode“) direkt aus der App.
- **Monatlicher „Money Date“-Report:** kurze Zusammenfassung als Push – fördert Gespräche statt Streit.
- **Trennung/Auszug:** Export & sauberer Abschluss des Haushalts (Endsaldo) – unangenehm, aber wichtig.
- **Mehrwährung** für Urlaube, **Widget** für schnelle Eingabe, **Wear-OS**-Schnelleingabe.
- **Monetarisierung (falls geplant):** Freemium – Sparziele/Statistiken/Bankanbindung als Premium.
