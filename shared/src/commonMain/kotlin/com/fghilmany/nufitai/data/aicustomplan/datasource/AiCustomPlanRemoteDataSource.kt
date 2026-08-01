package com.fghilmany.nufitai.data.aicustomplan.datasource

import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanRequestDto
import com.fghilmany.nufitai.data.aicustomplan.model.AiPlanResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/** Thrown on a non-2xx response; [AiCustomPlanRepositoryImpl] maps [statusCode] to a typed [com.fghilmany.nufitai.core.error.Failure]. */
class AiCustomPlanApiException(val statusCode: Int) : Exception("AI generate request failed with status $statusCode")

/**
 * issue #81 -- this project's first network feature. Backend now real (`supabase/functions/
 * ai-custom-plan-generate`, ADR-008). Plain Ktor `HttpClient` chosen over Ktorfit for
 * this pass (ADR-004) -- swapping to Ktorfit later only touches this class, per
 * [com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository]'s interface boundary.
 */
class AiCustomPlanRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun generatePlan(request: AiPlanRequestDto, accessToken: String): AiPlanResponseDto {
        val response = httpClient.post("$BASE_URL/v1/ai-custom-plan/generate") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw AiCustomPlanApiException(response.status.value)
        }
        return response.body()
    }

    companion object {
        // Real Supabase Functions invoke URL (not the dashboard page) -- functions must be deployed first (ADR-008).
        const val BASE_URL = "https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1"
    }
}
