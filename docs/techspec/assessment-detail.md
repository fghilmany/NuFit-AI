# TechSpec: Assessment Detail

> Source US: [fghilmany/NuFit-AI#14](https://github.com/fghilmany/NuFit-AI/issues/14)
> Depends on (read, not modified): `docs/techspec/quick-assessment.md`, `docs/techspec/monthly-plan.md`
> Referenced by: `usecase/monthlyplan/ActivatePlanFromFullAssessment.kt`'s docstring already calls this doc's UC-3 archive pattern by name — this techspec formalizes that reference

## 1. Overview & Goal

The user opens P-09 (from Home or, once it exists, Profile & Settings) and sees why their plan looks the way it does: their level, the plain-language reasoning behind it, any safety (PAR-Q) flags and what they filter out of the plan, the four Quick Assessment answers, the selected plan/template, and — for Logged-In-tier users — their Full Assessment data (body/BMI, full PAR-Q, self-test). A "Riwayat" tab shows past assessment cycles by date and level. A "Ulangi Assessment" action lets the user regenerate their plan from scratch, archiving the current one without losing log history.

This is a **read + one-write-action** feature: it renders data already produced by `01-quick-assessment.md` and `06-ai-custom-plan.md`'s Full Assessment (P-11), and adds exactly one new mutation (retake) that reuses the archive-then-regenerate pattern already implemented in `ActivatePlanFromFullAssessment.kt`.

**Known domain gap carried into this techspec (see §9 item 1):** the issue text expects a Beginner/Intermediate/**Advanced** badge, but `Level` only has `BEGINNER, INTERMEDIATE` today. P-09 displays only what the domain can produce; Advanced is out of scope until `Level` (or an equivalent) is extended elsewhere.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Status |
|---|---|---|
| P-09 Assessment Detail — Hasil tab | [node-id=12-987](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-987) | As designed. Header (back/title/share icon), "Hasil"/"Riwayat" tab bar, level badge card with plain-language explainer, PAR-Q safety card (icon+heading+body, amber/orange treatment), "Ringkasan Jawaban" 2x2 grid (Goal/Frekuensi/Pengalaman/Peralatan), "Rencana Terpilih" plan card (image, badge, title, description), dashed-border "Assessment Belum Lengkap" CTA card (lock icon, heading, body, full-width dark button), "Ulangi Assessment" footer link, bottom nav (NuFit AI tab active) |
| P-09 Riwayat tab | **Not designed** | No separate Figma frame for the history tab content. Build as a simple date-ordered list of cards (one per `MonthlyPlan` cycle: date, level, source badge), reusing the "Ringkasan Jawaban" card's visual language (white bg, rounded-24, drop shadow) — functional, not polished |
| Retake confirmation dialog | **Not designed** | No frame. Build as a standard confirmation dialog (title + warning body + confirm/cancel), consistent with the existing `DoctorConsultScreen`'s consult-acknowledgment pattern for tone |
| PAR-Q multi-flag stacking (§9 item 6) | **Not designed** | Figma shows exactly one flag card; when >1 flag is active, stack the same card component vertically with standard spacing — no new component needed |

Assets (icons) referenced in the design context are exported via Figma MCP asset URLs (expire ~7 days) — download-and-commit the actual icon bytes during `/code`, do not leave remote URLs in shipped code, per the figma-design-to-code skill's asset rule.

## 3. Scope

**In scope**
- P-09 "Hasil" tab: level badge + plain-language classification explainer, PAR-Q flag card(s) (0..n), Quick Assessment answer summary grid, selected plan/template card, Full-Assessment-incomplete CTA card (shown only when relevant — see §8), footer "Ulangi Assessment" action
- P-09 "Riwayat" tab: list of past assessment/plan cycles by date + level (UC-2)
- Full Assessment alternate-flow section: body data/BMI, full PAR-Q flags, self-test results — rendered when a `FullAssessmentResult` exists, each field independently handling its own null/"belum diisi" state
- Retake flow (UC-3): confirmation dialog → archive active plan → regenerate from the *same source* that produced the active plan (Quick Assessment re-run for Local tier, Full Assessment re-run for Logged-In tier — see §5) → new plan becomes active, log history untouched
- New usecases/repository methods needed to support the above (history query, retake orchestration, missing `GetLatestBodyMeasurement` wrapper) — see §6/§7
- Navigation entry from `Route.Home` (P-03); Profile & Settings (P-15) entry point deferred since P-15 doesn't exist yet (see Out of scope)

**Out of scope** (explicitly deferred)
- Profile & Settings screen (P-15) itself — `09-profile-settings.md`, not built; P-09 is reachable from Home only in this pass
- Real Google Sign-In — `08-auth-sync.md`, not built; the "Masuk & Lengkapi Assessment" CTA routes to the existing `Route.FullAssessmentStub` placeholder, not a real auth flow
- Extending `Level` with `ADVANCED` or any other domain-model change to the classification engine itself — display-only feature, does not touch `ResolveQuickAssessment`/`KalibrasiStartingLevel`
- Full per-cycle audit trail merging Quick Assessment + Full Assessment + plan-rollover events into one unified history feed — Riwayat is `MonthlyPlan`-cycle-based only (see §9 item 4)
- Retake anti-abuse / rate limiting (e.g. spamming retake) — no signal in the source US that this is a concern this pass

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin melihat hasil assessment saya beserta alasannya dan riwayatnya, sehingga saya paham kenapa plan saya seperti ini dan bisa melihat perkembangan level saya.

**AC-1** Given quick assessment selesai, when membuka Assessment Detail, then level + alasan klasifikasi tampil dalam bahasa awam.
**AC-2** Given hanya quick assessment, when dibuka, then bagian full assessment tampil sebagai "belum diisi" dengan CTA login + lengkapi assessment — bukan error.
**AC-3** Given ada flag PAR-Q, when dibuka, then flag tampil dengan bahasa awam beserta dampaknya ke plan (gerakan yang difilter).
**AC-4** Given retake dikonfirmasi, when selesai, then plan lama diarsipkan dan plan baru aktif; riwayat log tetap utuh.
**AC-5** Given retake dibatalkan, when kembali, then plan & data tidak berubah.

### AC Traceability Table

| AC | Handled by | Blocking? |
|---|---|---|
| AC-1 | `usecase/assessmentdetail/GetAssessmentDetail.kt` (composes `QuickAssessmentResult` + a new plain-language level-rationale mapping, reusing `splitEducationalNote`) + `presentation/assessmentdetail/screen/AssessmentDetailScreen.kt` "Hasil" tab | No |
| AC-2 | `presentation/assessmentdetail/viewmodel/AssessmentDetailViewModel.kt` reads `HasCompletedFullAssessment`; `false` renders the CTA card state, never an `Error` state | **BLOCKING** — must never render as error |
| AC-3 | `usecase/onboarding/ParQFlagMapping.kt` (`toHealthFlags()`, existing) for Local tier, `FullAssessmentParQMapping.kt` (existing) for Logged-In tier; new plain-language flag→copy renderer in `presentation/assessmentdetail/component/FlagExplanationCard.kt` | **BLOCKING** — safety-relevant messaging must reuse the shipped flag→exclusion mapping, never a re-derived one |
| AC-4 | `usecase/assessmentdetail/RetakeAssessment.kt` (new — mirrors `ActivatePlanFromFullAssessment`'s archive-then-regenerate pattern, branches on `MonthlyPlan.source`) | **BLOCKING** — data-loss-adjacent (plan replacement); must preserve `PlanDaySessionLog` rows (archival never deletes days/logs, per existing `archivePlan` semantics) |
| AC-5 | `AssessmentDetailViewModel` — dialog `Cancel` event is a pure UI-state no-op, `RetakeAssessment` is only invoked on explicit confirm | No |

## 5. Domain Model

No new core entities — this feature is a **read composition + one orchestrating usecase** over entities already defined in `domain/onboarding`, `domain/fullassessment`, and `domain/monthlyplan`. One new lightweight view-model-facing type:

```
AssessmentDetailSummary                      // presentation-facing aggregate, not persisted
  level: Level
  levelRationale: String                     // new plain-language mapping, keyed off input.experience (see below)
  quickAssessment: QuickAssessmentResult
  parQFlags: List<FlagExplanation>            // one per active HealthFlag/ExerciseFlag
  activePlan: MonthlyPlan
  fullAssessment: FullAssessmentResult?       // null => render "belum diisi" CTA (AC-2)
  bodyMeasurement: BodyMeasurement?           // for Full-tier BMI display

FlagExplanation                               // presentation-facing, not persisted
  flagLabel: String                           // e.g. "riwayat nyeri sendi"
  plainLanguageImpact: String                 // e.g. "Gerakan high-impact seperti Jumping Jacks akan difilter"
  sourceQuestionId: ParQQuestionId | FullAssessmentParQQuestionId
```

### Business rule — level rationale copy (new, small)

`ResolveQuickAssessment.kt` already produces `splitEducationalNote` for the split decision but nothing for *why this level*. Add a pure mapping (same file family, `domain/onboarding` or a sibling `LevelRationaleCopy.kt`) keyed off `Experience`:
- `BELUM_PERNAH` / `KURANG_1_TAHUN` → Beginner rationale string
- `RUTIN_BERTAHUN` → Intermediate rationale string

This is a **copy table, not a re-derivation of `resolveLevel`** — it must not diverge from `ResolveQuickAssessment`'s actual branching (DoD item, §10).

### Business rule — retake source branching (AC-4, BLOCKING)

Per §9 item 2's resolved gray area: retake targets whichever source produced the currently active `MonthlyPlan`:
- `PlanSource.LOCAL_TEMPLATE` → re-run Quick Assessment wizard (`Route.QuickAssessmentWizard`) → `GenerateLocalTemplatePlan`, archiving the current plan first (mirrors `ActivatePlanFromFullAssessment`'s archive-then-generate order, but archive happens *before* the wizard starts this time since the user must be able to cancel mid-wizard without having archived anything — see §9 item 9 for the exact sequencing decision)
- `PlanSource.LOGGED_IN_RULE_ENGINE` → re-run Full Assessment (`Route.FullAssessmentStub`) → `ActivatePlanFromFullAssessment` (already archives internally)

### Business rule — retake blocked during active session (§9 item 3, BLOCKING)

Before showing the retake confirmation dialog, check for an in-progress `PlanDaySessionLog` (a `PlanDay` with neither `completedAt` nor `skippedAt` set but session start recorded — reuses whatever "session in progress" signal PT Mode (`05-pt-mode.md`) persists once it ships; until PT Mode exists, this check is a no-op that always passes, since there is no way to be mid-session yet). Document this as a forward-compatibility hook, not dead code to be deleted later.

## 6. Architecture Breakdown

New feature slug `assessmentdetail`, depends on `onboarding`, `fullassessment`, and `monthlyplan` (read-only composition) plus orchestrates one write into `monthlyplan`/`onboarding`/`fullassessment` for retake.

```
shared/src/commonMain/kotlin/
├── domain/onboarding/entity/
│   └── LevelRationaleCopy.kt          // new — plain-language level explainer, sibling to ResolveQuickAssessment
├── domain/monthlyplan/repository/
│   └── MonthlyPlanRepository.kt       // extend: add getAllPlans(): AppResult<List<MonthlyPlan>>  (new method, additive)
│
├── usecase/onboarding/
│   └── GetLatestBodyMeasurement.kt    // new — missing usecase wrapper around existing repository.getLatestBodyMeasurement()
├── usecase/assessmentdetail/
│   ├── GetAssessmentDetail.kt         // new — composes QuickAssessmentResult + ParQResult + FullAssessmentResult? + BodyMeasurement? + active MonthlyPlan into AssessmentDetailSummary
│   ├── GetAssessmentHistory.kt        // new — wraps MonthlyPlanRepository.getAllPlans(), sorted startedAt desc
│   └── RetakeAssessment.kt            // new — orchestrates archive-then-regenerate, branching on MonthlyPlan.source (see §5)
│
├── data/monthlyplan/
│   ├── datasource/MonthlyPlanLocalDataSource.kt   // extend: add getAllPlans query
│   └── repository/MonthlyPlanRepositoryImpl.kt    // extend: implement getAllPlans()
│
└── di/AssessmentDetailModule.kt        // new — GetAssessmentDetail, GetAssessmentHistory, RetakeAssessment, AssessmentDetailViewModel
```

```
composeApp/src/commonMain/kotlin/
├── presentation/assessmentdetail/
│   ├── viewmodel/AssessmentDetailViewModel.kt   // sealed State (Loading/Loaded/Error), sealed Event (SwitchTab, RequestRetake, ConfirmRetake, CancelRetake)
│   ├── screen/AssessmentDetailScreen.kt          // P-09, both tabs
│   └── component/
│       ├── LevelBadgeCard.kt
│       ├── FlagExplanationCard.kt                // reused for both Quick-tier and Full-tier flags (AC-3)
│       ├── AnswerSummaryGrid.kt
│       ├── SelectedPlanCard.kt
│       ├── FullAssessmentCtaCard.kt              // AC-2's "belum diisi" state
│       ├── AssessmentHistoryList.kt              // Riwayat tab
│       └── RetakeConfirmationDialog.kt
└── navigation/ (route below)
```

expect/actual: none needed — pure local Compose + SQLDelight read/compose, no platform APIs, no network calls in this feature (the "Masuk" CTA navigates to an existing stub route, it does not itself call anything online).

## 7. API / Data Contract

### SQLDelight

No new tables. One additive query on the existing `monthly_plan` table (`MonthlyPlan.sq`, already shipped by `monthly-plan.md`):

| `.sq` file | Change | Generated query | Schema status |
|---|---|---|---|
| `MonthlyPlan.sq` | Add `selectAll` query (`SELECT * FROM monthly_plan ORDER BY started_at DESC`) | `monthlyPlanQueries.selectAll()` | Additive query only — no schema/column change, no migration needed |

### DI

New `AssessmentDetailModule` (manual DSL per ADR-001), registered in `KoinInit.kt` alongside the existing four:
- `factory { GetLatestBodyMeasurement(repository = get()) }` — registered in `OnboardingModule` (belongs to that feature's usecases, not a new module)
- `factory { GetAssessmentDetail(...) }`, `factory { GetAssessmentHistory(...) }`, `factory { RetakeAssessment(...) }`, `viewModel { AssessmentDetailViewModel(...) }` — registered in `AssessmentDetailModule`
- Add `assessmentDetailModule` to `DiVerificationTest.checkModules()`

### Navigation

New route:
- `Route.AssessmentDetail` — entry from `Route.Home` (P-03), e.g. a tappable element near the level/plan summary; no params needed (always reads the latest/active data)

`Route.QuickAssessmentWizard` and `Route.FullAssessmentStub` are re-entered (not new) for the two retake branches — confirm during `/code` that both can be entered as a "redo" without requiring a fresh `Route.Splash`/`Route.ParQ` traversal (Local-tier retake still needs a PAR-Q re-check per Quick Assessment's own gate — reuse its existing flow, don't bypass it).

## 8. State & Error Handling

| Screen | States |
|---|---|
| AssessmentDetailScreen (P-09) — Hasil tab | `Loading`, `Loaded(summary: AssessmentDetailSummary, activeTab: Hasil\|Riwayat)`, `Error(Failure.Database)` |
| AssessmentDetailScreen (P-09) — Riwayat tab | Same `Loaded` state carries `history: List<MonthlyPlan>` (loaded eagerly with the rest — history is cheap, no separate loading state needed); `Empty` sub-state when `history.size <= 1` (only the current cycle, nothing to show as "progress over time") |
| Full Assessment section | Not a separate screen state — a field within `Loaded`: `fullAssessment: FullAssessmentResult?`. `null` renders `FullAssessmentCtaCard` (AC-2), never `Error` |
| Retake dialog | `Hidden`, `Confirming(targetSource: PlanSource)`, `InProgress` (archive+regenerate running), `Blocked(reason: String)` (§5's in-progress-session check) |

`Failure.Database` is the only reachable `Failure` subtype (all local, no network calls). Map SQLDelight/repository exceptions consistent with the onboarding and monthlyplan features' existing pattern.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | `Level` has no `ADVANCED` value but the issue text names it | **Handle** | P-09 renders only `BEGINNER`/`INTERMEDIATE`; Advanced badge support deferred until the domain model gains it (§1) |
| 2 | Retake scope ambiguous for Logged-In tier (re-run P-02 vs Full Assessment) | **Handle** | Branch on `MonthlyPlan.source` — Local retakes via Quick Assessment, Logged-In retakes via Full Assessment (§5) |
| 3 | Retake while a session is in progress | **Handle** | Pre-check before showing the confirmation dialog; forward-compatibility no-op until PT Mode ships a "session in progress" signal (§5) |
| 4 | Riwayat (History) tab data source is ambiguous (Quick Assessment attempts vs Full Assessment completions vs plan cycles) | **Handle** | Defined as one row per `MonthlyPlan` (active + archived), matching what already exists and satisfies the literal AC wording ("list hasil per tanggal dengan level") |
| 5 | PAR-Q flag vocabulary differs between Quick tier (`HealthFlag`) and Full tier (`ExerciseFlag`) | **Handle** | `FlagExplanationCard` renders either, driven by a shared "flag → plain-language card" interface, not a shared data type — no forced type unification |
| 6 | Multiple simultaneous PAR-Q flags — Figma shows only one | **Handle** | Stack `FlagExplanationCard` vertically, one per active flag, standard spacing |
| 7 | No login/auth flow exists yet for the "Masuk & Lengkapi Assessment" CTA | **Handle (placeholder)** | CTA routes to existing `Route.FullAssessmentStub`; explicitly documented as a stand-in pending `08-auth-sync.md` |
| 8 | `GetLatestBodyMeasurement` usecase wrapper doesn't exist | **Handle** | Add it in `usecase/onboarding`, mirroring `GetLatestParQResult`'s shape |
| 9 | Retake-cancel must leave zero side effects | **Handle** | Archive only happens inside `RetakeAssessment`, only invoked from the dialog's explicit confirm event — cancel never calls it |
| 10 | Full-tier self-test fields (`CapacityTestResult`) are independently nullable | **Handle** | Each of pushup/plank/sit-to-stand renders its own "belum diisi" state rather than gating the whole self-test section on one missing field |
| 11 | `getAllPlans()` is a new repository method on an existing, already-shipped interface | **Handle** | Purely additive — no existing `MonthlyPlanRepository` call sites break; verify `MonthlyPlanRepositoryImpl` and any test fakes are updated together |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] Penjelasan klasifikasi (level rationale + split rationale) di-generate dari tabel keputusan yang sama dengan `ResolveQuickAssessment.kt` — no divergent/duplicated branching logic (unit test asserting rationale copy stays in sync per `Experience`/`SplitPreference` value)
- [ ] Riwayat assessment tersimpan permanen — retake never deletes `MonthlyPlan` rows (archives only), verified by a test that retakes twice and asserts 3 total plans exist (2 archived + 1 active)
- [ ] AC-2's "belum diisi" path is unit/UI-tested to never resolve to an `Error` state when `FullAssessmentResult` is legitimately absent
- [ ] AC-3's flag rendering reuses `ParQFlagMapping`/`FullAssessmentParQMapping` directly — a test asserts no flag-to-copy logic exists outside those two mapping sources
- [ ] AC-4 covered by a test exercising both retake branches (Local→Local, LoggedIn→LoggedIn) and asserting prior `PlanDaySessionLog` rows survive archival
- [ ] AC-5 covered by a test asserting cancel triggers zero repository writes
- [ ] `DiVerificationTest.checkModules()` passes with `assessmentDetailModule` registered
- [ ] `GetAssessmentDetail` and `GetAssessmentHistory` are pure read compositions with unit tests covering: no Full Assessment, 0/1/n PAR-Q flags, empty history (single cycle), multi-cycle history
