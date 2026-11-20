package com.fghilmany.nufitai.data.appconfig.datasource

import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

class AppConfigApiException(val statusCode: Int) : Exception("app_config fetch failed with status $statusCode")

@Serializable
private data class AppConfigRow(val key: String, val value: String)

/** Public, read-only key/value config (`app_config` table, no RLS auth needed). Plain PostgREST
 * GET, mirrors `EntitlementRemoteDataSource`'s pattern. */
class AppConfigRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun getConfig(): Map<String, String> {
        val response = httpClient.get("${AuthRemoteDataSource.SUPABASE_URL}/rest/v1/app_config") {
            header("apikey", AuthRemoteDataSource.SUPABASE_ANON_KEY)
            parameter("select", "key,value")
        }
        if (!response.status.isSuccess()) throw AppConfigApiException(response.status.value)
        val rows: List<AppConfigRow> = response.body()
        return rows.associate { it.key to it.value }
    }
}
