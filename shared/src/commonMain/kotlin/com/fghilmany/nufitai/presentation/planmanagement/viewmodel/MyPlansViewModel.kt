package com.fghilmany.nufitai.presentation.planmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.usecase.aicustomplan.CheckDraftQuota
import com.fghilmany.nufitai.usecase.aicustomplan.MAX_AI_CUSTOM_PLAN_QUOTA
import com.fghilmany.nufitai.usecase.planmanagement.ActivatePlan
import com.fghilmany.nufitai.usecase.planmanagement.DeletePlan
import com.fghilmany.nufitai.usecase.planmanagement.GetAllPlansForManagement
import com.fghilmany.nufitai.usecase.planmanagement.GetPlanProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** P-14 (UC-1, UC-2, UC-3). */
sealed interface MyPlansState {
    data object Loading : MyPlansState
    data class Loaded(
        val plans: List<MonthlyPlan>,
        val quotaCount: Int,
        val quotaMax: Int = MAX_AI_CUSTOM_PLAN_QUOTA,
        val activePlanProgressPercent: Int? = null,
        val confirmActivateTarget: MonthlyPlan? = null,
        val confirmDeleteTarget: MonthlyPlan? = null,
        /** C6 (issue #82 §9) -- disables Aktifkan/Pulihkan while a call is in flight. */
        val isBusy: Boolean = false,
    ) : MyPlansState
    data class Error(val message: String) : MyPlansState
}

sealed interface MyPlansEvent {
    data class RequestActivate(val plan: MonthlyPlan) : MyPlansEvent
    data object ConfirmActivate : MyPlansEvent
    data object CancelActivate : MyPlansEvent
    data class RequestDelete(val plan: MonthlyPlan) : MyPlansEvent
    data object ConfirmDelete : MyPlansEvent
    data object CancelDelete : MyPlansEvent
}

class MyPlansViewModel(
    private val getAllPlansForManagement: GetAllPlansForManagement,
    private val activatePlan: ActivatePlan,
    private val deletePlan: DeletePlan,
    private val checkDraftQuota: CheckDraftQuota,
    private val getPlanProgress: GetPlanProgress,
) : ViewModel() {
    private val _state = MutableStateFlow<MyPlansState>(MyPlansState.Loading)
    val state: StateFlow<MyPlansState> = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: MyPlansEvent) {
        when (event) {
            is MyPlansEvent.RequestActivate -> updateLoaded { it.copy(confirmActivateTarget = event.plan) }
            MyPlansEvent.CancelActivate -> updateLoaded { it.copy(confirmActivateTarget = null) }
            MyPlansEvent.ConfirmActivate -> confirmActivate()
            is MyPlansEvent.RequestDelete -> updateLoaded { it.copy(confirmDeleteTarget = event.plan) }
            MyPlansEvent.CancelDelete -> updateLoaded { it.copy(confirmDeleteTarget = null) }
            MyPlansEvent.ConfirmDelete -> confirmDelete()
        }
    }

    private fun updateLoaded(transform: (MyPlansState.Loaded) -> MyPlansState.Loaded) {
        _state.update { current -> if (current is MyPlansState.Loaded) transform(current) else current }
    }

    private fun load() {
        viewModelScope.launch {
            val plans = when (val result = getAllPlansForManagement()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            val quotaCount = when (val result = checkDraftQuota()) {
                is AppResult.Success -> result.data.existingAiPlans.size
                is AppResult.Error -> return@launch fail(result.failure.message)
            }
            val activePlan = plans.find { it.status == PlanStatus.ACTIVE }
            val progress = activePlan?.let {
                when (val result = getPlanProgress(it.id)) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> null
                }
            }

            _state.value = MyPlansState.Loaded(plans = plans, quotaCount = quotaCount, activePlanProgressPercent = progress)
        }
    }

    private fun confirmActivate() {
        val current = _state.value as? MyPlansState.Loaded ?: return
        val target = current.confirmActivateTarget ?: return
        _state.value = current.copy(isBusy = true)
        viewModelScope.launch {
            when (val result = activatePlan(target.id)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> load()
            }
        }
    }

    private fun confirmDelete() {
        val current = _state.value as? MyPlansState.Loaded ?: return
        val target = current.confirmDeleteTarget ?: return
        _state.value = current.copy(isBusy = true)
        viewModelScope.launch {
            when (val result = deletePlan(target.id)) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> load()
            }
        }
    }

    private fun fail(message: String) {
        _state.value = MyPlansState.Error(message)
    }
}
