package com.fghilmany.nufitai.data.aicustomplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.data.aicustomplan.datasource.AiCustomPlanApiException
import com.fghilmany.nufitai.data.aicustomplan.datasource.AiCustomPlanRemoteDataSource
import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanRequestDto
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

// Was 30_000L -- too short for glm-5.3-flash's observed latency on larger structured-output
// requests (see llmClient.ts's DEFAULT_MODEL comment); matches mealPlanModule's own
// GENERATE_TIMEOUT_MS, which hit the identical client-timeout-too-aggressive bug first and was
// raised to track Supabase Edge Functions' ~150s wall-clock cap instead of guessing a lower number.
private const val GENERATE_TIMEOUT_MS = 150_000L

class AiCustomPlanRepositoryImpl(
    private val remote: AiCustomPlanRemoteDataSource,
    private val authRepository: AuthRepository,
) : AiCustomPlanRepository {
    private val logger = AppLogger.tagged("AiCustomPlanRepository")

    override suspend fun generatePlan(request: AiPlanGenerationRequest): AppResult<AiGeneratedPlanDraft> {
        val session = authRepository.getCurrentSession() ?: return AppResult.Error(Failure.Unauthorized())
        logger.i { "generatePlan starting" }
        return try {
            val response = withTimeoutOrNull(GENERATE_TIMEOUT_MS) {
                remote.generatePlan(AiPlanRequestDto.from(request), session.accessToken)
            } ?: run {
                logger.w { "generatePlan timed out after ${GENERATE_TIMEOUT_MS}ms" }
                return AppResult.Error(Failure.Network("AI plan generation timed out"))
            }
            logger.i { "generatePlan succeeded" }
            AppResult.Success(response.toEntity())
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiCustomPlanApiException) {
            logger.e(e) { "generatePlan failed -- HTTP ${e.statusCode}" }
            when (e.statusCode) {
                401 -> AppResult.Error(Failure.Unauthorized())
                403 -> AppResult.Error(Failure.Validation("Not subscribed or quota exceeded"))
                422 -> AppResult.Error(Failure.Validation("AI request validation failed"))
                else -> AppResult.Error(Failure.Network("AI backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "generatePlan failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }
}
