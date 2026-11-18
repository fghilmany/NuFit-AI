package com.fghilmany.nufitai.data.chatai.datasource

import com.fghilmany.nufitai.data.chatai.model.ChatSendRequestDto
import com.fghilmany.nufitai.data.chatai.model.ChatSendResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * [reason] is the server's typed `{"reason": "..."}` body on a 403 (`chat-send/index.ts`):
 * `quota_exceeded`, `ai_access_blocked`, or `requires_login` -- null when the body isn't that
 * shape (network/5xx error page) or the status isn't 403. Previously this only carried a
 * `quotaExceeded: Boolean` set for EVERY 403 regardless of actual reason, so `ai_access_blocked`
 * and `requires_login` were both misreported as "quota habis" to the repository layer.
 */
class ChatApiException(val statusCode: Int, val responseBody: String = "", val reason: String? = null) :
    Exception("Chat request failed with status $statusCode: $responseBody")

/**
 * techspec chat-ai.md §7 -- mirrors `AiCustomPlanRemoteDataSource`'s exact shape. Backend now real
 * (`supabase/functions/chat-send`, ADR-008) -- deploy it and swap [BASE_URL].
 */
class ChatRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun send(request: ChatSendRequestDto, accessToken: String): ChatSendResponseDto {
        val response = httpClient.post("$BASE_URL/chat-send") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val bodyText = response.bodyAsText()
            val reason = if (response.status.value == 403) parseReason(bodyText) else null
            throw ChatApiException(response.status.value, bodyText, reason)
        }
        return response.body()
    }

    companion object {
        // Real Supabase Functions invoke URL (not the dashboard page) -- functions must be deployed first (ADR-008).
        const val BASE_URL = "https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1"
    }
}

/** Returns null (not throw) on an unparseable body -- a 403 whose body isn't the expected
 * `{"reason": "..."}` shape should still surface as a generic network error, not crash. */
private fun parseReason(body: String): String? = try {
    Json.parseToJsonElement(body).jsonObject["reason"]?.jsonPrimitive?.content
} catch (e: Exception) {
    null
}
