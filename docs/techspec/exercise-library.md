# TechSpec: Exercise Library

> Source US: [fghilmany/NuFit-AI#15](https://github.com/fghilmany/NuFit-AI/issues/15)
> Depends on (extends, not replaces): `docs/techspec/monthly-plan.md` §5 (`domain/exerciselibrary` entities already shipped: `Exercise`, `EquipmentCategory`, `MovementPattern`, `ExerciseLevel`, `BodyArea`, `ExerciseFlag`, seed pipeline)
> Source catalog: `Gym Techniques/` (7 equipment-category folders + `database-gerakan-dikelompokkan-per-kategori-pola-gerak.md`)

## 1. Overview & Goal

P-07 (browse/search/filter) and P-08 (exercise detail) — the presentation layer NuFit AI's rule engine has been silently depending on since `monthly-plan.md` shipped, but that no user has ever been able to see. The `Exercise` entity and its seed pipeline already exist; they were extracted for the rule engine's own needs (flags, pattern, level, equipment, media) and carry **zero display content** — no instructions, no common mistakes, no safety tips, no target-muscle labels. This techspec's first deliverable is a second extraction pass over the same source catalog to fill that gap (the content exists in the source `.md` files, per real content confirmed for `BW-SQUAT-002`'s "Cara Melakukan"/"Kesalahan Umum"/"Cue Verbal"/"Otot Utama" sections — it was just never pulled), then the P-07/P-08 screens on top of it.

## 2. UI Reference

File key: `dNMXJXWuUOoUF8lEaWC2Lw`

| Screen | Figma node | Status |
|---|---|---|
| P-07 Exercise Library (list) | [node-id=12-683](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-683) | Pull rate-limited this session (Figma MCP Starter plan) — dev-supplied screenshot used instead. Header (back/title/avatar), search bar "Cari gerakan...", 3 filter chips (Otot Target/Alat/Level, dropdown style), 2-column exercise card grid (photo + muscle-group badge overlay + name + level with signal-bar icon), "Butuh Saran?" AI CTA banner, bottom nav (Rencana tab shown active in the screenshot's context) |
| P-08 Exercise Detail | [node-id=12-835](https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-835) | Pull rate-limited — dev-supplied screenshot used instead. Header (back/title), 2 side-by-side photos (Awal/Akhir position badges), "Otot Target" chip row, "Instruksi Langkah-demi-Langkah" numbered card, "Hindari Hal Ini" red/error card (bold title + description per mistake), "Saran NuFit AI" mint gradient card (checkmark bullets), "Gerakan Alternatif" 2-card horizontal row (thumbnail + name + short subtitle) |
| Empty search/filter result state | **Not designed** | No frame. Build as a simple centered empty state (icon + message + "Hapus Filter" action), consistent with other undesigned-state treatments in this codebase |

Both screenshots should be re-verified against a real Figma pull once the MCP rate limit clears — flagged as a follow-up, not blocking this pass (screenshots are detailed enough to build from).

## 3. Scope

**In scope**
- **Content extraction (prerequisite step, same `/code` pass)**: re-extract `instructions`, `commonMistakes` (title+description pairs), `safetyTips`, `targetMusclesPrimary`/`targetMusclesSecondary` for all ~135 exercises from `Gym Techniques/*.md`, mirroring the original extraction pass's rigor (validate every exercise, no phantom IDs)
- `Exercise` entity extension + additive SQLDelight columns + updated seed JSON + `ExerciseSeedDto` mapper
- New `MuscleGroup` enum (coarse, closed set) driving the list-card badge and the "Otot Target" filter — derived from the finer-grained `targetMusclesPrimary` free-text labels at extraction time, not stored as a separate manual field
- P-07: search (name substring, client-side), filter (otot/alat/level, AND across categories, OR within a category — decision #7), 2-column grid, empty state
- P-08: photos (start/end, using existing `mediaSlug` → free-exercise-db mapping, same pipeline as the plan-generation image path), target muscle chips, numbered instructions, common mistakes card, safety tips card, alternatives (computed from `substitusiSetara` + same-pattern/same-equipment siblings — decision #2, not hand-curated)
- Navigation entry from `Route.Home`'s existing `QuickAccessBanner` (currently a no-op stub) — wiring it to P-07 for the first time
- Library remains the single source of truth: all references via `exerciseId`, no exercise content duplicated elsewhere (already true for plan generation; stays true here — P-07/P-08 read the same `Exercise` rows)

**Out of scope** (explicitly deferred)
- PT Mode (P-05) sheet/overlay entry point into P-08 — `05-pt-mode.md` not built yet; P-08 is a full-screen route only this pass (decision #6)
- Chat AI ("Butuh Saran?" banner target) — `10-chat-ai.md` planned, not built; banner renders but is inert (decision #9)
- Plans tab (P-14) entry point into P-07 — `07-plan-management.md` not built yet; Home quick-access is the only entry point this pass (keputusan #12's second entry point deferred)
- Resistance Band equipment category — source-confirmed gap, unchanged from `monthly-plan.md`
- Hand-curated/editorial "alternatives" list — computed heuristic only (decision #2)
- Server-side or indexed search — 135 exercises, pure in-memory filter is sufficient; revisit if the catalog grows an order of magnitude

## 4. User Story & Acceptance Criteria

> Sebagai user awam, saya ingin menjelajah daftar exercise dan mempelajari detail tiap gerakan (cara, kesalahan umum, keamanan), sehingga saya percaya diri melakukan gerakan dengan benar dan terhindar dari cedera.

**AC-1** Given filter otot + alat sekaligus, when diterapkan, then hasil sesuai kombinasi.
**AC-2** Given exercise apa pun, when dibuka, then instruksi tampil dalam Bahasa Indonesia.
**AC-3** Given dibuka dari sesi berjalan, when ditutup, then kembali ke posisi sesi tanpa kehilangan progress (timer/set tidak reset).
**AC-4** Semua data & gambar tersedia offline (bundled/pre-downloaded).
**AC-5** Common mistakes & tips keamanan wajib ada untuk semua exercise yang dipakai template.

### AC Traceability Table

| AC | Handled by | Blocking? |
|---|---|---|
| AC-1 | `usecase/exerciselibrary/FilterExercises.kt` (new) — AND across otot/alat/level, OR within a category (§9 item 7) | No |
| AC-2 | Extraction pass writes Indonesian instruction/mistake/tip strings directly (source catalog is already Indonesian); `presentation/exerciselibrary` renders them verbatim, no runtime translation | No |
| AC-3 | **Out of scope this pass** — PT Mode doesn't exist yet to lose/preserve progress against (§3, decision #6); revisit when `05-pt-mode.md` ships | No (deferred, not silently dropped — tracked) |
| AC-4 | Same bundled-seed pipeline as `monthly-plan.md` (`composeResources/files/exercises.json`), extended with the new content fields; images via existing `mediaSlug` → free-exercise-db mapping | No |
| AC-5 | **BLOCKING** — extraction pass must produce non-empty `commonMistakes`/`safetyTips` for every exercise referenced by any Local or Logged-In template (cross-checked against `docs/data/exercises.json`'s existing 135 IDs, all of which are template-reachable per `monthly-plan.md`'s pool construction) | **BLOCKING** — safety-relevant content, `/unittest` must assert non-empty on all 135 |

## 5. Domain Model

### Extended `Exercise` entity (`domain/exerciselibrary/entity/Exercise.kt`)

Additive fields only — every existing field/consumer (rule engine, plan generation) is untouched:

```
Exercise (extended)
  ... all existing fields unchanged ...
  primaryMuscleGroup: MuscleGroup          // new — drives P-07 card badge + "Otot Target" filter (coarse, closed set)
  targetMusclesPrimary: List<String>       // new — P-08 chip row, Indonesian display labels (e.g. "Kaki Depan (Quads)")
  targetMusclesSecondary: List<String>     // new — same, secondary muscles; empty list if none
  instructions: List<String>               // new — ordered "Cara Melakukan" steps, Indonesian
  commonMistakes: List<CommonMistake>      // new — from "Kesalahan Umum"
  safetyTips: List<String>                 // new — from "Cue Verbal"

CommonMistake                              // new value object
  title: String                            // e.g. "Punggung melengkung"
  description: String                      // e.g. "Jaga punggung tetap tegak."

MuscleGroup                                // new enum
  KAKI, DADA, PUNGGUNG, BAHU, LENGAN, INTI, GLUTES, KARDIO
```

### Business rule — extraction mapping (source → domain)

| Source catalog field | Domain field | Notes |
|---|---|---|
| `Otot Utama` | `targetMusclesPrimary` + derives `primaryMuscleGroup` | First/dominant muscle → `MuscleGroup` via a fixed translation table (e.g. "Quadriceps"/"Glutes" → `KAKI`, "Pectoralis" → `DADA`); full Indonesian label list → `targetMusclesPrimary` |
| `Otot Sekunder` | `targetMusclesSecondary` | Direct translation list, no group derivation needed |
| `Cara Melakukan` | `instructions` | Numbered list, preserve source order |
| `Kesalahan Umum` | `commonMistakes` | Source format is `"Problem → advice"` on one line — split at the arrow into `title`/`description` at extraction time, not at render time |
| `Cue Verbal` | `safetyTips` | Direct list; if a source entry has none (rare per the sample checked), fall back to a generic pattern-level tip rather than leaving empty (AC-5 is BLOCKING) |

### Business rule — level display mapping (decision #8)

`ExerciseLevel.REGRESI → "Pemula"`, `STANDAR → "Menengah"`, `PROGRESI → "Lanjutan"`. `KOREKTIF`/`AKSESORI` exercises are excluded from P-07's browsable list and the Level filter's option set entirely — they're plan-generation-only building blocks (warm-up correctives, accessory work), not user-facing "browse a technique" content. `GetExercisePool` (used by plan generation) is unaffected; the exclusion is a P-07-only view filter.

### Business rule — alternatives computation (decision #2, `usecase/exerciselibrary/GetExerciseAlternatives.kt`)

Not hand-curated. For a given `Exercise`:
1. Cross-equipment: resolve `substitusiSetara` (already exists, maps `EquipmentCategory → exerciseId`)
2. Same-equipment variant: other exercises sharing `movementPattern` + `equipmentCategory`, excluding self and excluding `CORRECTIVE`/`STRETCH`/`KOREKTIF`/`AKSESORI`
3. Cap at 4 results (Figma shows 2; cap is a safety ceiling, not a target)

This is a heuristic, not Figma-exact — documented as such since the design's specific picks (e.g. "Sumo Squat") aren't derivable from any currently-modeled relation.

## 6. Architecture Breakdown

Extends the existing `exerciselibrary` feature slug — no new slug, no dependency direction change (`monthly-plan.md`'s `domain/data/usecase` remain the base; this adds `presentation` for the first time).

```
shared/src/commonMain/kotlin/
├── domain/exerciselibrary/entity/
│   ├── Exercise.kt              // extend (additive fields, see §5)
│   ├── CommonMistake.kt         // new
│   └── MuscleGroup.kt           // new
│
├── usecase/exerciselibrary/
│   ├── GetExercisePool.kt       // existing, unchanged
│   ├── FilterExercises.kt       // new — pure function over an already-fetched pool (AC-1)
│   ├── GetExerciseDetail.kt     // new — wraps repository.getById()
│   └── GetExerciseAlternatives.kt // new — see §5 business rule
│
├── data/exerciselibrary/
│   ├── model/ExerciseSeedDto.kt         // extend: new fields + toEntity() mapping
│   ├── datasource/ExerciseLibrarySeedDataSource.kt  // unchanged (still reads the same bundled JSON path)
│   ├── datasource/ExerciseLibraryLocalDataSource.kt // extend: new columns in insert/select row mapping
│   └── repository/ExerciseLibraryRepositoryImpl.kt  // extend: map new columns in toEntity()
│
└── core/di/ExerciseLibraryModule.kt  // extend: register 3 new usecases
```

```
shared/src/commonMain/kotlin/com/fghilmany/nufitai/
├── presentation/exerciselibrary/
│   ├── viewmodel/
│   │   ├── ExerciseLibraryViewModel.kt   // P-07: sealed State (Loading/Loaded/Empty/Error), search+filter Events
│   │   └── ExerciseDetailViewModel.kt    // P-08: sealed State, loads by exerciseId
│   ├── screen/
│   │   ├── ExerciseLibraryScreen.kt      // P-07
│   │   └── ExerciseDetailScreen.kt       // P-08
│   └── component/
│       ├── ExerciseCard.kt               // P-07 grid cell (2-col), uses AppCard/AppElevatedCard (core/designsystem)
│       ├── FilterChipRow.kt              // 3 dropdown-style filter chips
│       ├── MuscleGroupCopy.kt            // MuscleGroup -> Indonesian short label (list-card badge)
│       └── ExerciseDetailSections.kt     // target-muscle chips, instructions card, mistakes card, tips card, alternatives row
└── core/navigation/
    └── Routes.kt / NavGraph.kt           // extend: Route.ExerciseLibrary, Route.ExerciseDetail(exerciseId)
```

expect/actual: none needed — pure local Compose + SQLDelight, no platform APIs, no network calls.

## 7. API / Data Contract

### SQLDelight

Additive columns only on the existing, already-shipped `exercise` table (`Exercise.sq`). Per `architecture.md` §4b, changing a shipped schema needs a migration or, in early dev, a recreation/version bump — this project is pre-release with only dev-local seeded data, so: **DB recreation** (uninstall/clear app data before first run post-merge), not a `.sqm` migration. New columns are nullable/JSON-array-as-TEXT so `ensureSeeded()`'s existing "seed only if empty" check needs no logic change — a wiped DB re-seeds fully via the updated JSON.

| `.sq` file | Change | Generated query | Schema status |
|---|---|---|---|
| `Exercise.sq` | Add columns: `primary_muscle_group TEXT NOT NULL`, `target_muscles_primary TEXT NOT NULL` (JSON array), `target_muscles_secondary TEXT NOT NULL` (JSON array), `instructions TEXT NOT NULL` (JSON array), `common_mistakes TEXT NOT NULL` (JSON array of `{title, description}`), `safety_tips TEXT NOT NULL` (JSON array); update `insertExercise` to include them | `exerciseQueries.insertExercise(...)` (signature grows) | **Schema change on a shipped table — DB recreation required, not additive-safe without it** |

### DI

`ExerciseLibraryModule` (existing, manual DSL per ADR-001) gains:
```kotlin
factory { FilterExercises() }
factory { GetExerciseDetail(repository = get()) }
factory { GetExerciseAlternatives(repository = get()) }
viewModel { ExerciseLibraryViewModel(getExercisePool = get(), filterExercises = get()) }
viewModel { ExerciseDetailViewModel(getExerciseDetail = get(), getExerciseAlternatives = get()) }
```
Add to `DiVerificationTest.kt` (already includes `exerciseLibraryModule` — no new module registration needed, just new bindings inside it).

### Navigation

New routes in `core/navigation/Routes.kt`:
- `Route.ExerciseLibrary` — entry from `Route.Home`'s existing `QuickAccessBanner` (`onClick = {}` today — this techspec wires it for the first time)
- `Route.ExerciseDetail(exerciseId: String)` — entry from `Route.ExerciseLibrary`'s cards; also the eventual entry point for P-04/P-05's "tap exercise" flows already stubbed elsewhere (`SessionDetailScreen` doesn't yet navigate to exercise detail — out of scope here, but the route existing unblocks it)

## 8. State & Error Handling

| Screen | States |
|---|---|
| `ExerciseLibraryScreen` (P-07) | `Loading`, `Loaded(exercises: List<Exercise>, query: String, filters: ExerciseFilter)`, `Empty(hasActiveFilters: Boolean)` (distinct empty-because-no-data vs empty-because-filtered, so the "hapus filter" suggestion only shows when relevant), `Error(Failure.Database)` |
| `ExerciseDetailScreen` (P-08) | `Loading`, `Loaded(exercise: Exercise, alternatives: List<Exercise>)`, `Error(Failure.Database)` (exercise not found is also `Error` — `exerciseId` should always be valid since it's only ever reached via a real `Exercise.id`, per DoD's "no exercise outside the library" invariant) |

`Failure.Database` is the only reachable `Failure` subtype (all local, no network). Search/filter run synchronously in-memory over an already-loaded pool — no separate loading state needed for filter changes, only initial pool load.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | Seeded `Exercise` data has no display content at all | **Handle** | Extraction pass (§5, §6) pulls it from the same source catalog; confirmed present via `BW-SQUAT-002` |
| 2 | "Gerakan Alternatif" relation is undefined (issue's own DoD flags this) | **Handle, scoped** | Computed heuristic from `substitusiSetara` + same-pattern siblings, not hand-curated (§5) |
| 3 | Target-muscle label translation (catalog is Latin/English muscle names) | **Handle** | Translation table at extraction time, not a runtime copy function (source data itself becomes Indonesian) |
| 4 | List-card badge doesn't map 1:1 to `MovementPattern` | **Handle** | New `MuscleGroup` enum derived from `targetMusclesPrimary` at extraction time (§5) |
| 5 | Empty search/filter result, no Figma frame | **Handle** | Standard empty state, `hasActiveFilters`-aware messaging |
| 6 | Opened from PT Mode as sheet/overlay (UC-2 alt flow) | **Defer** | PT Mode doesn't exist; P-08 is full-screen only this pass, revisit when `05-pt-mode.md` ships |
| 7 | Filter combination semantics unspecified beyond "sesuai kombinasi" | **Handle** | AND across otot/alat/level categories, OR within a category (standard faceted-filter convention) |
| 8 | `ExerciseLevel` has 5 values, Figma shows only 2 filter options | **Handle** | REGRESI/STANDAR/PROGRESI → Pemula/Menengah/Lanjutan; KOREKTIF/AKSESORI excluded from browse (§5) |
| 9 | "Butuh Saran?" banner target (Chat AI) doesn't exist | **Defer** | Renders per design, inert `onClick` — same treatment as other stubbed nav targets elsewhere in the app |
| 10 | Shipped `exercise` table schema changes | **Handle** | DB recreation (early dev, no real user data), documented in §7 rather than a `.sqm` migration |
| 11 | `mediaSlug` null for some exercises (pre-existing gap from `monthly-plan.md`) | **Handle** | P-08's photo section renders a placeholder illustration when null — same fallback already decided in `monthly-plan.md` §9 item 7, reused here, not re-litigated |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] Pipeline mapping gambar free-exercise-db ↔ katalog internal terdokumentasi (already true from `monthly-plan.md`; unchanged, just now actually *used* by a screen)
- [ ] Relasi "alternatif" antar exercise terdefinisi — `GetExerciseAlternatives` implemented and unit tested (§5 business rule), closing the issue's own flagged DoD gap
- [ ] Konten selaras katalog di folder `Gym Techniques` — extraction pass output spot-checked against source `.md` files, not paraphrased
- [ ] Library = satu-satunya sumber exercise — P-07/P-08 read only via `exerciseId`/`ExerciseLibraryRepository`, no hardcoded exercise content in presentation
- [ ] All 135 exercises have non-empty `instructions`, `commonMistakes`, `safetyTips` after extraction — asserted by a seed-integrity test (AC-5, BLOCKING)
- [ ] `FilterExercises` unit tested: AND-across-categories / OR-within-category (AC-1), including the KOREKTIF/AKSESORI exclusion (§5)
- [ ] `GetExerciseAlternatives` unit tested against at least one exercise with a real `substitusiSetara` entry and one same-pattern-sibling case
- [ ] `DiVerificationTest.checkModules()` passes with the 3 new bindings and 2 new ViewModels
- [ ] `QuickAccessBanner`'s `onClick` is no longer a no-op; navigates to `Route.ExerciseLibrary`
- [ ] Empty state (search/filter) manually verified for both "no data" and "filtered to zero" cases
