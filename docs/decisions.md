# Architecture Decision Records — NuFit AI

Approved deviations from `.claude/rules/` locked decisions. One short entry per decision.
The `kmp-auditor` reads this file — decisions recorded here are NOT flagged as violations.

## ADR-001 — Manual Koin DSL instead of koin-annotations

- **Date:** 2026-07-23
- **Decision:** DI uses manual Koin DSL modules (`OnboardingModule.kt`, `DatabaseModule.kt`), not `@Single`/`@Factory`/`@KoinViewModel` + KSP.
- **Rationale:** `@ComponentScan` KSP output proved non-deterministic during onboarding development; manual modules are explicit and predictable in KMP.
- **Guardrail:** the mandatory `checkModules()` test (`DiVerificationTest`) stays in place — every new usecase/repository/datasource/ViewModel must be registered manually and keep it green.
- **Approved by:** dev (fghilmany), following the onboarding architecture audit.

## ADR-002 — Exercise Library data modeling decisions (Monthly Plan techspec, issue #76)

- **Date:** 2026-07-23
- **Decision:** Five schema decisions not resolved by the techspec, made while implementing `domain/exerciselibrary` against the actual `Gym Techniques` source data:
  1. **`EquipmentCategory` replaces onboarding's `EquipmentType`** (renamed in place — `MACHINE`→`MACHINE_CABLE`, `CARDIO`→`CARDIO_EQUIPMENT`), values matching the 7 source folders exactly (`BARBELL, BODYWEIGHT, CARDIO_EQUIPMENT, DUMBBELL, KETTLEBELL, MACHINE_CABLE, PULL_UP_BAR`). `RESISTANCE_BAND` excluded — no source data (techspec §9 item 19).
  2. **`ExerciseLevel` normalization**: `enum { REGRESI, STANDAR, PROGRESI, KOREKTIF, AKSESORI }` + `levelVariant: Int?` (1/2/3) + `levelNote: String?` (raw qualifier text — "Alt", "Teaching", "fallback" — preserved, not discarded). Raw source has 15+ level strings that don't fit a plain 3-value enum.
  3. **Cardio schema gap**: cardio batch files lack `pola_gerak`/`Otot Utama`, using `Modalitas/Dampak` + `Sistem Energi Dominan` instead. `movementPattern = CARDIO` uniformly; added `highImpact: Boolean` (drives SAFE-07/SAFE-11 "dampak_tinggi" exclusion) and `isometricHeavy: Boolean` (drives SAFE-06 "isometrik_berat" exclusion), derived from source text, not fabricated.
  4. **Duplicate cross-category exercises** (e.g. Farmer's Carry in both Dumbbell and Kettlebell): imported as separate catalog-ID entries, matching actual source data — not the master doc's unimplemented single-entry/multi-tag aspiration.
  5. **`media_slug`** (free-exercise-db mapping): best-effort name match for unambiguous English-mappable names only; unmatched stays `null` → placeholder illustration (techspec §9 item 7). No guessed/fuzzy matches recorded as confident.
- **Rationale:** the techspec (`docs/techspec/monthly-plan.md`, GitHub issue #76) references these fields but doesn't resolve their concrete shape against the real, messier `Gym Techniques` source data.
- **Approved by:** dev (fghilmany), 2026-07-23, via `/code #76` planning discussion.

## ADR-003 — JSON-array-as-TEXT for multi-value SQLDelight columns (Exercise Library onward)

- **Date:** 2026-07-23
- **Decision:** New tables from issue #76 onward (`exercise`, and the Monthly Plan/Full Assessment tables that follow) store multi-value columns as JSON-encoded TEXT (`kotlinx.serialization`), decoded in the repository's `toEntity()` mapper — e.g. `flag_exclusion`, `substitusi_setara`. Onboarding's existing tables keep their comma-joined TEXT columns (`equipment` in `quick_assessment_result`) unchanged.
- **Rationale:** onboarding's multi-value columns only ever held flat enum names (`"BODYWEIGHT,DUMBBELL"`). The Exercise Library's `substitusiSetara` is a `Map<EquipmentCategory, String>` and several fields are `Set<String>` of arbitrary tokens — comma-joining can't represent that without ad-hoc secondary delimiters. No `ColumnAdapter`s used; encode/decode stays in the repository layer, consistent with existing enum `.name`/`.valueOf` mapping.
- **Approved by:** dev (fghilmany), 2026-07-23, via `/code #76` planning discussion.

## ADR-004 — Plain Ktor `HttpClient` over Ktorfit for network features (backfilled)

- **Date:** 2026-07-23 (implemented with issue #81; this entry backfills the record — the code comment referenced "ADR-004" before it was ever written here)
- **Decision:** `AiCustomPlanRemoteDataSource` (this repo's first network feature) and every network feature since (`AuthRemoteDataSource`, `SyncRemoteDataSource`, issue #83) use a plain Ktor `HttpClient` with manual `post`/`get` calls, not Ktorfit's KSP-generated typed client interfaces.
- **Rationale:** the endpoint surface per feature is small (1-4 calls); Ktorfit's codegen overhead isn't justified yet. Revisit if the number of remote endpoints grows enough that manual request-building becomes repetitive/error-prone.
- **Approved by:** dev (fghilmany).

## ADR-005 — Auth & Sync (issue #83) implementation decisions

- **Date:** 2026-07-26
- **Decision:** Three deviations/new patterns introduced while implementing `docs/techspec/auth-sync.md`:
  1. **Supabase via plain Ktor REST, not `supabase-kt`** (tech-stack.md lists `supabase-kt` as the default BaaS client) — extends ADR-004's rationale: `AuthRemoteDataSource`/`SyncRemoteDataSource` call Supabase's Auth REST and PostgREST endpoints directly (`apikey`/`Authorization` headers), no SDK dependency added.
  2. **`SecureStorage` interface + `TokenStorage` expect/actual implementing it** (`core/security/`) — `TokenStorage` alone (Android Keystore/iOS Keychain) can't be constructed in `DiVerificationTest` (pure JVM, no Android `Context`); the interface lets that test substitute an in-memory fake, mirroring how `EntitlementLocalDataSource` depends on `Settings` rather than a concrete platform class.
  3. **Native Google/Apple sign-in stubbed behind `GoogleSignInLauncher`/`AppleSignInLauncher` expect/actual** — both actuals return `Failure.Network(...)` with a `TODO(dev)` until real OAuth client IDs exist; a `DebugSimulateSignIn` usecase (mirrors the existing `SetDebugEntitlement`/`debugSimulateSubscriber()` pattern) exercises the rest of the flow (entitlement, sync, navigation) without them.
  4. **Sync scope this pass**: only `MonthlyPlanSyncParticipant` is fully wired (schema + adapter); the other 5 aggregates (`FullAssessmentSyncParticipant`, `QuickAssessmentSyncParticipant`, `ParQSyncParticipant`, `BodyMeasurementSyncParticipant`, `WorkoutHistorySyncParticipant`) are stub adapters with a `TODO(dev)` pointing at the worked example — avoids a single pass touching 6 already-shipped features' repository code.
- **Approved by:** dev (fghilmany), via `/techspec`/`/code #83` planning discussion (sync-scope question answered "MonthlyPlan fully wired now, others stubbed").

## ADR-006 — Real Google Sign-In on Android via Credential Manager, not Firebase Auth (issue #83 follow-up)

- **Date:** 2026-07-27
- **Decision:** Dev asked to "implement auth google for firebase"; clarified to mean *real native Google Sign-In*, not actually switching the auth backend to Firebase (which would have deviated from tech-stack.md's locked "Supabase, not full Firebase" decision). Implemented instead:
  1. **New dependencies** (approved): `androidx.credentials`, `androidx.credentials:credentials-play-services-auth`, `com.google.android.libraries.identity.googleid:googleid` — official Google/AndroidX libraries, Android-only this pass.
  2. **`GoogleSignInLauncher.android.kt`** now calls Credential Manager (`GetGoogleIdOption` + `GoogleIdTokenCredential`) for real, still returning the ID token to the SAME Supabase `grant_type=id_token` flow already built in ADR-005 — no backend change, no new BaaS.
  3. **`CurrentActivityHolder`** (new, `core/auth/`) — Credential Manager's `getCredential()` needs a live Activity at call time, unlike `DatabaseDriverFactory`/`SettingsFactory`/`TokenStorage` which only need Application context once at Koin startup. `MainActivity` registers/clears itself via `onResume`/`onPause`.
  4. **`GOOGLE_WEB_CLIENT_ID`** is a placeholder `const val` (mirrors the `SUPABASE_URL`/`SUPABASE_ANON_KEY` pattern) — dev must create a Google Cloud OAuth Web client ID + a separate Android client ID (package name + SHA-1) and paste the Web client ID into both this constant and Supabase Dashboard's Google provider config.
  5. **iOS scope deferred** — `GoogleSignInLauncher.ios.kt` stays the existing stub; the real iOS Google Sign-In SDK needs Xcode/Podfile changes and cinterop this agent can't compile/verify, tracked as a separate follow-up.
- **Approved by:** dev (fghilmany), 2026-07-27, via `/code` planning discussion.

## ADR-007 — Exercise Library data-source pivot to free-exercise-db-primary

- **Date:** 2026-07-27
- **Decision:** Dev regenerated `files/exercises.json` (1324 entries) with a new shape, reversing the overview's original decision #6 ("free-exercise-db hanya untuk gambar; data gerakan dari Gym Techniques"). New shape:
  1. **id/name now free-exercise-db's** (numeric id e.g. `"0001"`, English exercise name), not the old Gym Techniques catalog id (`"BB-SQUAT-000"`). All 1324 ids consistent, `regressionChain`/`progressionChain` cross-references verified intact (0 broken references).
  2. **`image`/`gif_url`** (new) — bundled photo/GIF paths, all 1324 verified present on disk (`composeResources/files/images/`, `files/videos/`) with zero missing files.
  3. **`instructions`/`commonMistakes`/`safetyTips`/`levelUpRequirement`** are now language-keyed maps, not flat Indonesian lists/strings. `commonMistakes`/`safetyTips`/`levelUpRequirement` ship with full en+id coverage; the raw `instruction_steps` (what the app actually renders) had ZERO Indonesian translations in the source data (only en/it/tr/es/ru/zh/hi/pl/ko/fr) — **all 1324 exercises' `instruction_steps.id` were LLM-translated from English by 12 parallel background agents this session** (dev explicitly requested this rather than a fallback-to-English placeholder), verified programmatically for id/order/step-count parity against the English source, then merged into the bundled JSON. Not human-reviewed.
  4. **`equivalentSubstitutes`** reshaped: keys are now `duplicate_variant`/`bodyweight_alt` (both exercise-id values) and `note` (free text, NOT an id) — previously equipment-category-keyed. `Exercise.equivalentSubstitutes` type changed `Map<EquipmentCategory, String>?` → `Map<String, String>?`; `GetExerciseAlternatives` now filters out the `note` key before resolving ids instead of relying on `ExerciseSeedDto`'s old (now-removed) equipment-category string mapping.
  5. **New dependency: Coil 3** (`coil-compose`, `coil-gif`) — tech-stack.md's already-named image loader, never actually added until now. `BundledMediaImage` (new, `core/designsystem/component/`) reads bundled bytes via `Res.readBytes()` and hands them to Coil as a `ByteArray` model; `App.kt` registers a `GifDecoder`-equipped `ImageLoader` via `setSingletonImageLoaderFactory` so `.gif` bytes animate instead of rendering only a static first frame.
  6. **`Exercise.sq`** gained `image_path`/`gif_path` columns (direct edit, no `.sqm` migration, consistent with this project's early-dev practice — dev must clear app data to re-seed).
- **Approved by:** dev (fghilmany), 2026-07-27, via `/code` planning discussion (all 4 sub-decisions — data-source pivot, LLM translation, `equivalentSubstitutes` semantics, Coil — explicitly confirmed).

## ADR-008 — Real Supabase Edge Functions backend, "functional first" pass (AI Custom Plan, Chat, Meal Plan)

- **Date:** 2026-07-31
- **Decision:** Built the first real backend for the 3 already-contracted AI features (`ai-custom-plan-generate`, `chat-send`, `meal-plan-generate`), as new `supabase/` Edge Functions (TypeScript/Deno) in this same repo. Deliberately scoped to "make it work" over polish, per dev instruction:
  1. **Gemini API as the LLM provider** (not OpenRouter/MiMo yet) — matches today's documented dev-phase plan (`Tech Plan/pricing.md` §7); routing lives behind one small interface (`_shared/llmClient.ts`) so swapping providers later is a one-file change. `Tech Plan/pricing.md`'s OpenRouter/MiMo routing + 20/month chat quota recommendation stays deferred (tracked in issue #88 / `docs/tech-debt-88-pricing-sync.md`), per the dev's explicit "jangan ubah doc apapun dulu" instruction — this pass keeps the currently-documented 5/day chat quota.
  2. **`user_entitlement` Postgres table stands in for RevenueCat** (not integrated) — new users default to `LOGGED_IN`; a row is flipped to `SUBSCRIBED` manually (dashboard/SQL) to test gated endpoints, mirroring the client's existing `[DEBUG] Simulate subscriber` bypass in spirit, server-side. Real billing wiring is separate future work.
  3. **Chat grounding is header-level only.** Only parent/header rows are synced to Postgres today (`monthly_plan`, `full_assessment_result`, `chat_message`, ...) — nested `plan_day`/`workout_set_log` detail isn't synced yet, so `get_active_plan()`/`get_workout_history()` (chat-ai.md's tool-calling design) return summaries, not full plan/log detail. Known limitation, not fabricated data; a fast-follow once the corresponding sync participants sync nested rows.
  4. **Chat replies are text-only this pass** — `exerciseSuggestion`/`suggestedReplies` extraction from the model's answer (chat-ai.md §5) is deferred (would need a second structured-output call per turn, conflicting with the token-cost discipline in `pricing.md` §2); the client already renders both fields as optional/absent gracefully, so nothing breaks, but "Terapkan ke Plan" won't trigger yet.
  5. **`exercise` Postgres table** seeded once from the same bundled `files/exercises.json` (ADR-007's merged shape) via a generated migration (`20260731000002_seed_exercise.sql`) — used for `exerciseId` validation (AI Custom Plan) and the `get_exercise`/`search_exercises` chat tools.
  6. **Client-side fix bundled in this pass:** `AiCustomPlanRepositoryImpl` had no `AuthRepository` dependency and sent no `Authorization` header (unlike `MealPlanRepositoryImpl`/`ChatRepositoryImpl`) — fixed to match the other two exactly, since the backend needs a user id for entitlement/quota checks.
- **Rationale:** dev asked to make AI personalization/chat/meal-plan "berfungsi dulu sesuai dengan yang diharapkan" (work first, as currently documented) rather than resolve the pricing.md sync in the same pass — this ADR is the single place that lists every simplification taken to get there, so `kmp-auditor` doesn't flag them as unexplained gaps.
- **Approved by:** dev (fghilmany), 2026-07-31, via plan-mode approval (`/Users/faris/.claude/plans/memoized-prancing-starlight.md`).

## ADR-009 — MVP pivot: AI features free (no paywall) during showcase phase, login still required

- **Date:** 2026-11-09
- **Decision:** Temporarily reverses `00-overview.md` keputusan #8 ("Strict Subscribe, tanpa trial") for the MVP showcase phase. Dev wants to let people try the app and share the experience before turning on billing. Three parts:
  1. **AI Custom Plan, Chat, and Meal Plan are free for any logged-in user** during this phase — the Subscribe gate is loosened, not removed structurally. Entitlement check logic changes (all `LOGGED_IN` users treated as entitled to AI features); the `Set<Capability>` pattern and `user_entitlement` table from ADR-008 are unchanged.
  2. **Login (Google one-tap) stays mandatory for AI features**, even though they're free — rejected making AI fully anonymous. Rationale: login gives a stable per-user identity needed for quota enforcement; without it there's no reliable way to rate-limit (device reset / reinstall would reset any anonymous counter), and `Tech Plan/pricing.md` already flagged quota-farming risk at this tier. Guest/local-only features (Quick Assessment, Monthly Plan template, PT Mode, Library) are unaffected — already login-free per existing architecture.
  3. **New quotas for the free showcase phase**: **Chat 20 messages/day**, **Meal Plan 5 "new" generations/month** (manual edits to an existing generated plan don't count against the quota — only calls that hit the LLM do). These replace the 5/day chat figure carried over from ADR-008 and the undocumented Meal Plan limit; superseded if/when the `pricing.md`/issue #88 sync (OpenRouter/MiMo, 20/month chat) happens.
  4. **Manual kill-switch**: add `ai_access_blocked boolean default false` (+ optional `blocked_reason text`) to `user_entitlement`. Every AI-calling Edge Function (`chat-send`, `meal-plan-generate`, `ai-custom-plan-generate`) checks this flag before processing and returns a clear denial message if set. Toggled by hand via Supabase SQL/Table Editor — no admin UI this pass. Addresses the still-open "per-capability kill switch" item from `Tech Plan/pricing.md` §7, scoped down to a single on/off flag rather than full `app_config`-driven kill switches.
- **Rationale:** dev decided a free-to-try MVP (to gather feedback/share experience) is more valuable right now than monetizing immediately; login-required-but-free balances that goal against AI backend cost exposure and abuse risk.
- **Approved by:** dev (fghilmany), 2026-11-09, via chat planning discussion.
- **Superseded in part, 2026-11-10:** Meal Plan generation moved from "1 call = 1 week" to "1 call = 1 day" (Doze/background-network issues with the week-loop design, see `docs/techspec/meal-plan.md` §3). The **Meal Plan 5/month** figure from part 3 above no longer fits — 5/month would exhaust itself filling less than one week once generation is per-day. Replaced with **Meal Plan 1/day** (`supabase/functions/_shared/quota.ts`), one generate-or-regenerate tap per calendar day regardless of which day-slot is picked. Chat's 20/day figure is unaffected.
