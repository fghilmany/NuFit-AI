package com.fghilmany.nufitai.data.sync.datasource

import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource.Companion.SUPABASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject

class SyncApiException(val statusCode: Int) : Exception("Sync request failed with status $statusCode")

/** techspec §7 -- generic PostgREST push/pull, one call per aggregate table, no supabase-kt SDK (ADR-005). */
class SyncRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun push(table: String, rows: List<JsonObject>, accessToken: String) {
        if (rows.isEmpty()) return
        val response = httpClient.post("$SUPABASE_URL/rest/v1/$table") {
            contentType(ContentType.Application.Json)
            header("apikey", com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer $accessToken")
            header("Prefer", "resolution=merge-duplicates")
            parameter("on_conflict", "id")
            setBody(rows)
        }
        if (!response.status.isSuccess()) throw SyncApiException(response.status.value)
    }

    suspend fun pull(table: String, userId: String, sinceIso: String, accessToken: String): List<JsonObject> {
        val response = httpClient.get("$SUPABASE_URL/rest/v1/$table") {
            header("apikey", com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer $accessToken")
            parameter("user_id", "eq.$userId")
            parameter("updated_at", "gt.$sinceIso")
        }
        if (!response.status.isSuccess()) throw SyncApiException(response.status.value)
        return response.body()
    }
}
