# TechSpec: AI Custom Plan

> Source US: [fghilmany/NuFit-AI#12](https://github.com/fghilmany/NuFit-AI/issues/12)
> Depends on: `full-assessment` tables/flow already shipped (issue #76, `monthly-plan.md`), Exercise Library (`exercise-library.md`)
> Blocks / feeds into (future, not this pass): `07-plan-management.md` (full My Plans UI), `08-auth-sync.md` (real login), billing (RevenueCat)

## 1. Overview & Goal

A **subscriber** fills a short form describing the plan they want (goal, frequency, session length, equipment, muscle focus, plan length), the app sends that plus their `FullAssessmentResult` and a workout-history summary to an AI backend, and gets back a plan they can preview, edit, and activate — reusing the exact `MonthlyPlan`/`PlanDay`/`PlannedExercise` shapes the rule-based tiers already use, so PT Mode and Home need zero changes to run an AI plan.

Two things this feature needs that **do not exist anywhere in the codebase yet**, scoped down to the minimum viable slice per dev decision:

1. **Entitlement gating** (guest / logged-in / subscriber) — a small local `Tier`/`Capability` stub (architecture.md §7), not a real auth/billing integration. Real login (`08-auth-sync.md`) and billing (RevenueCat) are separate, later features; this techspec only builds the *consumer* of that state.
2. **A remote AI call** — the first network feature in the app. Adds Ktorfit (already the locked tech-stack choice, just unused until now) to `libs.versions.toml`.

Full Assessment (P-11) already exists (issue #76) as a functional-not-polished stub covering the tahap-7b rule engine's needs. This techspec **extends** it with the two things it's still missing for AI Custom Plan: free-text injury detail and an "AI plan intent" continuation step — it does not rebuild it.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Dev-supplied screenshot | Status |
|---|---|---|---|
| P-11 Full Assessment — Kesehatan & Riwayat Cedera step | [node-id=12-1710](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1710) | Toggle list (jantung/dada/sendi/obat), free-text field revealed under "cedera/nyeri sendi" toggle, stepper header (Data Tubuh ✓ → Kesehatan 2 → Aktivitas 3 → ...) | New step content for the existing `FullAssessmentStubScreen` wizard — reuses its stepper shell, adds the free-text injury field (§9 item 1) |
| P-12 A — Paywall / value comparison | [node-id=12-1828](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1828) | Two-column compare card (Gratis rule-based vs Subscribe AI Custom Plan), "Chat AI 5x/hari tetap gratis" reassurance banner, price card, "Mulai Berlangganan" CTA | New screen, `AiCustomPlanGateScreen` |
| P-12 B — Request form | [node-id=12-1102](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1102) | Goal chips, frequency stepper, duration chips, equipment radio cards, muscle-focus multi-chips, plan-duration chips, "Buatkan Plan Saya" CTA | New screen, `AiCustomPlanFormScreen` |
| P-13 — Preview | [node-id=12-1947](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1947) | "DIBUAT OLEH AI" badge, conflict warning banner (red), collapsible week accordions, per-exercise "Ganti Gerakan"/"Sesuaikan Set" actions, AI tip card, Regenerate/Simpan Draft/Aktifkan Sekarang actions | New screen, `AiCustomPlanPreviewScreen` |

No frame was provided for: the guest-facing "why login" explainer (UC-1 guest branch — build as a simple explainer + login CTA consistent with the paywall card's visual language), the generate-loading state (UC-3, ≤30s — build as a full-screen loading state with an informative rotating message, not a blank spinner), and the quota-full state (UC-4 alternate flow — simple dialog: "Hapus draft lama dulu" + list of existing drafts to delete).

## 3. Scope

**In scope**
- P-11 extension: free-text injury detail field (stored as `injuryHistoryNote: String?` alongside the existing `injuryHistory: Set<BodyArea>`), and an "AI plan intent" continuation — completing Full Assessment while arriving from the AI Custom Plan CTA routes to P-12 instead of back to Home
- P-12 A: entitlement gate screen (guest explainer, non-subscriber paywall, subscriber pass-through)
- P-12 B: AI plan request form (goal, sessions/week, session duration, equipment, muscle focus, plan duration 1-4 weeks)
- P-13: AI-generated preview — per-exercise swap (reuses Exercise Library's `equivalentSubstitutes` relation, same as PT Mode's swap), set/rep edit, regenerate, save-as-draft, activate
- Minimal local `Tier`/`Capability` entitlement stub (`domain/entitlement`) — read-side only; how the underlying flags get set (real login, RevenueCat webhook) is out of scope
- Ktorfit remote client + documented request/response contract for the AI generate call
- `exerciseId` client-side validation with auto-substitution for AI responses referencing unknown/stale IDs
- Minimal Plan Management slice: `PlanStatus.DRAFT`, draft quota (max 5 AI-custom plans), delete-if-not-active — reusing the existing `MonthlyPlanRepository`/tables, not a new feature area
- Workout-history summary DTO sent as AI context (attendance, avg RPE, completed session count over the last cycle)

**Out of scope** (explicitly deferred)
- Real authentication (Google Sign-In, session tokens) — `08-auth-sync.md`. This techspec's `Tier`/`Capability` stub reads flags that a future auth/billing feature will actually set; until then, dev sets them manually (see §7 Entitlement)
- Real billing/subscription lifecycle (RevenueCat webhooks, payment) — future billing feature
- Full My Plans list/browse UI (P-14: viewing all plans, template picker, activating any plan) — `07-plan-management.md`; this techspec only adds the draft/quota/delete plumbing that UC-4 needs
- Chat AI (P-16) — mentioned only as reassurance copy on the paywall, not built here
- Self-test video capture, full postural/movement screening polish for P-11 — already deferred by `monthly-plan.md` §3, unchanged here
- Actual AI backend implementation (the LLM/service behind the endpoint) — this techspec only defines and consumes the contract; server-side is a separate workstream
- Persisting in-flight form state across app kill (network-drop retry within the same session is handled; process death during generate is not — §9 item 9)

## 4. User Story & Acceptance Criteria

> Sebagai subscriber, saya ingin mengisi form keinginan latihan dan AI membuatkan plan yang dipersonalisasi dari profil serta riwayat saya — yang bisa saya tinjau dan sunting sebelum dipakai — sehingga plan benar-benar sesuai kondisi dan keinginan saya.

**AC-1** Given user guest, when membuka fitur AI, then diarahkan ke penjelasan + login — bukan error.
**AC-2** ~~Given user login non-subscriber, when membuka fitur AI, then melihat paywall~~ **[ADR-009, 2026-11-09] Sementara tidak berlaku — user login non-subscriber sekarang pass through (AI Custom Plan gratis selama MVP showcase; `NonSubscriberPaywall` state kept but unreachable)**.
**AC-3** Given form disubmit, when AI selesai, then preview tampil ≤ 30 detik dengan loading state informatif.
**AC-4** Given hasil AI, when divalidasi, then 100% exercise ada di library (`exerciseId` valid); yang tidak valid diganti padanan otomatis.
**AC-5** Given riwayat cedera diisi, when plan dibuat, then gerakan berisiko terkait dihindari/dimodifikasi.
**AC-6** Given edit manual, when disimpan, then hasil edit yang dipakai (bukan output AI asli).
**AC-7** Given plan diaktifkan, when membuka Home, then hanya ada 1 plan aktif (plan lama diarsipkan).
**AC-8** Given koneksi putus saat generate, when retry, then form tidak perlu diisi ulang (same session).
**AC-9** Given keinginan user konflik dengan profil (mis. 6 hari tapi Beginner), when plan dibuat, then tetap digenerate + warning & saran tampil di preview.
**AC-10** Given kuota 5 draft custom plan penuh, when user membuat plan baru, then diminta hapus draft lama dulu — bukan error/silent fail.
**AC-11** Given Full Assessment belum lengkap, when user login membuka fitur AI, then diarahkan menyelesaikan Full Assessment dulu, lanjut otomatis ke form setelah selesai.

### AC Traceability Table

| AC | Handled by | Blocking? |
|---|---|---|
| AC-1 | `presentation/aicustomplan/screen/AiCustomPlanGateScreen.kt` + `usecase/entitlement/GetCurrentTier.kt` | **BLOCKING** — must never error/crash for a guest, this is the primary entry gate |
| AC-2 | Same gate screen, `Tier.LOGGED_IN` branch | **BLOCKING** — same reason |
| AC-3 | `usecase/aicustomplan/GenerateAiPlan.kt` (30s Ktorfit timeout) + `AiCustomPlanFormViewModel` loading state | No |
| AC-4 | `usecase/aicustomplan/ValidateAndFixExerciseIds.kt` | **BLOCKING** — safety/data-integrity, plan must never reference a phantom exercise |
| AC-5 | `usecase/aicustomplan/GenerateAiPlan.kt` request payload includes `injuryHistory`/`injuryHistoryNote`; server-side avoidance is contractual, client validates the *response* doesn't reintroduce excluded flags via `usecase/aicustomplan/CheckInjuryConflicts.kt` | **BLOCKING** — safety |
| AC-6 | `presentation/aicustomplan/viewmodel/AiCustomPlanPreviewViewModel.kt` — edits mutate local `PlanDay`/`PlannedExercise` state, `SaveAiCustomPlanDraft`/`ActivateAiCustomPlan` persist the edited state, never the raw AI response | No |
| AC-7 | `usecase/aicustomplan/ActivateAiCustomPlan.kt`, mirrors `ActivatePlanFromFullAssessment`'s archive-and-activate pattern | No |
| AC-8 | `AiCustomPlanFormViewModel` keeps form input in state across a failed `GenerateAiPlan` call; only cleared on success or explicit navigation away | No |
| AC-9 | `usecase/aicustomplan/DetectProfileConflicts.kt` (client-side, compares form vs `FullAssessmentResult.level`) — surfaces the warning banner; generation is never blocked on it | No |
| AC-10 | `usecase/aicustomplan/CheckDraftQuota.kt` + `usecase/aicustomplan/DeleteAiCustomPlan.kt` | No |
| AC-11 | `AiCustomPlanGateScreen` routes to `Route.FullAssessmentStub(continueTo = AI_CUSTOM_PLAN)` when `HasCompletedFullAssessment` returns false | No |

## 5. Domain Model

### Entitlement entities (`domain/entitlement`) — new

```
enum class Capability { AI_CUSTOM_PLAN, CHAT_AI_BASIC, CHAT_AI_PRO, MEAL_PLAN }

enum class Tier { GUEST, LOGGED_IN, SUBSCRIBED }

fun Tier.capabilities(): Set<Capability> = when (this) {
    GUEST -> emptySet()
    LOGGED_IN -> setOf(Capability.CHAT_AI_BASIC)
    SUBSCRIBED -> setOf(Capability.CHAT_AI_BASIC, Capability.CHAT_AI_PRO, Capability.AI_CUSTOM_PLAN, Capability.MEAL_PLAN)
}
```

Per architecture.md §7: this is a UX gate only. `EntitlementRepository` (below) is a thin read over local settings — nothing here enforces payment; a real backend/RevenueCat check is required before this can be trusted for anything beyond hiding/showing UI, and is explicitly future work.

### AI Custom Plan entities (`domain/aicustomplan`) — new

```
data class AiPlanFormInput(
    val goal: GoalCategory,                    // reuses onboarding's GoalCategory
    val sessionsPerWeek: Int,                   // 1-7, reuses FrequencyBucket-style validation
    val sessionDurationMinutes: Int,            // 30 | 45 | 60 | 90
    val equipmentPreference: Set<EquipmentCategory>,
    val muscleFocus: Set<BodyArea>,             // optional, multi-select
    val planDurationWeeks: Int,                 // 1-4
)

data class WorkoutHistorySummary(
    val cycleCount: Int,
    val attendanceRate: Float,                  // 0.0-1.0, last completed cycle
    val averageRpe: Float?,                     // null if no RPE ever logged
    val completedSessionCount: Int,
)

data class ProfileConflict(
    val field: String,                          // e.g. "sessionsPerWeek"
    val messageKey: String,                     // Compose string-resource key, not raw text (never persist user-facing text — architecture.md §3)
)
```

The generated plan itself is **not** a new entity — it reuses `MonthlyPlan` + `PlanDay` + `PlannedExercise` from `domain/monthlyplan` (see §7 for the two new enum values this requires), so PT Mode, Home, and session-status derivation work unmodified. `MonthlyPlan.smartGoalMeta` carries the AI's short rationale summary; `PlannedExercise.reasonRuleIds` carries `["AI_GENERATED"]` plus any conflict/substitution tags for transparency, consistent with the rule-based tiers' "kenapa saya dapat gerakan ini?" principle.

### Full Assessment extension (`domain/fullassessment`) — modifies existing `FullAssessmentResult`

```
data class FullAssessmentResult(
    ...,                          // existing fields unchanged
    val injuryHistoryNote: String?,   // new — free-text detail, e.g. "Lutut kiri saat jongkok"
)
```

Additive nullable field — no migration for existing rows beyond a schema addition (see §7).

### Business Rules

- **Entitlement resolution**: `Tier` is derived, not stored directly — `EntitlementRepository.getCurrentTier()` reads `isLoggedIn`/`isSubscribed` booleans from `multiplatform-settings` and maps to `Tier` (`!isLoggedIn → GUEST`, `isLoggedIn && !isSubscribed → LOGGED_IN`, `isLoggedIn && isSubscribed → SUBSCRIBED`). Until real auth/billing land, these two flags have no producer in the app — flag this explicitly to the dev as a manual-testing gap (§9 item 8).
- **Generate request payload**: `AiPlanFormInput` + `FullAssessmentResult` (full, including `injuryHistoryNote`) + `WorkoutHistorySummary` + client-computed `ProfileConflict` list (server may also compute its own; client-detected conflicts are shown regardless of what the server returns, since the UI must never block on a slow/absent server-side check).
- **Response validation (AC-4, BLOCKING)**: every `exerciseId` in the response is checked against `ExerciseLibraryRepository`. Unknown ID → look up `equivalentSubstitutes` for the *closest matching* exercise the server did send (matched by `movementPattern` + `equipmentCategory` from context, since the unknown ID itself has no library entry to read a pattern from) → if no substitute resolvable, drop the slot and add a `reasonRuleIds` entry `"AI_SUBSTITUTION_FAILED"` surfaced as a visible preview banner ("1 gerakan tidak bisa dimuat, coba regenerate") — never silently drop without surfacing, mirroring `monthly-plan.md` AC-7's POOL-04 principle.
- **Draft quota (AC-10)**: `CheckDraftQuota` counts `MonthlyPlan` rows where `source == AI_GENERATED && status != ARCHIVED` (i.e., DRAFT + ACTIVE both count — an activated AI plan still occupies a quota slot per issue #12's "Kuota 5 penuh" wording, until the dev says otherwise). Archived AI plans don't count.
- **Delete guard**: `DeleteAiCustomPlan` rejects (`Failure.Validation`) if `status == ACTIVE` — must archive-via-replacement instead, consistent with `07-plan-management.md`'s "plan aktif tidak bisa dihapus" rule.

## 6. Architecture Breakdown

Two new feature slugs (`entitlement`, `aicustomplan`); one extended feature (`fullassessment`); one touched-but-not-restructured feature (`monthlyplan`, two new enum values only).

```
shared/src/commonMain/kotlin/
├── domain/entitlement/
│   ├── entity/Capability.kt, Tier.kt
│   └── repository/EntitlementRepository.kt
├── domain/aicustomplan/
│   ├── entity/AiPlanFormInput.kt, WorkoutHistorySummary.kt, ProfileConflict.kt
│   └── repository/AiCustomPlanRepository.kt
├── domain/fullassessment/entity/FullAssessmentResult.kt        // + injuryHistoryNote field
├── domain/monthlyplan/entity/PlanSource.kt                     // + AI_GENERATED
├── domain/monthlyplan/entity/PlanStatus.kt                     // + DRAFT (currently in same file, PlanSource.kt)
│
├── usecase/entitlement/
│   └── GetCurrentTier.kt
├── usecase/aicustomplan/
│   ├── GenerateAiPlan.kt                  // calls repository, maps 4xx/timeout to Failure
│   ├── ValidateAndFixExerciseIds.kt       // AC-4, BLOCKING
│   ├── CheckInjuryConflicts.kt            // AC-5, BLOCKING
│   ├── DetectProfileConflicts.kt          // AC-9, client-side comparison
│   ├── BuildWorkoutHistorySummary.kt      // reads MonthlyPlanRepository session logs
│   ├── SaveAiCustomPlanDraft.kt
│   ├── ActivateAiCustomPlan.kt            // archive-and-activate, mirrors ActivatePlanFromFullAssessment
│   ├── CheckDraftQuota.kt
│   └── DeleteAiCustomPlan.kt
├── usecase/fullassessment/
│   └── SubmitFullAssessmentInjuryDetail.kt   // new, extends the existing wizard's usecase set
│
├── data/entitlement/
│   ├── datasource/EntitlementLocalDataSource.kt   // multiplatform-settings wrapper
│   └── repository/EntitlementRepositoryImpl.kt
├── data/aicustomplan/
│   ├── model/AiPlanRequestDto.kt, AiPlanResponseDto.kt   // @Serializable, toEntity()/toDomain() mappers
│   ├── datasource/AiCustomPlanApi.kt              // Ktorfit interface
│   ├── datasource/AiCustomPlanRemoteDataSource.kt // wraps AiCustomPlanApi, maps HTTP errors
│   └── repository/AiCustomPlanRepositoryImpl.kt
│
└── di/EntitlementModule.kt, AiCustomPlanModule.kt
```

```
composeApp/src/commonMain/kotlin/
├── presentation/aicustomplan/
│   ├── viewmodel/AiCustomPlanGateViewModel.kt, AiCustomPlanFormViewModel.kt, AiCustomPlanPreviewViewModel.kt
│   ├── screen/AiCustomPlanGateScreen.kt (P-12 A), AiCustomPlanFormScreen.kt (P-12 B), AiCustomPlanPreviewScreen.kt (P-13)
│   └── component/ GoalChipGroup, EquipmentRadioCards, MuscleFocusChips, ConflictWarningBanner, WeekAccordion (reuse from presentation/monthlyplan if it already exists — mirror, don't duplicate), ExerciseSwapDialog (reuse PT Mode's `presentation/ptmode/component/ExerciseSwapDialog.kt` if its shape fits; otherwise mirror it)
├── presentation/fullassessment/
│   ├── viewmodel/FullAssessmentViewModel.kt      // + injury free-text field, + continuation routing
│   └── screen/FullAssessmentStubScreen.kt        // + injury free-text input under the existing toggle
└── navigation/ (routes below)
```

expect/actual: none new. The Ktorfit HTTP engine actuals (`okhttp`/`darwin`) are standard KMP setup, not feature-specific expect/actual — added once in `shared/src/androidMain` / `shared/src/iosMain` per the tech-stack table, mirrored from Ktorfit's own multiplatform setup docs since no existing HTTP client precedent exists in this codebase to copy from.

## 7. API / Data Contract

### AI Generate Endpoint (documented, no live backend yet — client built against this contract)

`POST /v1/ai-custom-plan/generate`

Request:
```json
{
  "formInput": {
    "goal": "MUSCLE_GAIN",
    "sessionsPerWeek": 3,
    "sessionDurationMinutes": 45,
    "equipmentPreference": ["DUMBBELL", "BODYWEIGHT"],
    "muscleFocus": ["DADA", "PUNGGUNG"],
    "planDurationWeeks": 2
  },
  "fullAssessment": { "...": "full FullAssessmentResult fields, see §5" },
  "workoutHistorySummary": { "cycleCount": 2, "attendanceRate": 0.8, "averageRpe": 6.5, "completedSessionCount": 18 }
}
```

Response (success):
```json
{
  "smartGoalSummary": "string",
  "days": [
    {
      "dayNumber": 1,
      "type": "SESSION",
      "templateLetter": "A",
      "mainExercises": [
        { "exerciseId": "BW-SQUAT-001", "sets": 3, "repRangeOrDuration": "8-12", "rpeTargetMin": 6, "rpeTargetMax": 8, "restSeconds": 90 }
      ]
    }
  ],
  "conflicts": [ { "field": "sessionsPerWeek", "note": "string" } ]
}
```

Auth: `Authorization: Bearer <token>` header — token source is the future auth feature; until then, the Ktorfit client is built with the header slot present but the `AiCustomPlanRemoteDataSource` will fail closed (`Failure.Unauthorized`) if no token is available, which is correct behavior even pre-launch (guest/non-subscriber never reaches this call per AC-1/AC-2).

Errors: 401 → `Failure.Unauthorized`; 422 (validation) → `Failure.Validation`; 5xx/timeout (30s client-side timeout, AC-3) → `Failure.Network`. All mapped in `AiCustomPlanRemoteDataSource`, never a raw exception crossing into `usecase`.

### SQLDelight

| `.sq` file | Table | Generated `*Queries` | Schema status |
|---|---|---|---|
| `FullAssessmentResult.sq` | `full_assessment_result` | `fullAssessmentResultQueries` | **Modified** — add nullable `injury_history_note TEXT` column. Pre-release app, no user data yet → recreate DB rather than write a `.sqm` migration (established precedent, see `docs/decisions.md` if this needs its own ADR entry) |

No new tables for the AI plan itself — `monthly_plan`/`plan_day`/`planned_exercise` (already exist per `monthly-plan.md` §7) gain two new valid string values in their `source`/`status` columns (`"AI_GENERATED"`, `"DRAFT"`) — since these columns store `.name()` strings, this is a pure additive enum change, no schema/migration needed at all.

Entitlement flags (`is_logged_in`, `is_subscribed`) are stored via `multiplatform-settings` (already a locked tech-stack choice), not SQLDelight — simple booleans, no relational shape needed, and settings survive independently of the recreate-DB precedent used for actual app data.

### DI

Two new Koin modules (manual DSL, ADR-001):
- `EntitlementModule`: `EntitlementRepositoryImpl`, `GetCurrentTier`
- `AiCustomPlanModule`: `AiCustomPlanApi` (Ktorfit-generated), `AiCustomPlanRemoteDataSource`, `AiCustomPlanRepositoryImpl`, all `usecase/aicustomplan/*`, `AiCustomPlanGateViewModel`, `AiCustomPlanFormViewModel`, `AiCustomPlanPreviewViewModel`
- `FullAssessmentModule` (existing): register `SubmitFullAssessmentInjuryDetail`
- Add/verify both new modules in `DiVerificationTest.checkModules()`

### Navigation

New routes (type-safe, `@Serializable`):
- `Route.AiCustomPlanGate` — entry from P-03/P-14's "Buat Plan Custom" CTA
- `Route.AiCustomPlanForm` — P-12 B, reachable only after gate passes and Full Assessment is complete
- `Route.AiCustomPlanPreview(draftPlanId: String)` — P-13
- `Route.FullAssessmentStub` extended with an optional `continueTo: FullAssessmentContinuation? = null` param (`AI_CUSTOM_PLAN` value) — on wizard completion, routes to `Route.AiCustomPlanForm` instead of the default Home return when set (AC-11)

### New dependency

Ktorfit + Ktor Client (okhttp/darwin engines) added to `libs.versions.toml` — this is the tech-stack's already-locked choice (tech-stack.md), not a new decision requiring an ADR; still requires the dev to confirm exact versions before `/code` since no version was pre-selected in the catalog.

## 8. State & Error Handling

| Screen | States |
|---|---|
| AiCustomPlanGateScreen (P-12 A) | `Loading` (resolving tier), `Guest`, `NonSubscriberPaywall`, `NeedsFullAssessment` (subscriber, assessment incomplete — routes onward per AC-11), `PassThrough` (subscriber, assessment complete — routes to form) |
| AiCustomPlanFormScreen (P-12 B) | `Editing(input: AiPlanFormInput)`, `Generating` (loading overlay, informative rotating message, ≤30s), `Error(Failure, retainedInput: AiPlanFormInput)` — retained input satisfies AC-8 |
| AiCustomPlanPreviewScreen (P-13) | `Loading`, `Loaded(weeks: List<WeekPreview>, conflicts: List<ProfileConflict>, substitutionWarnings: List<String>)`, `Regenerating`, `QuotaFull(existingDrafts: List<MonthlyPlan>)` (AC-10), `Error(Failure.Database)` |
| FullAssessmentStubScreen (existing, extended) | Existing states unchanged; injury free-text is a field within the existing `Step` state for the health/injury step, not a new top-level state |

`Failure.Network`/`Failure.Unauthorized`/`Failure.Validation` are all reachable here (first feature besides GATE-01's conceptual "blocked" state to use more than `Failure.Database`) — each maps to a distinct, non-generic error message per the UI states above, never a raw exception message shown to the user.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | No Tier/Capability/auth system exists anywhere (A1) | **Handle** | Minimal local stub (`domain/entitlement`), UX gate only, real enforcement deferred — see §5 Business Rules |
| 2 | No Ktorfit/remote pattern exists anywhere (A2) | **Handle** | Contract documented in §7, real Ktorfit client built against it; no backend exists yet so this is unverifiable end-to-end until a server is available — flag to dev before `/code` |
| 3 | Plan Management (P-14) doesn't exist (A3) | **Handle — minimal slice** | Only `PlanStatus.DRAFT` + quota-check + delete-guard added to existing `MonthlyPlanRepository`; full list/browse UI stays `07-plan-management.md`'s scope |
| 4 | Draft quota — which plans count (L1/B5) | **Handle** | Only `source == AI_GENERATED`, excludes `ARCHIVED` — see §5 |
| 5 | Regenerate after manual edits (L2/B6) | **Handle** | Edits discarded on regenerate; confirm dialog warns before discarding |
| 6 | Full Assessment "already filled" re-entry via AI Custom Plan CTA (B7) | **Handle** | Confirm-banner ("data masih akurat?") with an edit escape hatch, reusing `HasCompletedFullAssessment`; does not force the full wizard again |
| 7 | Workout-history summary shape sent to AI (G8/C1) | **Handle** | `WorkoutHistorySummary` DTO defined in §5 — compact, structured, mirrors `10-chat-ai.md`'s context-injection precedent |
| 8 | Entitlement flags have no real producer yet (new, surfaced during analysis) | **Handle, flagged as testing gap** | `is_logged_in`/`is_subscribed` settings keys exist but nothing sets them until real auth/billing ship; dev needs a manual toggle (e.g. debug menu) to test tiers before then — call out explicitly in the `/code` handoff |
| 9 | App killed mid-generate, form state lost (D2/E10) | **Defer** | Only same-session network-drop retry (AC-8) is required; process death during a ≤30s call is treated as an acceptable rare loss for this pass, consistent with "in-flight state not persisted" scope decision |
| 10 | AI response references an `exerciseId` not in the local library (E11/L... AC-4) | **Handle — BLOCKING** | `ValidateAndFixExerciseIds` substitution chain — see §5 Business Rules; never silently drops without a visible banner |
| 11 | Subscription lapses after a draft/active AI plan exists (E12) | **Handle** | Existing drafts/active plans stay fully readable; `AiCustomPlanGateScreen` only blocks *new* generation, mirroring `07-plan-management.md`'s "lapsed = read-only" precedent |
| 12 | Double-tap "Buatkan Plan Saya" (E13) | **Handle** | Button disabled while `Generating` state is active |
| 13 | AI plan length (1-4 weeks) vs `MonthlyPlan`'s implicit 4-week/30-day framing (G9) | **Handle** | `PlanDay.dayNumber` simply runs `1..(planDurationWeeks * 7)` instead of a fixed 1..30; `MonthlyPlanRepository`/`GetSessionStatus`/Home rendering already key off actual `PlanDay` rows present for a plan, not a hardcoded day count, so no change needed there — confirm during `/code` by reading `HomeViewModel`'s week-grouping logic before assuming this holds |
| 14 | AI could return a plan with an excluded-flag exercise despite the request explicitly listing injuries (AC-5) | **Handle — BLOCKING** | `CheckInjuryConflicts` cross-checks the response's exercises against `FullAssessmentResult`'s active exclusion flags client-side, same as the rule engine's SAFE-* filters would — surfaces a blocking "gerakan berisiko" banner requiring edit/removal before activation, not just a soft warning |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`) — except full offline (this feature is *[online]* during generate only; running an activated AI plan through PT Mode is fully offline, unchanged)
- [ ] API request/response contract documented (§7) and reviewed by dev before `/code` builds the Ktorfit client against it
- [ ] `ValidateAndFixExerciseIds` unit tested: valid IDs pass through, unknown ID with a resolvable substitute gets replaced, unknown ID with no substitute produces a visible warning and never a crash
- [ ] `CheckInjuryConflicts` unit tested against at least one `ExerciseFlag` exclusion scenario per `HEALTH_*`/postural/movement category
- [ ] `CheckDraftQuota`/`DeleteAiCustomPlan` unit tested: 5 drafts blocks a 6th, delete of an `ACTIVE` plan is rejected, delete of a `DRAFT`/`ARCHIVED` plan succeeds
- [ ] `ActivateAiCustomPlan` verified to archive the prior active plan and preserve its log history, mirroring `ActivatePlanFromFullAssessment`'s existing test pattern
- [ ] `GetCurrentTier` unit tested for all three `Tier` outcomes given the two underlying settings flags
- [ ] `DiVerificationTest.checkModules()` passes with `EntitlementModule` and `AiCustomPlanModule` registered
- [ ] Full assessment hanya diminta sekali — `HasCompletedFullAssessment` reused unchanged, confirmed no duplicate-prompt path introduced by the new AI-intent continuation
- [ ] Manual entitlement-flag toggle available for dev testing before real auth/billing ship (§9 item 8)
- [ ] Every `AC` in §4 has a passing test or, for UI-only ACs (AC-1, AC-2, AC-3's loading UX), a manual verification note
