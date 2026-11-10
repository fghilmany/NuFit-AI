# TechSpec: Chat with AI

Source US: fghilmany/NuFit-AI#8 · Halaman: P-16 (Chat AI)

## 1. Overview & Goal

A contextual chat with an LLM that knows the user's `FitnessProfile`, active plan, and recent workout log — grounded via server-side context injection + tool calling (no vector-RAG, per the US's locked architecture decision). Basic tier (LOGGED_IN, **20 chats/day** — ADR-009, 2026-11-09, was 5/day) and Pro tier (SUBSCRIBED, higher limit) are both in scope; entitlement is checked server-side, this client only reflects it.

This pass also closes a gap discovered during analysis: the US assumes the server's grounding tools (`get_active_plan()`, `get_workout_history()`) can read structured user data, but today only `monthly_plan` is actually synced to Supabase (`FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant` are stubs per `auth-sync.md`). Per dev decision, this techspec **also** implements those two sync participants for real — without them, Chat AI's "personal" grounding would be reading empty/stale server data.

## 2. UI Reference

- Figma (P-16 Chat AI): https://www.figma.com/design/dNMXJXWuUOoUF8lEaWC2Lw/Untitled?node-id=12-1624 (Figma MCP was rate-limited this pass — re-pull when available; screenshot below is the source of truth for `/code` until then)
- Screenshot provided by the dev (single screen, in-conversation): top bar (back arrow, "Chat AI" title, overflow/3-dot menu) → scrollable message list → suggested-reply chip row → text composer + send button → bottom nav (Chat AI tab active).
  - **User message bubble**: dark green (`NuFitColors.Primary`-ish), right-aligned, rounded, timestamp below in small gray text.
  - **AI message bubble**: white/card background, left-aligned, small circular bot-avatar icon to its left, timestamp below.
  - **AI message may embed an exercise suggestion card** (this is the interesting part): exercise photo, level/equipment badge chips (e.g. "Pemula", "Tanpa Alat" — reuse the same badge style as Exercise Library), exercise name (bold), then two full-width stacked buttons: primary "Terapkan ke Plan" (dark, filled) and secondary "Tanya Lagi" (light green, filled-tonal).
  - **Suggested-reply chips**: a horizontally scrollable row of pill chips below the latest AI message (e.g. "Versi tanpa alat", "Lebih singkat", "Pemanasan") — tapping one sends its text as the next user message.
  - **Composer**: rounded pill text field ("Tanya NuFit AI...") + circular filled send button (paper-plane icon).
- No screenshot for: quota-exhausted state, offline/error state, empty state (first open, no messages), the overflow menu's contents, or the paywall/gate screens (Guest / non-subscriber-wanting-pro). Build these plain/functional, matching the app's existing card/button styles — mirror `AiCustomPlanGateScreen.kt`'s `GateCard` for the entitlement gates.

## 3. Scope

**In scope**
- P-16 Chat screen: message list (user/AI bubbles), AI messages may carry an **exercise suggestion attachment** (photo, badges, name, "Terapkan ke Plan" / "Tanya Lagi"), a **suggested-reply chip row** (server-provided per response), text composer, send.
- Entitlement gate: Guest → login CTA (mirrors `AiCustomPlanGateScreen`); LOGGED_IN and SUBSCRIBED both pass through to chat (difference is quota/model tier, enforced server-side) — per keputusan #12, the tab itself is never hidden.
- Quota indicator: "X/20 chat hari ini" (LOGGED_IN, ADR-009) shown in the top bar area; when server reports quota exhausted, composer disables with inline upsell text (no hard block without explanation, per US).
- Chat history persisted locally (SQLDelight, single continuous conversation per user — no multi-thread UI in this design) and lightly synced (new `ChatMessageSyncParticipant`, mirrors `MonthlyPlanSyncParticipant`'s shape) so history survives reinstall/new-device for logged-in users.
- **"Terapkan ke Plan"**: applies the suggested exercise as a **session-scoped substitution** (mirrors PT Mode's `SwapExercise` semantics — "plan asli tidak berubah untuk sesi berikutnya") on the **nearest upcoming/today session** that currently contains the exercise the AI's suggestion is replacing. Implemented via a new pending-override mechanism (§5/§7) that `GetOrCreateExerciseLog` (PT Mode) consults when a session is first started — PT Mode's own code changes minimally.
- Remote chat endpoint contract (request/response) — **no live backend** (mirrors `AiCustomPlanRemoteDataSource`'s existing placeholder-`BASE_URL` pattern); the actual LLM/tool-calling server is out of this KMP client's reach, tracked as tech debt (§10) same as issues #84/#86.
- **`FullAssessmentSyncParticipant` and `WorkoutHistorySyncParticipant`**: real implementations (were stubs), so server-side tool calls have real data to read. New `.sq` schema changes for `user_id`/`updated_at` sync columns (§7).

**Out of scope (explicit, deferred)**
- The LLM/tool-calling server itself (prompt, tool implementations, entitlement/quota enforcement) — tracked as tech debt, mirrors the AI Custom Plan / Delete Account precedent.
- Multi-thread / multiple conversations, message editing, message deletion, the overflow menu's actual contents (placeholder "Clear conversation" only).
- "Tambah exercise baru ke sesi" (adding, not substituting) — per dev decision, only substitution is in scope.
- Meal Plan tab, Chat AI Pro-specific UI differences beyond the quota label (no design provided for Pro).
- Streaming/typewriter response rendering — response renders as one complete bubble once the request resolves (no design signal either way; simplest choice, revisit if the dev wants streaming later).

## 4. User Story & Acceptance Criteria

> Sebagai user login, saya ingin berdiskusi dengan AI tentang gym dan makanan yang memahami profil serta riwayat latihan saya, sehingga jawaban yang saya dapat personal — bukan generik.

### AC Traceability

| # | AC (Given/When/Then) | Handled in | Blocking? |
|---|---|---|---|
| 1 | Given guest, when opening Chat AI tab, then shown login CTA, not chat | `ChatGateViewModel`/`ChatGateScreen` (mirrors `AiCustomPlanGateScreen`) | BLOCKING |
| 2 | Given LOGGED_IN user, when sending a message, then AI response reflects FitnessProfile/plan/log context (server-side grounding) | Client sends the documented request contract (§7); grounding logic itself is server-side (tech debt) | Handle (client contract only) |
| 3 | Given quota (20/day for basic — ADR-009, was 5/day) reached, when trying to send, then composer disables with an upsell explanation, not a silent/hard block | `ChatViewModel` reads `quotaRemaining` from the server response; UI disables composer + shows upsell copy | BLOCKING |
| 4 | Given an AI response suggests an exercise, when tapping "Terapkan ke Plan", then it substitutes that exercise in the nearest upcoming session only — the plan template itself is unchanged for future cycles | `ApplyChatExerciseSuggestion` usecase + pending-override table, consumed once by `GetOrCreateExerciseLog` (§5/§7) | BLOCKING |
| 5 | Given a medical-adjacent question, then AI redirects to a professional, no diagnosis | Server-side guardrail (prompt); client renders whatever text comes back, no special client-side detection (§9) | Deferred (server) |
| 6 | Given offline, when trying to send, then the typed message is not lost and shows a retry affordance | `ChatViewModel` keeps the draft + failed-message state client-side (§8) | BLOCKING |
| 7 | Given app killed mid-request, when reopened, then the pending message shows as failed/retryable, never silently dropped | Message persisted locally as `SENDING` before the network call; reconciled to `FAILED` on next app start if still `SENDING` (§8) | Handle |

## 5. Domain Model

```kotlin
// domain/chatai/entity/ChatMessage.kt
enum class ChatMessageRole { USER, ASSISTANT }
enum class ChatMessageStatus { SENDING, SENT, FAILED }

data class ExerciseSuggestion(
    val exerciseId: String,           // must resolve in the Exercise Library (client-side validated, mirrors AI Custom Plan's exerciseId validation)
    val replacesExerciseId: String?,  // which exercise in the user's plan this substitutes for "Terapkan ke Plan"; null = informational only, button hidden
)

data class ChatMessage(
    val id: String,
    val role: ChatMessageRole,
    val text: String,
    val suggestion: ExerciseSuggestion?,
    val suggestedReplies: List<String>,  // only ever non-empty on the latest ASSISTANT message
    val status: ChatMessageStatus,
    val createdAt: Instant,
)

// domain/chatai/entity/ChatQuota.kt
data class ChatQuota(val used: Int, val max: Int, val resetsAt: Instant)
```

**Business rules**
- `suggestedReplies` is meaningful only on the most recent assistant message — older messages' chips are not re-rendered (matches the screenshot: chips sit directly under the latest bubble, above the composer, not per-message).
- `ChatQuota` is always server-authoritative -- the client never decrements it locally; every send response returns the updated quota (§7).
- A pending exercise override (§7) is single-use: consumed the first time `GetOrCreateExerciseLog` creates the log row for that slot, then deleted -- it must never silently reapply on a later plan cycle.

## 6. Architecture Breakdown

```
domain/chatai/
  entity/ ChatMessage.kt, ChatQuota.kt
  repository/ ChatRepository.kt

usecase/chatai/
  GetChatHistory.kt
  SendChatMessage.kt              -- persists optimistic USER message (SENDING), calls remote, persists ASSISTANT reply + updates quota
  RetryFailedMessage.kt
  ApplyChatExerciseSuggestion.kt  -- finds nearest actionable PlanDay containing replacesExerciseId, writes the pending override
  GetChatQuota.kt

data/chatai/
  model/ ChatSendRequestDto.kt, ChatSendResponseDto.kt
  datasource/ ChatLocalDataSource.kt, ChatRemoteDataSource.kt
  repository/ ChatRepositoryImpl.kt
  sync/ ChatMessageSyncParticipant.kt   -- new, mirrors MonthlyPlanSyncParticipant

presentation/chatai/
  viewmodel/ ChatGateViewModel.kt (mirrors AiCustomPlanGateViewModel), ChatViewModel.kt
  screen/ ChatGateScreen.kt, ChatScreen.kt
  component/ UserMessageBubble.kt, AssistantMessageBubble.kt, ExerciseSuggestionCard.kt, SuggestedReplyRow.kt, ChatComposer.kt, QuotaBanner.kt

# PT Mode integration (minimal touch)
usecase/ptmode/GetOrCreateExerciseLog.kt   -- extended: check pending override before defaulting to the plan's exerciseId

# Sync participants going from stub to real (was already-scaffolded, per dev decision)
data/fullassessment/sync/FullAssessmentSyncParticipant.kt
data/ptmode/sync/WorkoutHistorySyncParticipant.kt
```

No new expect/actual needed.

## 7. API / Data Contract

### Remote endpoint (no live backend yet -- documented contract only, tech debt §10)

```
POST {BASE_URL}/v1/chat/send
Headers: Authorization: Bearer {access_token}
Body: {
  "message": "Bisa kasih alternatif Push Up yang lebih mudah?",
  "conversationHistoryLimit": 20   // how many prior turns the client wants echoed back for context display; server still holds full history for grounding
}

200 OK
{
  "reply": { "text": "...", "exerciseSuggestion": { "exerciseId": "...", "replacesExerciseId": "..." } | null, "suggestedReplies": ["...", "..."] },
  "quota": { "used": 2, "max": 20, "resetsAt": "2026-07-30T00:00:00Z" }
}

401 -> Failure.Unauthorized()
403 with body {"reason": "quota_exceeded"} -> mapped to a dedicated Failure so the UI shows the upsell copy, not a generic network error
422 -> Failure.Validation(...)
```

`ChatRemoteDataSource` mirrors `AiCustomPlanRemoteDataSource`'s exact shape (plain Ktor, `BASE_URL` placeholder constant with a `TODO(dev)` comment).

### SQLDelight

**New file** `ChatMessage.sq` (one table, per architecture.md §4b):
```sql
CREATE TABLE chat_message (
    id TEXT NOT NULL PRIMARY KEY,
    role TEXT NOT NULL,
    text TEXT NOT NULL,
    suggestion_exercise_id TEXT,
    suggestion_replaces_exercise_id TEXT,
    suggested_replies TEXT,   -- JSON array, empty/null except on the latest assistant message
    status TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    user_id TEXT,
    updated_at INTEGER NOT NULL DEFAULT 0
);
```
→ `chatMessageQueries`. `packageName` matches the existing `NuFitDatabase` config (confirm in `build.gradle.kts` before first build, per architecture.md §4b).

**New file** `ChatExerciseOverride.sq` (the "Terapkan ke Plan" pending-substitution table):
```sql
CREATE TABLE chat_exercise_override (
    id TEXT NOT NULL PRIMARY KEY,
    plan_day_id TEXT NOT NULL,
    planned_exercise_index INTEGER NOT NULL,
    substitute_exercise_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (plan_day_id) REFERENCES plan_day(id)
);
```
Not synced (device-local, single-use, consumed before it would ever need to travel cross-device).

**Schema changes to existing tables** (direct `.sq` edits, no `.sqm` migration -- matches this project's current early-dev practice, same as `monthly_plan.deleted_at`/`updated_at`):
- `full_assessment_result`: add `user_id TEXT`, `updated_at INTEGER NOT NULL DEFAULT 0`.
- `workout_session`, `workout_exercise_log`, `workout_set_log`, `plan_day_session_log`: each adds `user_id TEXT`, `updated_at INTEGER NOT NULL DEFAULT 0`. `WorkoutHistorySyncParticipant.tableName = "workout_session"` and its payload nests the three child tables' rows, per its existing doc comment (`workout_exercise_log`, `workout_set_log`, `plan_day_session_log` as nested children) -- mirrors how `auth-sync.md` already anticipated this shape.
- Each participant's `getChangedSince`/`upsertRemoteRows`/`backfillUserId` follows `MonthlyPlanSyncParticipant`'s exact worked-example pattern (JSON row mapping, LWW via `updated_at` comparison).

**Backend note for the dev** (infra, not client code, mirrors `auth-sync.md`'s existing note): the new Supabase tables (`full_assessment_result`, `workout_session`, `workout_exercise_log`, `workout_set_log`, `plan_day_session_log`, `chat_message`) need the same RLS policy shape as `monthly_plan` (`user_id = auth.uid()`) before push/pull will work end-to-end -- same gap already documented for `monthly_plan`.

### DI
- New `chatAiModule` (mirrors `aiCustomPlanModule`'s shape): local/remote datasources, repository, all `usecase/chatai/*`, `ChatGateViewModel`, `ChatViewModel`.
- `syncModule`: register `ChatMessageSyncParticipant` in the `List<SyncParticipant>` assembly; `FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant` registrations already exist (constructors likely gain a `local` datasource dependency now that they're real -- update those `single { }` lines).
- `ptModeModule`: `GetOrCreateExerciseLog`'s registration gains its new dependency (the override datasource/repository).

### Navigation
- `Route.ChatGate` / `Route.Chat` -- wired from the bottom nav's `NavTab.CHAT_AI` (currently inert per `BottomNavBar.kt`'s existing comment) in `HomeScreen`/`MyPlansScreen`/`AccountScreen`'s `BottomNavBar` callers, same popUpTo tab-switch pattern already used for Home/MyPlans/Account.

## 8. State & Error Handling

- **Send flow**: composer submit → optimistic `ChatMessage(status=SENDING)` inserted immediately (message never appears to vanish) → on success, replace with `SENT` + append the assistant reply + update quota banner → on failure, mark `FAILED`, show inline retry tap target on that bubble, composer text is NOT cleared if the failure happens before the optimistic insert (e.g. validation), but IS cleared once optimistically inserted (user can retype something else while the failed one waits for retry).
- **Offline**: composer stays enabled (so the message can queue as `SENDING`→`FAILED` fast, honest feedback) but the send button shows a distinct offline affordance; no silent infinite spinner.
- **App killed mid-send**: on next `ChatViewModel` init, any message still `SENDING` (impossible for the network call to still be in flight after process death) is reconciled to `FAILED`.
- **Quota exceeded (403 quota_exceeded)**: composer disabled, `QuotaBanner` shows upsell copy + CTA (SUBSCRIBED users never see this, only LOGGED_IN at 20/20 — ADR-009, was 5/5).
- **Empty state**: first open, no messages -- a short "Tanya apa saja soal gym & makanan" placeholder (no design provided; simplest reasonable default) with a couple of static starter-prompt chips reusing `SuggestedReplyRow`.
- **Guest**: `ChatGateScreen` (login CTA), never reaches `ChatScreen`.

## 9. Edge Case Decisions

| # | Item | Decision | How it's handled |
|---|---|---|---|
| 1 | Quota reset timing/timezone | Handle | Server-authoritative (`ChatQuota.resetsAt` from response); client never computes/assumes a reset boundary itself |
| 2 | Tool-calling grounding depends on synced data | Handle (this techspec) | `FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant` implemented for real (§6/§7), closing the gap found during analysis |
| 3 | Guest access | Handle | `ChatGateScreen`, mirrors `AiCustomPlanGateScreen` |
| 4 | Chat history persistence | Handle | Local SQLDelight + lightweight sync (`ChatMessageSyncParticipant`) |
| 5 | Quota exhausted mid-conversation | Handle | Composer disables + inline upsell, not a silent/hard block (explicit US requirement) |
| 6 | Medical guardrail client-side detection | Defer | Purely a server/prompt concern; client renders plain text, no special styling logic |
| 7 | Streaming response rendering | Defer | Single complete bubble on resolve; no design signal for streaming |
| 8 | Offline send | Handle | Draft never lost; `FAILED` status + retry affordance (§8) |
| 9 | App killed mid-request | Handle | `SENDING` reconciled to `FAILED` on next app start (§8) |
| 10 | Double-tap send | Handle | Composer disables immediately on submit until the optimistic insert completes |
| 11 | Very long user input | Handle | Client-side soft character cap on the composer (exact number is a dev/product call, not specified by design -- flag for the dev to confirm before `/code`, default to a generous cap like 1000 chars if not specified) |
| 12 | "Terapkan ke Plan" target exercise not found in any upcoming session | Handle | Inline error on the card ("tidak ditemukan di plan aktif kamu"), no override written |
| 13 | Overflow (3-dot) menu contents | Defer | Placeholder "Clear conversation" only; full scope undefined by design |

## 10. Definition of Done

- [ ] DoD global (lihat `00-overview.md`)
- [ ] Guest sees login gate, never an error, never reaches the composer
- [ ] Quota shown, always sourced from the latest server response, never computed client-side
- [ ] Sending while offline never loses the typed message; failed sends are retryable
- [ ] "Terapkan ke Plan" writes a session-scoped-only override (verified: the underlying `PlanDay.mainExercises` template is never mutated; a *second* future plan cycle for the same day type does not inherit the substitution)
- [ ] `FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant` push/pull real rows (verified against a live Supabase project once the dev provisions the new tables' RLS policies, per §7's backend note)
- [ ] Chat history persists across app restart and is scoped per logged-in user (not visible to a different user on the same device)
- [ ] `exerciseId`/`replacesExerciseId` from the server are validated against the local Exercise Library before rendering the suggestion card (mirrors AI Custom Plan's existing exerciseId validation discipline) -- invalid ids render the message without the card rather than crashing
- [ ] New GitHub tech-debt issue filed for the chat/tool-calling backend (mirrors #84/#86), linked from this techspec's issue
