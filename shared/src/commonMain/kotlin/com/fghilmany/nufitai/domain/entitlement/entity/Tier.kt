package com.fghilmany.nufitai.domain.entitlement.entity

/**
 * issue #81 -- client-side UX gate only (architecture.md §7). Nothing here enforces payment;
 * real auth (`08-auth-sync.md`) and billing (RevenueCat) are separate, later features that will
 * eventually be the actual producer of [com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository]'s
 * underlying flags.
 */
enum class Tier { GUEST, LOGGED_IN, SUBSCRIBED }

/** ADR-009 (2026-11-09): AI features are free for any logged-in user during the MVP showcase
 * phase -- LOGGED_IN gets the same set as SUBSCRIBED. Revert this single line when billing ships. */
fun Tier.capabilities(): Set<Capability> = when (this) {
    Tier.GUEST -> emptySet()
    Tier.LOGGED_IN -> setOf(Capability.CHAT_AI_BASIC, Capability.CHAT_AI_PRO, Capability.AI_CUSTOM_PLAN, Capability.MEAL_PLAN)
    Tier.SUBSCRIBED -> setOf(Capability.CHAT_AI_BASIC, Capability.CHAT_AI_PRO, Capability.AI_CUSTOM_PLAN, Capability.MEAL_PLAN)
}
