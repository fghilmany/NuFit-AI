# Exercises Seed — Gaps & Judgment Calls

Source: `Gym Techniques/` (7 equipment-category folders) + `database-gerakan-dikelompokkan-per-kategori-pola-gerak.md`.
Output: `docs/data/exercises.json` (135 exercises).

## Per-category count (sanity check)

| Category | Count |
|---|---|
| BARBELL | 14 |
| BODYWEIGHT | 45 |
| CARDIO_EQUIPMENT | 8 |
| DUMBBELL | 29 |
| KETTLEBELL | 12 |
| MACHINE_CABLE | 19 |
| PULL_UP_BAR | 8 |
| **Total** | **135** |

Matches the expected ~135 total, and matches each category's own katalog table row count (cross-checked) and each category's sum of batch-file exercise counts.

## Dangling ID references

**None found.** Every `rantaiRegresi`, `rantaiProgresi`, and `substitusiSetara` value that carried an explicit ID resolved to a real `id` present in the dataset. This was verified programmatically (build script nulls + logs any non-resolving reference before writing the JSON; the run reported 0 dangling references). The source docs were in fact careful about this — the root doc's own "Gap jujur" section calls out exactly which substitution slots are intentionally left undocumented, and those are precisely the ones we set to `null` (see below) rather than the ones with real IDs.

## Fields set to `null`/`[]` because no explicit ID/value was given in source (not "dangling", just undocumented)

These are prose progression/regression mentions with **no ID at all** (a named exercise or technique with no code to reference) — set to `null` per the "don't invent IDs" rule:

- `BW-CORR-005` Side-Lying Leg Raise — progresi utama named "Clamshell dengan Resistance Band", no ID exists anywhere in the corpus (Resistance Band category has no database at all, confirmed by the root doc). `rantaiProgresi` = `null`.
- `BW-CORE-003` Side Plank — progresi utama named "Pallof Press" (implying the resistance-band variant), no ID given in that bullet. Note: `MC-CORE-000` Cable Pallof Press does exist in the Machine/Cable catalog, but the source text did not attach an ID to this specific mention, and it's a different equipment path (band vs. cable) than the one being progressed from (bodyweight), so I did not infer the link. `rantaiProgresi` = `null`.
- `BW-LUNGE-002` Step-Up — regresi described only as "turunkan ketinggian bangku" (lower the bench height), a parameter change rather than a different exercise/ID. `rantaiRegresi` = `null`.
- `MC-SQUAT-002` Leg Extension, `MC-HINGE-000` Seated Leg Curl, `MC-PUSHH-001` Cable Chest Fly, `MC-PUSHV-001` Cable Lateral Raise, `MC-PULLV-002` Straight-Arm Pulldown — explicitly documented as isolation/accessory movements with "tidak punya regresi bertingkat" / "tidak wajib naik", i.e. no chain by design. `rantaiRegresi`/`rantaiProgresi` = `null` (intentional, not a gap).
- Several "Progresi utama: tambah beban/rep di gerakan yang sama" entries (e.g. `BB-SQUAT-001`, `BB-HINGE-002`, `BB-PUSHH-002`, `MC-SQUAT-001`'s self-referential option, `DB-HINGE-003`, `DB-CORE-002`) describe progression via load/rep increase on the *same* exercise, not a different ID — correctly left `null` since there is no next-ID to point to. Where the same bullet also offered an *alternate-category* ID (e.g. `MC-SQUAT-001` → alt "Kalau Dumbbell tersedia: DB-SQUAT-000"), that alternate ID was used instead, per the instruction to prefer a real ID when one exists in the text.

## `areaTerbebani`: null vs `[]`

Per the root doc's documented gap, `areaTerbebani` is **only** populated for Bodyweight and Dumbbell katalogs. For Barbell, Cardio Equipment, Kettlebell, Machine/Cable, and Pull-up Bar, `areaTerbebani` is `null` for every exercise (category-level gap, not per-exercise ambiguity).

Within Bodyweight/Dumbbell, where the source text explicitly says "tidak ada beban signifikan" (e.g. Thoracic Rotation, Side-Lying Leg Raise, Ankle Circles, Figure-4 Glute Stretch, Wall Calf Stretch), I encoded that as an **empty array `[]`** (explicitly documented "no load"), reserving `null` strictly for "category doesn't have this column at all."

## `substitusiSetara`

Populated only where the source katalog table explicitly filled it in (Bodyweight/Dumbbell, 5 core slots at Standar level, per the documented prioritization). One entry is a deliberately imperfect equivalence, kept because the source explicitly names it as the intended fallback rather than leaving it blank:

- `DB-CORE-001` Dumbbell Russian Twist → `substitusiSetara.bodyweight = "BW-CORE-005"` (Mountain Climber). Source text is explicit that this is *not* a true equivalent ("BW-CORE-005 Mountain Climber sbg pendekatan terdekat, bukan level setara sempurna") — flagging here since it's a judgment call to include it at all rather than null it.
- `BW-CORE-002` Plank has no true equivalent substitution documented for dumbbell/mesin/band (source explicitly says none exists and Plank itself is its own fallback since it needs no equipment) — left `substitusiSetara = null` rather than invent a self-reference.

## Classification judgment calls

1. **`KB-FULLBODY-000` Turkish Get-Up** — source `Pola Gerak` is literally "Full-Body (integrasi, bukan pola gerak tunggal)", which has no matching enum value. Per the task instructions, mapped to `movementPattern: "CORE"` as the dominant/primary pattern (the exercise's own description emphasizes "Core (integrasi penuh...)" as the first-listed primary muscle target and the multi-plane core-stability demand is the throughline across all 7 stages). Documented here as instructed rather than silently guessed.

2. **Stretch entries have no `level`** — all 8 `BW-STRETCH-*` entries plus the two dual-role entries (`BW-CORR-003`, `BW-CORR-007`, which are Corrective, not Stretch-category, so they keep `level: "REGRESI"`) have source `Level: —` (explicitly "no level applies" — the root doc treats Stretch as a distinct pendinginan-only category outside the Regresi/Standar/Progresi ladder). Since the target schema's `level` enum has no "N/A" value, I set `level: null` for the 8 Stretch entries. This is a schema/source mismatch, not a data-quality issue — flagging for awareness in case the rule engine expects a non-null level everywhere.

3. **Compound level strings not cleanly in the mapping table:**
   - `CE-002` Sepeda Statis: raw level "Regresi-Standar" → mapped to `REGRESI` with `levelNote` documenting the raw text, since the instructions listed "Regresi-Standar" → REGRESI but didn't specify how to preserve the "-Standar" part.
   - `MC-PULLV-000` Lat Pulldown: raw level "Regresi/Standar" (not identical to the "Regresi/Entry" or "Regresi/Korektif" patterns given in the instructions) → mapped to `REGRESI` with `levelNote` preserving "Standar", by analogy with the closest listed rule.

4. **`isometricHeavy` judgment calls** (marked `true` where the source tags the movement as a sustained/timed isometric hold, even if not literally the word "berat"):
   - `BW-CORE-002` Plank, `BW-CORE-003` Side Plank, `BW-CORE-004` Hollow Hold — all explicitly `Tipe: ... Isometrik` with hold-based `Parameter Default` (20–60s+).
   - `BW-CARRY-000` Isometric Suitcase Hold — name itself says "Isometric", `Tipe: Unilateral, Isometrik/Dinamis ringan"`, parameter default includes a 20-30s/side static-hold variant. Included as `true` despite the "ringan" (light) qualifier since the exercise is explicitly isometric-mode by design; flagging as a judgment call since "ringan" could argue for `false`.
   - `PUB-PULLV-000` Dead Hang — `Tipe: Bilateral, Isometrik`, target hold times, textbook isometric hang.
   - All other exercises default to `false` per instructions ("default false if unclear").

5. **`highImpact` judgment calls:**
   - `BW-SQUAT-002` Jump Squat — plyometric, explicit landing-impact cues, flagged `WAJIB dihindari` for joint/bone flags → `true`.
   - `CE-007` Jump Rope — explicitly the highest-impact cardio modality in its own katalog ("Modalitas/Dampak: Tinggi") → `true`.
   - `CE-006` Stair Climber ("Sedang-Tinggi") and `CE-008` Battle Ropes ("Rendah (sendi) / Tinggi (intensitas)") were kept `false` — their own text clarifies joint/landing impact is low-to-moderate (no jumping/landing), even though cardiovascular *intensity* is high; `highImpact` here is read as landing/joint-impact, not cardio intensity, consistent with the field's purpose (paired with `flag_sendi` filtering).

6. **`polaGerakTerkait` token mapping** — source uses lowercase/abbreviated tokens (`pull_h`, `pull_v`, `push_h`, `push_v`, `hinge`, `squat`, `lunge`, `core`, `carry`, `cardio`) inconsistently across the root doc and the Stretch batch file. Normalized 1:1 to the `movementPattern` enum (e.g. `pull_horizontal`/`pull_h` → `PULL_HORIZONTAL`). No ambiguity in the mapping itself, just noting the normalization.

## Not extracted (out of scope per instructions)

Otot Utama/Sekunder, Cara Melakukan steps, Cue Verbal, Kesalahan Umum, Parameter Default (sets/reps/RPE), Alat & Ruang — all present in source prose but intentionally excluded from this seed per the task's scope (rule-engine seed, not a content library).

## `mediaSlug`

Left `null` for all 135 entries as instructed — media/free-exercise-db mapping is a separate pass.
