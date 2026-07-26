package com.fghilmany.nufitai.data.aicustomplan.datasource

import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanRequestDto
import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/** Thrown on a non-2xx response; [AiCustomPlanRepositoryImpl] maps [statusCode] to a typed [com.fghilmany.nufitai.core.error.Failure]. */
class AiCustomPlanApiException(val statusCode: Int) : Exception("AI generate request failed with status $statusCode")

/**
 * issue #81 -- this project's first network feature. No live backend exists yet (techspec §7);
 * built against the documented contract only. Plain Ktor `HttpClient` chosen over Ktorfit for
 * this pass (ADR-004) -- swapping to Ktorfit later only touches this class, per
 * [com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository]'s interface boundary.
 */
class AiCustomPlanRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun generatePlan(request: AiPlanRequestDto): AiPlanResponseDto {
        val response = httpClient.post("$BASE_URL/v1/ai-custom-plan/generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw AiCustomPlanApiException(response.status.value)
        }
        return response.body()
    }

    companion object {
        // TODO(dev): no real backend exists yet -- replace before first live use (techspec §9 item 2).
        const val BASE_URL = "https://api.nufit.example.com"
    }
}
