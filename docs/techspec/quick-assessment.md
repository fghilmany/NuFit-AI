# TechSpec: Quick Assessment (Onboarding)

> Source US: [fghilmany/NuFit-AI#2](https://github.com/fghilmany/NuFit-AI/issues/2)
> Flow detail: [#26 Tahap 0: PAR-Q](https://github.com/fghilmany/NuFit-AI/issues/26), [#30 General Flow](https://github.com/fghilmany/NuFit-AI/issues/30), [#20 Tahap 6: Sintesis](https://github.com/fghilmany/NuFit-AI/issues/20), [#25 Tahap 1: Wawancara Awal](https://github.com/fghilmany/NuFit-AI/issues/25), [#27 Flow Algoritma](https://github.com/fghilmany/NuFit-AI/issues/27)

## 1. Overview & Goal

A brand-new user with no local profile answers a mandatory 12-question health screening (PAR-Q), then 4 simple card-based questions (experience, goal, frequency, split preference). The app maps the answers to a pre-built template plan (Level × Goal × Split × Frequency) and lands the user on Home with an active plan — entirely offline, no login required, total time under 2 minutes.

This is the **Lokal tier** engine per Keputusan #3 in `00-overview.md`: a template matrix, not the rule-based engine (`tahap-7b`) used by the Logged-In tier's Full Assessment, and not AI. Full Assessment (issues #21-#30, features `06-ai-custom-plan.md`) is out of scope here.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Status |
|---|---|---|
| P-01 Splash / Entry | [node-id=46-5](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=46-5) | As designed — logo, tagline, loading indicator |
| P-02a "Cek Kesehatan Dulu" (PAR-Q, all 12 questions) | [node-id=12-26](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-26) | **Updated this session** — original design only had 7 questions; expanded to the 12 questions from issue #26 (see §9, item 1). Single scroll screen, one shared "Lanjutkan" CTA (disabled/grey until interaction rule in §8 is met) |
| P-02b "Rekomendasi Konsultasi Dokter" (non-blocking consult-doctor outcome) | node-id `74:2` (new frame, same file, unpublished node-id path) | **Partially built this session** — wrapper + header only (Figma MCP call quota hit mid-build). Full content spec below in §8; finish building in Figma before/while `/code` runs, or `/code` may implement directly from this spec's content description if the frame isn't finished in time |
| P-02c Quick Assessment wizard, step 2 of 4 ("Apa tujuan utamamu?" — goal) | [node-id=12-117](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-117) | As designed |
| P-02c Quick Assessment wizard, steps 1, 3, 4 (experience, frequency, split) | **Not designed** | Reuse step 2's card-list visual pattern (progress bar + %, heading, card options with icon+title+subtitle, sticky footer CTA). Copy for each option given in §5 |
| P-02d Optional body data (UC-2) | **Not designed** | Simple form (height/weight, skip option). Reuse existing input patterns from the project's design system once one exists |

**P-02b content spec** (for `/code` to build against if Figma isn't finished first):
- Header: back button (chevron-left, 40x40 circle) + title "Perhatian Kesehatan", bg `#f8f9fa`, matches P-02a header style.
- Warning card: amber-tinted rounded-24 card (bg `#fef3c7` @ 60% opacity, border amber @ 40%), 40px amber circle icon (alert-triangle), heading "Sebaiknya konsultasi ke dokter dulu" (SemiBold 16), body copy: "Berdasarkan jawaban Anda, ada kondisi yang sebaiknya didiskusikan dengan dokter sebelum memulai program. Kami tetap menyesuaikan latihan Anda agar aman, tapi konsultasi medis kami sarankan." (Regular 14, `#414844`).
- Flagged conditions card: white rounded-24 card w/ shadow, heading "Kondisi yang Anda tandai:" (SemiBold 14), bullet list of the specific questions the user answered "Ya" to (plain language, not the raw question text — e.g. "Nyeri dada saat tidak beraktivitas (istirahat)").
- Acknowledgment row: bordered rounded-16 card, checkbox (20x20) + label "Saya memahami dan akan/sudah berkonsultasi dengan dokter mengenai kondisi ini." — **this checkbox is what gates the footer CTA (see §8), not a hard block**.
- Disclaimer note: small icon + "Ini bukan diagnosis medis. Aplikasi ini bukan pengganti konsultasi dengan tenaga medis profesional." (Medium 12, 70% opacity), matches P-02a's privacy note pattern.
- Footer CTA: sticky "Lanjutkan" button, disabled (grey `#c1c8c2`) until checkbox is ticked, then active (`#012d1d` dark green) — same enable/disable visual language as P-02a's CTA and P-02c's primary button.

## 3. Scope

**In scope**
- P-01 Splash routing (new local profile → PAR-Q; existing local profile → Home directly)
- P-02a PAR-Q gate: 12 questions, non-blocking flagging (see §9 item 1)
- P-02b Consult-doctor outcome screen (shown only if ≥1 "Ya" answer)
- P-02c Quick Assessment wizard: 4 questions → template matrix → active plan
- P-02d Optional body data (height/weight → BMI), skippable
- Local persistence of PAR-Q answers, quick assessment answers, and body measurement history
- Retake flow entry point (re-runs PAR-Q + wizard; detailed activation/archiving belongs to `07-plan-management.md`, this spec only covers re-collecting answers)

**Out of scope** (explicitly deferred, per dev decisions in this session)
- Full Assessment (Tahap 1-9 engine, issues #20-#30) — separate feature, Logged-In tier
- Free-text capture/keyword escalation for PAR-Q Q12 "kondisi lain" — boolean-only for MVP
- PAR-Q periodic re-ask cadence for guest users (e.g. "every few months") — single completion persists until explicit retake
- Body fat %, waist-to-hip ratio, progress photos — Tahap 2 optional instruments, not part of Quick Assessment's minimal UC-2
- Actual exercise/template *content* authoring (which exercises, sets, reps per templateId) — belongs to the Exercise Library / template data set (`04-exercise-library.md`), this spec only defines the decision table that resolves to a `templateId`

## 4. User Story & Acceptance Criteria

> Sebagai user baru yang awam gym, saya ingin menjawab pertanyaan singkat dan langsung mendapat plan sebulan, sehingga saya bisa mulai latihan tanpa proses rumit dan tanpa harus paham istilah fitness.

**AC-1** Given a new user, when the app opens, then it routes directly to PAR-Q without requesting login.
**AC-2** Given PAR-Q unanswered, when the user tries to start a workout, then they are always blocked by the PAR-Q gate (cannot be skipped).
**AC-3** Given ≥1 "Ya" answer in PAR-Q, when the plan is generated, then movements tagged with the matching `flag_exclusion` are filtered out of the plan, AND a `requires_doctor_consult` record is written locally, AND the user sees the non-blocking consult-doctor screen before continuing (§8).
**AC-4** Given all 4 quick-assessment questions answered, when finished, then a monthly plan appears in under 3 seconds and is saved locally.
**AC-5** Given split = "Tidak tahu", when the plan is built, then the system resolves the split and shows the reason (§5 rule).
**AC-6** Given the optional body-data prompt, when the user taps "Lewati", then all local features still work fully.
**AC-7** Total completion time (PAR-Q + wizard) is under 2 minutes.
**AC-8** Given a returning user with an existing local profile, when the app opens, then it routes straight to Home (P-03), not back to PAR-Q/wizard.
**AC-9** Given the app is killed mid-PAR-Q or mid-wizard, when reopened, then in-progress answers are restored and the user can go back and change prior answers without losing progress.
**AC-10** Given a user retakes the assessment (`03-assessment-detail.md` UC-3), when they confirm, then PAR-Q is re-run first, followed by the 4-question wizard.

## 5. Domain Model

### Entities

```
ParQAnswer
  questionId: ParQQuestionId       // enum, 12 values, Q1..Q12
  answer: Boolean                  // Ya = true, Tidak = false

ParQResult
  id, answeredAt: Instant
  answers: List<ParQAnswer>
  flagsGenerated: Set<HealthFlag>  // derived, see table below
  requiresDoctorConsult: Boolean   // true if any answer == true
  consultAcknowledgedAt: Instant?  // set when user ticks the acknowledgment checkbox

HealthFlag (enum)
  JANTUNG, TEKANAN_DARAH, SENDI, SENDI_OPERASI_RECENT,
  DIABETES, KANKER_AKTIF, KEHAMILAN, LAINNYA

QuickAssessmentAnswer
  experience: Experience            // BELUM_PERNAH | KURANG_1_TAHUN | RUTIN_BERTAHUN
  goal: GoalCategory                // FAT_LOSS | MUSCLE_GAIN | GENERAL_HEALTH | STRENGTH
  frequency: FrequencyBucket        // TWO_TO_THREE | FOUR_TO_FIVE
  splitPreference: SplitPreference  // UPPER_LOWER | FULL_BODY | TIDAK_TAHU

QuickAssessmentResult
  id, answeredAt: Instant
  input: QuickAssessmentAnswer
  level: Level                      // BEGINNER | INTERMEDIATE
  resolvedSplit: ResolvedSplit       // FULL_BODY | UPPER_LOWER
  splitEducationalNote: String?     // non-null only when preference was suboptimal for frequency
  templateId: String                 // resolves to Exercise Library template data (out of scope here)

BodyMeasurement
  id, recordedAt: Instant
  heightCm: Double?, weightKg: Double?   // both nullable independently; BMI computed, not stored
```

### Business Rules — PAR-Q → HealthFlag mapping

| Q# | Question topic | HealthFlag(s) set on "Ya" |
|---|---|---|
| Q1 | Dokter bilang masalah jantung | `JANTUNG` |
| Q2 | Nyeri dada saat beraktivitas | `JANTUNG` |
| Q3 | Nyeri dada saat istirahat | `JANTUNG` (highest-severity copy in UI, same flag) |
| Q4 | Pusing/pingsan tanpa sebab jelas | `JANTUNG` |
| Q5 | Tekanan darah tinggi belum terkontrol | `TEKANAN_DARAH` |
| Q6 | Masalah tulang/sendi | `SENDI` |
| Q7 | Operasi tulang/sendi 12 bulan terakhir | `SENDI_OPERASI_RECENT` |
| Q8 | Obat rutin jantung/tekanan darah | `JANTUNG`, `TEKANAN_DARAH` |
| Q9 | Diabetes tidak stabil | `DIABETES` |
| Q10 | Pengobatan kanker aktif | `KANKER_AKTIF` |
| Q11 | Hamil | `KEHAMILAN` |
| Q12 | Kondisi lain (boolean only, no free text in MVP) | `LAINNYA` |

**Rule PARQ-01**: any `true` answer → `requiresDoctorConsult = true`, all matching `HealthFlag`s added to `flagsGenerated`, non-blocking (see §9 item 1 for the reversal of the original hard-stop design).
**Rule PARQ-02**: `flagsGenerated` feeds template/plan generation as `flag_exclusion` filters against the Exercise Library (mechanism defined in `04-exercise-library.md`; this spec only produces the flag set).
**Rule PARQ-03 (confirmed)**: `KEHAMILAN` (Q11) is included in the non-blocking treatment — same mechanism as every other question, confirmed explicitly by the dev. This reverses the original spec (#26), which treated pregnancy as a hard, unconditional stop because the app has no prenatal program content. The consult-doctor screen's copy for Q11 should still be clear that the generated plan is a general program, not a pregnancy-specific one, and that medical guidance is strongly advised — but it must never block app usage.

### Business Rules — Level determination

Quick Assessment has no capacity/movement-screening data (that's Full Assessment only), so level comes from self-reported experience alone, capped conservatively:

| Experience answer | Level |
|---|---|
| Belum pernah | Beginner |
| < 1 tahun | Beginner |
| Rutin bertahun-tahun | **Intermediate** (capped — Advanced requires movement/capacity validation from Full Assessment, not available at this tier) |

### Business Rules — Split resolution

| splitPreference | Resolution |
|---|---|
| `FULL_BODY` | Full Body (respected as-is) |
| `UPPER_LOWER` | Upper/Lower (respected as-is); if `frequency == TWO_TO_THREE` → set `splitEducationalNote` explaining it's suboptimal at this frequency, but still save the user's choice |
| `TIDAK_TAHU` | System decides: `level == BEGINNER OR frequency == TWO_TO_THREE` → Full Body; else → Upper/Lower. `splitEducationalNote` always set explaining why |

### Business Rules — templateId

`templateId = "{level}_{goal}_{resolvedSplit}_{frequencyBucket}"` — 2 levels × 4 goals × 2 splits × 2 frequency buckets = **32 combinations**, fully enumerable, unit-testable as a pure function `resolveTemplateId(QuickAssessmentAnswer, healthFlags): QuickAssessmentResult`. Actual template *content* (exercises/sets/reps per templateId) is Exercise Library data, out of scope here.

### Business Rules — BMI (UC-2)

Standard formula, metric only for MVP: `BMI = weightKg / (heightCm / 100)²`. Both fields optional and independent — a user can supply weight without height (no BMI shown) with no error.

## 6. Architecture Breakdown

Feature slug: `onboarding` (covers PAR-Q + Quick Assessment wizard + optional body data; matches how `00-overview.md` groups P-01/P-02 as one feature file).

```
shared/src/commonMain/kotlin/
├── domain/onboarding/
│   ├── entity/ParQAnswer.kt, ParQResult.kt, HealthFlag.kt,
│   │           QuickAssessmentAnswer.kt, QuickAssessmentResult.kt,
│   │           BodyMeasurement.kt, Level.kt, GoalCategory.kt, etc.
│   └── repository/OnboardingRepository.kt   // interface
│
├── usecase/onboarding/
│   ├── SubmitParQAnswers.kt        // persists answers, derives flags/requiresDoctorConsult
│   ├── AcknowledgeDoctorConsult.kt // sets consultAcknowledgedAt
│   ├── ResolveQuickAssessment.kt   // pure function: input -> level/split/templateId (unit-test-heavy)
│   ├── SubmitQuickAssessment.kt    // persists result, activates generated plan
│   ├── SaveBodyMeasurement.kt
│   ├── GetLocalProfileStatus.kt    // used by Splash routing (AC-1/AC-8)
│   └── RetakeAssessment.kt         // clears in-progress wizard state, re-enters PAR-Q
│
├── data/onboarding/
│   ├── model/ (DTOs only if/when remote sync is added later; none needed for this offline-only spec)
│   ├── datasource/OnboardingLocalDataSource.kt   // wraps SQLDelight queries
│   └── repository/OnboardingRepositoryImpl.kt
│
└── di/OnboardingModule.kt
```

```
composeApp/src/commonMain/kotlin/
├── presentation/onboarding/
│   ├── viewmodel/
│   │   ├── ParQViewModel.kt          // State: Loading/Questions/Submitting; Event: Answer, Next
│   │   ├── DoctorConsultViewModel.kt // State holds flaggedQuestions + acknowledge toggle
│   │   ├── QuickAssessmentViewModel.kt // 4-step wizard state machine
│   │   └── BodyDataViewModel.kt
│   ├── screen/
│   │   ├── ParQScreen.kt              // P-02a
│   │   ├── DoctorConsultScreen.kt     // P-02b
│   │   ├── QuickAssessmentWizardScreen.kt  // P-02c, 4 steps
│   │   └── BodyDataScreen.kt          // P-02d
│   └── component/ (QuestionCard, ProgressBar, CardOption, etc.)
└── navigation/ (routes below)
```

No expect/actual needed for this feature — pure local Compose + SQLDelight, no platform-specific APIs.

## 7. API / Data Contract

### SQLDelight

All local, no remote calls in this feature (100% offline per DoD).

| `.sq` file | Table | Generated `*Queries` object | packageName | Schema status |
|---|---|---|---|---|
| `ParQResult.sq` | `par_q_result` | `parQResultQueries` | `com.nufitai.shared.db` (confirm against actual `sqldelight { databases { ... packageName.set(...) } }` in `shared/build.gradle.kts` before use) | New schema |
| `ParQAnswerDetail.sq` | `par_q_answer_detail` | `parQAnswerDetailQueries` | same | New schema — one row per question per result, so individual "Ya" answers can be listed on the consult-doctor screen and in Assessment Detail (`03-assessment-detail.md`) |
| `QuickAssessmentResult.sq` | `quick_assessment_result` | `quickAssessmentResultQueries` | same | New schema |
| `BodyMeasurement.sq` | `body_measurement` | `bodyMeasurementQueries` | same | New schema, append-only (history, never overwritten — per `09-profile-settings.md` UC-1) |

`ParQResult` columns: `id, answered_at, requires_doctor_consult, consult_acknowledged_at (nullable)`.
`ParQAnswerDetail` columns: `id, par_q_result_id (FK), question_id, answer (boolean)`.
`QuickAssessmentResult` columns: `id, answered_at, experience, goal, frequency, split_preference, resolved_split, level, template_id, split_educational_note (nullable)`.
`BodyMeasurement` columns: `id, recorded_at, height_cm (nullable), weight_kg (nullable)`.

In-progress wizard state (for AC-9, app-kill resume) is persisted incrementally — write each answer to its row as it's given, not only at final submit. `ParQAnswerDetail` rows accumulate one at a time; `QuickAssessmentResult` can use a draft row updated per step, finalized on submit.

### DI

Register in `OnboardingModule` (`@Module @ComponentScan("com.nufitai.shared.data.onboarding")`, mirrors the pattern in `architecture.md` §4):
- `OnboardingRepositoryImpl` → `@Single(binds = [OnboardingRepository::class])`
- All 7 use cases above → `@Factory`
- All 4 ViewModels → `@KoinViewModel`
- Add/verify in `DiVerificationTest.checkModules()`.

### Navigation

New routes (type-safe, `@Serializable`):
- `Route.ParQ` — entry from Splash when no local profile
- `Route.DoctorConsult` — entry only when `requiresDoctorConsult == true` after PAR-Q submit
- `Route.QuickAssessmentWizard(step: Int = 1)` — 4-step wizard
- `Route.BodyData` — after wizard submit, before Home
- Splash (`Route.Splash`) routing logic: `GetLocalProfileStatus` → profile exists → `Route.Home`; else → `Route.ParQ`

## 8. State & Error Handling

All screens are local-only; no network `Failure` states apply here (this feature has no remote calls). States needed per screen:

| Screen | States |
|---|---|
| ParQScreen | `Loading` (restoring in-progress answers), `Questions(answers: Map<QuestionId, Boolean?>, canProceed: Boolean)`, `Submitting` |
| DoctorConsultScreen | `Content(flaggedQuestions: List<ParQQuestionId>, acknowledged: Boolean)` — footer CTA enabled only when `acknowledged == true` (checkbox ticked); this is a **local UI gate**, not a `Failure` state — there is no error path here since nothing can fail offline |
| QuickAssessmentWizardScreen | `Loading`, `Step(index: Int, selectedOptions: ..., canGoBack: Boolean)`, `Generating` (plan-building, target < 3s per AC-4), `Error(Failure.Database)` only if the local write itself fails (rare, e.g. disk full) |
| BodyDataScreen | `Content(heightCm: String?, weightKg: String?)` — skip always available, no validation failure state needed beyond basic numeric input guards |

`Failure.Database` is the only `Failure` subtype realistically reachable in this feature (local write failure). Map any SQLDelight exception to it in the repository layer.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | PAR-Q design only showed 7/12 questions; 2 of 3 original hard-stop questions (Q3 nyeri istirahat, Q11 kehamilan) were missing from the design | **Handle** — Figma screen updated to all 12 questions this session (§2). **Business logic changed**: hard-stop no longer blocks the flow at all; instead `requiresDoctorConsult` is set locally and the user proceeds after acknowledgment (§5 Rule PARQ-01). See flagged concern on Q11/kehamilan specifically (§5 Rule PARQ-03) |
| 2 | No consult-doctor / flagged-outcome screen existed in Figma | **Handle** — new screen designed this session (P-02b, §2); Figma build incomplete (header only) due to MCP quota, full content spec given as fallback |
| 3 | PAR-Q Q12 "kondisi lain" free text | **Handle as boolean-only** for MVP — no text field, no keyword escalation, defaults to `LAINNYA` flag |
| 4 | No template-matrix decision table existed anywhere | **Handle** — derived this session from #20 (level logic), #25 (experience categories), #27 (split matrix), adapted down to Quick Assessment's simpler inputs (§5). Flagged as a draft for dev review since no source-of-truth table existed |
| 5 | Wizard steps 1/3/4 have no Figma reference | **Defer** — reuse step 2's card-list pattern; exact icons/copy TBD for `/code`, listed in §5 answer enums |
| 6 | UC-2 body data screen position/no Figma ref | **Handle** — interstitial shown once right after wizard submit, skippable, also reachable later from Profile & Settings |
| 7 | BMI units/formula | **Handle** — metric (cm/kg) only for MVP, standard formula (§5) |
| 8 | Retake re-runs PAR-Q too? | **Handle** — yes, retake always re-enters PAR-Q first (AC-10) |
| 9 | Re-ask cadence for PAR-Q over time | **Defer** — out of MVP scope, single completion persists until explicit retake |
| 10 | App killed mid-PAR-Q/mid-wizard | **Handle** — incremental local writes per answer (§7), resumable (AC-9) |
| 11 | Returning user routing | **Handle** — explicit AC-8, `GetLocalProfileStatus` usecase |
| 12 | Self-report gaming (flip PAR-Q answer to avoid consult) | **Ignore** — inherent to any self-report instrument; no longer even a blocking concern since PAR-Q is non-blocking by design now |
| 13 | Uninstall/reinstall wipes local PAR-Q history | **Ignore** — consistent with locked local-first/no-backup-without-login decision (`08-auth-sync.md`) |
| 14 | Should Q11 (kehamilan) stay a hard stop given the app has no prenatal content? | **Confirmed non-blocking** — dev decision: "for apps non blocking, we must still make user can use the app." Consult-doctor screen copy for this flag must clarify the plan is general-purpose, not pregnancy-specific |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] `resolveTemplateId` (Level × Goal × Split × Frequency → templateId) is a pure function with unit tests covering all reachable branches, including the split-resolution rule and the frequency/split educational-note case
- [ ] PAR-Q → HealthFlag mapping table (§5) unit tested per question
- [ ] No local feature errors when body-data fields are empty (AC-6)
- [ ] Wizard supports back-navigation without resetting later or prior answers (AC-9)
- [ ] `DiVerificationTest.checkModules()` passes with `OnboardingModule` registered
- [ ] Splash routing verified for both new-profile and existing-profile paths (AC-1, AC-8)
- [ ] Consult-doctor screen: footer CTA provably disabled until acknowledgment checkbox is ticked (component/UI test)
- [ ] Total PAR-Q + wizard completion time measured under 2 minutes in a manual run-through
- [ ] Consult-doctor screen copy for the `KEHAMILAN` flag explicitly clarifies the generated plan is general-purpose, not pregnancy-specific (§9 item 14)
