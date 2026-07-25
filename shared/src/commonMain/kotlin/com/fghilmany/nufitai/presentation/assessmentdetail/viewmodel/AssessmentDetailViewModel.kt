package com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.usecase.assessmentdetail.AssessmentDetailSummary
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentDetail
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentHistory
import com.fghilmany.nufitai.usecase.assessmentdetail.RetakeAssessment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AssessmentDetailTab { HASIL, RIWAYAT }

sealed interface RetakeDialogState {
    data object Hidden : RetakeDialogState
    data object Confirming : RetakeDialogState

    /** UC-3 forward-compat guard (§5 GetAssessmentDetail doc) -- always unreachable until PT Mode exists. */
    data object Blocked : RetakeDialogState
}

sealed interface AssessmentDetailState {
    data object Loading : AssessmentDetailState
    data class Loaded(
        val summary: AssessmentDetailSummary,
        val history: List<MonthlyPlan>,
        val activeTab: AssessmentDetailTab = AssessmentDetailTab.HASIL,
        val retakeDialog: RetakeDialogState = RetakeDialogState.Hidden,
    ) : AssessmentDetailState

    /** Terminal state -- screen observes this via LaunchedEffect and navigates, mirrors DoctorConsultState.Continued. */
    data class RetakeReady(val source: PlanSource) : AssessmentDetailState
    data class Error(val message: String) : AssessmentDetailState
}

sealed interface AssessmentDetailEvent {
    data class SwitchTab(val tab: AssessmentDetailTab) : AssessmentDetailEvent
    data object RequestRetake : AssessmentDetailEvent
    data object ConfirmRetake : AssessmentDetailEvent
    data object CancelRetake : AssessmentDetailEvent
}

/** P-09 (03-assessment-detail.md, issue #77). */
class AssessmentDetailViewModel(
    private val getAssessmentDetail: GetAssessmentDetail,
    private val getAssessmentHistory: GetAssessmentHistory,
    private val retakeAssessment: RetakeAssessment,
) : ViewModel() {
    private val _state = MutableStateFlow<AssessmentDetailState>(AssessmentDetailState.Loading)
    val state: StateFlow<AssessmentDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: AssessmentDetailEvent) {
        when (event) {
            is AssessmentDetailEvent.SwitchTab -> switchTab(event.tab)
            AssessmentDetailEvent.RequestRetake -> requestRetake()
            AssessmentDetailEvent.ConfirmRetake -> confirmRetake()
            AssessmentDetailEvent.CancelRetake -> cancelRetake()
        }
    }

    private fun load() {
        viewModelScope.launch {
            when (val detailResult = getAssessmentDetail()) {
                is AppResult.Success -> {
                    when (val historyResult = getAssessmentHistory()) {
                        is AppResult.Success -> _state.value = AssessmentDetailState.Loaded(
                            summary = detailResult.data,
                            history = historyResult.data,
                        )
                        is AppResult.Error -> _state.value = AssessmentDetailState.Error(historyResult.failure.message)
                    }
                }
                is AppResult.Error -> _state.value = AssessmentDetailState.Error(detailResult.failure.message)
            }
        }
    }

    private fun switchTab(tab: AssessmentDetailTab) = _state.update { current ->
        if (current is AssessmentDetailState.Loaded) current.copy(activeTab = tab) else current
    }

    /** AC-4-adjacent: blocked instead of confirming when a session is in progress -- see GetAssessmentDetail doc. */
    private fun requestRetake() = _state.update { current ->
        if (current !is AssessmentDetailState.Loaded) return@update current
        val dialog = if (current.summary.hasInProgressSession) RetakeDialogState.Blocked else RetakeDialogState.Confirming
        current.copy(retakeDialog = dialog)
    }

    /** AC-5: cancel is a pure UI no-op -- RetakeAssessment is never invoked. */
    private fun cancelRetake() = _state.update { current ->
        if (current is AssessmentDetailState.Loaded) current.copy(retakeDialog = RetakeDialogState.Hidden) else current
    }

    private fun confirmRetake() {
        val current = _state.value
        if (current !is AssessmentDetailState.Loaded || current.retakeDialog != RetakeDialogState.Confirming) return

        viewModelScope.launch {
            when (val result = retakeAssessment()) {
                is AppResult.Success -> _state.value = AssessmentDetailState.RetakeReady(result.data)
                is AppResult.Error -> _state.value = AssessmentDetailState.Error(result.failure.message)
            }
        }
    }
}
