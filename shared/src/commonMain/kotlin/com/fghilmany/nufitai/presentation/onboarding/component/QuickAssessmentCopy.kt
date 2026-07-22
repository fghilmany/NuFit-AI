package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference

data class OptionCopy(val icon: ImageVector, val title: String, val subtitle: String)

val ExperienceOptions: List<Pair<Experience, OptionCopy>> = listOf(
    Experience.BELUM_PERNAH to OptionCopy(Icons.Filled.EmojiPeople, "Belum pernah", "Ini pertama kalinya saya ke gym"),
    Experience.KURANG_1_TAHUN to
        OptionCopy(Icons.Filled.TrendingUp, "Kurang dari 1 tahun", "Masih baru, belum terlalu konsisten"),
    Experience.RUTIN_BERTAHUN to
        OptionCopy(Icons.Filled.WorkspacePremium, "Rutin bertahun-tahun", "Sudah terbiasa latihan secara rutin"),
)

val GoalOptions: List<Pair<GoalCategory, OptionCopy>> = listOf(
    GoalCategory.FAT_LOSS to
        OptionCopy(Icons.Filled.LocalFireDepartment, "Turun berat badan", "Bakar lemak secara efisien"),
    GoalCategory.MUSCLE_GAIN to
        OptionCopy(Icons.Filled.FitnessCenter, "Bentuk otot", "Tingkatkan massa dan definisi"),
    GoalCategory.GENERAL_HEALTH to
        OptionCopy(Icons.Filled.Favorite, "Sehat & bugar", "Stamina untuk kegiatan harian"),
    GoalCategory.STRENGTH to
        OptionCopy(Icons.Filled.Bolt, "Jadi lebih kuat", "Fokus pada kekuatan fungsional"),
)

val FrequencyOptions: List<Pair<FrequencyBucket, OptionCopy>> = listOf(
    FrequencyBucket.TWO_TO_THREE to
        OptionCopy(Icons.Filled.CalendarToday, "2-3x per minggu", "Cocok untuk memulai secara realistis"),
    FrequencyBucket.FOUR_TO_FIVE to
        OptionCopy(Icons.Filled.CalendarMonth, "4-5x per minggu", "Untuk yang sudah siap lebih intens"),
)

val SplitOptions: List<Pair<SplitPreference, OptionCopy>> = listOf(
    SplitPreference.FULL_BODY to OptionCopy(Icons.Filled.FitnessCenter, "Full Body", "Seluruh tubuh dilatih tiap sesi"),
    SplitPreference.UPPER_LOWER to
        OptionCopy(Icons.Filled.SwapVert, "Upper/Lower", "Dipisah antara tubuh atas dan bawah"),
    SplitPreference.TIDAK_TAHU to
        OptionCopy(Icons.Filled.HelpOutline, "Tidak tahu", "Serahkan pilihan ke sistem"),
)
