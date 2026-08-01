package com.fghilmany.nufitai.data.mealplan.datasource

import com.fghilmany.nufitai.data.mealplan.model.MealPlanRequestDto
import com.fghilmany.nufitai.data.mealplan.model.MealPlanResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class MealPlanApiException(val statusCode: Int) : Exception("Meal plan generate request failed with status $statusCode")

/**
 * techspec meal-plan.md §7 -- backend now real (`supabase/functions/meal-plan-generate`, ADR-008);
 * mirrors `AiCustomPlanRemoteDataSource`/`ChatRemoteDataSource`'s exact shape (plain Ktor,
 * `BASE_URL` placeholder constant with a `TODO(dev)` comment until deployed).
 */
class MealPlanRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun generate(request: MealPlanRequestDto, accessToken: String): MealPlanResponseDto {
        val response = httpClient.post("$BASE_URL/v1/meal-plan/generate") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw MealPlanApiException(response.status.value)
        }
        return response.body()
    }

    companion object {
        // Real Supabase Functions invoke URL (not the dashboard page) -- functions must be deployed first (ADR-008).
        const val BASE_URL = "https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1"
    }
}
