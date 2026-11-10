# TechSpec: Auth & Sync

Source US: #10 · Halaman: P-10 (Login), plus new minimal P-15 stub (Account) and a new "Lengkapi Profil" screen.

## 1. Overview & Goal

Give users an optional, one-tap Google/Apple sign-in that upgrades their tier from `GUEST` to `LOGGED_IN`, backs up all local data to the cloud, restores it on a new device, and keeps it synced in the background afterward — without ever blocking local-first functionality for users who stay guests.

This is the first feature in the codebase to introduce a real user identity, a real network auth flow, and a generic cross-table sync engine. Today `Tier`/`EntitlementRepository` only reflect two debug booleans (`SetDebugEntitlement`) with no backing session — this feature replaces the "is logged in" half of that with a real mechanism (the "is subscribed" half stays a debug toggle; billing/RevenueCat is a separate, later feature).

## 2. UI Reference

- Figma (P-10 Login): https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1558
- Screenshot reviewed: hero header "NuFit AI" + illustration, a decorative username/password card (see §3 scope note — NOT implemented, contradicts the locked Google-only decision), a "Simpan Progres Kamu" benefits section (3 rows: cloud backup, personal AI, restore on new device), then the real interactive elements: "Lanjutkan dengan Google" button, "Masuk dengan Apple" button (iOS only), "Nanti saja" (skip) link, and legal text ("Dengan melanjutkan, Anda menyetujui Syarat Penggunaan & Kebijakan Privasi").
- No Figma reference exists yet for "Lengkapi Profil" (age/gender) or the minimal Account (P-15 stub) screen — build these plain/functional, matching the app's existing Material 3 theme and card/button styles (mirror `AiCustomPlanGateScreen.kt`'s `GateCard` layout for both, since it's the closest existing analog: title, body copy, primary CTA, optional secondary CTA).

## 3. Scope

**In scope**
- `AuthRepository`: sign-in (Google/Apple ID token → Supabase session), session refresh, sign-out.
- Native ID-token retrieval is **stubbed behind expect/actual** (`GoogleSignInLauncher`, `AppleSignInLauncher`) per dev decision — no new SDK dependency added in this pass. Each actual currently returns `AppResult.Error(Failure.Network("Native sign-in not yet configured"))` with a `TODO(dev)` marking exactly where the real Credential Manager / `ASAuthorizationController` call goes. This keeps the rest of the architecture (session handling, sync engine, screens) fully buildable and testable today.
- Supabase accessed via plain Ktor HTTP (REST: `/auth/v1/token`, PostgREST `/rest/v1/{table}`) — consistent with ADR-004 (plain Ktor over a typed client), so **no `supabase-kt` dependency is added**. `SUPABASE_URL`/`SUPABASE_ANON_KEY` are placeholder constants (mirroring `AiCustomPlanRemoteDataSource.BASE_URL`'s existing pattern) the dev fills in once a Supabase project exists.
- Secure token storage: session token encrypted via platform Keystore (Android) / Keychain (iOS), expect/actual, **no new dependency** (uses `android.security.keystore`/`javax.crypto` and the iOS `Security` framework, both already available without a Gradle entry).
- "Lengkapi Profil" screen (age/gender, one-time, post-first-login) — new minimal `ProfileBasics` entity/table, decoupled from `FullAssessmentResult`.
- Generic sync engine at the **aggregate** level (5 aggregates, not 14 raw tables — see §5), timestamp/`updated_at`-cursor based, LWW per record, triggered on login success, app foreground (when tier ≠ GUEST and online), and after local writes to a syncable aggregate (debounced) — no OS-level background scheduler (WorkManager/BGTaskScheduler) in this pass.
- Minimal Account screen (P-15 stub): login/logout entry point, sync status indicator, guest benefits CTA. Wired to the currently-inert "Akun" bottom nav tab.
- Replaces the `onNavigateToLogin = {}` no-ops in `AiCustomPlanGateScreen`/`AssessmentDetailScreen` with a real `Route.Login`.

**Out of scope (explicit, deferred)**
- Real native Google/Apple SDK wiring (dev follow-up once OAuth client IDs exist — tracked via `TODO(dev)` in the two expect/actual files).
- Real Supabase project provisioning, RLS policies (documented conceptually in §7, not code).
- Full P-15 (body-data editing, disclaimers, delete-account) — stays homed in `09-profile-settings.md`; this feature only builds the minimal stub needed for its own DoD.
- Full local database encryption (SQLCipher) — only the session token is encrypted at rest.
- OS-level background sync scheduling — sync runs on the triggers listed above only.
- Wiring `FullAssessmentResult.age`/`gender` to read from the new `ProfileBasics` — left as a future integration note; not touched this pass.
- Recalculating Tahap 5 norma scores on gender change — that scoring engine doesn't exist yet.
- The decorative username/email/password card seen in the Figma screenshot — treated as non-functional filler; only the Google/Apple/Skip CTAs are real, per the locked "Google-only, no form" decision.
- Account deletion — `09-profile-settings.md`.

## 4. User Story & Acceptance Criteria

> Sebagai user, saya ingin login sekali tap dengan akun Google tanpa mengisi form apa pun, sehingga data saya ter-backup di cloud, bisa dipulihkan di device baru, dan saya bisa memakai fitur personalisasi & AI — sementara tanpa login app tetap berfungsi penuh secara lokal.

### AC Traceability

| # | AC (Given/When/Then) | Handled in | Blocking? |
|---|---|---|---|
| 1 | Given guest with local data, when first login, then all local data uploads without loss | `SyncRepositoryImpl.syncNow()` initial merge pass (upload-then-reconcile, §5/§7) | BLOCKING |
| 2 | Given login on a new device, when done, then profile/plan/log history are restored | Same sync pass, pull direction | BLOCKING |
| 3 | Given no internet, when opening a non-AI feature, then the app works fully without forcing login | Unchanged — every existing feature already reads local-first; `Route.Login`/`Route.Account` never gate non-AI screens | BLOCKING |
| 4 | Given data changes while online, when sync runs, then it happens in the background without disrupting UI | `SyncRepository.status: StateFlow<SyncStatus>` observed only by the Account screen; no other screen blocks on it | Handle |
| 5 (alt, UC-1) | Given gender changed later, then Tahap 5 norma score recalculates | Out of scope — scoring engine not built yet (documented in §3) | Deferred |
| 6 (alt, UC-2) | Given a two-device conflict, then last-write-wins per record | `updated_at` comparison during pull (§7) | BLOCKING |
| 7 (UC-3) | Given no account, then all local features work fully; AI/sync show a login gate only when accessed | Already true today for AI (`AiCustomPlanGateScreen`); no change needed for other local features | Handle |

## 5. Domain Model

```kotlin
// domain/auth/entity/AuthSession.kt
data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
)

enum class AuthProvider { GOOGLE, APPLE }

// domain/auth/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun signIn(provider: AuthProvider, idToken: String): AppResult<AuthSession>
    suspend fun getCurrentSession(): AuthSession?
    suspend fun refreshSessionIfNeeded(): AppResult<AuthSession?>
    suspend fun signOut(): AppResult<Unit>
}

// domain/profile/entity/ProfileBasics.kt
data class ProfileBasics(val age: Int, val gender: Gender, val updatedAt: Instant)
// reuses existing domain.fullassessment.entity.Gender — do not duplicate the enum

// domain/profile/repository/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfileBasics(): ProfileBasics?
    suspend fun saveProfileBasics(age: Int, gender: Gender): AppResult<Unit>
}

// domain/sync/entity/SyncStatus.kt
sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val lastSyncedAt: Instant) : SyncStatus
    data class Failed(val lastAttemptAt: Instant, val failure: Failure) : SyncStatus
}

// domain/sync/repository/SyncRepository.kt
interface SyncRepository {
    val status: StateFlow<SyncStatus>
    suspend fun syncNow(): AppResult<Unit> // push local changes, then pull remote changes
}
```

**Business rules**
- A user is `LOGGED_IN` the moment `AuthRepository.signIn()` succeeds; `EntitlementLocalDataSource` gets a real `setLoggedIn(true)` call (new method, replacing the debug-only path for this one flag — `isSubscribed` still only moves via `SetDebugEntitlement` until billing lands).
- Sync is **aggregate-based, not per-raw-table**: 5 aggregates map onto the 14 SQLDelight tables (nested children travel with their parent in one payload, exactly like `AiCustomPlanRemoteDataSource` already bundles nested day/exercise DTOs in one request):
  1. `MonthlyPlanAggregate` — `monthly_plan` + `plan_day` + `planned_exercise`
  2. `FullAssessmentAggregate` — `full_assessment_result` + `full_assessment_flag_detail`
  3. `QuickAssessmentAggregate` — `quick_assessment_result`
  4. `ParQAggregate` — `par_q_result` + `par_q_answer_detail`
  5. `BodyMeasurementAggregate` — `body_measurement` (append-only history)
  6. `WorkoutHistoryAggregate` — `workout_session` + `workout_exercise_log` + `workout_set_log` + `plan_day_session_log`
- `exercise` (bundled catalog) is explicitly **never synced** — it's static, identical across installs.
- Conflict rule (locked decision): last-write-wins **per record**, compared via each aggregate's own `updated_at`. No merge UI; the losing side's edit is silently discarded but logged via Kermit for future auditability.
- Only `monthly_plan` has delete-sync (its existing `deleted_at` soft-delete, already built in issue #82); every other aggregate is append/update-only from a sync perspective — no new tombstone columns needed elsewhere.
- Logout clears the session/tier flag only; local data stays cached (local-first). Logging into a *different* account on the same device reconciles via the same LWW pass — it is not a blind overwrite.

## 6. Architecture Breakdown

```
domain/auth/{entity,repository}/            — AuthSession, AuthProvider, AuthRepository
domain/profile/{entity,repository}/         — ProfileBasics, ProfileRepository
domain/sync/{entity,repository}/            — SyncStatus, SyncRepository

usecase/auth/SignIn.kt                      — invoke(provider, idToken): AppResult<AuthSession>; on success: EntitlementLocalDataSource.setLoggedIn(true), then SyncRepository.syncNow()
usecase/auth/SignOut.kt                     — invoke(): AppResult<Unit>; clears session + setLoggedIn(false); local data untouched
usecase/profile/GetProfileBasics.kt
usecase/profile/SaveProfileBasics.kt
usecase/sync/SyncNow.kt                     — thin wrapper over SyncRepository.syncNow()
usecase/sync/ObserveSyncStatus.kt           — exposes SyncRepository.status

core/security/TokenStorage.kt               — expect class TokenStorage { fun save(json: String); fun read(): String?; fun clear() }
core/security/TokenStorage.android.kt       — actual: AndroidKeyStore AES key + Cipher, Base64 ciphertext persisted via existing multiplatform-settings Settings instance
core/security/TokenStorage.ios.kt           — actual: Keychain (Security framework) via cinterop

data/auth/model/SupabaseAuthResponseDto.kt  — @Serializable, toEntity(): AuthSession
data/auth/datasource/AuthRemoteDataSource.kt    — Ktor calls to Supabase Auth REST
data/auth/datasource/AuthLocalDataSource.kt     — wraps TokenStorage, (de)serializes AuthSession via kotlinx.serialization
data/auth/repository/AuthRepositoryImpl.kt

data/profile/datasource/ProfileLocalDataSource.kt   — SQLDelight userProfileQueries
data/profile/repository/ProfileRepositoryImpl.kt

data/sync/contract/SyncParticipant.kt       — interface implemented by one small adapter per aggregate (see §7)
data/sync/datasource/SyncRemoteDataSource.kt — generic inline<reified T> push/pull over PostgREST
data/sync/repository/SyncRepositoryImpl.kt  — orchestrates: for each SyncParticipant, getChangedSince → push; then pull → upsertRemoteRows, comparing updated_at
data/monthlyplan/sync/MonthlyPlanSyncParticipant.kt   — worked example adapter (mirror for the other 5 aggregates, one small file each)

presentation/auth/viewmodel/LoginViewModel.kt + screen/LoginScreen.kt           (P-10)
presentation/auth/viewmodel/CompleteProfileViewModel.kt + screen/CompleteProfileScreen.kt
presentation/account/viewmodel/AccountViewModel.kt + screen/AccountScreen.kt   (P-15 stub)

core/navigation/Routes.kt   — + Route.Login(returnTo: LoginReturnTarget = LoginReturnTarget.HOME), Route.CompleteProfile(returnTo: LoginReturnTarget), Route.Account
core/navigation/NavGraph.kt — wire the 3 routes; replace onNavigateToLogin = {} in AiCustomPlanGateScreen and the FullAssessmentCta in AssessmentDetailScreen; wire BottomNavBar's "Akun" tab to Route.Account

core/di/AuthModule.kt, ProfileModule.kt, SyncModule.kt   — manual Koin DSL, per ADR-001
```

`LoginReturnTarget` (new small enum in `core/navigation/`): `HOME, AI_CUSTOM_PLAN, FULL_ASSESSMENT` — generalizes the existing `FullAssessmentStub(continueToAiCustomPlan: Boolean)` boolean-flag precedent now that 3 call sites need to route back after login.

## 7. API / Data Contract

**Auth (Supabase Auth REST, no SDK):**
- Sign in: `POST {SUPABASE_URL}/auth/v1/token?grant_type=id_token` — body `{ "provider": "google"|"apple", "id_token": "<native SDK token>" }` → `{ access_token, refresh_token, expires_in, user: { id } }`
- Refresh: `POST {SUPABASE_URL}/auth/v1/token?grant_type=refresh_token` — body `{ "refresh_token": "..." }`
- `SUPABASE_URL`/`SUPABASE_ANON_KEY` are placeholder `const val`s in `AuthRemoteDataSource`, mirroring `AiCustomPlanRemoteDataSource.BASE_URL`'s existing pattern — dev fills in before first real-device test.

**Sync (PostgREST, per aggregate):**
- Push: `POST {SUPABASE_URL}/rest/v1/{aggregate_table}?on_conflict=id` header `Prefer: resolution=merge-duplicates`, body = JSON array of changed rows (nested children inline), headers `apikey`, `Authorization: Bearer {accessToken}`.
- Pull: `GET {SUPABASE_URL}/rest/v1/{aggregate_table}?user_id=eq.{userId}&updated_at=gt.{lastSyncedAtIso}`.
- **Backend note for the dev** (infra, not client code): each aggregate's Supabase table needs an RLS policy `user_id = auth.uid()` — out of this KMP client's scope but required before push/pull will actually work end-to-end.
- `SyncParticipant` contract (`data/sync/contract/SyncParticipant.kt`):
  ```kotlin
  interface SyncParticipant {
      val tableName: String
      suspend fun getChangedSince(since: Instant): List<JsonObject>
      suspend fun upsertRemoteRows(rows: List<JsonObject>)
      suspend fun backfillUserId(userId: String) // one-time, run once on first successful login
  }
  ```
  `SyncRepositoryImpl` holds `private val participants: List<SyncParticipant>` (built explicitly in `SyncModule.kt` — Koin manual DSL has no auto-multibinding, so the list is assembled by hand: `single<List<SyncParticipant>> { listOf(get<MonthlyPlanSyncParticipant>(), ...) }`).
  `lastSyncedAt` is one global cursor stored via `multiplatform-settings` (key `sync_last_synced_at`), not a DB table — coarse-grained but sufficient at this data volume; document this as a known simplification.

**SQLDelight changes** (direct `.sq` edits, no `.sqm` migration — consistent with the project's current early-dev practice of direct schema edits + dev clears app data, as already done for the `monthly_plan.deleted_at` addition and the enum-rename fixes this session):

| File | Change | Resulting query object |
|---|---|---|
| `MonthlyPlan.sq` | + `user_id TEXT`, `updated_at INTEGER NOT NULL DEFAULT 0`; + `selectChangedSince` query; INSERT/UPDATE now bind `updated_at` | `monthlyPlanQueries` |
| `FullAssessmentResult.sq` | same 2 columns + query | `fullAssessmentResultQueries` |
| `QuickAssessmentResult.sq` | same | `quickAssessmentResultQueries` |
| `ParQResult.sq` | same | `parQResultQueries` |
| `BodyMeasurement.sq` | same | `bodyMeasurementQueries` |
| `WorkoutSession.sq` | same | `workoutSessionQueries` |
| `UserProfile.sq` (new) | `user_profile(user_id TEXT PRIMARY KEY, age INTEGER NOT NULL, gender TEXT NOT NULL, updated_at INTEGER NOT NULL)` | `userProfileQueries` |

`packageName`: confirm against `shared/build.gradle.kts`'s `sqldelight { databases { ... packageName.set(...) } }` before referencing `*Queries` — per architecture.md §4b, don't guess.

**DI**: `AuthModule.kt` (AuthRemoteDataSource, AuthLocalDataSource, AuthRepositoryImpl, SignIn, SignOut, LoginViewModel, CompleteProfileViewModel), `ProfileModule.kt` (ProfileRepositoryImpl, GetProfileBasics, SaveProfileBasics), `SyncModule.kt` (SyncRemoteDataSource, SyncRepositoryImpl, the 6 `*SyncParticipant` adapters, SyncNow, ObserveSyncStatus, AccountViewModel). `TokenStorage` is constructed at each platform's entry point and handed in via a plain `fun authModule(tokenStorage: TokenStorage) = module { single { tokenStorage } }` function — same pattern as `DatabaseDriverFactory`/`databaseModule(driverFactory)` in `core/di/DatabaseModule.kt` and `MainActivity.kt`.

**Navigation**: `Route.Login(returnTo)`, `Route.CompleteProfile(returnTo)`, `Route.Account` added to `Routes.kt`; wired in `NavGraph.kt` per §6.

## 8. State & Error Handling

```kotlin
sealed interface LoginState {
    data object Idle : LoginState
    data object SigningIn : LoginState
    data class Error(val message: String) : LoginState
    data object Success : LoginState // triggers navigation
}
sealed interface CompleteProfileState {
    data class Editing(val age: String, val gender: Gender?) : CompleteProfileState
    data object Saving : CompleteProfileState
    data object Saved : CompleteProfileState
}
sealed interface AccountState {
    data class Guest(val syncStatus: SyncStatus? = null) : AccountState // syncStatus null for guests
    data class LoggedIn(val syncStatus: SyncStatus) : AccountState
}
```
- Cancelled native sign-in (user dismisses the system sheet) → `LoginState.Idle`, no error shown (not a real failure).
- Network failure during sign-in → `LoginState.Error` with a friendly message, form/CTAs stay interactive, no partial account created.
- `SyncRepositoryImpl` failures map to `Failure.Network`/`Failure.Unauthorized` (expired refresh token → forces a fresh sign-in, does not silently strand the user logged in with a dead session).
- Sync failures never surface as blocking UI — only reflected in `AccountScreen`'s status row (`SyncStatus.Failed`).

## 9. Edge Case Decisions

| # | Item | Decision | How handled |
|---|---|---|---|
| 1 | Decorative form card in Figma | Ignore (not implemented) | Only Google/Apple/Skip CTAs built |
| 2 | Double-tap sign-in button | Handle | Button disabled while `LoginState.SigningIn` |
| 3 | New device with pre-existing local guest data | Handle | Every first-sync is push-then-pull with LWW, never a blind overwrite |
| 4 | Which tables sync | Handle | 6 aggregates listed in §5; `exercise` excluded |
| 5 | Age/gender capture | Handle | New `ProfileBasics`/`user_profile`, decoupled from `FullAssessmentResult` |
| 6 | P-15 doesn't exist | Handle (minimal) | Stub `AccountScreen` only; full scope stays in `09-profile-settings.md` |
| 7 | Native SDKs unavailable | Handle (stub) | expect/actual returns `Failure.Network` with `TODO(dev)`; rest of architecture fully wired |
| 8 | At-rest encryption | Handle (token only) | Keystore/Keychain-encrypted session token; DB itself unencrypted (unchanged) |
| 9 | Logout data retention | Handle | Session/tier cleared only; local data cached |
| 10 | Delete account | Defer | `09-profile-settings.md` |
| 11 | Offline during login | Handle | `Failure.Network` surfaced inline, no partial state |
| 12 | Cancelled native sign-in sheet | Handle | Silent return to `Idle`, no error |
| 13 | App killed mid initial-upload | Handle | Timestamp-cursor sync is naturally resumable/idempotent (re-running `syncNow()` re-evaluates `updated_at > lastSyncedAt`) |
| 14 | Repeated background sync failures | Handle | Simple capped retry on next trigger (login/foreground/local-write), no exponential backoff scheduler needed at this trigger cadence |
| 15 | Local edit during in-flight sync | Handle | Push reads current rows at push-time (no queue snapshot to go stale); a mid-sync edit is simply picked up on the *next* sync pass |
| 16 | Android vs iOS sign-in SDK | Handle | `GoogleSignInLauncher`/`AppleSignInLauncher` expect/actual |
| 17 | iOS App Store 4.8 (Apple button parity) | Handle | Apple CTA rendered only on iOS (platform check in `LoginScreen`) |
| 18 | Session/token expiry | Handle | `refreshSessionIfNeeded()` called before any sync/authenticated call |
| 19 | Simultaneous two-device conflicting edit | Handle (minimal) | LWW per `updated_at`; losing edit logged via Kermit, no merge UI |
| 20 | OS-level background sync scheduling | Deferred | Trigger-based only (login/foreground/local-write); no WorkManager/BGTaskScheduler this pass |

## 10. Definition of Done

- [ ] DoD global (`00-overview.md`)
- [ ] `AuthRepository`, `ProfileRepository`, `SyncRepository` implemented and unit-tested (domain/usecase priority per `testing.md`)
- [ ] Native sign-in expect/actuals compile on both platforms and return a typed `Failure` with a `TODO(dev)` marker — not a crash
- [ ] Session token round-trips through `TokenStorage` (encrypted) on both platform actuals
- [ ] All 6 sync aggregates have a working `SyncParticipant` adapter; `exercise` confirmed excluded
- [ ] LWW conflict resolution covered by a unit test (local newer wins, remote newer wins, tie stays local)
- [ ] First-login merge test: local guest data + remote account data → both preserved (AC #1/#2)
- [ ] `Route.Login`/`Route.CompleteProfile`/`Route.Account` wired; `AiCustomPlanGateScreen` and `AssessmentDetailScreen`'s `onNavigateToLogin = {}` no-ops replaced with real navigation
- [ ] `AccountScreen` shows sync status (idle/syncing/success+timestamp/failed) and a working logout
- [ ] `checkModules()` green with `AuthModule`/`ProfileModule`/`SyncModule` registered
- [ ] No feature that worked offline before this change requires login now (AC #3, #7)
