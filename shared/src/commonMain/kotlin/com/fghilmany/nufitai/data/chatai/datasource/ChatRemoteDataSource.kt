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

class ChatApiException(val statusCode: Int, val responseBody: String = "", val quotaExceeded: Boolean = false) :
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
            val quotaExceeded = response.status.value == 403
            throw ChatApiException(response.status.value, response.bodyAsText(), quotaExceeded)
        }
        return response.body()
    }

    companion object {
        // Real Supabase Functions invoke URL (not the dashboard page) -- functions must be deployed first (ADR-008).
        const val BASE_URL = "https://wnruvcpeyxssxpqwptst.supabase.co/functions/v1"
    }
}
