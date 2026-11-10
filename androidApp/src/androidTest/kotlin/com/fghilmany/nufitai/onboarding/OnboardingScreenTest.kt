package com.fghilmany.nufitai.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository
import com.fghilmany.nufitai.presentation.onboarding.screen.ParQScreen
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.ParQViewModel
import com.fghilmany.nufitai.usecase.onboarding.SubmitParQAnswers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun parQShowsSafetyGateAndKeepsContinueDisabledUntilEveryAnswerIsProvided() {
        val viewModel = ParQViewModel(SubmitParQAnswers(InMemoryOnboardingRepository()))

        composeRule.setContent {
            NuFitTheme {
                ParQScreen(onSubmitted = { _, _ -> }, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("Health Check First").assertIsDisplayed()
        composeRule.onNodeWithText("0 / 12 Answered").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").assertIsNotEnabled()

        composeRule.onAllNodesWithText("No")[0].performClick()

        composeRule.onNodeWithText("1 / 12 Answered").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

}

private class InMemoryOnboardingRepository : OnboardingRepository {
    override suspend fun saveParQResult(result: ParQResult): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun acknowledgeDoctorConsult(parQResultId: String, acknowledgedAt: Instant): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun getLatestParQResult(): AppResult<ParQResult?> = AppResult.Success(null)
    override suspend fun saveQuickAssessmentResult(result: QuickAssessmentResult): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun getLatestQuickAssessmentResult(): AppResult<QuickAssessmentResult?> = AppResult.Success(null)
    override suspend fun saveBodyMeasurement(measurement: BodyMeasurement): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun getLatestBodyMeasurement(): AppResult<BodyMeasurement?> = AppResult.Success(null)
    override suspend fun getAllBodyMeasurements(): AppResult<List<BodyMeasurement>> = AppResult.Success(emptyList())
    override suspend fun hasLocalProfile(): AppResult<Boolean> = AppResult.Success(false)
}
