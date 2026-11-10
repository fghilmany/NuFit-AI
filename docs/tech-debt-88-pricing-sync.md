# Draft update for GitHub issue #88 (pending — GitHub token 403 on issue update, not just comments)

> This content was meant to be pushed to https://github.com/fghilmany/NuFit-AI/issues/88 via the
> GitHub MCP but the write failed (`403 Resource not accessible by personal access token`) —
> paste manually into #88, or ask to retry once the token's issue-write scope is fixed.

## New title

```
[Tech Debt] AI backend (Supabase Edge Functions) — shared by AI Custom Plan, Chat AI, Meal Plan
```
(drops "Gemini-based" from the original title — see the model-routing conflict below.)

## Section to add: "⚠️ Provider/quota decisions still need syncing — see `Tech Plan/pricing.md`"

`Tech Plan/pricing.md` (companion script: `Tech Plan/pricing-model.py`) already made cost-driven
decisions on the exact things issue #88 covers, and **pricing.md itself flags they haven't been
synced into the product docs yet**:

- **Model routing (§2, §7)**: chat on all tiers → **MiMo v2.5 basic** ($0.112/$0.224 per 1M
  tokens); program/meal-plan generation → **MiMo v2.5 Pro** ($0.348/$0.696 per 1M), both via
  **OpenRouter** (prepaid credit, not pay-as-you-go). Gemini's free API key is dev-only, for quick
  prompt experiments that don't touch production behavior — pricing.md §7 explicitly recommends
  routing *dev* through OpenRouter too (same model as prod) rather than tuning a prompt on Gemini
  and re-tuning for MiMo later, since tool-calling reliability and Bahasa Indonesia quality differ
  meaningfully between models. This conflicts with the dev's stated plan going into the Chat AI
  `/code` session (Gemini now, OpenRouter later) — flagged as an open question, not decided here.
- **Chat basic quota should be 20/month, not 5/day** (§5): a daily quota can be farmed (5/day →
  150/month); pricing.md's own P&L model assumes 20/month. Documented as contradicting
  `tier-plan.md` §2, `Product Analysis/10-chat-ai.md`, keputusan #9 in `00-overview.md`, **and**
  everything already built for Chat AI in this repo (`ChatQuota` entity, `QuotaBanner`, AC-3 in
  `docs/techspec/chat-ai.md`, which all currently assume 5/day). No docs or code have been changed
  for this yet, per explicit dev instruction — this issue just tracks that the sync is outstanding.
- **Token ceilings enforced server-side**: max 8,000 input / 800 output tokens per request,
  enforced via the API call's `max_tokens` param — never just a prompt instruction.
- **Prompt caching** on the repeated context block (profile + active plan + recent log) — biggest
  cost lever per pricing.md §2, verify provider support before launch.
- **Credit management (§6)**: prepaid credit on OpenRouter, alert at 30% remaining + auto top-up,
  tiered degrade when credit runs low (`Pro → basic → on-device rule-based engine tahap-7b` — user
  still gets a program, just not AI-generated), per-capability kill switch in `app_config` (e.g.
  disable meal plan without disabling chat).
- **Rate limiting**: per IP + per device, not just per account — Google-only login makes new
  accounts cheap to farm.
- **`ai_usage` tracking table** (§9): `user_id, capability, model, token_in, token_out, cost,
  timestamp` — needed from day one to replace pricing.md's cost assumptions with real data; also
  feeds the conversion/retention metrics pricing.md says matter more than token cost.

## Additional checklist items to append under "Scope of this debt"

- [ ] Resolve the model-routing open question (Gemini-then-OpenRouter vs. OpenRouter/MiMo from the
      start) and document the decision in `docs/decisions.md`
- [ ] Sync `tier-plan.md` §2, `Product Analysis/10-chat-ai.md`, `00-overview.md` keputusan #9, and
      `docs/techspec/chat-ai.md` AC-3 to whatever the final basic-tier quota decision is (pricing.md
      currently recommends 20/month over 5/day)
- [ ] Update the already-built Chat AI client code (`ChatQuota`, `QuotaBanner`, quota copy) to
      match once the quota decision is finalized
- [ ] `max_tokens` enforced server-side on every LLM call
- [ ] Prompt caching verified active on the context block
- [ ] Prepaid credit + 30%-remaining alert + auto top-up
- [ ] Tiered degrade (Pro → basic → on-device rule-based) + per-capability kill switch in
      `app_config`
- [ ] Rate limiting per IP + per device
- [ ] `ai_usage` table live before the first real subscriber

## Related-links line to append

`Tech Plan/pricing.md` + `Tech Plan/pricing-model.py` (cost model and quota/routing decisions
referenced throughout this issue)

## Addendum 2026-08-01 — Edge Functions unreachable via local `supabase functions serve`

**Symptom:** calling the deployed (cloud) function directly (`https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1/chat-send`) works; the identical request against local `http://localhost:54321/functions/v1/chat-send` (via `supabase functions serve --env-file ./supabase/.env.local`, Docker running) fails. Exact status/response body not yet captured -- dev moved to testing against cloud directly instead of root-causing this.

**Not investigated further this pass** (dev deprioritized in favor of cloud-direct testing). Candidate causes for whoever picks this up:
1. The `functions serve` terminal process not actually running/restarted (distinct from the `supabase start` Docker containers, which stay up independently).
2. Local Kong gateway (port 54321) requiring an `apikey` header on `/functions/v1/*` routes that the cloud path doesn't strictly enforce the same way.
3. `.env.local` values stale/mistyped relative to the real project keys.

Re-attempt with the exact status code + response body captured before spending real debugging time -- current guesses are unconfirmed.

## Addendum 2026-11-09 — ADR-009 landed a different interim chat quota (don't confuse with pricing.md's number)

`docs/decisions.md` ADR-009 set **Chat 20/day** and **Meal Plan 5 new-generations/month** as the live
quotas for the free MVP showcase phase (AI free for any LOGGED_IN user, login still required). This
is implemented in `supabase/functions/_shared/quota.ts` (`CHAT_AI_BASIC: { max: 20, period: "day" }`,
`MEAL_PLAN: { max: 5, period: "month" }`) and `requireTier(..., "SUBSCRIBED")` was relaxed to
`"LOGGED_IN"` in `meal-plan-generate`/`ai-custom-plan-generate` (mirrors `chat-send`'s existing
LOGGED_IN minimum). A manual kill switch (`user_entitlement.ai_access_blocked`) was also added.

**This 20/day figure is NOT the same as this issue's "20/month" recommendation above** — the
20/month number comes from `pricing.md`'s cost model for the eventual *paid* phase and assumes
OpenRouter/MiMo routing; ADR-009's 20/day is a dev judgment call for the *free showcase* phase on
the current Gemini-based backend (ADR-008), chosen to avoid being so stingy it undercuts the
"let people try it" goal. Whoever eventually does the full pricing.md/issue #88 sync (model
routing, real quota economics) should treat ADR-009's numbers as superseded by that work, not as
the already-reconciled answer — they were never meant to be the same number.

## Addendum 2026-11-09 — root cause found for the 2026-08-01 "local functions serve unreachable" bug

Candidate cause #2 from the 2026-08-01 addendum was it. The Supabase CLI's `functions serve
--env-file` **silently drops any env-file entry whose name starts with `SUPABASE_`** (that prefix
is reserved for the CLI's own local-stack auto-injection) — so `.env.local`'s `SUPABASE_URL` /
`SUPABASE_ANON_KEY` / `SUPABASE_SERVICE_ROLE_KEY` were always being overridden by the **local**
stack's values, not the cloud project's, no matter what `.env.local` said. A cloud-issued
`access_token` validated against the local GoTrue container (different JWT secret) → always 401.

**Fixed** in `_shared/supabaseClient.ts`: added a `CLOUD_SUPABASE_URL` / `CLOUD_SUPABASE_ANON_KEY`
/ `CLOUD_SUPABASE_SERVICE_ROLE_KEY` override, checked first and falling back to the
platform-injected `SUPABASE_*` names (so deployed functions, which never set the `CLOUD_` names,
are unaffected). `.env.local` must use the `CLOUD_SUPABASE_*` names going forward for local
cloud-DB testing — see `docs/deploy-supabase.md` §7b (update that doc's example if it still shows
the old `SUPABASE_*` names).

## Addendum 2026-11-09 — provider switched to Surplus Intelligence (OpenRouter-compatible marketplace), model = glm-5.3-flash; Meal Plan latency accepted as known risk

Backend LLM routing moved again since ADR-008 (Gemini) — current live provider is **Surplus
Intelligence** (`api.surplusintelligence.ai`, OpenAI-compatible, routes to a marketplace of
discounted third-party inference sellers), model `glm-5.3-flash`. Not yet its own ADR; tracked here
because the decision was mid-deliberation when this session ended. `_shared/llmClient.ts`'s header
comment is the source of truth for *why* this model specifically (vs. `glm-5.2`, `deepseek-v4.1-flash`,
`glm-4.7-flash` — all tried/rejected same session).

**Known risk, accepted — not fixed:** `meal-plan-generate`'s 7-day plan is split into sequential
batches (`BATCH_SIZE_DAYS = 2`, 4 LLM calls) to fit Supabase Edge Functions' ~150s wall-clock
execution cap. Live-tested behavior is **inconsistent**, independent of batch size or
`max_tokens` tuning:
- Per-call *generation* time is consistently fast (~20-22s) regardless of batch size.
- But there's an unpredictable **queueing/connection overhead before each call even starts** --
  observed anywhere from ~0s to ~60s+ per call. With 4 sequential calls needed, total wall-clock
  time is effectively a lottery; one test run finished in 110s (comfortable margin), another
  timed out entirely at 150s with only 2 of 4 batches done.
- Separately (and seen even within the 150s budget): when a call hits its `max_tokens` cap, the
  response `message.content` field sometimes comes back **empty** rather than truncated JSON --
  suspected cause is the model spending part of its token budget on internal reasoning before
  writing the final answer, cut off before any `content` was emitted. Diagnostic logging was added
  for this (`llmClient.ts`'s "EMPTY CONTENT raw message" / "finish_reason" console.log on the
  `!content` branch) -- **left in intentionally**, not cleaned up, so a real production failure of
  this kind is debuggable from Edge Function logs without re-adding instrumentation.

**Decision (dev, 2026-11-09): ship as-is, accept the risk.** This is Surplus Intelligence's
marketplace nature (buying spare/discounted capacity, not dedicated throughput) colliding with a
hard, non-negotiable platform timeout -- not something fixable by further prompt/batch-size tuning
in this backend. Real fixes, not done this pass:
- [ ] Check whether a paid Supabase tier raises the Edge Function wall-clock limit above ~150s
      (would directly widen the margin without any model/provider change)
- [ ] Move `meal-plan-generate` off the synchronous request/response model entirely -- client
      triggers generation, a background job (not bound by one Edge Function invocation's timeout)
      does the actual work, client polls/gets notified on completion. This is the architecturally
      correct fix for a long-running generation task but is a real scope change, not a tuning pass.
- [ ] Evaluate a non-marketplace (dedicated-capacity) provider specifically for `meal-plan-generate`
      if the above two aren't viable -- trades the marketplace's cost advantage for predictable
      latency on just this one endpoint.
- [ ] Add client-side handling for a `meal-plan-generate` timeout/failure that's better than a bare
      error (retry affordance, "try again in a bit" messaging) -- not yet designed.

## Addendum 2026-11-10 — day-splitting moved client-side, reduces (not eliminates) the 150s-lottery risk

`meal-plan-generate` went back to **1 LLM call per Edge Function invocation** -- the day-splitting
now happens client-side (`MealPlanRepositoryImpl.generateRemote` makes 7 sequential HTTP calls,
one per day, each its own invocation) instead of 4 sequential LLM calls inside one invocation.
This directly shrinks the worst-case queueing-lottery exposure described above: each invocation
now only needs **one** call's unpredictable queueing overhead (~0-60s+) plus its ~20-22s
generation time to fit under the 150s cap (worst case ≈82s, comfortable margin), instead of 4
calls' cumulative worst case (which is what produced the observed 150s timeout with only 2/4
batches done). Doesn't eliminate the underlying marketplace queueing variance itself -- a single
day's call can still, in principle, hit a bad queueing spike -- but makes any individual
invocation's failure mode "that one day's HTTP call failed, retry just that" rather than "the
whole week's generation died partway through." The background-job real fix above is still the
architecturally correct answer and remains undone.
