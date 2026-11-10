package com.fghilmany.nufitai.presentation.mealplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.startOfWeek
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.MealScheduleMode
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.mealplan.CalculateTdeeAndMacros
import com.fghilmany.nufitai.usecase.mealplan.GenerateMealPlan
import com.fghilmany.nufitai.usecase.mealplan.GetMealLogsForPlan
import com.fghilmany.nufitai.usecase.mealplan.GetMealPlanForWeek
import com.fghilmany.nufitai.usecase.mealplan.GetMealPreference
import com.fghilmany.nufitai.usecase.mealplan.GetMealReminderState
import com.fghilmany.nufitai.usecase.mealplan.GetPlannedMealsForPlan
import com.fghilmany.nufitai.usecase.mealplan.SetMealReminderEnabled
import com.fghilmany.nufitai.usecase.mealplan.ShouldSuggestRegenerate
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class MealPlanState(
    val weekStartDate: Instant,
    val currentWeekStart: Instant,
    val mealPlan: MealPlan? = null,
    val plannedMeals: List<PlannedMeal> = emptyList(),
    /** Null = no day currently generating. Disables every day's button while non-null (one day at a time). */
    val generatingDayNumber: Int? = null,
    /** Null = no regenerate confirmation pending. Set when the tapped day already has non-PENDING logs. */
    val pendingRegenerateDay: Int? = null,
    val error: String? = null,
    val showRegenerateSuggestion: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHasPermission: Boolean = true,
) {
    val mealsByDay: Map<Int, List<PlannedMeal>> get() = plannedMeals.groupBy { it.dayNumber }
    val canGoNext: Boolean get() = weekStartDate < currentWeekStart

    /** Null unless viewing the current week -- drives the weight-change suggestion banner's CTA. */
    val todayDayNumber: Int?
        get() {
            if (weekStartDate != currentWeekStart) return null
            val diffDays = (currentInstant() - weekStartDate).inWholeDays.toInt()
            return (diffDays + 1).coerceIn(1, 7)
        }
}

/** P-17 A (AC-6/AC-7/AC-8/AC-9/AC-10). Per-day generate (2026-11-10) -- see docs/techspec/meal-plan.md. */
class MealPlanViewModel(
    private val getMealPlanForWeek: GetMealPlanForWeek,
    private val getPlannedMealsForPlan: GetPlannedMealsForPlan,
    private val getMealLogsForPlan: GetMealLogsForPlan,
    private val generateMealPlan: GenerateMealPlan,
    private val calculateTdeeAndMacros: CalculateTdeeAndMacros,
    private val getLatestBodyMeasurement: GetLatestBodyMeasurement,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
    private val getMealPreference: GetMealPreference,
    private val shouldSuggestRegenerate: ShouldSuggestRegenerate,
    private val getMealReminderState: GetMealReminderState,
    private val setMealReminderEnabled: SetMealReminderEnabled,
) : ViewModel() {
    private val initialWeekStart = startOfWeek(currentInstant())
    private val _state = MutableStateFlow(MealPlanState(weekStartDate = initialWeekStart, currentWeekStart = initialWeekStart))
    val state: StateFlow<MealPlanState> = _state.asStateFlow()

    init {
        loadWeek(initialWeekStart)
        val reminderState = getMealReminderState()
        _state.update { it.copy(reminderEnabled = reminderState.enabled, reminderHasPermission = reminderState.hasPermission) }
    }

    fun onToggleReminder(enabled: Boolean) {
        setMealReminderEnabled(enabled)
        val reminderState = getMealReminderState()
        _state.update { it.copy(reminderEnabled = reminderState.enabled, reminderHasPermission = reminderState.hasPermission) }
    }

    fun onPrevWeek() = navigateWeek(_state.value.weekStartDate.minus(7.days))

    fun onNextWeek() {
        if (!_state.value.canGoNext) return
        navigateWeek(_state.value.weekStartDate.plus(7.days))
    }

    /** AC-8, per-day: confirms first if [day] already has a non-PENDING log; generates directly otherwise (nothing to lose). */
    fun onGenerateDayClick(day: Int) {
        val plan = _state.value.mealPlan
        if (plan == null) {
            performGenerateDay(day)
            return
        }
        viewModelScope.launch {
            val dayMealIds = _state.value.mealsByDay[day]?.map { it.id }?.toSet() ?: emptySet()
            val hasLogs = if (dayMealIds.isEmpty()) {
                false
            } else {
                when (val result = getMealLogsForPlan(plan.id)) {
                    is AppResult.Error -> false
                    is AppResult.Success -> result.data.any { it.plannedMealId in dayMealIds && it.status != MealLogStatus.PENDING }
                }
            }
            if (hasLogs) _state.update { it.copy(pendingRegenerateDay = day) } else performGenerateDay(day)
        }
    }

    fun onConfirmRegenerateDay() {
        val day = _state.value.pendingRegenerateDay ?: return
        _state.update { it.copy(pendingRegenerateDay = null) }
        performGenerateDay(day)
    }

    fun onDismissRegenerateConfirm() {
        _state.update { it.copy(pendingRegenerateDay = null) }
    }

    private fun navigateWeek(target: Instant) {
        if (target > _state.value.currentWeekStart) return
        viewModelScope.launch {
            when (val result = getMealPlanForWeek(target)) {
                is AppResult.Error -> _state.update { it.copy(error = result.failure.message) }
                is AppResult.Success -> {
                    val plan = result.data
                    // AC-6: a week that was never generated is only viewable if it's the current
                    // calendar week (which can always be viewed, empty, with per-day Generate CTAs) --
                    // otherwise this is a boundary, no-op the navigation.
                    if (plan == null && target != _state.value.currentWeekStart) return@launch
                    loadWeek(target, plan)
                }
            }
        }
    }

    private fun loadWeek(weekStart: Instant, preloadedPlan: MealPlan? = null) {
        viewModelScope.launch {
            val plan = preloadedPlan ?: when (val result = getMealPlanForWeek(weekStart)) {
                is AppResult.Error -> {
                    _state.update { it.copy(weekStartDate = weekStart, error = result.failure.message) }
                    return@launch
                }
                is AppResult.Success -> result.data
            }
            if (plan == null) {
                _state.update {
                    it.copy(weekStartDate = weekStart, mealPlan = null, plannedMeals = emptyList(), showRegenerateSuggestion = false, error = null)
                }
                return@launch
            }
            val meals = when (val result = getPlannedMealsForPlan(plan.id)) {
                is AppResult.Error -> {
                    _state.update { it.copy(weekStartDate = weekStart, mealPlan = plan, error = result.failure.message) }
                    return@launch
                }
                is AppResult.Success -> result.data
            }
            val suggest = (shouldSuggestRegenerate(plan) as? AppResult.Success)?.data ?: false
            _state.update { it.copy(weekStartDate = weekStart, mealPlan = plan, plannedMeals = meals, showRegenerateSuggestion = suggest, error = null) }
        }
    }

    private fun performGenerateDay(day: Int) {
        viewModelScope.launch {
            _state.update { it.copy(generatingDayNumber = day, error = null) }

            val bodyMeasurement = when (val result = getLatestBodyMeasurement()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val weightKg = bodyMeasurement?.weightKg
            if (weightKg == null || fullAssessment == null) {
                return@launch fail("Data tubuh/aktivitas belum lengkap")
            }
            val target = when (val result = calculateTdeeAndMacros(bodyMeasurement, fullAssessment)) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data
            }
            val preference = when (val result = getMealPreference()) {
                is AppResult.Error -> return@launch fail(result.failure.message)
                is AppResult.Success -> result.data ?: MealPreference(
                    cuisineThemes = emptySet(),
                    mood = null,
                    allergies = emptyList(),
                    scheduleMode = MealScheduleMode.AI_RANDOM,
                )
            }

            when (
                val result = generateMealPlan(
                    weekStartDate = _state.value.weekStartDate,
                    dayNumber = day,
                    target = target,
                    goal = fullAssessment.goal,
                    weightKgAtGeneration = weightKg,
                    preference = preference,
                )
            ) {
                is AppResult.Error -> fail(result.failure.message)
                is AppResult.Success -> {
                    _state.update { it.copy(generatingDayNumber = null) }
                    loadWeek(_state.value.weekStartDate, result.data)
                }
            }
        }
    }

    private fun fail(message: String) {
        _state.update { it.copy(generatingDayNumber = null, error = message) }
    }
}
