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

    /**
     * Entry: Home upsell or Profile & Settings, when tier is Logged-In but no result exists yet
     * (issue #76 §7). [continueToAiCustomPlan] (issue #81, AC-11): when arriving from the AI
     * Custom Plan gate with an incomplete assessment, completion routes onward to
     * [AiCustomPlanForm] instead of the default Home return.
     */
    @Serializable
    data class FullAssessmentStub(val continueToAiCustomPlan: Boolean = false) : Route

    /** P-12 A -- AI Custom Plan entitlement gate (issue #81). */
    @Serializable
    data object AiCustomPlanGate : Route

    /** P-12 B -- AI Custom Plan request form (issue #81). */
    @Serializable
    data object AiCustomPlanForm : Route

    /** P-13 -- AI Custom Plan preview/edit/activate (issue #81). Draft lives in `AiCustomPlanDraftHolder`, not route args. */
    @Serializable
    data object AiCustomPlanPreview : Route

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

    /** P-14 -- My Plans (issue #82). Real destination for the bottom nav's Plans tab. */
    @Serializable
    data object MyPlans : Route

    /** P-14 UC-4 -- manual template picker (Level -> Goal -> Split), issue #82. */
    @Serializable
    data object TemplatePicker : Route

    /** P-10 -- Login (Google/Apple), issue #83. [returnTo] mirrors [FullAssessmentStub]'s boolean-flag precedent, generalized to 3 call sites. */
    @Serializable
    data class Login(val returnTo: LoginReturnTarget = LoginReturnTarget.HOME) : Route

    /** "Lengkapi Profil" -- age/gender, once, post-first-login (issue #83). */
    @Serializable
    data class CompleteProfile(val returnTo: LoginReturnTarget = LoginReturnTarget.HOME) : Route

    /** P-15 -- Account (techspec profile-settings.md). */
    @Serializable
    data object Account : Route

    /**
     * techspec profile-settings.md §7 -- [isFirstRun] true when Splash routes here (blocking,
     * no back button); false when opened from Account's Informasi list (re-readable, non-blocking).
     */
    @Serializable
    data class MedicalDisclaimer(val isFirstRun: Boolean = false) : Route

    @Serializable
    data object PrivacyPolicy : Route

    @Serializable
    data object AboutApp : Route
}

/** issue #83 -- where [Route.Login]/[Route.CompleteProfile] return to once signed in. */
@Serializable
enum class LoginReturnTarget { HOME, AI_CUSTOM_PLAN, FULL_ASSESSMENT }
