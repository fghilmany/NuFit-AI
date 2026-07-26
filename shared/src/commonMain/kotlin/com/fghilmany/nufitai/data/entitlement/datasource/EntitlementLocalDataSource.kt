package com.fghilmany.nufitai.data.entitlement.datasource

import com.russhwolf.settings.Settings

private const val KEY_IS_LOGGED_IN = "entitlement_is_logged_in"
private const val KEY_IS_SUBSCRIBED = "entitlement_is_subscribed"

/**
 * issue #81 -- until real auth/billing ship, nothing in the app sets these keys yet
 * (techspec §9 item 8); a manual debug toggle for dev testing is expected to write here too.
 */
class EntitlementLocalDataSource(private val settings: Settings) {
    fun isLoggedIn(): Boolean = settings.getBoolean(KEY_IS_LOGGED_IN, false)
    fun isSubscribed(): Boolean = settings.getBoolean(KEY_IS_SUBSCRIBED, false)

    /** issue #83 -- real producer of this flag (SignIn/SignOut usecases). */
    fun setLoggedIn(isLoggedIn: Boolean) {
        settings.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
    }

    /** DEBUG ONLY -- remove once real billing (RevenueCat) sets `isSubscribed` for real. */
    fun setDebugEntitlement(isLoggedIn: Boolean, isSubscribed: Boolean) {
        settings.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        settings.putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
    }
}
