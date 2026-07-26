package com.fghilmany.nufitai.domain.monthlyplan.entity

enum class PlanSource { LOCAL_TEMPLATE, LOGGED_IN_RULE_ENGINE, AI_GENERATED }

/** issue #81 -- DRAFT: saved but not activated (AI Custom Plan preview, "Simpan Draft"). */
enum class PlanStatus { ACTIVE, ARCHIVED, DRAFT }

enum class DayType { SESSION, REST, LIGHT_ACTIVITY }
