package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import com.fghilmany.nufitai.usecase.onboarding.SubmitParQAnswers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParQViewModelTest {

    @BeforeTest
    fun setUp() {
        // Unconfined so viewModelScope.launch { ... } bodies run eagerly/synchronously,
        // avoiding virtual-time coordination between Dispatchers.Main and runTest's own scheduler.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ParQViewModel = ParQViewModel(SubmitParQAnswers(FakeOnboardingRepository()))

    @Test
    fun `given no answers when checked then cannot proceed`() = runTest {
        val vm = viewModel()
        val state = vm.state.value as ParQState.Questions
        assertFalse(state.canProceed)
    }

    @Test
    fun `given all questions answered then can proceed`() = runTest {
        val vm = viewModel()
        ParQQuestionId.entries.forEach { vm.onEvent(ParQEvent.Answer(it, false)) }

        val state = vm.state.value as ParQState.Questions
        assertTrue(state.canProceed)
    }

    @Test
    fun `given submit before all answered then state does not transition to submitting`() = runTest {
        val vm = viewModel()
        vm.onEvent(ParQEvent.Answer(ParQQuestionId.Q1_JANTUNG_DIAGNOSIS, false))
        vm.onEvent(ParQEvent.Submit)

        assertTrue(vm.state.value is ParQState.Questions)
    }

    @Test
    fun `given all answered when submitted then transitions to submitted with correct flag`() = runTest {
        val vm = viewModel()
        ParQQuestionId.entries.forEach { questionId ->
            vm.onEvent(ParQEvent.Answer(questionId, questionId == ParQQuestionId.Q1_JANTUNG_DIAGNOSIS))
        }

        vm.onEvent(ParQEvent.Submit)

        val finalState = vm.state.value
        assertTrue(finalState is ParQState.Submitted)
        assertTrue(finalState.requiresDoctorConsult)
    }
}
