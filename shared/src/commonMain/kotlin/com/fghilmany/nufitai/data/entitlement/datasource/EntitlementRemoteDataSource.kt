package com.fghilmany.nufitai.data.entitlement.datasource

import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

class EntitlementApiException(val statusCode: Int) : Exception("Entitlement fetch failed with status $statusCode")

@Serializable
private data class UserEntitlementRow(val tier: String)

/**
 * ADR-008 follow-up -- the real replacement for the client-side `[DEBUG] Simulate subscriber`
 * toggle. Plain PostgREST GET (ADR-005 -- no supabase-kt SDK), mirrors `SyncRemoteDataSource`'s
 * existing pattern exactly. `user_entitlement` requires the new self-select RLS policy
 * (`20260801000001_user_entitlement_self_select.sql`) for this to return anything but 200-empty.
 */
class EntitlementRemoteDataSource(private val httpClient: HttpClient) {
    /** Returns the tier string ("LOGGED_IN"/"SUBSCRIBED") or null if no row exists yet (server
     * only lazily creates one the first time an AI Edge Function is called for this user). */
    suspend fun getTier(userId: String, accessToken: String): String? {
        val response = httpClient.get("${AuthRemoteDataSource.SUPABASE_URL}/rest/v1/user_entitlement") {
            header("apikey", AuthRemoteDataSource.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer $accessToken")
            parameter("user_id", "eq.$userId")
            parameter("select", "tier")
        }
        if (!response.status.isSuccess()) throw EntitlementApiException(response.status.value)
        val rows: List<UserEntitlementRow> = response.body()
        return rows.firstOrNull()?.tier
    }
}
