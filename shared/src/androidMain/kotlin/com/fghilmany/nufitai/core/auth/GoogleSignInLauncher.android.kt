package com.fghilmany.nufitai.core.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.logging.AppLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException

/**
 * issue #83 follow-up (ADR-006) -- real Android Google Sign-In via Credential Manager. Requires
 * [CurrentActivityHolder] to hold a live Activity (registered by `MainActivity`'s
 * onResume/onPause) since `getCredential()` needs one to show the account picker sheet.
 */
actual class GoogleSignInLauncher actual constructor() {
    private val logger = AppLogger.tagged("GoogleSignInLauncher")

    actual suspend fun launch(): AppResult<NativeSignInResult> {
        val activity = CurrentActivityHolder.activity
            ?: return AppResult.Error(Failure.Network("Couldn't open sign-in right now. Please try again.")).also {
                logger.w { "No active Activity to show the Google sign-in sheet" }
            }

        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        // Dev report (19 Nov 2026): on a fresh install, the very first getCredential() call
        // often fails with GetCredentialException because Google Play Services hasn't warmed up
        // its credential-provider cache yet -- a second tap always worked. Retrying once
        // internally fixes the symptom without making the user tap twice. Must NOT retry on
        // GetCredentialCancellationException (the user closed the sheet themselves) -- retrying
        // that would pop the sheet right back up immediately after they dismissed it.
        return try {
            fetchCredential(activity, request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            logger.i { "Google sign-in cancelled by user" }
            AppResult.Error(Failure.Network("Google sign-in was cancelled."))
        } catch (e: GetCredentialException) {
            logger.e(e) { "Google sign-in failed on first attempt, retrying once" }
            try {
                fetchCredential(activity, request)
            } catch (e2: CancellationException) {
                throw e2
            } catch (e2: GetCredentialException) {
                logger.e(e2) { "Google sign-in failed on retry" }
                AppResult.Error(Failure.Network("Couldn't sign in with Google. Please try again."))
            } catch (e2: Exception) {
                logger.e(e2) { "Google sign-in failed on retry (unexpected error)" }
                AppResult.Error(Failure.Network("Couldn't sign in with Google. Please try again."))
            }
        } catch (e: Exception) {
            logger.e(e) { "Google sign-in failed (unexpected error)" }
            AppResult.Error(Failure.Network("Couldn't sign in with Google. Please try again."))
        }
    }

    private suspend fun fetchCredential(activity: Activity, request: GetCredentialRequest): AppResult<NativeSignInResult> {
        val response = CredentialManager.create(activity).getCredential(activity, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
        return AppResult.Success(
            NativeSignInResult(
                idToken = googleIdTokenCredential.idToken,
                displayName = googleIdTokenCredential.displayName,
                profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString(),
            ),
        )
    }

    companion object {
        // TODO(dev): replace with your real OAuth Web Client ID from Google Cloud Console
        // (Credentials -> Create OAuth client ID -> Web application). This is the client ID
        // Supabase verifies the ID token's audience against -- paste the SAME value into
        // Supabase Dashboard -> Authentication -> Providers -> Google. You'll also need a
        // second OAuth client ID (type "Android") registered with your app's package name +
        // SHA-1 signing fingerprint for Credential Manager itself to authorize the request.
        const val GOOGLE_WEB_CLIENT_ID = "42470672876-dqk6hcjh9m6b3cc5g50p51gqgg58rg1m.apps.googleusercontent.com"
    }
}
