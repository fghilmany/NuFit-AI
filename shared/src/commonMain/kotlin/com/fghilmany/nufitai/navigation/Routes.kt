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

    /** Stub only -- the real Home / Monthly Plan feature (P-03) is a separate, not-yet-built spec. */
    @Serializable
    data object Home : Route
}
