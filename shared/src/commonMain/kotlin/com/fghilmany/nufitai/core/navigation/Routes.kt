package com.fghilmany.nufitai.core.navigation

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

    /** P-05 -- PT Mode Guided Workout (issue #80). */
    @Serializable
    data class PtMode(val planDayId: String) : Route

    /** P-06 -- Workout Summary (issue #80). */
    @Serializable
    data class WorkoutSummary(val planDayId: String) : Route

    /** P-09 -- Assessment Detail (issue #77). */
    @Serializable
    data object AssessmentDetail : Route

    /** P-07 -- Exercise Library (issue #79). */
    @Serializable
    data object ExerciseLibrary : Route

    /** P-08 -- Exercise Detail (issue #79). */
    @Serializable
    data class ExerciseDetail(val exerciseId: String) : Route
}
