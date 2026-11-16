package com.fghilmany.nufitai.domain.fullassessment.entity

/** Asked before the equipment checklist so a beginner only sees equipment relevant to where
 * they'll actually train -- gym machines for a home-only user (or vice versa) just adds
 * confusing choices. */
enum class TrainingLocation { GYM, HOME }
