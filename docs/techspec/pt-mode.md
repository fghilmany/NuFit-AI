# TechSpec: PT Mode (Guided Workout)

**Source US**: [#13](https://github.com/fghilmany/NuFit-AI/issues/13) — Fitur: PT Mode (Guided Workout)
**Pages**: P-05 (Guided Workout), P-06 (Workout Summary)

## 1. Overview & Goal

Let a user with an active plan press Start and be guided set-by-set through a session — entering actual weight/reps per set, resting on an automatic timer, optionally skipping or swapping an exercise for that session only — ending in a summary screen with volume/duration/PR stats and an optional RPE rating. Every set is logged individually (not just per-session), 100% offline, resumable across app kills.

## 2. UI Reference

- Figma P-05: https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-308
- Figma P-06: https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-188
- Dev-supplied screenshots (Figma MCP unavailable at spec time): P-05 `/Users/faris/.claude/image-cache/eed12faf-77a7-4d19-a051-77f9d82b1263/10.png`, P-06 `/Users/faris/.claude/image-cache/eed12faf-77a7-4d19-a051-77f9d82b1263/11.png`

### P-05 — Guided Workout (Active Set)
- Top bar: "NUFIT AI COACH" eyebrow + "Latihan X dari Y" (exercise index / `mainExercises.size`), pause button (top-right).
- Media card: back chevron ("Latihan Terpandu"), "Active Set - {exerciseName}" live-indicator label, rest-timer countdown (`mm:ss`) top-right of media, exercise image, live weight/rep/heart-rate overlay (heart-rate overlay is decorative/unavailable data — out of scope, static or omitted).
- Exercise name (large).
- "{sets} x {reps} @ {weight} kg" target line, "Set N dari M" pill (current set / planned sets).
- Two steppers: BEBAN (KG) and REPETISI, each with −/+ and a numeric value.
- Primary button: "Selesai Set" with checkmark — confirms the current set.
- Bottom row: "Ganti gerakan" (swap icon, left) / "Lewati" (skip icon, right).
- **Not in this pass**: an explicit exit/end-session-early affordance is not visible in the mock but is required by the AC (see Edge Case Decisions #3) — add a small back/close icon to the top bar, styled consistent with the existing top-bar icon language (mirror `AssessmentDetailScreen`'s `TopBar` icon-button pattern).

### P-06 — Workout Summary
- Header: "✨ Selesai!" title, share icon, close (X). Share icon is decorative only in this pass (see Edge Case Decisions #7 — not wired).
- Trophy icon, "Sesi Selesai! Kamu Hebat!" headline, supportive subtitle.
- Stat card: "Total Beban" (total volume, kg).
- Two smaller cards: "Durasi" (minutes), "Gerakan" ("N dari M Selesai" — completed vs total, skipped excluded from the completed count).
- PR banner(s): dark card "Rekor Baru!" / "{exerciseName}: {weightKg}kg" / "(+{delta}kg dari sebelumnya)" — one card per PR achieved this session (stack vertically if more than one; render nothing if none).
- "Seberapa berat latihannya?" section: RPE descriptor card ("Tingkat {n}: {label}" + plain-language description) + a 1–10 slider.
- "Simpan" primary button, "Tutup" text link (close without saving RPE — AC allows this, session itself is still saved).
- Bottom nav visible in the mock; this is Figma's standard frame chrome for context, **not** a literal requirement that P-06 renders as an overlay above the tab bar — P-06 is a normal full-screen navigation destination (see §6 Navigation).

## 3. Scope

**In scope**
- Guided walkthrough of `PlanDay.mainExercises` only (warmup/cooldown excluded, matches existing `exerciseCount()`/`estimatedDurationMinutes()` convention).
- Per-set logging (weight, reps, timestamp) — new table, not reusing the session-level `PlanDaySessionLog`.
- Automatic rest timer (wall-clock-anchored, skippable).
- Exercise swap (session-only, reuses `GetExerciseAlternatives` from Exercise Library) and per-exercise skip.
- Pause / resume across app kill, resuming from the last **completed set**.
- Explicit "End Session Now" action (new, not in the mock) distinct from Pause.
- P-06 summary: volume, duration, exercise completion count, PR detection (max-weight-per-exercise, all PRs shown), RPE 1–10 with plain-language labels, optional (non-blocking) save.
- Keep-awake during an active session; wall-clock-based rest timer immune to backgrounding.
- Writing the existing `PlanDaySessionLog` (`completedAt`, `rpeReported`) on session finish so P-03/P-04's existing `SessionStatus` derivation keeps working unchanged.

**Out of scope (deferred, per Phase 1 decisions)**
- Warmup/cooldown steps in the guided flow.
- Pain reporting on P-06 (`painReported` column stays null; schema already supports it for a future pass).
- Share functionality (icon shown inert or omitted, dev's call at `/code` time).
- Real-time AI adjustment (Subscribe tier, `05-pt-mode.md` explicitly marks this planned/architecture-only).
- Heart-rate display (no data source exists).
- Any change to `GenerateNextMonthPlan`'s progression rules — this spec only guarantees RPE is persisted correctly; the engine that reads it already exists.

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin menekan Start dan dipandu langkah demi langkah sampai sesi selesai, sehingga saya tidak bingung di gym dan semua latihan saya tercatat otomatis.

### AC Traceability

| # | Acceptance Criterion | Handled by | Blocking |
|---|---|---|---|
| AC-1 | Given sesi dimulai, when 1 set diinput, then rest timer mulai otomatis dan bisa di-skip | `ConfirmSet` usecase sets `restEndAt`; `PtModeViewModel` renders countdown from it; "Skip Rest" clears `restEndAt` | No |
| AC-2 | Given app tertutup paksa di tengah sesi, when dibuka lagi, then sesi lanjut dari posisi terakhir | `GetOrCreateWorkoutSession` resumes the `IN_PROGRESS` `WorkoutSession` row by `planDayId`; state persisted on every `ConfirmSet`/`SkipExercise`/`SwapExercise` | **Yes** |
| AC-3 | Given exercise diganti, when sesi selesai, then log mencatat pengganti; plan asli tidak berubah untuk sesi berikutnya | `WorkoutExerciseLog.exerciseId` stores the substituted id; `PlanDay`/`PlannedExercise` rows are never written to by PT Mode | **Yes** |
| AC-4 | Given exercise di-skip, when summary tampil, then skip tercatat dan tidak dihitung volume | `WorkoutExerciseLog.skippedAt` set; `GetWorkoutSummary`'s volume sum only reads `WorkoutSetLog` rows (skipped exercises contribute only already-logged sets, if any, per Edge Case #7) | **Yes** |
| AC-5 | Given RPE diisi, when plan bulan berikutnya dibuat, then RPE jadi input progression | `SaveWorkoutSummary` writes `rpeReported` into the existing `PlanDaySessionLog` row (already consumed by `GenerateNextMonthPlan`, unchanged) | No |
| AC-6 | Sesi berjalan 100% offline | No network calls anywhere in this feature; all persistence is SQLDelight | **Yes** |
| AC-7 (alt, UC-1) | Pause → state tersimpan, app di-kill, bisa dilanjut kapan pun | Same mechanism as AC-2; Pause performs no extra write, it's just navigating away with state already durable | **Yes** |
| AC-8 (alt, UC-1) | Keluar di tengah → sesi tersimpan partial, bisa dilanjut atau diakhiri | "End Session Now" (new UX, Edge Case #3) finalizes early via `EndSessionEarly` usecase; leaving via Pause instead keeps it `IN_PROGRESS` and resumable | **Yes** |
| AC-9 (alt, UC-3) | Tutup tanpa isi RPE → sesi tetap tersimpan, RPE kosong (tidak memblokir) | `WorkoutSession`/`PlanDaySessionLog.completedAt` is written at session finish regardless of RPE; RPE itself is a separate, optional, later write from P-06's "Simpan" | No |

## 5. Domain Model

```kotlin
// domain/ptmode/entity/WorkoutSession.kt
enum class WorkoutSessionStatus { IN_PROGRESS, COMPLETED }

data class WorkoutSession(
    val id: String,
    val planDayId: String,
    val startedAt: Instant,
    val completedAt: Instant?,
    val status: WorkoutSessionStatus,
    /** Index into PlanDay.mainExercises; drives which exercise P-05 shows on resume. */
    val currentExerciseIndex: Int,
    val currentSetNumber: Int,
    /** Wall-clock target for the active rest countdown; null when not resting. AC-1. */
    val restEndAt: Instant?,
)

// domain/ptmode/entity/WorkoutExerciseLog.kt
/** One row per exercise slot actually attempted this session -- carries substitution + skip state (AC-3, AC-4). */
data class WorkoutExerciseLog(
    val id: String,
    val workoutSessionId: String,
    /** Stable position in PlanDay.mainExercises, independent of substitution. */
    val plannedExerciseIndex: Int,
    /** Actual exercise performed -- equals the plan's target unless swapped (AC-3). */
    val exerciseId: String,
    val skippedAt: Instant?,
)

// domain/ptmode/entity/WorkoutSetLog.kt
/** One row per confirmed set (DoD: "Log otomatis tersimpan per set"). */
data class WorkoutSetLog(
    val id: String,
    val workoutExerciseLogId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val completedAt: Instant,
)

// domain/ptmode/entity/PersonalRecord.kt (pure derived value, not persisted separately)
data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val newWeightKg: Double,
    val previousBestKg: Double?,
)

// domain/ptmode/entity/WorkoutSummary.kt
data class WorkoutSummary(
    val totalVolumeKg: Double,
    val durationMinutes: Int,
    val completedExerciseCount: Int,
    val totalExerciseCount: Int,
    val personalRecords: List<PersonalRecord>,
)
```

**Business rules**
- Exactly one `WorkoutSession` per `(planDayId, status=IN_PROGRESS)` — `GetOrCreateWorkoutSession` looks up an existing `IN_PROGRESS` row before creating one, so pressing Start on an already-started day resumes rather than duplicates.
- Volume = `Σ weightKg × reps` across all `WorkoutSetLog` rows belonging to the session (skipped exercises simply have fewer/no set rows — no separate exclusion logic needed).
- PR = for each distinct `exerciseId` logged this session, `max(weightKg)` compared against `max(weightKg)` from all `WorkoutSetLog` rows for that `exerciseId` from **prior** sessions (`completedAt` before this session's `startedAt`). Emitted only when the new max strictly exceeds the previous max (or no previous history exists → **not** a PR by this definition, first-ever lift isn't a "record" over nothing; confirm at `/code` time if a different first-time treatment is wanted).
- `restSeconds` addition to `PlannedExercise` (nullable): PT Mode falls back to a 90s default when a plan doesn't set it (Phase 1 decision).

## 6. Architecture Breakdown

```
domain/ptmode/
  entity/ WorkoutSession.kt, WorkoutExerciseLog.kt, WorkoutSetLog.kt, PersonalRecord.kt, WorkoutSummary.kt
  repository/ PtModeRepository.kt

usecase/ptmode/
  GetOrCreateWorkoutSession.kt   -- AC-2/AC-7: resume-or-start
  ConfirmSet.kt                  -- writes WorkoutSetLog, advances currentSetNumber/currentExerciseIndex, sets restEndAt (AC-1)
  SkipRestTimer.kt                -- clears restEndAt early
  SkipExercise.kt                 -- writes WorkoutExerciseLog.skippedAt, advances to next exercise (AC-4)
  SwapExercise.kt                  -- updates WorkoutExerciseLog.exerciseId for the current slot (AC-3), delegates alternatives list to existing GetExerciseAlternatives
  EndSessionEarly.kt               -- marks remaining un-attempted exercises skipped, finalizes session (AC-8)
  GetWorkoutSummary.kt              -- derives WorkoutSummary (volume/duration/PRs) from logs, pure read
  SaveWorkoutSummary.kt             -- writes PlanDaySessionLog.completedAt + rpeReported (AC-5, AC-9), marks WorkoutSession COMPLETED

data/ptmode/
  model/ (row mappers colocated in datasource, mirrors monthlyplan convention -- no separate DTOs needed, this is local-only, no remote/DTO boundary)
  datasource/ PtModeLocalDataSource.kt
  repository/ PtModeRepositoryImpl.kt

presentation/ptmode/
  viewmodel/ PtModeViewModel.kt (State: Loading/Active/Error; Event: ConfirmSet, SkipRest, SkipExercise, SwapExercise(exerciseId), EndSessionEarly, Pause)
  viewmodel/ WorkoutSummaryViewModel.kt (State: Loading/Loaded(WorkoutSummary)/Error; Event: SelectRpe(Int), Save, Close)
  screen/ PtModeScreen.kt (P-05)
  screen/ WorkoutSummaryScreen.kt (P-06)
  component/ RestTimerOverlay.kt, SetInputSteppers.kt, ExerciseSwapSheet.kt, PrBanner.kt, RpeSlider.kt

core/keepawake/  (new, small expect/actual per architecture.md §6)
  KeepScreenOn.kt (expect @Composable fun KeepScreenOn())
  KeepScreenOn.android.kt (actual -- Activity Window FLAG_KEEP_SCREEN_ON via LocalView/LocalContext, DisposableEffect to clear on dispose)
  KeepScreenOn.ios.kt (actual -- UIApplication.shared.isIdleTimerDisabled, DisposableEffect to restore on dispose)
```

Mirrors: `SessionDetailViewModel`'s `load(planDayId)` pattern for the load flow; `GetExerciseAlternatives` reused as-is for swap candidates; `PlanDaySessionLog` write pattern (`insertPlanDaySessionLog` upsert) mirrored for the new tables' upsert-style inserts.

## 7. API / Data Contract

### SQLDelight (all new, one table per file, `packageName` = `com.fghilmany.nufitai.db` per existing `build.gradle.kts`)

**`WorkoutSession.sq`** → `workoutSessionQueries`
```sql
CREATE TABLE workout_session (
    id TEXT NOT NULL PRIMARY KEY,
    plan_day_id TEXT NOT NULL,
    started_at INTEGER NOT NULL,
    completed_at INTEGER,
    status TEXT NOT NULL,
    current_exercise_index INTEGER NOT NULL,
    current_set_number INTEGER NOT NULL,
    rest_end_at INTEGER,
    FOREIGN KEY (plan_day_id) REFERENCES plan_day(id)
);
```
Queries: `insertWorkoutSession` (upsert), `getInProgressSessionForDay` (`WHERE plan_day_id = ? AND status = 'IN_PROGRESS'`), `updateSessionProgress`, `completeSession`.

**`WorkoutExerciseLog.sq`** → `workoutExerciseLogQueries`
```sql
CREATE TABLE workout_exercise_log (
    id TEXT NOT NULL PRIMARY KEY,
    workout_session_id TEXT NOT NULL,
    planned_exercise_index INTEGER NOT NULL,
    exercise_id TEXT NOT NULL,
    skipped_at INTEGER,
    FOREIGN KEY (workout_session_id) REFERENCES workout_session(id),
    FOREIGN KEY (exercise_id) REFERENCES exercise(id)
);
```
Queries: `insertWorkoutExerciseLog` (upsert, keyed by `(workout_session_id, planned_exercise_index)` for swap-in-place updates), `getExerciseLogsForSession`, `markExerciseSkipped`.

**`WorkoutSetLog.sq`** → `workoutSetLogQueries`
```sql
CREATE TABLE workout_set_log (
    id TEXT NOT NULL PRIMARY KEY,
    workout_exercise_log_id TEXT NOT NULL,
    set_number INTEGER NOT NULL,
    weight_kg REAL NOT NULL,
    reps INTEGER NOT NULL,
    completed_at INTEGER NOT NULL,
    FOREIGN KEY (workout_exercise_log_id) REFERENCES workout_exercise_log(id)
);
```
Queries: `insertWorkoutSetLog`, `getSetLogsForSession` (join through `workout_exercise_log`), `getSetLogsForExercise` (by `exercise_id`, across all sessions, for PR history lookups — join needed since `exercise_id` lives on the parent row).

**`PlannedExercise.sq` — schema change (existing table)**: add `rest_seconds INTEGER` (nullable) column; update `insertPlannedExercise` to accept the new column. Per `architecture.md` §4b, this is a schema change to a shipped table — in this early-dev repo (single migration-free schema so far) a DB recreation/version bump is acceptable rather than a `.sqm` migration; confirm with the dev at `/code` time which they prefer.

### DI (Koin, manual DSL per ADR-001)
New `ptModeModule` (mirrors `monthlyPlanModule`'s shape):
```kotlin
val ptModeModule = module {
    single { PtModeLocalDataSource(database = get()) }
    single<PtModeRepository> { PtModeRepositoryImpl(local = get()) }

    factory { GetOrCreateWorkoutSession(repository = get()) }
    factory { ConfirmSet(repository = get()) }
    factory { SkipRestTimer(repository = get()) }
    factory { SkipExercise(repository = get()) }
    factory { SwapExercise(repository = get(), getExerciseAlternatives = get()) }
    factory { EndSessionEarly(repository = get()) }
    factory { GetWorkoutSummary(repository = get()) }
    factory { SaveWorkoutSummary(repository = get(), monthlyPlanRepository = get()) }

    viewModel { PtModeViewModel(getOrCreateWorkoutSession = get(), confirmSet = get(), skipRestTimer = get(), skipExercise = get(), swapExercise = get(), endSessionEarly = get(), getExercisePool = get()) }
    viewModel { WorkoutSummaryViewModel(getWorkoutSummary = get(), saveWorkoutSummary = get()) }
}
```
Registered alongside the other feature modules in `KoinInit.kt`.

### Navigation
- Replace the existing `Route.PtModeStub` placeholder with `Route.PtMode(val planDayId: String)`.
- New `Route.WorkoutSummary(val workoutSessionId: String)`.
- `NavGraph.kt`: `Home`'s and `SessionDetail`'s `onStartSession` callbacks (already typed `(planDayId: String) -> Unit`, currently ignored/hardcoded to `Route.PtModeStub`) now navigate to `Route.PtMode(planDayId)`.
- `PtModeScreen`'s "session complete" transition (after the last exercise's last set) navigates to `Route.WorkoutSummary(workoutSessionId)`, popping `Route.PtMode` off the stack (can't navigate back into a finished guided session).
- `WorkoutSummaryScreen`'s "Simpan"/"Tutup" both pop back to `Route.Home` (matches P-06's mock context — Plans/Home tab visible underneath).

## 8. State & Error Handling

**PtModeState**
- `Loading` — resolving/creating the `WorkoutSession` + loading `PlanDay`/exercise pool.
- `Active(session, currentExercise, currentPlanned, exercisesById, restRemainingSeconds: Int?)` — restRemainingSeconds is `null` when not resting, else derived live from `restEndAt - now`.
- `Completed(workoutSessionId)` — triggers navigation to P-06.
- `Error(message)`.

**WorkoutSummaryState**
- `Loading`, `Loaded(summary, selectedRpe: Int?)`, `Saved` (triggers nav pop), `Error(message)`.

**Error mapping**: standard `AppResult`/`Failure` throughout, same as every other feature (`Failure.Database` on SQLDelight errors — no network calls in this feature so `Failure.Network`/`Unauthorized` don't apply).

**Empty/edge states**
- No `PlanDay` found for the given `planDayId` (deep-link race, plan archived mid-session) → `Error` state with a "session no longer available" message, no crash.
- `mainExercises` empty or null for the target day → shouldn't be reachable (Start CTA only renders for exercise days per existing `SessionCard`/`GetSessionStatus`), defensive `Error` state if it happens anyway.

## 9. Edge Case Decisions

| # | Item | Decision | How handled |
|---|---|---|---|
| 1 | Rest-timer duration source | Handle | `PlannedExercise.restSeconds: Int?` added; PT Mode defaults to 90s when null |
| 2 | Default weight/reps, no prior value | Handle | Fallback chain: last logged actual for that exercise (any past session, via `getSetLogsForExercise`) → 0kg / plan's target rep count |
| 3 | Explicit "End Session Now" (not in mock) | Handle | New exit icon on P-05 top bar → confirm dialog (Continue Later = pause / End Now = `EndSessionEarly`) |
| 4 | Mid-session resume granularity | Handle | Resume from last **completed set**; unsaved draft stepper values reset to defaults on resume (no per-keystroke persistence) |
| 5 | Stepper increments (BEBAN/REPETISI) | Handle | 2.5kg / 1 rep |
| 6 | PR definition + multiple PRs per session | Handle | Max single-set weight per exercise vs. prior best; all PRs this session shown as stacked banners |
| 7 | Skip granularity (exercise vs. set) | Handle | Skip abandons the rest of the *current* exercise; sets already logged before the skip remain counted in volume |
| 8 | Rest timer accuracy in background | Handle | Wall-clock `restEndAt: Instant` target, recomputed on every tick — no foreground service needed |
| 9 | Keep-awake | Handle | New small expect/actual `KeepScreenOn()`, composed only while P-05 is on screen |
| 10 | Warmup/cooldown in guided flow | Defer | Not shown to the user in PT Mode this pass; `mainExercises` only, matches existing duration/count helpers |
| 11 | Pain reporting on P-06 | Defer | `PlanDaySessionLog.painReported` stays null; column already exists for a future pass |
| 12 | Share icon on P-06 | Defer | Not wired to any action this pass |
| 13 | RPE feeding next-month progression | Ignore (already exists) | `SaveWorkoutSummary` only needs to correctly persist into the existing `PlanDaySessionLog.rpeReported`; the consuming engine (`GenerateNextMonthPlan`) is unchanged |
| 14 | One `IN_PROGRESS` `WorkoutSession` per plan day | Handle | `GetOrCreateWorkoutSession` looks up before creating — prevents duplicate rows from repeated Start taps |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] Layar tetap menyala (keep-awake) selama sesi aktif (P-05 only, released on navigation away)
- [ ] Rest timer tetap akurat setelah app di-background lalu di-foreground-kan lagi (wall-clock-anchored, no drift)
- [ ] Log tersimpan per SET (`workout_set_log`), bukan hanya per sesi
- [ ] AC-1 through AC-9 above all implemented and traceable to the table in §4
- [ ] `GetOrCreateWorkoutSession` prevents duplicate `IN_PROGRESS` rows for the same `planDayId`
- [ ] Volume/PR calculations covered by usecase-level unit tests (pure functions, `★★★` priority per `testing.md`)
- [ ] `checkModules()` green with `ptModeModule` registered
- [ ] `PlannedExercise.restSeconds` schema addition confirmed with dev (recreate vs. `.sqm` migration) before implementation
