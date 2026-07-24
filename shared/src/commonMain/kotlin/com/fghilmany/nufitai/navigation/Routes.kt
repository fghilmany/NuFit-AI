package com.fghilmany.nufitai.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object ParQ : Route

    @Serializable
    data class DoctorConsult(val parQResultId: String) : Route

    @Serializable
    data class QuickAssessmentWizard(val step: Int = 1) : Route

    @Serializable
    data object BodyData : Route

    /** P-03 -- Monthly Plan Home (issue #76). */
    @Serializable
    data object Home : Route

    /** P-04 -- Session Detail (issue #76). */
    @Serializable
    data class SessionDetail(val planDayId: String) : Route

    /** Entry: Home upsell or Profile & Settings, when tier is Logged-In but no result exists yet (issue #76 §7). */
    @Serializable
    data object FullAssessmentStub : Route

    /** Placeholder target for "Mulai Sesi Ini"/"Mulai Latihan" until PT Mode (`05-pt-mode.md`) ships. */
    @Serializable
    data object PtModeStub : Route
}
