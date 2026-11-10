# TechSpec: Monthly Plan

> Source US: [fghilmany/NuFit-AI#16](https://github.com/fghilmany/NuFit-AI/issues/16)
> Rule engine spec: [#29 Algoritma Rules: Formula Latihan](https://github.com/fghilmany/NuFit-AI/issues/29)
> Program design principles: [#19 Tahap 7](https://github.com/fghilmany/NuFit-AI/issues/19), [#18 Tahap 7B: Algoritma Generate Program 30 Hari](https://github.com/fghilmany/NuFit-AI/issues/18)
> Worked example: [#28 Contoh Skenario Lengkap: Rina](https://github.com/fghilmany/NuFit-AI/issues/28)
> Exercise catalog: issues labeled `exercise` (#33-#74), sourced from `Gym Techniques/`; images from [free-exercise-db](https://github.com/yuhonas/free-exercise-db) per Keputusan #6
> Parent epic: [#4 Gym Flow](https://github.com/fghilmany/NuFit-AI/issues/4)

## 1. Overview & Goal

The user sees a 4-week (30-day) training plan on Home (P-03), taps into any session's detail (P-04), and the plan's content, safety filtering, and progression come from a rule-based engine — never manual authoring, never ML. The screens are identical across tiers; only the plan's **source** differs:

- **Local tier**: template matrix (Level × Goal × Split) — reuses the already-shipped `QuickAssessmentResult`, no new user input.
- **Logged-In tier**: the full `tahap-7b` rule engine (8-layer pipeline from #29, 8-step generator from #18) — consumes a **Full Assessment** (postural/movement/capacity screening) that does not exist yet as a data-collection flow.

Both tiers draw from the same Exercise Library, seeded in this pass from the `Gym Techniques` catalog (all 7 equipment categories, ~130 exercises).

This techspec covers **both tiers now** (dev decision) plus a **stubbed Full Assessment** — a minimal data-collection flow limited to exactly the fields the rule engine's `input_schema` needs, not the full P-11 experience described in `06-ai-custom-plan.md` (which also feeds AI Custom Plan and can be expanded later without re-architecting).

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Status |
|---|---|---|
| P-03 Home — Monthly Plan | [node-id=12-539](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-539) | As designed. Dev-supplied screenshot matches: header (avatar/app name/bell), greeting, week progress bar, "Sesi Minggu Ini" list (done/today-highlighted/rest-day/upcoming cards), "Jelajahi Gerakan" quick-access, collapsed week accordions (Minggu 1/3/4), bottom nav 5 tabs |
| P-04 Session Detail | [node-id=12-382](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-382) | As designed. Dev-supplied screenshot matches: header (back/title/overflow menu), duration+exercise-count summary chip, session description, exercise cards (thumbnail, set×rep, beban, rest), "Saran NuFit AI" tip card, sticky "Mulai Sesi Ini" CTA |
| Week-complete / next-week-preview state (UC-1 alternate flow) | **Not designed** | No Figma frame or screenshot provided. Build as a simple card-based treatment consistent with the session-card visual language already established (see §9 item 9) |
| Rest-day homework section (corrective-exercise overflow, §5 SAFE-09) | **Not designed** | Lightweight addition to the existing "Rest Day" card on P-03, not a new screen (see §9 item 6) |
| Full Assessment stub (postural/movement/capacity data collection) | **Not designed** | No P-11 design exists yet. Build as a plain sequential form reusing the Quick Assessment wizard's card/stepper visual pattern (`presentation/onboarding/component`) — functional, not polished; full P-11 UX is `06-ai-custom-plan.md`'s job later |

Session cards on P-04 must render an exercise thumbnail — sourced from the `media` mapping to free-exercise-db per Keputusan #6 (§5, §9 item 4).

## 3. Scope

**In scope**
- P-03 Home: 4-week plan view, weekly accordion, today-highlighted session, week-complete state
- P-04 Session Detail: exercise list with warm-up/main/cardio/cool-down structure, tap-through to Exercise Detail (stub navigation only — Exercise Library screens P-07/P-08 are a separate feature)
- Exercise Library **data seed**: all 7 equipment categories from `Gym Techniques`, ~130 exercises with full metadata (`pola_gerak`, `level`, `flag_exclusion`, `flag_prioritas`, `rantai_regresi_progresi`, `substitusi_setara`, `area_terbebani` where available, `media` mapping)
- Full Assessment **stub**: minimal data collection for PAR-Q Kategori B/postural/movement/capacity fields the rule engine needs (§5 input_schema), gated by the same GATE-01 hard-stop logic already shipped for Quick Assessment's PAR-Q
- Local tier plan generation: template matrix, reusing `QuickAssessmentResult`, no flags beyond the existing PAR-Q-ringkas gate
- Logged-In tier plan generation: full `tahap-7b` 8-layer/8-step rule engine (#29, #18), pure `usecase`/`domain` logic, unit-tested against the Rina scenario (#28)
- Session status (done/skipped/upcoming) derived from a minimal session-level log (completedAt/skippedAt) — **not** full per-set logging, which belongs to PT Mode (`05-pt-mode.md`, next in build order)
- Tier transition: Local → Logged-In plan replacement when Full Assessment completes (archive-and-activate pattern)
- Progression gating (checkpoint days 14/21, or 21/28 in conservative mode) per pola gerak

**Out of scope** (explicitly deferred)
- Exercise Library browse/detail screens (P-07/P-08) — separate feature, `04-exercise-library.md`
- PT Mode / Guided Workout execution (P-05/P-06) — separate feature, `05-pt-mode.md`; the "Mulai Sesi Ini" / "Mulai Latihan" CTAs navigate to a stub route for now
- Full P-11 Full Assessment UX (AI Custom Plan intent, injury history free text beyond flags, self-test video capture) — `06-ai-custom-plan.md`
- Re-assessment flow UI (Tahap 9) — triggered as a reminder only, the re-assessment screens themselves are future work
- Resistance Band equipment category — `Gym Techniques` explicitly has no database for it yet (source-confirmed gap, not this techspec's gap)
- Progression-gating anti-cheat / plausibility checks on logged reps/RPE (§9 item 2, deferred per dev decision)
- Timezone/DST-aware calendar day boundaries (§9 item 5, deferred per dev decision)

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin melihat plan latihan 4 minggu dengan progression otomatis dan plan bulan berikutnya dibuat dari performa saya, sehingga saya selalu tahu apa yang harus dilakukan setiap kali ke gym tanpa mengulang assessment.

**AC-1** Given plan aktif, when membuka Home, then 4 minggu penuh terlihat dengan status per sesi (dihitung dari log, bukan disimpan manual).
**AC-2** Given sesi minggu depan, when dibuka, then detail tetap bisa dilihat (tidak dikunci).
**AC-3** Given minggu 3-4, when dibandingkan minggu 1-2, then ada progression sesuai aturan template/rule engine.
**AC-4** Given bulan selesai (hari 30/31), when membuka Home, then plan bulan baru tersedia dengan penjelasan perubahan.
**AC-5** Given kehadiran rendah (<50%), when plan baru dibuat, then intensitas tidak dinaikkan.
**AC-6** Given user Local tier menyelesaikan Full Assessment, when selesai, then plan Local diarsipkan dan plan Logged-In (tahap-7b) menjadi aktif — mengikuti pola arsip di `03-assessment-detail.md` UC-3.
**AC-7** Given kolam gerakan kosong untuk satu pola_gerak setelah filter alat+flag, when program dirakit, then sistem fallback ke `substitusi_setara`/pola terdekat, tidak pernah membiarkan slot kosong tanpa penjelasan ke user.
**AC-8** Given ≥1 flag PAR-Q Kategori A aktif (GATE-01) saat Full Assessment, when user mencoba lanjut, then diblokir sampai konfirmasi eksplisit — sama seperti gate Quick Assessment yang sudah ada.
**AC-9** Given user absen >14 hari, when membuka Home, then kalender dibekukan dan sistem menawarkan re-assessment ringan (tidak auto-geser tanggal).
**AC-10** Given user mengganti preferensi alat di tengah siklus, when disimpan, then hanya hari tersisa yang dibangun ulang; `starting_level_per_pola` dan riwayat log sebelumnya tidak berubah.

### AC Traceability Table

| AC | Handled by | Blocking? |
|---|---|---|
| AC-1 | `usecase/monthlyplan/GetSessionStatus.kt` (derivation, not stored) + `presentation/monthlyplan` P-03 | No |
| AC-2 | Navigation: no lock on future-week session taps; `presentation/monthlyplan/screen/SessionDetailScreen.kt` | No |
| AC-3 | `usecase/monthlyplan/rules/GatingProgresi.kt` (PROG-01..06) for Logged-In; template's fixed fase table for Local | No |
| AC-4 | `usecase/monthlyplan/GenerateNextMonthPlan.kt` — triggers on day 30/31, reads log summary | No |
| AC-5 | Same as AC-4 — attendance/RPE read from log summary before intensity decision | No |
| AC-6 | `usecase/monthlyplan/ActivatePlanFromFullAssessment.kt`, mirrors `RetakeAssessment` archive pattern | No |
| AC-7 | `usecase/monthlyplan/rules/BangunKolamGerakan.kt` (POOL-01..05) | **BLOCKING** — safety/completeness, never silently omit |
| AC-8 | `usecase/fullassessment/SubmitFullAssessmentParQ.kt`, reuses `HealthFlag`/GATE-01 logic from onboarding | **BLOCKING** — safety gate |
| AC-9 | `usecase/monthlyplan/rules/GatingProgresi.kt` (PROG-04) | No |
| AC-10 | `usecase/monthlyplan/RebuildRemainingDays.kt` (Tahap 7B section E) | No |

## 5. Domain Model

### Exercise Library entities (`domain/exerciselibrary`)

```
Exercise
  id: String                        // e.g. "BW-SQUAT-001" — matches Gym Techniques catalog IDs
  name: String
  equipmentCategory: EquipmentCategory   // BODYWEIGHT | DUMBBELL | BARBELL | KETTLEBELL | MACHINE_CABLE | PULL_UP_BAR | CARDIO_EQUIPMENT
  movementPattern: MovementPattern       // SQUAT | HINGE | PUSH_HORIZONTAL | PUSH_VERTICAL | PULL_HORIZONTAL | PULL_VERTICAL | LUNGE | CORE | CARRY | CORRECTIVE | STRETCH | CARDIO
  level: ExerciseLevel                    // REGRESI | STANDAR | PROGRESI (+ numbered variants, e.g. "Regresi 2")
  flagExclusion: Set<HealthOrMovementFlag>
  flagPrioritas: Set<HealthOrMovementFlag>
  areaTerbebani: Set<BodyArea>?           // punggung_bawah | lutut | bahu | leher; nullable where source data incomplete (§9 item 3)
  substitusiSetara: Map<EquipmentCategory, String>?  // equivalent-level exercise IDs across categories
  rantaiRegresi: String?                  // exercise ID, same category
  rantaiProgresi: String?                 // exercise ID, same category
  polaGerakTerkait: Set<MovementPattern>?  // Stretch category only — used by COOL-02
  mediaExerciseDbSlug: String?            // free-exercise-db mapping key; null = placeholder illustration

MovementPattern, EquipmentCategory, ExerciseLevel, BodyArea — enums, one source of truth shared by both tiers' rule engines
```

### Assessment entities (`domain/fullassessment`)

```
FullAssessmentResult
  id, completedAt: Instant
  profil: ProfilRingkas                    // usia, gender — reused from existing "Lengkapi Profil" if present
  parQKategoriB: Set<HealthFlag>            // reuses onboarding's HealthFlag enum, extended with GATE-02 conservative-mode flags
  preferensiAlat: Set<EquipmentCategory>
  riwayatCedera: Set<BodyArea>              // maps to SAFE-10 soft-block
  flagsPostural: Set<PosturalFlag>          // forward_head, rounded_shoulder, kyphosis, apt, ppt, asimetri_bahu, asimetri_pinggul
  flagsGerak: Set<MovementScreeningFlag>    // ankle_mobility, knee_valgus, hinge_from_back, shoulder_mobility, core_instability, balance_asimetri
  capacityTest: CapacityTestResult?         // nullable — CAL-05: skip -> Regresi default

CapacityTestResult
  pushup: PushupResult?      // reps, versi (standar|lutut), protokol (maximal|submaximal_rpe7)
  plankDetik: PlankResult?   // nilai, protokol
  sitToStandReps: SitToStandResult?  // nilai, protokol (maximal|tempo_terkontrol)
```

### Program entities (`domain/monthlyplan`)

```
MonthlyPlan
  id, startedAt: Instant, cycleNumber: Int   // 1, 2, 3... each 30-day cycle
  source: PlanSource                          // LOCAL_TEMPLATE | LOGGED_IN_RULE_ENGINE
  status: PlanStatus                          // ACTIVE | ARCHIVED
  levelMeta: String, goalMeta: GoalCategory, smartGoalMeta: String?
  flagsAktif: Set<String>                      // union of all active flags at generation time, for "kenapa saya dapat gerakan ini?"
  startingLevelPerPola: Map<MovementPattern, ExerciseLevel>
  mode: ProgressionMode                        // NORMAL | KONSERVATIF (drives checkpoint days per PROG-06)
  checkpointDays: List<Int>                    // [14,21] normal, [21,28] konservatif

PlanDay
  id, planId: String, dayNumber: Int (1..30)
  type: DayType                                // SESSION | REST | LIGHT_ACTIVITY
  templateLetter: String?                      // "A" | "B" | "C" for rotation reference
  warmup: WarmupBlock?, mainExercises: List<PlannedExercise>?, cardio: CardioBlock?, cooldown: CooldownBlock?
  homework: List<PlannedExercise>?             // corrective overflow (SAFE-09), only on REST days

PlannedExercise
  exerciseId: String, sets: Int, repRangeOrDuration: String, rpeTarget: IntRange
  reasonRuleIds: List<String>                  // e.g. ["SAFE-02", "CAL-01"] — transparency principle from #29

PlanDaySessionLog   // minimal, session-level only — full per-set logging is PT Mode's domain
  id, planDayId: String
  completedAt: Instant?, skippedAt: Instant?
  rpeReported: Int?, painReported: Set<BodyArea>?
```

### Business Rules — Local tier (template matrix)

Reuses `QuickAssessmentResult` (already persisted). No flags beyond the existing PAR-Q-ringkas gate exist at this tier, so:
- Skip layers 1 (GATE, already applied at Quick Assessment time), 3 (SAFETY filter — no postural/movement flags collected), 5 (CALIBRATION — no capacity test), 7 (GATING progression — see below).
- Apply layer 2 (POOL — build from `QuickAssessmentResult`'s implied equipment, which defaults to Bodyweight-only since Quick Assessment doesn't collect equipment preference), layer 4 (RESEP from goal), layer 6 (JADWAL, using `resolvedSplit` from Quick Assessment as the split structure instead of layer 6's own frequency×fokus matrix).
- `starting_level_per_pola` fixed: every pattern starts at `Regresi` if `level == Beginner`, `Standar` if `level == Intermediate` (CAL-05's "skip test = safest default" logic, applied uniformly since no test exists at this tier).
- No progression gating (layer 7) — fixed fase table only (adaptasi hari 1-14, progresi 15-28, buffer 29-30), same for every Local-tier user. This is the "template" in "template matrix": content is fixed once starting level is set, no per-user rep/RPE-driven branching.

### Business Rules — Logged-In tier (tahap-7b full engine)

Implements the full pipeline from #29 verbatim: `gate_kesehatan → bangun_kolam_gerakan → filter_keamanan → resep_goal → kalibrasi_starting_level → penjadwalan_30_hari → gating_progresi`. Rule IDs (GATE-*, POOL-*, SAFE-*, CAL-*, PROG-*, WARM-*, COOL-*) map 1:1 to #29's JSON spec — implement as named functions/objects per rule ID so `reasonRuleIds` stays traceable and unit tests can target individual rules (e.g. `SafetyFilterTest.safe01_hingeFromBack_excludesLoadedDeadlifts()`).

Key structural points carried into the domain model:
- **CAL-01 wins over test scores**: an active `flag_exclusion` for a pattern forces `starting_level[pattern] = Regresi`, regardless of capacity test result.
- **Per-pattern independence**: `startingLevelPerPola` and `checkpointDays` gating (PROG-01/02) operate per `MovementPattern`, never as one global level.
- **SAFE-09 corrective budget**: max 3 corrective exercises per session's warm-up; overflow becomes `PlanDay.homework` on the nearest rest day, never dropped.
- **Conservative mode (PROG-06)**: `mode = KONSERVATIF` when `GATE-02` fired OR `flagsAktif.size >= 3`; shifts `checkpointDays` to `[21,28]` and extends adaptasi phase to day 21 (`override_konservatif` in layer 6).
- **Session structure is mandatory for every user**: warm-up (umum + spesifik + korektif) and cool-down (penurunan HR + stretch) generate for everyone, not just flagged users — only WARM-03 (korektif) depends on flags.

### Business Rules — Tier transition (Local → Logged-In)

Full Assessment completion (not login alone) triggers replacement: archive the active Local plan (log history untouched, per `03-assessment-detail.md`'s established archive pattern), generate and activate a new Logged-In plan from the freshly completed `FullAssessmentResult`.

### Business Rules — Full Assessment GATE-01 (safety, BLOCKING)

Reuses the exact hard-stop semantics already shipped for Quick Assessment's PAR-Q ringkas gate (`SubmitParQAnswers` / `HealthFlag` logic in `usecase/onboarding`) — do not reimplement a weaker or divergent version. GATE-01 triggers (chest pain, fainting during activity, severe breathlessness at rest, recent major surgery unhealed, active cancer treatment, pregnancy) block progression until explicit consult-doctor acknowledgment, identical UX pattern to the existing `DoctorConsultScreen`.

## 6. Architecture Breakdown

Three feature slugs, since they have independent lifecycles and dependency directions: `exerciselibrary` (foundational, no dependencies), `fullassessment` (depends on `exerciselibrary` only for validation, not generation), `monthlyplan` (depends on both).

```
shared/src/commonMain/kotlin/
├── domain/exerciselibrary/
│   ├── entity/Exercise.kt, MovementPattern.kt, EquipmentCategory.kt, ExerciseLevel.kt, BodyArea.kt
│   └── repository/ExerciseLibraryRepository.kt
├── domain/fullassessment/
│   ├── entity/FullAssessmentResult.kt, PosturalFlag.kt, MovementScreeningFlag.kt, CapacityTestResult.kt
│   └── repository/FullAssessmentRepository.kt
├── domain/monthlyplan/
│   ├── entity/MonthlyPlan.kt, PlanDay.kt, PlannedExercise.kt, PlanDaySessionLog.kt, ProgressionMode.kt
│   └── repository/MonthlyPlanRepository.kt
│
├── usecase/exerciselibrary/
│   └── GetExercisePool.kt          // filter by equipment+flags, shared by both tiers
├── usecase/fullassessment/
│   ├── SubmitFullAssessmentParQ.kt // GATE-01/02, reuses onboarding's flag logic
│   ├── SubmitFullAssessmentInterview.kt, SubmitPosturalScreening.kt, SubmitMovementScreening.kt, SubmitCapacityTest.kt
│   └── CompleteFullAssessment.kt   // finalizes FullAssessmentResult, triggers ActivatePlanFromFullAssessment
├── usecase/monthlyplan/
│   ├── rules/  // one object per rule-ID family, named to match #29 exactly
│   │   ├── BangunKolamGerakan.kt      (POOL-01..05)
│   │   ├── FilterKeamanan.kt          (SAFE-01..18)
│   │   ├── ResepGoal.kt               (layer 4 mapping table)
│   │   ├── KalibrasiStartingLevel.kt  (CAL-01..08)
│   │   ├── PenjadwalanBulan.kt        (layer 6, WARM-*, COOL-*)
│   │   └── GatingProgresi.kt          (PROG-01..06)
│   ├── GenerateLocalTemplatePlan.kt   // Local tier: simplified pipeline
│   ├── GenerateLoggedInPlan.kt        // Logged-In tier: full 8-step pipeline
│   ├── ActivatePlanFromFullAssessment.kt
│   ├── GetSessionStatus.kt            // pure derivation from PlanDaySessionLog, unit-test-heavy
│   ├── GenerateNextMonthPlan.kt       // day 30/31 trigger
│   ├── RebuildRemainingDays.kt        // Tahap 7B section E, equipment-change mid-cycle
│   └── FreezeCalendarOnAbsence.kt     // PROG-04
│
├── data/exerciselibrary/
│   ├── datasource/ExerciseLibrarySeedDataSource.kt   // ships seed data compiled from Gym Techniques catalog
│   └── repository/ExerciseLibraryRepositoryImpl.kt
├── data/fullassessment/
│   ├── datasource/FullAssessmentLocalDataSource.kt
│   └── repository/FullAssessmentRepositoryImpl.kt
├── data/monthlyplan/
│   ├── datasource/MonthlyPlanLocalDataSource.kt
│   └── repository/MonthlyPlanRepositoryImpl.kt
│
└── di/ExerciseLibraryModule.kt, FullAssessmentModule.kt, MonthlyPlanModule.kt
```

```
composeApp/src/commonMain/kotlin/
├── presentation/monthlyplan/
│   ├── viewmodel/HomeViewModel.kt (extends/replaces the existing placeholder HomeScreen), SessionDetailViewModel.kt
│   ├── screen/HomeScreen.kt (P-03), SessionDetailScreen.kt (P-04)
│   └── component/ SessionCard, WeekAccordion, RestDayCard (+homework section), WeekCompleteCard
├── presentation/fullassessment/
│   ├── viewmodel/FullAssessmentViewModel.kt  // sequential multi-step, mirrors QuickAssessmentViewModel's wizard pattern
│   └── screen/FullAssessmentStubScreen.kt
└── navigation/ (routes below)
```

expect/actual: none needed — pure local Compose + SQLDelight + in-memory rule engine, no platform APIs.

## 7. API / Data Contract

### SQLDelight

All additive — no changes to existing onboarding tables (`ParQResult`, `ParQAnswerDetail`, `QuickAssessmentResult`, `BodyMeasurement`). Package: `com.fghilmany.nufitai.db` (confirmed from `shared/build.gradle.kts`).

| `.sq` file | Table | Generated `*Queries` | Schema status |
|---|---|---|---|
| `Exercise.sq` | `exercise` | `exerciseQueries` | New — seeded at first launch (or build-time asset), not user-writable |
| `FullAssessmentResult.sq` | `full_assessment_result` | `fullAssessmentResultQueries` | New |
| `FullAssessmentFlagDetail.sq` | `full_assessment_flag_detail` | `fullAssessmentFlagDetailQueries` | New — one row per flag (postural/movement/health), mirrors `ParQAnswerDetail`'s one-row-per-answer pattern |
| `MonthlyPlan.sq` | `monthly_plan` | `monthlyPlanQueries` | New |
| `PlanDay.sq` | `plan_day` | `planDayQueries` | New |
| `PlannedExercise.sq` | `planned_exercise` | `plannedExerciseQueries` | New — one row per exercise per plan day |
| `PlanDaySessionLog.sq` | `plan_day_session_log` | `planDaySessionLogQueries` | New — session-level only; PT Mode (`05-pt-mode.md`) later adds per-set logging in its own `.sq` file that references `plan_day_id` |

`exercise` columns: `id (PK, matches catalog ID), name, equipment_category, movement_pattern, level, flag_exclusion (JSON array), flag_prioritas (JSON array), area_terbebani (JSON array, nullable), substitusi_setara (JSON object, nullable), rantai_regresi (nullable FK), rantai_progresi (nullable FK), pola_gerak_terkait (JSON array, nullable), media_slug (nullable)`.

`monthly_plan` columns: `id, started_at, cycle_number, source, status, level_meta, goal_meta, smart_goal_meta (nullable), flags_aktif (JSON array), starting_level_per_pola (JSON object), mode, checkpoint_days (JSON array)`.

`plan_day` columns: `id, monthly_plan_id (FK), day_number, type, template_letter (nullable)`.

`planned_exercise` columns: `id, plan_day_id (FK), slot (warmup_umum|warmup_spesifik|warmup_korektif|main|cardio|cooldown_hr|cooldown_stretch|homework), exercise_id (FK), sets, rep_range_or_duration, rpe_target_min, rpe_target_max, reason_rule_ids (JSON array)`.

`plan_day_session_log` columns: `id, plan_day_id (FK), completed_at (nullable), skipped_at (nullable), rpe_reported (nullable), pain_reported (JSON array, nullable)`.

**Migration note**: since these are all new tables with no existing data, no `.sqm` migration is needed — straightforward schema addition. Confirm no version bump conflicts with onboarding's existing schema before merging (§4c extension checklist step 2 — run the build, confirm `*Queries` names).

### DI

Three new Koin modules (manual DSL per ADR-001), registered the same way `OnboardingModule`/`DatabaseModule` already are:
- `ExerciseLibraryModule`: `ExerciseLibraryRepositoryImpl`, `GetExercisePool`
- `FullAssessmentModule`: `FullAssessmentRepositoryImpl`, all `usecase/fullassessment/*`, `FullAssessmentViewModel`
- `MonthlyPlanModule`: `MonthlyPlanRepositoryImpl`, all `usecase/monthlyplan/*` (including the 6 rule objects if they need DI; pure stateless rule objects may not need Koin registration at all — confirm during `/code` per the mirror-existing-pattern rule), `HomeViewModel`, `SessionDetailViewModel`
- Add/verify all three in `DiVerificationTest.checkModules()`

### Navigation

New routes (type-safe, `@Serializable`):
- `Route.Home` — replaces the current placeholder `HomeScreen`; becomes the real P-03
- `Route.SessionDetail(planDayId: String)` — P-04
- `Route.FullAssessmentStub` — entry from Home upsell or Profile & Settings when tier is Logged-In but no `FullAssessmentResult` exists yet
- `Route.PtModeStub` — placeholder target for "Mulai Sesi Ini"/"Mulai Latihan" CTAs until `05-pt-mode.md` ships; shows a simple "Coming soon" state, does not error

## 8. State & Error Handling

| Screen | States |
|---|---|
| HomeScreen (P-03) | `Loading`, `Loaded(weeks: List<WeekSummary>, todaySessionId: String?, weekCompleteMessage: String?)`, `Empty` (no active plan — should not normally happen post-onboarding, but guard it), `Error(Failure.Database)` |
| SessionDetailScreen (P-04) | `Loading`, `Loaded(planDay: PlanDay, isCompleted: Boolean, actualResults: LoggedResults?)` — `actualResults` populated only when `isCompleted == true`, replacing target values with logged actuals per issue #16's alternate flow, `Error(Failure.Database)` |
| FullAssessmentStubScreen | `Loading`, `Step(...)` per sub-section (interview, postural, movement, capacity test — capacity test is skippable, CAL-05), `GateBlocked(flaggedItems, acknowledged)` — same shape as onboarding's `DoctorConsultScreen` state, `Submitting`, `Error(Failure.Database)` |
| Plan generation (background, triggered from Full Assessment completion or day-30 rollover) | Not screen-owned state — a `AppResult<MonthlyPlan>` returned to the caller; UI shows a loading overlay during generation (§9 item 7 performance concern), then navigates to Home on success or shows `Error(Failure.Database)` on failure |

`Failure.Database` is the only reachable `Failure` subtype (all local, no network calls in this feature). Map SQLDelight exceptions to it in each repository layer, consistent with the onboarding feature's existing pattern.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | `exerciseId` in a planned day could reference a seed ID that's wrong/missing (L1) | **Handle** | Validate every seed `exerciseId` reference (`rantai_regresi`, `rantai_progresi`, `substitusi_setara`, `reasonRuleIds` targets) against the full exercise set at seed-authoring time; runtime lookups return `AppResult.Error(Failure.Database)`, never crash, never render blank |
| 2 | Progression gating trusts self-reported reps/RPE with no plausibility check (L2) | **Defer** | Documented trust boundary (local-first, no anti-cheat this pass); flagged for future soft bounds (reject RPE outside 1-10, reps outside a sane multiple of target) |
| 3 | GATE-01 hard-stop for Full Assessment must match Quick Assessment's, not be reimplemented weaker (L3) | **Handle — BLOCKING** | Full Assessment's PAR-Q Kategori B/GATE-01 logic directly reuses `usecase/onboarding`'s `HealthFlag`/`SubmitParQAnswers` mechanism — see §5 |
| 4 | Local vs Logged-In tier data relationship (G1) | **Handle** | `FullAssessmentResult` is a separate table/entity, not an extension of `QuickAssessmentResult`; Quick Assessment result persists as history even after Full Assessment completes |
| 5 | When exactly does Local plan get replaced by Logged-In plan (G2) | **Handle** | Only on Full Assessment *completion* (not login alone) — see §5 tier transition rule, AC-6 |
| 6 | Local tier template matrix content source (G3) | **Handle** | Reuses the same Exercise Library and layers 4/6 as Logged-In tier, skips layers 1/3/5/7 (no flags beyond PAR-Q-ringkas, no calibration, no gating) — fixed starting level per §5 |
| 7 | `media`/image mapping ownership and missing-image behavior (G4) | **Handle** | Mapping is a seed-time concern (exercise ID → free-exercise-db slug), stored in the `exercise` table's `media_slug` column; missing slug renders a placeholder illustration, never a blocking error |
| 8 | Calendar day boundaries across timezones/DST (G5) | **Defer** | Device-local calendar days via kotlinx-datetime; travel/DST edge cases explicitly out of scope this pass |
| 9 | 30-day rule-engine generation is a heavy synchronous computation (E1) | **Handle** | Runs on a background dispatcher (`Dispatchers.Default`), loading overlay shown; must complete well under a few seconds on low-end devices since it's pure in-memory Kotlin over ~130 exercises, no I/O until the final write |
| 10 | App killed mid-generation, partial write risk (E2) | **Handle** | Generate the full `MonthlyPlan` + all `PlanDay`/`PlannedExercise` rows in memory first, write in a single SQLDelight transaction; nothing partial is ever visible on relaunch |
| 11 | Exercise pool empty for a required pattern after filtering (E3, AC-7) | **Handle — BLOCKING** | POOL-04 fallback chain (`substitusi_setara` from another equipment category, then skip-with-visible-note as last resort); never silently omit a pattern from the schedule without surfacing why |
| 12 | User absent >14 days (E4, AC-9) | **Handle** | `FreezeCalendarOnAbsence` (PROG-04): freeze remaining days, surface a re-assessment offer card on Home; no auto date-shifting |
| 13 | Mid-cycle equipment preference change (E5, AC-10) | **Handle** | `RebuildRemainingDays` reruns only layer 2 (POOL) + downstream for days from the edit point forward; `startingLevelPerPola` and all prior days' history are preserved untouched (Tahap 7B section E) |
| 14 | Corrective-exercise overflow (>3, SAFE-09) has no designated screen | **Handle** | Renders as a "homework" section appended to the existing Rest Day card on P-03 — no new screen, minimal addition to `RestDayCard` component |
| 15 | Session status must be computed, not stored (AC-1) | **Handle** | Single `GetSessionStatus` derivation function in `usecase/monthlyplan`, unit tested, used by both P-03's session cards and P-04's actual-vs-target rendering — one source of truth |
| 16 | New tables alongside already-shipped onboarding schema | **Handle** | Purely additive `.sq` files; zero changes to `ParQResult`/`ParQAnswerDetail`/`QuickAssessmentResult`/`BodyMeasurement`; no migration needed |
| 17 | Week-complete celebratory state + next-week preview has no design | **Handle** | Minimal card-based treatment reusing the existing session-card visual language; flagged for dev's visual review since no Figma/screenshot reference exists (§2) |
| 18 | Session-level log vs full per-set log — how much does *this* techspec own | **Handle** | `PlanDaySessionLog` here is intentionally minimal (completed/skipped/RPE/pain at the session level) — enough to satisfy AC-1/AC-3/AC-5's status/progression needs. Full per-set weight/rep logging is `05-pt-mode.md`'s table, added later with a FK to `plan_day_id`; "Mulai Sesi Ini"/"Mulai Latihan" navigate to a stub route until then (§7) |
| 19 | Resistance Band equipment category has no exercise database at all | **Ignore this pass** | Source-confirmed gap in `Gym Techniques` itself (not something this techspec can close); `EquipmentCategory.RESISTANCE_BAND` is not seeded, POOL rules simply never produce it as an option in the equipment-preference UI for now |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] Aturan progression terdokumentasi selaras `algoritma-rules-formula-latihan.md` (#29), diuji unit test per rule ID (GATE-*, POOL-*, SAFE-*, CAL-*, PROG-*)
- [ ] Template plan (Local) dan hasil rule engine (Logged-In) hanya mereferensi exercise via `exerciseId` dari Exercise Library — tidak ada exercise hardcoded di luar seed
- [ ] Estimasi durasi tampil di setiap kartu sesi
- [ ] Every seed `exerciseId` reference validated against the full exercise set (no phantom IDs) — covered by a seed-integrity test, not just manual review
- [ ] `GetSessionStatus` is a pure function with unit tests covering done/skipped/upcoming/today-highlighted branches
- [ ] `GenerateLoggedInPlan` unit tested end-to-end against the Rina scenario (#28) — same inputs, same expected Week 1-2 session content and flag-driven substitutions
- [ ] `GenerateLocalTemplatePlan` unit tested for all reachable `QuickAssessmentResult` → plan combinations (32 templateIds carried over from `quick-assessment.md` §5)
- [ ] POOL-04 fallback and SAFE-09 corrective-budget overflow both covered by unit tests with an intentionally sparse equipment selection (e.g. Bodyweight-only + 3+ active flags)
- [ ] Tier-transition (`ActivatePlanFromFullAssessment`) verified to archive the Local plan and preserve its log history, mirroring the existing retake-archive test pattern from onboarding
- [ ] `DiVerificationTest.checkModules()` passes with `ExerciseLibraryModule`, `FullAssessmentModule`, `MonthlyPlanModule` registered
- [ ] Plan generation runs off the main dispatcher and completes within a few seconds in a manual low-end-device check
- [ ] GATE-01 hard-stop in Full Assessment verified to block progression identically to the existing Quick Assessment PAR-Q gate (shared test fixtures where practical)
