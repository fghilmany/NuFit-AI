# TechSpec: Meal Plan

> Source US: [fghilmany/NuFit-AI#7](https://github.com/fghilmany/NuFit-AI/issues/7)
> Depends on: `full-assessment` (issue #76), `Data Tubuh`/`BodyMeasurement` (`09-profile-settings.md`), entitlement stub (`Tier`/`Capability`, `ai-custom-plan.md`)
> Backend for meal-content generation tracked in tech debt issue #88 (same Edge Function family as AI Custom Plan / Chat AI)

## 1. Overview & Goal

A **subscriber** gets a week-by-week meal plan whose calorie/macro targets are computed **on-device** (deterministic, offline, testable) from their body data + activity level + goal, while the actual **meal content** (names, descriptions, per-meal macros) is AI-generated server-side, personalized further by an explicit **Preferensi Makan** profile (cuisine themes, mood, allergies, AI-random vs. manual scheduling) — same rule-based-numbers/AI-generated-content split already established by Monthly Plan's tiers, reused here rather than invented fresh.

Gaps discovered during analysis, all closed by this techspec's scope:

1. **No "aktivitas harian" field exists anywhere in the domain model**, despite `00-overview.md` keputusan #9 and this feature's own US claiming Full Assessment already asks it. This techspec adds `ActivityLevel` to `FullAssessmentResult`.
2. **`BodyMeasurement` (height/weight) is optional** everywhere else in the app — TDEE calculation requires both. This techspec adds a body-data completeness gate, same shape as AI Custom Plan's Full-Assessment gate.
3. **The real design (dev-supplied screenshots) is meal-photo-heavy and includes a whole "Preferensi Makan" settings screen** that a first pass at this techspec (written from the US text alone, before screenshots were available) had scoped out as deferred. This revision corrects that — see §3.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw` — node-id `12-1624` (P-17 A) and `12-2213` (P-17 B) were named in the source US, but the Figma MCP was rate-limited (Starter plan tool-call limit) on every attempt this pass. **The dev supplied two screenshots directly instead** — those are the source of truth for `/code`, per the same precedent as Chat AI's screenshot-only pass.

| Screen | Screenshot content | Notes |
|---|---|---|
| P-17 A — Meal Plan home | Top bar: avatar + "NuFit" wordmark + bell icon (mirrors `HomeScreen`'s top bar). Week navigator: `< 15 – 21 Mei >` / "Minggu Ini" caption. Content grouped by day (`Senin`, `Selasa`, ... with a divider rule), each day listing meal cards for that day. Each **meal card**: large food photo, overline label (`SARAPAN`/`MAKAN SIANG`/`MAKAN MALAM`), bold meal name, a duration pill (`15 mnt` — prep/cook time, NOT calories) + one descriptive tag pill (`Nusantara` / `Tinggi Serat` / `Tinggi Protein`). One card shows a small floating nutrition-summary widget overlaid on its photo (`Kalori 470 kkal · Protein 15g · Karbo 55g`) — treated as an example of a possible tap-to-preview affordance, not a persistent per-card element (only one card shows it in the screenshot). Full-width `✨ Generate Ulang Minggu Ini (AI)` button pinned above the bottom nav. Bottom nav: Home · Plans · Chat AI · **Meal Plan** (active) · Akun. |
| P-17 B — Preferensi Makan | Back arrow + "Preferensi Makan" title + avatar. **Tema Makanan Favorit**: multi-select pill chips (Nusantara, Western, Sundanese, Javanese, Chinese, Japanese). **Suasana Hati (Mood)**: 2×2 single-select icon cards (Cepat / Comfort / Sehat / Pedas). **Alergi & Pantangan**: text input + "Tambah" button, entries render as removable red pill tags (`Kacang ×`, `Seafood ×`). **Preferensi Jadwal**: two selectable cards — "AI Random" (sparkle icon, "Biarkan AI mengatur kejutan menu harian berdasarkan profil nutrisimu") vs. "Manual per Hari" (calendar icon, "Saya ingin memilih tema makanan sendiri setiap hari"). "Simpan Preferensi" CTA. |

**Per dev decision, made explicit here since the screenshots don't show them**: meal cards render **without photos** in this pass (no food-image sourcing pipeline exists — no stock library, no external image API approved; adding one is a separate, larger decision) — build the card with the same text content minus the `BundledMediaImage`/photo slot. There is also **no visible "mark eaten/skip" affordance** anywhere in these two screens; per dev decision, that action lives on a **meal detail screen** (tapped from a card), which has no screenshot — build it plain, mirroring `ExerciseDetailScreen`'s layout conventions (title, description, then action buttons).

No screenshot for: empty state (no plan generated yet for the viewed week), loading state during generate, error state, quota/entitlement-lapsed inline messaging, the weight-change regenerate-suggestion banner (§8), notification settings UI, or "Manual per Hari" mode's actual day-by-day picker (its trigger card exists in the screenshot, its resulting flow doesn't).

## 3. Scope

**In scope**
- Entitlement + data-completeness gate: guest → login explainer; **[ADR-009, 2026-11-09] logged-in non-subscriber now passes through too (free MVP showcase; `NonSubscriberPaywall` state kept but unreachable)**; LOGGED_IN/SUBSCRIBED user with incomplete body data (`BodyMeasurement.heightCm`/`weightKg` or the new `FullAssessmentResult.activityLevel` missing) → CTA to complete it; complete data → pass through. Generate is capped at **5 new plans/month** per ADR-009's quota.
- **On-device TDEE + macro calculation** (`CalculateTdeeAndMacros`, pure Kotlin, unit-testable, BLOCKING) — Mifflin-St Jeor BMR + activity multiplier + goal-based calorie adjustment. Exact coefficients proposed in §5; flagged for dev confirmation before `/code`.
- **Preferensi Makan** (P-17 B, now in scope per dev decision): cuisine-theme multi-select, mood single-select, free-text allergy/restriction tags, AI-Random-vs-Manual-per-Hari schedule mode. Persisted locally, editable any time, read by `GenerateMealPlan` as additional AI context alongside the TDEE/macro target. "Manual per Hari"'s actual day-by-day theme picker has no design — build its entry point (the selectable card) and persist the mode, but the picker UI itself is a stub/TODO for a follow-up pass (no screenshot to build against).
- **AI-generated weekly meal content** (`GenerateMealPlan`) — sends the computed TDEE/macro target + goal + `MealPreference` to a remote endpoint, gets back 7 days × meal slots (breakfast/lunch/dinner — screenshot shows no snack slot; dropped from the original draft's `MealSlot.SNACK` unless the dev wants it back) of named meals with macros, a prep-time estimate, and one descriptive tag. No live backend (mirrors `AiCustomPlanRemoteDataSource`'s placeholder-`BASE_URL` pattern; actual generation tracked in issue #88).
- **Week-by-week browsing**: the home screen's `< 15 – 21 Mei >` navigator moves between weeks that have actually been generated (not an infinite calendar) — corrects the original draft's "only current + one archived" limitation, since browsing past weeks is clearly core UI here.
- **Meal detail + food logging as a checklist**: tapping a card opens a detail screen; marking a meal eaten/skipped happens there (mirrors PT Mode's `WorkoutExerciseLog`/skip pattern), not a free-form food-entry/database feature.
- **Regenerate**, manual-triggered only via the persistent "Generate Ulang Minggu Ini (AI)" button (applies to the currently-viewed week) — if that week already has any `MealLog` entries, confirm before overwriting (mirrors `assessment-detail.md`'s retake-confirmation pattern), since regenerating replaces that week's meals and would otherwise silently orphan existing logs.
- A weight-change regenerate **suggestion banner** (no screenshot; dev-approved design decision) — never auto-regenerates.
- **Meal reminder notifications** (Subscribe tier): fixed default times per meal slot, on/off toggle only. First feature in this codebase to implement tech-stack.md's already-locked local-notification choice (`expect`/`actual`) — no existing pattern to mirror.
- Subscription-lapse behavior: existing meal plans + logs stay readable across all previously-generated weeks; only "Generate"/"Regenerate" actions re-check entitlement and gate.
- New `FullAssessmentResult.activityLevel: ActivityLevel?` field (extends the existing entity/table, no migration per this project's early-dev convention), asked as one additional step in the existing Full Assessment wizard.

**Out of scope (explicit, deferred)**
- Meal photos — no image-sourcing pipeline exists; cards render text-only this pass (dev decision, §2).
- Free-form food logging / a food database / search — checklist-against-plan only.
- "Manual per Hari" mode's actual day-by-day theme-picker flow — entry point + persisted mode only, picker UI is a follow-up (no design).
- Custom notification times, snooze, per-day override — fixed default schedule only.
- Actual meal-generation backend (LLM call, prompt) — tracked in issue #88.
- Auto-regenerate on weight change — manual trigger only (banner suggestion).

## 4. User Story & Acceptance Criteria

> Sebagai subscriber, saya ingin kebutuhan kalori dan makro saya dihitung dari profil serta goal saya, lengkap dengan rencana makan harian, sehingga latihan dan pola makan saya saling mendukung.

### AC Traceability

| # | AC (Given/When/Then) | Handled in | Blocking? |
|---|---|---|---|
| 1 | Given guest, when membuka Meal Plan, then diarahkan ke penjelasan + login — bukan error | `MealPlanGateViewModel`/`MealPlanGateScreen` (mirrors `AiCustomPlanGateScreen`) | BLOCKING |
| 2 | ~~Given logged-in non-subscriber, when membuka Meal Plan, then melihat paywall~~ **[ADR-009] Sementara tidak berlaku — logged-in non-subscriber sekarang pass through (gratis selama showcase)** | `MealPlanGateViewModel`/`MealPlanGateScreen` | BLOCKING |
| 3 | Given subscriber dengan data tubuh/aktivitas belum lengkap, when membuka Meal Plan, then diarahkan melengkapi data — bukan generate dengan data kosong/salah | `MealPlanGateViewModel` (`NeedsBodyData` state) + `CalculateTdeeAndMacros`'s precondition | BLOCKING |
| 4 | Given data lengkap, when generate, then TDEE/makro dihitung deterministic di device (tidak butuh online) | `CalculateTdeeAndMacros` (pure usecase, no repository dependency) | BLOCKING |
| 5 | Given Preferensi Makan tersimpan, when generate, then preferensi (tema/mood/alergi) ikut jadi konteks AI, bukan diabaikan | `GenerateMealPlan` reads `MealPreference` alongside `MacroTarget` | Handle |
| 6 | Given menu minggu ini, when user navigasi minggu sebelumnya/berikutnya, then hanya minggu yang sudah pernah di-generate yang bisa dilihat (bukan kalender tak terbatas) | `MealPlanViewModel`/`GetMealPlanForWeek` | Handle |
| 7 | Given detail satu menu, when user tandai eaten/skip, then tersimpan lokal segera dan tidak hilang meski offline | `LogMealStatus` usecase, local-first commit | BLOCKING |
| 8 | Given minggu yang sedang dilihat sudah punya log, when regenerate ditekan, then user diminta konfirmasi dulu (log lama akan hilang) — tidak langsung menimpa diam-diam | `MealPlanViewModel`'s regenerate confirmation, mirrors `assessment-detail.md`'s retake pattern | BLOCKING |
| 9 | Given berat badan berubah, when dibuka, then muncul saran regenerate — TIDAK auto-generate ulang tanpa konfirmasi | `MealPlanViewModel` reads latest `BodyMeasurement` vs. the plan's snapshot weight, shows a banner only | Handle |
| 10 | Given subscription lapse, when membuka Meal Plan, then plan & log lama (semua minggu) tetap terbaca; generate/regenerate terkunci dengan penjelasan | `MealPlanViewModel` (read path ignores tier; write actions re-check `GetCurrentTier`) | BLOCKING |
| 11 | Given notifikasi makan diaktifkan, when waktunya tiba, then reminder muncul sesuai slot | `MealReminderScheduler` expect/actual | Handle |

## 5. Domain Model

```kotlin
// domain/fullassessment/entity/ActivityLevel.kt (NEW field on the existing FullAssessmentResult)
enum class ActivityLevel(val tdeeMultiplier: Double) {
    SEDENTARY(1.2),       // desk job, little/no exercise
    LIGHT(1.375),         // light exercise 1-3x/week
    MODERATE(1.55),       // moderate exercise 3-5x/week
    ACTIVE(1.725),        // hard exercise 6-7x/week
    VERY_ACTIVE(1.9),     // physical job + hard exercise
}
// FullAssessmentResult gains: val activityLevel: ActivityLevel? = null

// domain/mealplan/entity/MealPlan.kt
enum class MealSlot { BREAKFAST, LUNCH, DINNER }   // no SNACK -- screenshot shows 3 slots/day only
enum class MealLogStatus { PENDING, EATEN, SKIPPED }
enum class MealMood { CEPAT, COMFORT, SEHAT, PEDAS }         // P-17 B "Suasana Hati"
enum class MealScheduleMode { AI_RANDOM, MANUAL_PER_DAY }    // P-17 B "Preferensi Jadwal"

data class MacroTarget(val calories: Int, val proteinG: Int, val carbG: Int, val fatG: Int)

/**
 * One row per week -- keyed by [weekStartDate], NOT soft-deleted/archived on regenerate.
 * Regenerating the currently-viewed week overwrites that week's row in place (after the
 * confirmation in AC-8); other weeks stay untouched and independently browsable (AC-6).
 */
data class MealPlan(
    val id: String,
    val weekStartDate: LocalDate,
    val target: MacroTarget,
    /** Body weight snapshot at generation time -- compared against the latest `BodyMeasurement` for the regenerate-suggestion banner (AC-9). */
    val weightKgAtGeneration: Double,
    val goal: GoalCategory,
    val createdAt: Instant,
)

data class PlannedMeal(
    val id: String,
    val mealPlanId: String,
    val dayNumber: Int,       // 1..7
    val slot: MealSlot,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val macros: MacroTarget,
    /** Screenshot's "15 mnt" pill. */
    val prepMinutes: Int,
    /** Screenshot's single descriptive pill ("Nusantara" / "Tinggi Serat" / "Tinggi Protein") -- AI-provided free text, not a fixed enum (mixes cuisine and nutrition descriptors). */
    val tag: String,
)

data class MealLog(
    val id: String,
    val plannedMealId: String,
    val status: MealLogStatus,
    val loggedAt: Instant?,
)

/** P-17 B, singleton per user -- read by GenerateMealPlan as extra AI context. */
data class MealPreference(
    val cuisineThemes: Set<String>,     // free text, mirrors the chip labels shown (Nusantara, Western, Sundanese, ...) -- not a closed enum, screenshot's set isn't asserted as exhaustive
    val mood: MealMood?,
    val allergies: List<String>,
    val scheduleMode: MealScheduleMode,
)
```

**Business rules**
- `CalculateTdeeAndMacros` is pure: `(BodyMeasurement, FullAssessmentResult) -> AppResult<MacroTarget>` — `AppResult.Error(Failure.Validation(...))` when height/weight/age/gender/activityLevel is missing (this is the enforcement point for AC-3, not just UI gating).
- Mifflin-St Jeor BMR: male `10×kg + 6.25×cm − 5×age + 5`; female `10×kg + 6.25×cm − 5×age − 161`. TDEE = BMR × `ActivityLevel.tdeeMultiplier`.
- Goal calorie adjustment (proposed default, **flag for dev confirmation before `/code`**): `FAT_LOSS` → TDEE × 0.85; `MUSCLE_GAIN` → TDEE × 1.10; `GENERAL_HEALTH`/`STRENGTH`/`ENDURANCE` → TDEE × 1.0.
- Macro split (proposed default, **flag for dev confirmation**): protein = 1.8g × body weight kg; fat = 25% of total calories ÷ 9; carb = remainder ÷ 4.
- `MealPlan` rows are unique per `weekStartDate`; regenerating the viewed week is an upsert on that same row (with the AC-8 confirmation when logs exist), not a new-row-plus-archive.
- `MealLog` defaults to `PENDING` for every `PlannedMeal` at generation time; user action on the detail screen flips it to `EATEN`/`SKIPPED`. Never silently reset — regenerating a week with existing logs requires the AC-8 confirmation precisely because it would otherwise discard them.
- `MealPreference` is a singleton (one row, upserted) — not versioned/historized.

## 6. Architecture Breakdown

```
domain/fullassessment/
  entity/ ActivityLevel.kt (new), FullAssessmentResult.kt (extended: + activityLevel)

domain/mealplan/
  entity/ MealPlan.kt, PlannedMeal.kt, MealLog.kt, MacroTarget.kt, MealSlot.kt, MealLogStatus.kt,
          MealPreference.kt, MealMood.kt, MealScheduleMode.kt
  repository/ MealPlanRepository.kt, MealPreferenceRepository.kt

usecase/mealplan/
  CalculateTdeeAndMacros.kt     -- pure, no repository dependency (BLOCKING, AC-4)
  GenerateMealPlan.kt           -- repository.generateRemote(target, goal, preference) -> upserts the viewed week's MealPlan
  GetMealPlanForWeek.kt         -- weekStartDate -> MealPlan? (null = never generated, AC-6)
  LogMealStatus.kt              -- flips a PlannedMeal's MealLog status, local-first
  ShouldSuggestRegenerate.kt    -- compares latest BodyMeasurement.weightKg vs MealPlan.weightKgAtGeneration (threshold TBD by dev, propose >=3kg)
  GetMealPreference.kt / SaveMealPreference.kt

data/mealplan/
  model/ MealPlanRequestDto.kt, MealPlanResponseDto.kt
  datasource/ MealPlanLocalDataSource.kt (SQLDelight), MealPlanRemoteDataSource.kt (Ktor)
  repository/ MealPlanRepositoryImpl.kt, MealPreferenceRepositoryImpl.kt
  sync/ MealPlanSyncParticipant.kt -- mirrors MonthlyPlanSyncParticipant; syncs the `meal_plan` header row only this pass, same scope-note precedent as WorkoutHistorySyncParticipant (nested `planned_meal`/`meal_log` sync deferred as a documented follow-up)

core/notification/ (NEW subsystem -- first local-notification feature in this codebase)
  MealReminderScheduler.kt (expect) -- schedule(slot, timeOfDay) / cancel(slot); androidMain actual via AlarmManager, iosMain actual via UNUserNotificationCenter. Kept to the smallest possible expect surface per architecture.md §6.

presentation/mealplan/
  viewmodel/ MealPlanGateViewModel.kt (mirrors AiCustomPlanGateViewModel/ChatGateViewModel), MealPlanViewModel.kt,
             MealDetailViewModel.kt, MealPreferenceViewModel.kt
  screen/ MealPlanGateScreen.kt, MealPlanScreen.kt, MealDetailScreen.kt, MealPreferenceScreen.kt
  component/ WeekNavigator.kt, MealCard.kt (no image), RegenerateSuggestionBanner.kt, CuisineChipRow.kt, MoodGrid.kt, AllergyTagInput.kt, ScheduleModeCard.kt
```

No changes to PT Mode, Monthly Plan, or Exercise Library — Meal Plan is fully separate from the workout domain except for reading `BodyMeasurement` and `FullAssessmentResult`.

## 7. API / Data Contract

### Remote endpoint

**Called once per day the user taps Generate/Regenerate for** (`dayNumber` 1-7 — see §3 "Generate/
Regenerate flow" for why it's per-day, not per-week). `days` in the response always contains
exactly 1 element. Quota: `assertQuotaAvailable`/`recordUsage` run unconditionally on every call —
each tap is its own `MEAL_PLAN` quota unit (**1/day**, 2026-11-10 — see §3).

```
POST {BASE_URL}/v1/meal-plan/generate
Headers: Authorization: Bearer {access_token}
Body: {
  "target": { "calories": 2200, "proteinG": 140, "carbG": 220, "fatG": 61 },
  "goal": "FAT_LOSS",
  "preference": {
    "cuisineThemes": ["Nusantara"],
    "mood": "COMFORT",
    "allergies": ["Kacang", "Seafood"],
    "scheduleMode": "AI_RANDOM"
  },
  "dayNumber": 1
}

200 OK
{
  "days": [
    { "dayNumber": 1, "meals": [
      { "slot": "BREAKFAST", "name": "Bubur Ayam Organik", "description": "...", "ingredients": ["..."],
        "macros": { "calories": 450, "proteinG": 30, "carbG": 45, "fatG": 12 }, "prepMinutes": 15, "tag": "Nusantara" }
    ] }
  ]
}

401 -> Failure.Unauthorized()
403 with body {"reason": "not_subscribed"} -> Failure.Validation (paywall; unreachable since the backend now gates MEAL_PLAN generate at LOGGED_IN minimum, ADR-009 — kept for when billing ships)
403 with body {"reason": "quota_exceeded"} -> Failure.Validation (5 new plans/month, ADR-009)
422 -> Failure.Validation(...)
```

`MealPlanRemoteDataSource` mirrors `AiCustomPlanRemoteDataSource`/`ChatRemoteDataSource`'s exact shape (plain Ktor, `BASE_URL` placeholder constant with a `TODO(dev)` comment).

### SQLDelight

**New file** `MealPlan.sq`:
```sql
CREATE TABLE meal_plan (
    id TEXT NOT NULL PRIMARY KEY,
    week_start_date TEXT NOT NULL UNIQUE,   -- ISO date; UNIQUE enforces the one-row-per-week rule
    calories INTEGER NOT NULL,
    protein_g INTEGER NOT NULL,
    carb_g INTEGER NOT NULL,
    fat_g INTEGER NOT NULL,
    weight_kg_at_generation REAL NOT NULL,
    goal TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    user_id TEXT,
    updated_at INTEGER NOT NULL DEFAULT 0
);
```
→ `mealPlanQueries`.

**New file** `PlannedMeal.sq`:
```sql
CREATE TABLE planned_meal (
    id TEXT NOT NULL PRIMARY KEY,
    meal_plan_id TEXT NOT NULL,
    day_number INTEGER NOT NULL,
    slot TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    ingredients TEXT NOT NULL,   -- JSON array
    calories INTEGER NOT NULL,
    protein_g INTEGER NOT NULL,
    carb_g INTEGER NOT NULL,
    fat_g INTEGER NOT NULL,
    prep_minutes INTEGER NOT NULL,
    tag TEXT NOT NULL,
    FOREIGN KEY (meal_plan_id) REFERENCES meal_plan(id)
);
```
→ `plannedMealQueries`. Not synced this pass (see §6's scope note). Regenerating a week deletes and re-inserts this week's rows (cascaded by `meal_plan_id`, application-level since SQLDelight doesn't do `ON DELETE CASCADE` by default here — confirm against an existing cascade precedent, e.g. how `MonthlyPlan`'s `PlanDay` deletion is handled, before assuming a convention).

**New file** `MealLog.sq`:
```sql
CREATE TABLE meal_log (
    id TEXT NOT NULL PRIMARY KEY,
    planned_meal_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    logged_at INTEGER,
    FOREIGN KEY (planned_meal_id) REFERENCES planned_meal(id)
);
```
→ `mealLogQueries`. Not synced this pass.

**New file** `MealPreference.sq` (singleton row, upserted):
```sql
CREATE TABLE meal_preference (
    id TEXT NOT NULL PRIMARY KEY,   -- fixed constant id, mirrors other singleton-settings tables' convention if one exists; confirm during /code
    cuisine_themes TEXT NOT NULL,   -- JSON array
    mood TEXT,
    allergies TEXT NOT NULL,        -- JSON array
    schedule_mode TEXT NOT NULL DEFAULT 'AI_RANDOM'
);
```
→ `mealPreferenceQueries`. Not synced this pass (device-local preference, low value to sync given it's quick to re-enter — revisit if the dev disagrees).

**Schema change to existing table**: `full_assessment_result` adds `activity_level TEXT` (nullable, no migration per this project's early-dev convention).

`packageName` matches the existing `NuFitDatabase` config — confirm in `build.gradle.kts` before first build, per architecture.md §4b.

### DI
- New `mealPlanModule` (mirrors `chatAiModule`'s shape): local/remote datasources, repositories, `usecase/mealplan/*`, all 4 ViewModels.
- `syncModule`: register `MealPlanSyncParticipant` in the `List<SyncParticipant>` assembly.
- `fullAssessmentModule`: no new registrations needed — `activityLevel` rides along inside the existing `FullAssessmentResult` save/load path.
- New `notificationModule` (or fold into `mealPlanModule`) for `MealReminderScheduler`'s platform actuals.

### Navigation
- `Route.MealPlanGate` / `Route.MealPlan` / `Route.MealDetail(plannedMealId)` / `Route.MealPreference` — wired from the bottom nav's `NavTab.MAKAN` (currently inert) in `HomeScreen`/`MyPlansScreen`/`AccountScreen`/`ChatScreen`'s `BottomNavBar` callers, same tab-switch `popUpTo` pattern already used for Chat AI. `MealPreference` is reached from a settings/gear icon on `MealPlanScreen`'s top bar (no screenshot for that icon's exact placement — mirrors `AccountScreen`'s top-bar icon convention).

## 8. State & Error Handling

- **Gate**: `Loading` → `Guest` / `NonSubscriberPaywall` / `NeedsBodyData` (missing height, weight, or `activityLevel`) / `PassThrough` — mirrors `AiCustomPlanGateState`/`ChatGateState` exactly.
- **Generate/Regenerate flow (current architecture, 2026-11-10)**: generation is **per-day, one tap at a time** — not a single week-wide action. History: a single 7-day LLM call originally measured ~3min real-world; splitting into backend-internal day-batches (2026-11-09) still packed the whole batch sequence into *one* Edge Function invocation's ~150s wall-clock cap; moving the day-splitting to a **client-orchestrated 7-call loop** (also 2026-11-09) fixed the per-call budget but real-world testing then hit Android's Doze/background-network restrictions mid-loop (a run can take 10+ minutes end to end) — confirmed via `UnknownHostException` right after the app backgrounded. Current design instead shrinks the unit of work to match the UI: each of the week's 7 day-slots renders its own independent Generate/Regenerate button; tapping one fires exactly **one** `MealPlanRepositoryImpl.generateRemote` call for that `dayNumber`, guarded by a client-side timeout matching Supabase's own **150s** platform wall-clock cap. Loading is a plain spinner + "Sedang membuat menu..." text on that day's slot only (`MealPlanState.generatingDayNumber`); other days' buttons disable meanwhile but stay otherwise independent. `MealPlanLocalDataSource.upsertDayMeals` deletes+re-inserts only the tapped day's `planned_meal`/`meal_log` rows (not the whole week) — a failed/timed-out call only costs that one day, never the rest of the week. **Quota**: every generate tap is its own `MEAL_PLAN` unit — **1 per calendar day** (2026-11-10, replaces the 5/month figure, which exhausted itself filling less than one week once generation became per-day), see §7.
- **Regenerate confirmation** (AC-8): if the viewed week has any non-`PENDING` `MealLog`, show a confirmation dialog ("log minggu ini akan hilang") before calling generate; skip the dialog if every meal is still `PENDING` (nothing to lose).
- **Log flow**: tapping eaten/skip on the detail screen commits `MealLog` status locally immediately (optimistic, local-first).
- **Week navigation**: prev/next arrows disable (or no-op) past the earliest/latest generated week — never show a blank "generate?" prompt while browsing history, only on the current/newest week.
- **Regenerate-suggestion banner**: shown when `ShouldSuggestRegenerate` returns true; dismissible; never auto-triggers generation.
- **Lapsed subscription**: reading any previously-generated week's plan + logs works regardless of current tier; "Generate"/"Regenerate" re-check the gate inline and show the paywall instead.
- **Offline**: viewing any generated week and logging meals both work fully offline (local-first). Generate/regenerate requires connectivity.
- **Notification permission denied**: reminder toggle stays visible but shows a small inline note that reminders won't fire until permission is granted.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | Backend for meal-content generation doesn't exist | Handle (this techspec, client-only) | Documented contract only; tracked in issue #88 |
| 2 | No `ActivityLevel` field exists anywhere | Handle | New field on `FullAssessmentResult`, asked as an extra Full Assessment wizard step |
| 3 | `BodyMeasurement` is optional elsewhere in the app | Handle | `NeedsBodyData` gate state |
| 4 | TDEE/macro calculation location | Handle | On-device, deterministic, pure usecase |
| 5 | Preferensi Makan scope (cut vs. kept) | Handle, kept in scope | Corrected from this techspec's first draft once real screenshots showed it's core UI (§1) |
| 6 | Meal photo sourcing | Defer | No pipeline exists; cards render text-only this pass |
| 7 | Food logging scope (checklist vs. free entry) | Handle (checklist only, on a detail screen) | `LogMealStatus` against `PlannedMeal` rows only |
| 8 | Week browsing scope | Handle | Only weeks that were actually generated are navigable — not an infinite calendar |
| 9 | Regenerating a week with existing logs | Handle | Confirmation dialog before overwrite (AC-8) |
| 10 | Weight change mid-plan | Handle | Suggestion banner only, never auto-regenerates |
| 11 | Subscription lapse mid-plan | Handle | Read path always works across all weeks; only generate/regenerate re-check entitlement |
| 12 | Notification scheduling | Handle (minimal) | Fixed default times per slot, on/off toggle only |
| 13 | Notification permission denied | Handle | Inline note, toggle stays visible, no crash/silent failure |
| 14 | "Manual per Hari" scheduling mode's actual picker | Defer | Entry point + persisted mode only; picker flow has no design |
| 15 | Offline generate attempt | Handle | Same retryable-error pattern as AI Custom Plan |
| 16 | App killed mid-generate | Handle | Same as AI Custom Plan: network-drop retry within the same session is handled; process death during generate is not |
| 17 | Dietary preferences/restrictions beyond the P-17 B form | Defer | The form itself (cuisine/mood/allergy/schedule) is in scope; anything beyond what it captures (e.g. strict halal/kosher certification) is not |

## 10. Definition of Done

- [ ] DoD global (lihat `00-overview.md`)
- [ ] Guest/non-subscriber/incomplete-body-data all see an appropriate gate screen, never an error or a broken generate attempt
- [ ] `CalculateTdeeAndMacros` unit-tested against known BMR/TDEE reference values (happy path + missing-field error path) — pure, no mocked repository needed
- [ ] Preferensi Makan persists and is actually sent as part of `GenerateMealPlan`'s request (not silently ignored)
- [ ] Food logging commits locally and survives offline/app-restart
- [ ] Regenerating a week with existing logs always confirms first (AC-8) — verified it cannot be bypassed
- [ ] Week navigation never fabricates a week that was never generated
- [ ] Lapsed-subscription read path verified across multiple previously-generated weeks, not just the most recent one
- [ ] `MealPlanSyncParticipant` implemented for real from day one (not stub-then-fix-later, unlike `FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant`'s history in `chat-ai.md`)
- [ ] Notification permission-denied state verified on both Android 13+ and iOS
- [ ] Macro-split/goal-adjustment coefficients in §5 confirmed with the dev before `/code`
- [ ] `NavTab.MAKAN` made navigable across every `BottomNavBar` caller (`HomeScreen`, `MyPlansScreen`, `AccountScreen`, `ChatScreen`)
