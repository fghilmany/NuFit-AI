package com.fghilmany.nufitai.data.sync.datasource

import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wipes every account/user-data table in one transaction -- used on sign-out (privacy: the next
 * guest on this device shouldn't see the previous user's plans/chat/assessments) and when a
 * sign-in conflicts with pre-existing local guest data. `exercise` is deliberately excluded --
 * it's the bundled catalog, identical for everyone, not account data.
 */
class LocalDataWipeDataSource(private val database: NuFitDatabase) {
    /** True if any of the 8 synced "did this guest actually do anything" tables has rows --
     * used to detect a conflict when signing into an existing account on a device that already
     * has local guest data. Child/local-only tables (plan_day, meal_preference, user_profile,
     * etc.) are deliberately excluded -- meaningless on their own as a "has activity" signal. */
    suspend fun hasAnyData(): Boolean = withContext(Dispatchers.Default) {
        database.transactionWithResult {
            database.monthlyPlanQueries.countAll().executeAsOne() > 0 ||
                database.fullAssessmentResultQueries.countAll().executeAsOne() > 0 ||
                database.quickAssessmentResultQueries.countAll().executeAsOne() > 0 ||
                database.parQResultQueries.countAll().executeAsOne() > 0 ||
                database.bodyMeasurementQueries.countAll().executeAsOne() > 0 ||
                database.workoutSessionQueries.countAll().executeAsOne() > 0 ||
                database.chatMessageQueries.countAll().executeAsOne() > 0 ||
                database.mealPlanQueries.countAll().executeAsOne() > 0
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.Default) {
        database.transaction {
            database.monthlyPlanQueries.deleteAll()
            database.planDayQueries.deleteAll()
            database.plannedExerciseQueries.deleteAll()
            database.planDaySessionLogQueries.deleteAll()
            database.fullAssessmentResultQueries.deleteAll()
            database.fullAssessmentFlagDetailQueries.deleteAll()
            database.quickAssessmentResultQueries.deleteAll()
            database.parQResultQueries.deleteAll()
            database.parQAnswerDetailQueries.deleteAll()
            database.bodyMeasurementQueries.deleteAll()
            database.workoutSessionQueries.deleteAll()
            database.workoutExerciseLogQueries.deleteAll()
            database.workoutSetLogQueries.deleteAll()
            database.chatMessageQueries.deleteAll()
            database.chatExerciseOverrideQueries.deleteAll()
            database.mealPlanQueries.deleteAll()
            database.plannedMealQueries.deleteAll()
            database.mealLogQueries.deleteAll()
            database.mealPreferenceQueries.deleteAll()
            database.userProfileQueries.deleteAll()
        }
    }
}
