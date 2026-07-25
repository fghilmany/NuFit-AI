package com.fghilmany.nufitai.presentation.ptmode.component

import androidx.compose.runtime.Composable
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_rpe_10_desc
import nufitai.shared.generated.resources.ptmode_rpe_10_label
import nufitai.shared.generated.resources.ptmode_rpe_1_desc
import nufitai.shared.generated.resources.ptmode_rpe_1_label
import nufitai.shared.generated.resources.ptmode_rpe_2_desc
import nufitai.shared.generated.resources.ptmode_rpe_2_label
import nufitai.shared.generated.resources.ptmode_rpe_3_desc
import nufitai.shared.generated.resources.ptmode_rpe_3_label
import nufitai.shared.generated.resources.ptmode_rpe_4_desc
import nufitai.shared.generated.resources.ptmode_rpe_4_label
import nufitai.shared.generated.resources.ptmode_rpe_5_desc
import nufitai.shared.generated.resources.ptmode_rpe_5_label
import nufitai.shared.generated.resources.ptmode_rpe_6_desc
import nufitai.shared.generated.resources.ptmode_rpe_6_label
import nufitai.shared.generated.resources.ptmode_rpe_7_desc
import nufitai.shared.generated.resources.ptmode_rpe_7_label
import nufitai.shared.generated.resources.ptmode_rpe_8_desc
import nufitai.shared.generated.resources.ptmode_rpe_8_label
import nufitai.shared.generated.resources.ptmode_rpe_9_desc
import nufitai.shared.generated.resources.ptmode_rpe_9_label
import org.jetbrains.compose.resources.stringResource

/** issue #80 P-06 -- plain-language RPE 1-10 anchors (US example: "7 = berat tapi masih sanggup 3 rep lagi"). */
data class RpeLevel(val value: Int, val label: String, val description: String)

@Composable
fun rpeLevels(): List<RpeLevel> = listOf(
    RpeLevel(1, stringResource(Res.string.ptmode_rpe_1_label), stringResource(Res.string.ptmode_rpe_1_desc)),
    RpeLevel(2, stringResource(Res.string.ptmode_rpe_2_label), stringResource(Res.string.ptmode_rpe_2_desc)),
    RpeLevel(3, stringResource(Res.string.ptmode_rpe_3_label), stringResource(Res.string.ptmode_rpe_3_desc)),
    RpeLevel(4, stringResource(Res.string.ptmode_rpe_4_label), stringResource(Res.string.ptmode_rpe_4_desc)),
    RpeLevel(5, stringResource(Res.string.ptmode_rpe_5_label), stringResource(Res.string.ptmode_rpe_5_desc)),
    RpeLevel(6, stringResource(Res.string.ptmode_rpe_6_label), stringResource(Res.string.ptmode_rpe_6_desc)),
    RpeLevel(7, stringResource(Res.string.ptmode_rpe_7_label), stringResource(Res.string.ptmode_rpe_7_desc)),
    RpeLevel(8, stringResource(Res.string.ptmode_rpe_8_label), stringResource(Res.string.ptmode_rpe_8_desc)),
    RpeLevel(9, stringResource(Res.string.ptmode_rpe_9_label), stringResource(Res.string.ptmode_rpe_9_desc)),
    RpeLevel(10, stringResource(Res.string.ptmode_rpe_10_label), stringResource(Res.string.ptmode_rpe_10_desc)),
)
