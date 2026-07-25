package com.fghilmany.nufitai.presentation.assessmentdetail.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.assessmentdetail_level_beginner
import nufitai.shared.generated.resources.assessmentdetail_level_intermediate
import nufitai.shared.generated.resources.assessmentdetail_split_full_body
import nufitai.shared.generated.resources.assessmentdetail_split_upper_lower
import org.jetbrains.compose.resources.stringResource

@Composable
fun Level.shortLabel(): String = when (this) {
    Level.BEGINNER -> stringResource(Res.string.assessmentdetail_level_beginner)
    Level.INTERMEDIATE -> stringResource(Res.string.assessmentdetail_level_intermediate)
}

@Composable
fun ResolvedSplit.shortLabel(): String = when (this) {
    ResolvedSplit.FULL_BODY -> stringResource(Res.string.assessmentdetail_split_full_body)
    ResolvedSplit.UPPER_LOWER -> stringResource(Res.string.assessmentdetail_split_upper_lower)
}
