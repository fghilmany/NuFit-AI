# TechSpec: Plan Management (My Plans)

> Source US: [fghilmany/NuFit-AI#11](https://github.com/fghilmany/NuFit-AI/issues/11)
> Depends on: `monthly-plan.md` (MonthlyPlan/PlanDay), `ai-custom-plan.md` (issue #81 — supersedes its `DeleteAiCustomPlan`/hard-delete `deletePlan`)
> Figma: file `dNMXJXWuUOoUF8lEaWC2Lw`, node [12:1251](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1251) (P-14)

## 1. Overview & Goal

A single screen (P-14, "Rencana Latihan") where the user sees every plan they've ever had — active, draft, archived — with a clear "exactly one active plan" invariant, and two ways to start a new one: a free 3-step template picker (Level → Goal → Split, no assessment) or the AI Custom Plan flow (issue #81, already built but unreachable until now — this techspec gives it its real entry point).

Pulling the actual Figma design surfaced scope the source User Story text didn't mention: a "Pulihkan" (restore) action on archived plans, and delete availability on template-sourced plans too, not just AI ones. It also surfaced a real bug in the AI Custom Plan pass's `deletePlan`: it hard-deletes `plan_day`/`planned_exercise` rows without touching the 4-table-deep PT Mode logging chain underneath them, silently orphaning workout history — which violates this feature's own DoD ("hapus plan ≠ hapus log"). This techspec fixes that as part of building the real delete flow.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Notes |
|---|---|---|
| P-14 My Plans ("Rencana Latihan") | [node-id=12-1251](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1251) | Verified via Figma MCP screenshot. Top bar (title + bell) → two entry cards ("Buat dari Template" free, "Buat dengan AI" Subscribe badge) → "Jelajahi Gerakan" quick-access row (reuses existing `Route.ExerciseLibrary`) → "Rencana Kamu" section header with a "Custom plan: X/5" quota pill → plan cards: active (AKTIF + source badge, checkmark, % progress bar), draft (DRAFT + source badge, "Aktifkan"/"Hapus"), archived (ARSIP + source badge, grayed text, "Pulihkan"/"Hapus") → bottom nav, Plans tab active |
| Template picker (Level → Goal → Split, 3 steps) | **Not designed** — no frame exists for the picker itself, only its entry card | Build as a plain sequential wizard reusing Quick Assessment's card/stepper visual pattern (`presentation/onboarding/component`), consistent with the "functional, not polished" precedent already used for Full Assessment's stub screens |

The image initially attached to this techspec request was actually the AI Custom Plan form (P-12 B) screenshot reused by mistake — confirmed by pulling the real P-14 node directly from Figma instead.

## 3. Scope

**In scope**
- P-14 screen: full plan list (all sources, all non-deleted statuses), quota pill, two creation entry cards
- Wiring the *already-built* AI Custom Plan flow (issue #81, `Route.AiCustomPlanGate`) to the "Buat dengan AI" card — this is the flow's first real entry point in the app
- Template picker (3 steps: Level → Goal → Split) → save-as-draft or activate-now, reusing `GenerateLocalTemplatePlan` (refactored, see §5)
- Generalized `ActivatePlan`/`DeletePlan` usecases covering any plan source (template, rule-engine, AI), replacing `ai-custom-plan.md`'s AI-only `ActivateAiCustomPlan`-for-existing-plans gap and `DeleteAiCustomPlan`
- **Soft delete**: `monthly_plan.deleted_at` column — `DeletePlan` never physically removes a row; it only sets this column, preserving the full `plan_day`/`plan_day_session_log`/`workout_session`/`workout_exercise_log`/`workout_set_log` chain underneath, satisfying "hapus plan ≠ hapus log" literally rather than by accident
- Restore ("Pulihkan"): reuses `ActivatePlan` — archives the current active plan, reactivates the target
- Active-plan progress percentage (completed/total sessions so far)
- "Jelajahi Gerakan" entry (reuses existing `Route.ExerciseLibrary`, zero new work)

**Out of scope** (explicitly deferred)
- A literal "un-delete" / recycle-bin UI for soft-deleted plans — the rows survive in the DB for data-integrity reasons, but no screen surfaces them; this is a data-safety net, not a feature
- Editing a draft/archived plan's content (swapping exercises, changing sets) outside of AI Custom Plan's own preview screen — Local/Logged-In tier plans have no edit UI in this pass
- Changing the 5-plan cap's number itself, or adding any cap to template-sourced plans (explicitly uncapped per dev decision)
- Re-ordering or filtering the plan list (search, sort by date, etc.) — plain reverse-chronological list only, matching the Figma reference

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin melihat semua plan saya (aktif, draft, arsip) dan berpindah plan dengan sadar, sehingga latihan saya tetap terarah dengan hanya satu plan aktif.

**AC-1** Given ≥1 plan pernah dibuat, when membuka P-14, then semua plan (non-deleted) tampil dengan badge status (Aktif/Draft/Arsip) dan sumber (Template/AI/Personalized).
**AC-2** Given user memilih plan lain (Aktifkan/Pulihkan), when dikonfirmasi, then plan aktif lama diarsipkan dan hanya 1 plan aktif; Home (P-03) langsung mencerminkan plan baru.
**AC-3** Given user membatalkan konfirmasi ganti plan aktif, when kembali, then tidak ada perubahan.
**AC-4** Given user mencoba menghapus plan aktif, when dicoba, then dicegah dengan penjelasan (harus ganti plan aktif dulu) — berlaku untuk semua sumber plan, bukan hanya AI.
**AC-5** Given plan (draft/arsip, sumber apa pun) dihapus, when dikonfirmasi, then plan hilang dari daftar TAPI riwayat log (`plan_day_session_log`/PT Mode's per-set logs) tetap utuh secara fisik di database — never a hard DELETE that could orphan or destroy log rows.
**AC-6** Given kuota 5 custom AI plan penuh (draft+aktif, arsip tidak dihitung), when user coba "Buat dengan AI", then diarahkan ke P-14 untuk hapus draft lama dulu (reuses issue #81's `CheckDraftQuota`/`QuotaFull` state, now with the corrected soft-delete). Template plans tidak dibatasi kuota.
**AC-7** Given template dipilih manual (Level/Goal/Split, tanpa assessment), when disimpan, then plan ter-generate rule-based lokal offline dan tunduk aturan 1-plan-aktif yang sama (save-as-draft tidak mengubah plan aktif; "Aktifkan sekarang" mengarsipkan plan aktif lama).
**AC-8** Given kombinasi split tidak ideal untuk goal/level (mis. Beginner + Upper/Lower berfrekuensi tinggi), when plan dibuat, then tetap dibuat + info edukasi singkat (pola sama dengan Quick Assessment).

### AC Traceability Table

| AC | Handled by | Blocking? |
|---|---|---|
| AC-1 | `usecase/planmanagement/GetAllPlansForManagement.kt` + `presentation/planmanagement/screen/MyPlansScreen.kt` | No |
| AC-2 | `usecase/planmanagement/ActivatePlan.kt` (archive-current + activate-target in one usecase call) | No |
| AC-3 | ViewModel confirm-dialog state only, no repository call until confirmed | No |
| AC-4 | `usecase/planmanagement/DeletePlan.kt` rejects `status == ACTIVE` regardless of `source` | **BLOCKING** — same safety invariant as issue #81, now source-agnostic |
| AC-5 | `MonthlyPlanRepository.softDeletePlan` (sets `deleted_at`, no cascading DELETE anywhere) | **BLOCKING** — data-integrity fix, this techspec's core correction |
| AC-6 | `usecase/aicustomplan/CheckDraftQuota.kt` (issue #81, reused unchanged — its `getAllPlans()` source already excludes soft-deleted rows once §7's query filter lands) | No |
| AC-7 | `usecase/planmanagement/CreateTemplatePlan.kt` + refactored `GenerateLocalTemplatePlan` (status param) | No |
| AC-8 | Reuses Quick Assessment's existing split/frequency conflict education copy pattern, applied client-side in the picker | No |

## 5. Domain Model

### New value object (`domain/monthlyplan/entity`)

```
/** Smaller than QuickAssessmentResult -- the 3-4 fields GenerateLocalTemplatePlan actually needs,
 *  shared by both Quick Assessment (adapts its QuickAssessmentResult into this) and the manual
 *  template picker (constructs this directly, no assessment involved). */
data class TemplatePlanInput(
    val level: Level,
    val goal: GoalCategory,
    val equipment: Set<EquipmentCategory>,
    val sessionsPerWeek: Int,
    val resolvedSplit: ResolvedSplit,
)
```

### Modified (`domain/monthlyplan/entity/MonthlyPlan.kt`)

```
data class MonthlyPlan(
    ...,                          // existing fields unchanged
    val deletedAt: Instant? = null,   // new -- soft delete, never physically removed
)
```

### Business Rules

- **Soft delete is the only delete.** `DeletePlan` guards `status != ACTIVE`, then calls `MonthlyPlanRepository.softDeletePlan(planId)` — a single `UPDATE monthly_plan SET deleted_at = ? WHERE id = ?`. Nothing else is touched: `plan_day`, `planned_exercise`, `plan_day_session_log`, and the whole PT Mode chain (`workout_session` → `workout_exercise_log` → `workout_set_log`) survive untouched, physically and logically (still joinable, just excluded from list/quota queries by the new `deleted_at IS NULL` filter).
- **`ActivatePlan(planId)` covers both "Aktifkan" and "Pulihkan"** — they are the same operation (archive current active if one exists and differs from target, then set target's `status = ACTIVE`) with only the button label differing based on the target's prior status (`DRAFT` → "Aktifkan", `ARCHIVED` → "Pulihkan"). No plan regeneration involved — the target's `plan_day`/`planned_exercise` rows already exist from when it was first saved.
- **`CreateTemplatePlan`** builds a `TemplatePlanInput` from the picker's 3 manual choices — `equipment` defaults to `{BODYWEIGHT}` (no equipment step in the manual picker, matching the "template matrix" simplicity principle) and `sessionsPerWeek` is inferred from the chosen split (`FULL_BODY → 3`, `UPPER_LOWER → 4`, same direction Quick Assessment already infers in reverse). If `activateNow == true`, archives the current active plan first (same guarded pattern as `ActivatePlan`), then calls `GenerateLocalTemplatePlan(input, status = ACTIVE)`; if saving as draft, calls it with `status = DRAFT` and no archiving.
- **Quota (AC-6) is unchanged from issue #81**: `CheckDraftQuota` counts `source == AI_GENERATED && status != ARCHIVED` — soft-deleted rows are already excluded because `getAllPlans()` itself filters `deleted_at IS NULL` at the query level, so no usecase-level change is needed there.
- **Progress percentage** (active plan card): `completedSessionCount / totalSessionDayCount` for the active plan's own days — identical math to `usecase/aicustomplan/BuildWorkoutHistorySummary.kt`'s `attendanceRate`, reused directly (not duplicated) via a small `usecase/planmanagement/GetPlanProgress.kt` that runs the same computation against `GetActivePlanOverview`'s bundle.

## 6. Architecture Breakdown

New feature slug `planmanagement`; modifies `monthlyplan` (entity, repository, refactored usecase) and `aicustomplan` (no code change, but `DeleteAiCustomPlan` is superseded — see §9).

```
shared/src/commonMain/kotlin/
├── domain/monthlyplan/entity/
│   ├── MonthlyPlan.kt                    // + deletedAt
│   └── TemplatePlanInput.kt              // new
├── domain/monthlyplan/repository/MonthlyPlanRepository.kt   // deletePlan -> softDeletePlan; getAllPlans/getActivePlan now implicitly exclude soft-deleted
│
├── usecase/monthlyplan/GenerateLocalTemplatePlan.kt   // refactored: TemplatePlanInput + status param, no longer takes QuickAssessmentResult directly
├── usecase/onboarding/ (call site)                    // one-line adapter: TemplatePlanInput.from(quickAssessmentResult)
├── usecase/planmanagement/
│   ├── GetAllPlansForManagement.kt
│   ├── ActivatePlan.kt
│   ├── DeletePlan.kt
│   ├── CreateTemplatePlan.kt
│   └── GetPlanProgress.kt
│
├── data/monthlyplan/datasource/MonthlyPlanLocalDataSource.kt   // deletePlan (hard) removed; softDeletePlan added; queries gain `WHERE deleted_at IS NULL`
├── data/monthlyplan/repository/MonthlyPlanRepositoryImpl.kt    // mirrors the above
│
└── di/PlanManagementModule.kt (new), MonthlyPlanModule.kt (updated)
```

```
composeApp/src/commonMain/kotlin/
├── presentation/planmanagement/
│   ├── viewmodel/MyPlansViewModel.kt, TemplatePickerViewModel.kt
│   ├── screen/MyPlansScreen.kt (P-14), TemplatePickerScreen.kt
│   └── component/ PlanCard (status+source badges, progress bar), QuotaPill, CreateEntryCards
└── navigation/ (routes below)
```

Superseded from `ai-custom-plan.md` (issue #81): `usecase/aicustomplan/DeleteAiCustomPlan.kt` and `MonthlyPlanRepository.deletePlan`'s hard-delete are removed/replaced by this techspec's `DeletePlan`/`softDeletePlan`. `CheckDraftQuota`, `SaveAiCustomPlanDraft`, and `ActivateAiCustomPlan` (the fresh-in-memory-draft variant) are unchanged and still used by the AI Custom Plan flow itself — only the "manage an already-persisted plan" operations move here.

expect/actual: none needed.

## 7. API / Data Contract

### SQLDelight

| `.sq` file | Change | Generated `*Queries` | Schema status |
|---|---|---|---|
| `MonthlyPlan.sq` | Add nullable `deleted_at INTEGER` column. `getActiveMonthlyPlan`/`getAllMonthlyPlans` gain `AND deleted_at IS NULL` / `WHERE deleted_at IS NULL`. `deleteMonthlyPlan` (hard delete) removed; add `softDeleteMonthlyPlan: UPDATE monthly_plan SET deleted_at = ? WHERE id = ?` and `activateMonthlyPlan: UPDATE monthly_plan SET status = 'ACTIVE' WHERE id = ?` | `monthlyPlanQueries` | **Modified** — pre-release app, no user data yet → recreate DB (established precedent), no `.sqm` needed |
| `PlanDay.sq` | `deletePlanDaysForPlan`/`getPlanDayIdsForPlan` (added for the now-removed hard-delete path) become dead code — removed | `planDayQueries` | Modified (query removal only, no column change) |

No changes to `plan_day_session_log`, `workout_session`, `workout_exercise_log`, `workout_set_log` — that is the entire point of this fix; they are simply never touched by delete.

### DI

- New `PlanManagementModule`: `GetAllPlansForManagement`, `ActivatePlan`, `DeletePlan`, `CreateTemplatePlan`, `GetPlanProgress`, `MyPlansViewModel`, `TemplatePickerViewModel`
- `MonthlyPlanModule`: register the refactored `GenerateLocalTemplatePlan` (signature changed, same registration line otherwise)
- `AiCustomPlanModule`: remove `DeleteAiCustomPlan` registration (superseded)
- Add `PlanManagementModule` to `KoinInit.kt` and `DiVerificationTest.kt`

### Navigation

- `Route.MyPlans` — new, becomes the real destination for the "Plans" bottom-nav tab (currently decorative, per this session's earlier investigation — this techspec is what gives it an `onClick`)
- `Route.TemplatePicker` — 3-step wizard, reached from P-14's "Buat dari Template" card
- `Route.AiCustomPlanGate` (issue #81, already built) — reached from P-14's "Buat dengan AI" card; this is its first real entry point in the app
- `BottomNavBar`'s `NavTab.RENCANA` item gets an `onClick` navigating to `Route.MyPlans`, mirroring how `NavTab.HOME` already works

## 8. State & Error Handling

| Screen | States |
|---|---|
| MyPlansScreen (P-14) | `Loading`, `Loaded(plans: List<MonthlyPlan>, quotaCount: Int)`, `ConfirmActivate(target: MonthlyPlan)` (AC-2/AC-3 gate), `ConfirmDelete(target: MonthlyPlan)`, `Error(Failure.Database)` |
| TemplatePickerScreen | `Step(level, goal, split, activateNow)`, `Submitting`, `Error(Failure.Database)` — no network states, fully offline |

`Failure.Database` is the only reachable type here (all local, no network calls in this feature — AI Custom Plan's own network states are unchanged and owned by its existing screens).

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | `deletePlan` (issue #81) hard-deletes `plan_day`/`planned_exercise` without touching the PT Mode logging chain underneath, orphaning workout history | **Handle — BLOCKING** | Replaced with soft delete (`deleted_at` column); nothing under a plan is ever physically removed — see §5/§7 |
| 2 | "Pulihkan" (restore) wasn't in the source US at all | **Handle** | Same `ActivatePlan` usecase as "Aktifkan", button copy differs by prior status |
| 3 | Delete shown on template-sourced plans too, not just AI, in the real design | **Handle** | `DeletePlan` is source-agnostic; supersedes `DeleteAiCustomPlan` |
| 4 | Manual template picker has no frequency/equipment step but `GenerateLocalTemplatePlan` needs both | **Handle** | Equipment defaults to Bodyweight-only; frequency inferred from split choice (Full Body=3x, Upper/Lower=4x) |
| 5 | Template-sourced draft plans — capped like AI ones? | **Ignore this pass** | No cap; only AI-generated plans are quota-limited (generation cost, not storage) |
| 6 | Double-tap "Aktifkan"/"Pulihkan" racing two archive-and-activate calls | **Handle** | Button disabled while the ViewModel's activate call is in flight |
| 7 | Progress-bar % on the active plan card had no existing derivation | **Handle** | Reuses `BuildWorkoutHistorySummary`'s completed/total math via `GetPlanProgress`, not duplicated |
| 8 | App killed mid archive-and-activate | **Handle, accepted existing precedent** | Sequential archive-then-activate calls, not one atomic transaction — same non-atomic-but-sequential pattern already accepted by `ActivatePlanFromFullAssessment`/`ActivateAiCustomPlan`; not a new risk introduced here |
| 9 | `GenerateLocalTemplatePlan` originally required a full `QuickAssessmentResult`, coupling it to onboarding | **Handle** | Refactored to accept the smaller `TemplatePlanInput` + a `status` param; Quick Assessment's call site adapts with a one-line mapper (dev-approved refactor, see conversation) |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] Invariant "tepat 1 plan aktif" enforced at the data level: `ActivatePlan`/`CreateTemplatePlan`'s activate-now path always archives the prior active plan before/atomically-adjacent-to activating the new one; unit tested for both "an active plan exists" and "no active plan yet" branches
- [ ] `DeletePlan` unit tested: rejects `ACTIVE` regardless of source, soft-deletes `DRAFT`/`ARCHIVED` regardless of source, and a test asserting `plan_day_session_log`/`workout_session` rows for a deleted plan are still readable via direct query after deletion (proves AC-5, not just "no crash")
- [ ] `ActivatePlan` unit tested for both "Aktifkan" (from DRAFT) and "Pulihkan" (from ARCHIVED) call shapes
- [ ] `CreateTemplatePlan` unit tested: save-as-draft doesn't touch the active plan; activate-now archives the prior active plan; frequency/equipment defaults verified
- [ ] `GetPlanProgress` unit tested against the same fixture scenarios as `BuildWorkoutHistorySummaryTest` (shared math, shared confidence)
- [ ] `DiVerificationTest.checkModules()` passes with `PlanManagementModule` registered and `DeleteAiCustomPlan` removed
- [ ] Manual verification: "Plans" bottom-nav tab now navigates to a real screen (previously decorative, per this session's investigation)
- [ ] Manual verification: AI Custom Plan flow (issue #81) is reachable end-to-end from a cold app launch for the first time via P-14's "Buat dengan AI" card
