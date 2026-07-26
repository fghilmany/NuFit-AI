package com.fghilmany.nufitai.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fghilmany.nufitai.presentation.account.screen.AccountScreen
import com.fghilmany.nufitai.presentation.aicustomplan.screen.AiCustomPlanFormScreen
import com.fghilmany.nufitai.presentation.aicustomplan.screen.AiCustomPlanGateScreen
import com.fghilmany.nufitai.presentation.aicustomplan.screen.AiCustomPlanPreviewScreen
import com.fghilmany.nufitai.presentation.assessmentdetail.screen.AssessmentDetailScreen
import com.fghilmany.nufitai.presentation.auth.screen.CompleteProfileScreen
import com.fghilmany.nufitai.presentation.auth.screen.LoginScreen
import com.fghilmany.nufitai.presentation.exerciselibrary.screen.ExerciseDetailScreen
import com.fghilmany.nufitai.presentation.exerciselibrary.screen.ExerciseLibraryScreen
import com.fghilmany.nufitai.presentation.fullassessment.screen.FullAssessmentStubScreen
import com.fghilmany.nufitai.presentation.monthlyplan.screen.HomeScreen
import com.fghilmany.nufitai.presentation.monthlyplan.screen.SessionDetailScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.BodyDataScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.DoctorConsultScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.ParQScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.QuickAssessmentWizardScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.SplashScreen
import com.fghilmany.nufitai.presentation.planmanagement.screen.MyPlansScreen
import com.fghilmany.nufitai.presentation.planmanagement.screen.TemplatePickerScreen
import com.fghilmany.nufitai.presentation.ptmode.screen.PtModeScreen
import com.fghilmany.nufitai.presentation.ptmode.screen.WorkoutSummaryScreen

@Composable
fun NuFitNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Splash) {
        composable<Route.Splash> {
            SplashScreen(
                onRouted = { hasLocalProfile ->
                    val destination = if (hasLocalProfile) Route.Home else Route.ParQ
                    navController.navigate(destination) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<Route.ParQ> {
            ParQScreen(
                onSubmitted = { requiresDoctorConsult, parQResultId ->
                    val destination = if (requiresDoctorConsult) {
                        Route.DoctorConsult(parQResultId)
                    } else {
                        Route.QuickAssessmentWizard(step = 1)
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.ParQ) { inclusive = true }
                    }
                },
            )
        }

        composable<Route.DoctorConsult> { backStackEntry ->
            backStackEntry.toRoute<Route.DoctorConsult>()
            DoctorConsultScreen(
                onContinue = {
                    navController.navigate(Route.QuickAssessmentWizard(step = 1)) {
                        popUpTo<Route.DoctorConsult> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.QuickAssessmentWizard> { backStackEntry ->
            backStackEntry.toRoute<Route.QuickAssessmentWizard>()
            QuickAssessmentWizardScreen(
                onCompleted = {
                    navController.navigate(Route.BodyData) {
                        popUpTo<Route.QuickAssessmentWizard> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.BodyData> {
            BodyDataScreen(
                onDone = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.BodyData) { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Home> {
            HomeScreen(
                onSessionClick = { planDayId -> navController.navigate(Route.SessionDetail(planDayId)) },
                onStartSession = { planDayId -> navController.navigate(Route.PtMode(planDayId)) },
                onAssessmentDetailClick = { navController.navigate(Route.AssessmentDetail) },
                onExerciseLibraryClick = { navController.navigate(Route.ExerciseLibrary) },
                onPlansClick = { navController.navigate(Route.MyPlans) },
                onAccountClick = { navController.navigate(Route.Account) },
            )
        }

        composable<Route.MyPlans> {
            MyPlansScreen(
                onCreateFromTemplate = { navController.navigate(Route.TemplatePicker) },
                onCreateWithAi = { navController.navigate(Route.AiCustomPlanGate) },
                onExerciseLibraryClick = { navController.navigate(Route.ExerciseLibrary) },
                onHomeClick = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.MyPlans> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.TemplatePicker> {
            TemplatePickerScreen(
                onCompleted = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.TemplatePicker> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.SessionDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SessionDetail>()
            SessionDetailScreen(
                planDayId = route.planDayId,
                onBack = { navController.popBackStack() },
                onStartSession = { navController.navigate(Route.PtMode(route.planDayId)) },
            )
        }

        composable<Route.PtMode> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PtMode>()
            PtModeScreen(
                planDayId = route.planDayId,
                onSessionCompleted = { planDayId ->
                    navController.navigate(Route.WorkoutSummary(planDayId)) {
                        popUpTo<Route.PtMode> { inclusive = true }
                    }
                },
                onExit = { navController.popBackStack() },
            )
        }

        composable<Route.WorkoutSummary> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WorkoutSummary>()
            WorkoutSummaryScreen(
                planDayId = route.planDayId,
                onDone = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.WorkoutSummary> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.FullAssessmentStub> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.FullAssessmentStub>()
            FullAssessmentStubScreen(
                onCompleted = {
                    // Tier transition (AC-6, ActivatePlanFromFullAssessment) triggers inside the
                    // ViewModel itself. AC-11 (issue #81): arriving from the AI Custom Plan gate
                    // continues on to the form instead of returning to Home.
                    val destination = if (route.continueToAiCustomPlan) Route.AiCustomPlanForm else Route.Home
                    navController.navigate(destination) {
                        popUpTo<Route.FullAssessmentStub> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.AiCustomPlanGate> {
            AiCustomPlanGateScreen(
                // issue #83 -- real Login destination now that Auth & Sync exists.
                onNavigateToLogin = { navController.navigate(Route.Login(returnTo = LoginReturnTarget.AI_CUSTOM_PLAN)) },
                onNavigateToSubscribe = {}, // billing (RevenueCat) still doesn't exist -- no-op until then.
                onNavigateToFullAssessment = { navController.navigate(Route.FullAssessmentStub(continueToAiCustomPlan = true)) },
                onPassThrough = {
                    navController.navigate(Route.AiCustomPlanForm) {
                        popUpTo<Route.AiCustomPlanGate> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Login> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Login>()
            LoginScreen(
                onSignedIn = { needsCompleteProfile ->
                    val destination = if (needsCompleteProfile) Route.CompleteProfile(route.returnTo) else route.returnTo.toDestination()
                    navController.navigate(destination) { popUpTo<Route.Login> { inclusive = true } }
                },
                onSkip = { navController.popBackStack() },
            )
        }

        composable<Route.CompleteProfile> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.CompleteProfile>()
            CompleteProfileScreen(
                onCompleted = {
                    navController.navigate(route.returnTo.toDestination()) {
                        popUpTo<Route.CompleteProfile> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Account> {
            AccountScreen(onNavigateToLogin = { navController.navigate(Route.Login()) })
        }

        composable<Route.AiCustomPlanForm> {
            AiCustomPlanFormScreen(
                onGenerated = { navController.navigate(Route.AiCustomPlanPreview) },
            )
        }

        composable<Route.AiCustomPlanPreview> {
            AiCustomPlanPreviewScreen(
                onSaved = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.AiCustomPlanPreview> { inclusive = true }
                    }
                },
                onActivated = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.AiCustomPlanPreview> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.AssessmentDetail> {
            AssessmentDetailScreen(
                onBack = { navController.popBackStack() },
                // UC-3 retake (issue #77): RetakeAssessment already archived the active plan --
                // re-enter the same wizard/stub flows first-time onboarding uses, popping this
                // screen off the stack so "back" from the wizard returns to Home, not here.
                onRetakeLocal = {
                    navController.navigate(Route.QuickAssessmentWizard(step = 1)) {
                        popUpTo<Route.AssessmentDetail> { inclusive = true }
                    }
                },
                onRetakeLoggedIn = {
                    navController.navigate(Route.FullAssessmentStub()) {
                        popUpTo<Route.AssessmentDetail> { inclusive = true }
                    }
                },
                onOpenFullAssessment = { navController.navigate(Route.FullAssessmentStub()) },
            )
        }

        composable<Route.ExerciseLibrary> {
            ExerciseLibraryScreen(
                onBack = { navController.popBackStack() },
                onExerciseClick = { exerciseId -> navController.navigate(Route.ExerciseDetail(exerciseId)) },
            )
        }

        composable<Route.ExerciseDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ExerciseDetail>()
            ExerciseDetailScreen(
                exerciseId = route.exerciseId,
                onBack = { navController.popBackStack() },
                // Tapping an alternative pushes a new detail screen -- lets "back" walk the chain.
                onAlternativeClick = { alternativeId -> navController.navigate(Route.ExerciseDetail(alternativeId)) },
            )
        }
    }
}

/** issue #83 -- where Login/CompleteProfile hand off to once signed in. */
private fun LoginReturnTarget.toDestination(): Route = when (this) {
    LoginReturnTarget.HOME -> Route.Home
    LoginReturnTarget.AI_CUSTOM_PLAN -> Route.AiCustomPlanForm
    LoginReturnTarget.FULL_ASSESSMENT -> Route.FullAssessmentStub()
}
