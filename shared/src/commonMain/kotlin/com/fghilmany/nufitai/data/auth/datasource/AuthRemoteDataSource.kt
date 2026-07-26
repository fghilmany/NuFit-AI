package com.fghilmany.nufitai.data.auth.datasource

import com.fghilmany.nufitai.data.auth.model.RefreshTokenRequestDto
import com.fghilmany.nufitai.data.auth.model.SignInWithIdTokenRequestDto
import com.fghilmany.nufitai.data.auth.model.SupabaseAuthResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthApiException(val statusCode: Int) : Exception("Auth request failed with status $statusCode")

/** techspec §7 (auth-sync.md) -- Supabase Auth REST, plain Ktor (ADR-004/ADR-005), no supabase-kt SDK. */
class AuthRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun signInWithIdToken(provider: String, idToken: String): SupabaseAuthResponseDto {
        val response = httpClient.post("$SUPABASE_URL/auth/v1/token?grant_type=id_token") {
            contentType(ContentType.Application.Json)
            header("apikey", SUPABASE_ANON_KEY)
            setBody(SignInWithIdTokenRequestDto(provider = provider, id_token = idToken))
        }
        if (!response.status.isSuccess()) throw AuthApiException(response.status.value)
        return response.body()
    }

    suspend fun refreshToken(refreshToken: String): SupabaseAuthResponseDto {
        val response = httpClient.post("$SUPABASE_URL/auth/v1/token?grant_type=refresh_token") {
            contentType(ContentType.Application.Json)
            header("apikey", SUPABASE_ANON_KEY)
            setBody(RefreshTokenRequestDto(refresh_token = refreshToken))
        }
        if (!response.status.isSuccess()) throw AuthApiException(response.status.value)
        return response.body()
    }

    companion object {
        // TODO(dev): no real Supabase project exists yet -- replace before first live use (techspec §7).
        const val SUPABASE_URL = "https://your-project.supabase.co"
        const val SUPABASE_ANON_KEY = "your-anon-key"
    }
}
