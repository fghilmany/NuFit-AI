# TechSpec: Profile & Settings

Source US: fghilmany/NuFit-AI#9 · Halaman: P-15 (Akun)

## 1. Overview & Goal

Replace the current P-15 stub (`AccountScreen` — login/logout + sync status only, explicitly marked "full scope stays in 09-profile-settings.md" when built in issue #83) with the real Account screen: body-data management with weight history, profile/assessment summary, sync status, informational content (medical disclaimer, privacy policy, about), and account deletion — all reusing existing local-first infrastructure (`BodyMeasurement` history table, `ProfileBasics`, `SyncRepository`, `EntitlementRepository`) rather than introducing new persistence patterns.

This is also the first feature to need a **first-launch blocking disclaimer** (shown once, ever, before any other screen) and the first to need a **server-side account-deletion boundary** (Supabase requires the `service_role` key for `auth.admin.deleteUser`, which must never live in the mobile client).

## 2. UI Reference

- Figma (P-15 Akun): https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1401
- Single static frame covering: Top App Bar ("Akun" + notification bell icon), Guest-state hero card ("Simpan Progres Kamu" + Google sign-in CTA), "Data Tubuh" (height/weight cards + weight history bar chart), "Profil & Assessment" ("Lengkapi profil" placeholder + "Lihat Hasil Assessment Terakhir"), "Sinkronisasi" (Google Fit / Health Connect card), "Informasi" (Disclaimer Medis / Kebijakan Privasi / Tentang Aplikasi), "Danger Zone" (Hapus Akun + version footer).
- **Divergence from source US (flagged and resolved with the dev, see §9)**: the Figma frame shows a "Google Fit / Health Connect" sync section and a notification bell icon that are absent from US #9's body/AC/DoD entirely. Resolved: both render visually per the design but are non-functional this pass (§3, §9).
- No separate Figma frame exists for the Logged-In hero state, the weight/height edit dialog, the Medical Disclaimer / Privacy Policy / About content screens, or the Delete Account confirmation — build these plain/functional, matching the app's existing Material 3 theme. Mirror `AiCustomPlanGateScreen.kt`'s `GateCard` (title/body/CTA layout) for simple content screens, and `EndSessionDialog.kt`/`MyPlansScreen.kt`'s `AlertDialog` usage for the destructive delete-account confirmation.

## 3. Scope

**In scope**
- Real Account screen (P-15) replacing the stub: Guest hero card (reuses login CTA), Data Tubuh section (height/weight display + weight history bar chart + edit), Profil & Assessment section (profile-completeness card + link to Assessment Detail), Sinkronisasi section (existing sync status/Sync Now, unchanged from stub), Informasi section (3 content entries), Danger Zone (Hapus Akun).
- Weight/height editing via a lightweight dialog (tap card → 2-field numeric dialog → `SaveBodyMeasurement`, already exists) — inserts a **new** history row, never overwrites (already guaranteed by `BodyMeasurement.sq`'s insert-only schema).
- Weight history bar chart: last 6 raw `BodyMeasurement` rows with non-null `weightKg`, plus a "vs ~30 days ago" delta badge.
- Profile/Assessment card: 3-state (Guest hidden / Logged-in-incomplete placeholder linking to `Route.CompleteProfile` / Logged-in-complete shows actual age+gender), plus "Lihat Hasil Assessment Terakhir" linking to `Route.AssessmentDetail`.
- First-launch Medical Disclaimer: new blocking screen shown exactly once ever (persisted flag), inserted into the Splash routing decision before ParQ/Home; re-readable anytime from Informasi → Disclaimer Medis (same content, non-blocking).
- Privacy Policy and Tentang Aplikasi: static content screens (plain text, in-repo copy — not fetched remotely).
- Delete Account: client-side confirmation flow + call to a documented (not-yet-deployed) Supabase Edge Function endpoint; on success, signs out locally (mirrors `signOut()`'s existing "local data stays cached" behavior — see §9 item 6) and returns to Guest. **Edge Function deployment itself is out of scope of this techspec's `/code` pass** — tracked as a separate tech-debt issue (§10), same pattern as issue #84.
- Google Fit / Health Connect card and the top-bar notification bell: rendered per the Figma design but **non-functional** ("Segera Hadir" / static icon respectively) — no Health Connect API integration, no notification system.

**Out of scope (explicit, deferred)**
- Health Connect / Google Fit real integration (Android-only API, no iOS equivalent exists in this codebase) — tracked as a future feature, not a tech-debt item, since it's genuinely new scope beyond US #9.
- Notification system (bell icon is decorative only).
- Recalculating Tahap 5 norma scores on gender change — that scoring/self-test engine doesn't exist anywhere in the codebase yet (confirmed via repo-wide search); `CompleteProfileScreen`'s existing gender edit is unchanged by this pass.
- Actual Supabase Edge Function implementation/deployment for account deletion (client contract only — §7, §10).
- SQLCipher / full local DB encryption (unchanged from `auth-sync.md`'s existing scope decision).

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin mengelola data diri dan pengaturan app di satu tempat, sehingga app tetap sesuai kondisi saya dan saya paham status data saya.

### AC Traceability

| # | AC (Given/When/Then) | Handled in | Blocking? |
|---|---|---|---|
| 1 | Given berat diubah, when disimpan, then riwayat berat bertambah (bukan menimpa) dan grafik progres terbarui | `SaveBodyMeasurement` (existing, insert-only) + new `WeightHistoryChart` component reading `getAllBodyMeasurements` | BLOCKING |
| 2 | Given pertama kali memakai app, when onboarding, then disclaimer medis tampil sekali dan bisa dibaca ulang dari P-15 | New `HasSeenMedicalDisclaimer`/`MarkMedicalDisclaimerSeen` usecases + `Route.MedicalDisclaimer` inserted at Splash routing (blocking, once) + Informasi entry (non-blocking, always) | BLOCKING |
| 3 | Given guest, when membuka bagian akun, then tampil CTA login + benefit, bukan error | Reuses existing `AccountState.Guest` branch (unchanged logic, restyled per Figma hero card) | Handle |
| 4 (UC-1 alt, login) | Ganti gender → skor norma Tahap 5 dihitung ulang otomatis | Deferred — scoring engine not built (§3) | Deferred |
| 5 (UC-3, DoD) | Delete account tersedia in-app sesuai kebijakan Google Play | Client confirmation + Edge Function contract (§7); actual deletion requires the Edge Function to be deployed (tech debt, §10) — **not fully satisfied end-to-end until that lands** | BLOCKING (client half) |

## 5. Domain Model

No new entities for body data / profile — reuses `BodyMeasurement` (`domain/onboarding/entity`) and `ProfileBasics` (`domain/profile/entity`) unchanged.

```kotlin
// domain/profilesettings/entity/WeightHistoryPoint.kt
// Thin presentation-facing projection, NOT persisted -- composed from BodyMeasurement rows.
data class WeightHistorySummary(
    val points: List<BodyMeasurement>, // chronological, oldest first, max 6, weightKg != null only
    val deltaKgVsThirtyDaysAgo: Double?, // null if <2 points or no point older than ~30 days
)

// domain/profilesettings/entity/AccountDeletionResult.kt
sealed interface AccountDeletionResult {
    data object Deleted : AccountDeletionResult
    data class Failed(val reason: String) : AccountDeletionResult
}
```

No new SQLDelight table for the medical disclaimer flag — a single boolean persisted via the existing `Settings` (multiplatform-settings) mechanism, mirroring `EntitlementLocalDataSource`'s pattern exactly (not a new `.sq` table for one boolean).

## 6. Architecture Breakdown

```
domain/profilesettings/
  entity/ WeightHistorySummary.kt, AccountDeletionResult.kt
  repository/ ProfileSettingsRepository.kt  -- disclaimer flag, delete-account call

usecase/profilesettings/
  GetWeightHistorySummary.kt        -- reads BodyMeasurement history, computes delta
  HasSeenMedicalDisclaimer.kt
  MarkMedicalDisclaimerSeen.kt
  DeleteAccount.kt                  -- calls repository, then signOut() + entitlement reset on success
  GetProfileSettingsSummary.kt      -- composes ProfileBasics? + latest assessment date (reuses GetAssessmentDetail's
                                        constituent usecases the same way GetAssessmentDetail itself does, not GetAssessmentDetail
                                        directly -- avoids depending on P-09's full aggregate, which requires a quick assessment
                                        to exist and would throw Failure.Database for a guest with none)

data/profilesettings/
  datasource/ ProfileSettingsLocalDataSource.kt   -- wraps Settings for the disclaimer flag
  datasource/ AccountDeletionRemoteDataSource.kt  -- plain Ktor POST to the Edge Function contract (§7)
  repository/ ProfileSettingsRepositoryImpl.kt

presentation/profilesettings/  (renamed from presentation/account/ -- see note below)
  viewmodel/ AccountViewModel.kt          -- extended, not replaced (existing Guest/LoggedIn/Error states gain new fields)
  viewmodel/ MedicalDisclaimerViewModel.kt -- shared by both the blocking first-run screen and the re-readable Informasi entry
  screen/ AccountScreen.kt                -- rebuilt per Figma
  screen/ MedicalDisclaimerScreen.kt
  screen/ PrivacyPolicyScreen.kt
  screen/ AboutAppScreen.kt
  component/ WeightHistoryChart.kt
  component/ BodyDataEditDialog.kt
  component/ DeleteAccountConfirmDialog.kt
```

**Note on `presentation/account` → `presentation/profilesettings`**: the existing folder is named after the stub (`account`), but the feature file is `09-profile-settings.md`. Per the mirror-existing-pattern rule, every other feature's presentation folder matches its feature name (`aicustomplan`, `assessmentdetail`, `planmanagement`), so `/code` should rename `presentation/account/` → `presentation/profilesettings/` while implementing, moving `AccountScreen`/`AccountViewModel` as-is (content extended, package renamed) rather than leaving a naming mismatch. This is a rename, not new scope — flag it to the dev as part of the diff, don't ask separately.

No new expect/actual needed — Health Connect (which would require one) is deferred (§3).

## 7. API / Data Contract

### SQLDelight

No new `.sq` files. Reuses `body_measurement` (`BodyMeasurement.sq`) and `user_profile` (`UserProfile.sq`) unchanged — both already fit this feature's needs.

### Settings keys (new, in `ProfileSettingsLocalDataSource`, mirrors `EntitlementLocalDataSource`)

```kotlin
private const val KEY_HAS_SEEN_MEDICAL_DISCLAIMER = "profile_settings_has_seen_medical_disclaimer"
fun hasSeenMedicalDisclaimer(): Boolean = settings.getBoolean(KEY_HAS_SEEN_MEDICAL_DISCLAIMER, false)
fun markMedicalDisclaimerSeen() = settings.putBoolean(KEY_HAS_SEEN_MEDICAL_DISCLAIMER, true)
```

### Delete Account — Edge Function contract (client-side call; server deployment tracked separately, §10)

```
POST {SUPABASE_URL}/functions/v1/delete-account
Headers: Authorization: Bearer {access_token}, apikey: {SUPABASE_ANON_KEY}
Body: {} (user identified via the bearer token server-side, mirrors AuthRemoteDataSource's existing header pattern)

200 OK   -> account + cloud data deleted
401      -> AppResult.Error(Failure.Unauthorized())  (mirrors AuthRepositoryImpl's existing 401 mapping)
5xx      -> AppResult.Error(Failure.Network(...))
```

`AccountDeletionRemoteDataSource` mirrors `AuthRemoteDataSource`'s exact shape (plain Ktor `HttpClient`, same header pattern, same `SUPABASE_URL`/`SUPABASE_ANON_KEY` constants already filled in — no new placeholder needed since Supabase is already provisioned this session). `DeleteAccount` usecase: on `AppResult.Success`, calls the existing `SignOut` usecase (clears local session + `entitlementRepository.setLoggedIn(false)`) so the device returns to Guest — local on-device data (plans, logs, body measurements) is **not** wiped, consistent with `signOut()`'s existing documented behavior (`AuthRepository.signOut` KDoc: "local data stays cached, never wiped").

### DI

- `profileSettingsModule` (new file) — registers everything under `data/profilesettings`, `usecase/profilesettings`, and the new screens' ViewModels (`MedicalDisclaimerViewModel`).
- `AccountViewModel`'s registration moves from `authModule` into `profileSettingsModule` (feature-ownership cleanup that follows from the `presentation/account` → `presentation/profilesettings` rename) — constructor gains `getWeightHistorySummary`, `getProfileSettingsSummary`, `deleteAccount`.
- `SplashViewModel`'s constructor gains `hasSeenMedicalDisclaimer` (registered in the existing `onboardingModule` where `SplashViewModel` already lives).

### Navigation

New routes in `Routes.kt`:
```kotlin
@Serializable data object MedicalDisclaimer : Route  // blocking, first-run only; also pushed non-blocking from Account
@Serializable data object PrivacyPolicy : Route
@Serializable data object AboutApp : Route
```
`Splash` routing logic changes from a 2-way branch (`hasLocalProfile ? Home : ParQ`) to a 3-way check: `!hasSeenMedicalDisclaimer` takes priority over both (routes to `MedicalDisclaimer`, which itself then routes on to `Home`/`ParQ` per the existing logic once acknowledged — the disclaimer screen needs `hasLocalProfile` passed through or re-queried after acknowledgement).

## 8. State & Error Handling

- **Weight history chart**: empty state (0 measurements) → "Belum ada data" placeholder, not an empty chart. 1 measurement → chart shows a single bar, delta badge hidden (needs ≥2 points spanning >0 days).
- **Height/weight cards**: null value → "—" instead of blank/crash (mirrors existing `BodyMeasurement.bmi`'s null-safe pattern).
- **Profile/Assessment card — Guest**: hidden entirely (ProfileBasics is a login-only concept per keputusan #11); only "Lihat Hasil Assessment Terakhir" shows (guests do have Quick Assessment results).
- **Delete Account**: `Idle → Confirming (dialog) → Deleting (loading, CTA disabled) → Success (navigate to Splash/Guest) | Error (dialog stays, shows Failure.message, account untouched)`. Disabled entirely (CTA + explanatory copy) when offline — never queued.
- **Medical Disclaimer (first-run)**: cannot be dismissed without explicit acknowledgement tap (same pattern as `DoctorConsultScreen`'s existing confirm-to-proceed gate) — no back-button skip.
- Sync/guest/logged-in states: unchanged from the existing stub's `AccountState` sealed interface (`Loading`/`Guest`/`LoggedIn`/`Error`), only its `LoggedIn` payload grows new fields.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | Google Fit / Health Connect section (Figma, absent from US) | Defer | Rendered per design, "Segera Hadir" badge, no tap action, no API integration |
| 2 | Notification bell icon (Figma, absent from US) | Defer | Static icon, no badge, no tap action |
| 3 | Weight/height edit interaction (not shown in mockup) | Handle | Lightweight dialog (tap card → 2 numeric fields → save), not the full onboarding wizard |
| 4 | Weight history chart data window | Handle | Last 6 raw `BodyMeasurement` rows (no weekly aggregation); delta badge vs. closest measurement ≥~30 days old, hidden if unavailable |
| 5 | "Lihat Hasil Assessment Terakhir" source (Quick vs Full) | Handle | Most recent of either by `completedAt`/`updatedAt`, reusing the same constituent usecases `GetAssessmentDetail` already composes |
| 6 | Delete Account requires `service_role`, can't run client-only | Handle (client) / Defer (server) | Client sends the documented request; Edge Function deployment tracked as separate tech debt (§10) |
| 7 | Delete Account while offline | Handle | CTA disabled + explanatory copy; never queued for later sync (irreversible action) |
| 8 | Delete Account fate of local on-device data | Handle | Local data is kept (device becomes ordinary Guest-local), matching `signOut()`'s existing precedent — must be stated explicitly in the confirmation dialog copy |
| 9 | Medical disclaimer first-run trigger (nothing exists today) | Handle | New persisted flag + blocking screen inserted at Splash routing, ahead of ParQ/Home |
| 10 | Gender change → Tahap 5 norm score recompute | Defer | Scoring/self-test engine doesn't exist in the codebase yet |
| 11 | Height/weight never filled (skipped at onboarding) | Handle | Cards show "—", chart shows empty state — no crash |
| 12 | App killed mid-delete-account | Handle | Single atomic request; no partial local state mutation happens before a 200 response |
| 13 | `presentation/account` naming mismatch vs. feature name | Handle | Rename to `presentation/profilesettings` while implementing (§6 note) |

## 10. Definition of Done

- [ ] DoD global (lihat `00-overview.md`)
- [ ] Weight/height edits append a new `body_measurement` row (verified: never an `UPDATE`), chart reflects it immediately
- [ ] Medical disclaimer shown exactly once across app reinstall-free lifetime (flag persisted), re-readable anytime from Informasi without re-triggering the blocking flow
- [ ] Disclaimer medis direview (bukan pengganti dokter/PT bersertifikat) — content sign-off from the dev, not just implemented
- [ ] Guest sees CTA + benefit copy in the account area, never an error state
- [ ] Delete Account: client confirmation flow, offline-disabled CTA, and local-data-kept messaging all implemented and unit-tested; **end-to-end deletion is NOT complete until the Edge Function (tracked separately) is deployed** — this techspec's DoD covers the client half only, call this out explicitly when reporting done
- [ ] `presentation/account` renamed to `presentation/profilesettings`; `AccountViewModel` DI registration moved to the new `profileSettingsModule`
- [ ] New GitHub tech-debt issue filed for the Delete-Account Edge Function (mirrors issue #84's pattern), linked from this techspec's issue
