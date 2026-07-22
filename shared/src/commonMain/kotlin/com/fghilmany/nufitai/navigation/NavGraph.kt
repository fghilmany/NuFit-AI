package com.fghilmany.nufitai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fghilmany.nufitai.presentation.home.screen.HomeScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.BodyDataScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.DoctorConsultScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.ParQScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.QuickAssessmentWizardScreen
import com.fghilmany.nufitai.presentation.onboarding.screen.SplashScreen

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
            HomeScreen()
        }
    }
}
