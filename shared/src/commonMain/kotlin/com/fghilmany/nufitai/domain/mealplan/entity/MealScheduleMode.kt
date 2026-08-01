package com.fghilmany.nufitai.domain.mealplan.entity

/** P-17 B "Preferensi Jadwal". MANUAL_PER_DAY's actual day-by-day picker UI is deferred (techspec §9 item 14) -- only the mode itself is persisted this pass. */
enum class MealScheduleMode { AI_RANDOM, MANUAL_PER_DAY }
