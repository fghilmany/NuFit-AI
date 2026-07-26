package com.fghilmany.nufitai.data.aicustomplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.data.aicustomplan.datasource.AiCustomPlanApiException
import com.fghilmany.nufitai.data.aicustomplan.datasource.AiCustomPlanRemoteDataSource
import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanRequestDto
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

private const val GENERATE_TIMEOUT_MS = 30_000L

class AiCustomPlanRepositoryImpl(
    private val remote: AiCustomPlanRemoteDataSource,
) : AiCustomPlanRepository {
    override suspend fun generatePlan(request: AiPlanGenerationRequest): AppResult<AiGeneratedPlanDraft> = try {
        val response = withTimeoutOrNull(GENERATE_TIMEOUT_MS) { remote.generatePlan(AiPlanRequestDto.from(request)) }
            ?: return AppResult.Error(Failure.Network("AI plan generation timed out"))
        AppResult.Success(response.toEntity())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AiCustomPlanApiException) {
        when (e.statusCode) {
            401 -> AppResult.Error(Failure.Unauthorized())
            422 -> AppResult.Error(Failure.Validation("AI request validation failed"))
            else -> AppResult.Error(Failure.Network("AI backend error (${e.statusCode})"))
        }
    } catch (e: Exception) {
        AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
    }
}
