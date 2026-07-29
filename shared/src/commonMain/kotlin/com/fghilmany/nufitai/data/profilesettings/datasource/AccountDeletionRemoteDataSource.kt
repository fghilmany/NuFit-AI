package com.fghilmany.nufitai.data.profilesettings.datasource

import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.isSuccess

class AccountDeletionApiException(val statusCode: Int) : Exception("Delete-account request failed with status $statusCode")

/**
 * techspec profile-settings.md §7 -- mirrors `AuthRemoteDataSource`'s exact shape (plain Ktor,
 * same header pattern, same Supabase constants). The `/functions/v1/delete-account` Edge
 * Function isn't deployed yet (tracked separately, issue #86) -- this class is ready for it.
 */
class AccountDeletionRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun deleteAccount(accessToken: String) {
        val response = httpClient.post("${AuthRemoteDataSource.SUPABASE_URL}/functions/v1/delete-account") {
            header("Authorization", "Bearer $accessToken")
            header("apikey", AuthRemoteDataSource.SUPABASE_ANON_KEY)
        }
        if (!response.status.isSuccess()) {
            throw AccountDeletionApiException(response.status.value)
        }
    }
}
