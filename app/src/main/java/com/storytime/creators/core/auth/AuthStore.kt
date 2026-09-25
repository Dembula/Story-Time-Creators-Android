package com.storytime.creators.core.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.storytime.creators.core.model.CreatorUser
import com.storytime.creators.core.model.CsrfResponse
import com.storytime.creators.core.model.EntryRedirectResponse
import com.storytime.creators.core.network.ApiClient
import com.storytime.creators.core.network.ApiException
import com.storytime.creators.core.network.AppConfig
import com.storytime.creators.core.network.get

/** Compose snapshot-backed auth store, mirroring the iOS AuthService.shared. */
class AuthStore(private val client: ApiClient) {

    var isAuthenticated by mutableStateOf(false)
        private set
    var currentUser by mutableStateOf<CreatorUser?>(null)
        private set
    /** Relative path when license/plan is incomplete (e.g. /creator/onboarding/license). */
    var pendingOnboardingPath by mutableStateOf<String?>(null)
        private set
    var lastError by mutableStateOf<String?>(null)
    var isBusy by mutableStateOf(false)
        private set

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val needsPlanSetup: Boolean
        get() {
            val path = pendingOnboardingPath ?: return false
            if (path.isEmpty()) return false
            return path.contains("onboarding") || path.contains("license") || path.contains("subscription")
        }

    fun applyProfile(user: CreatorUser) {
        currentUser = user
    }

    suspend fun restoreSession() {
        try {
            val me: CreatorUser = client.get("/api/me")
            if (!me.isCreatorPortalEligible) {
                clearLocalSession()
                return
            }
            currentUser = me
            isAuthenticated = true
            refreshPackageGate()
        } catch (e: Exception) {
            clearLocalSession()
        }
    }

    suspend fun signIn(email: String, password: String) {
        isBusy = true
        lastError = null
        try {
            val csrf: CsrfResponse = client.get("/api/auth/csrf")
            val fields = mapOf(
                "csrfToken" to csrf.csrfToken,
                "email" to email.trim().lowercase(),
                "password" to password,
                "selectedRole" to AppConfig.CREATOR_ROLE,
                "json" to "true",
                "redirect" to "false",
                "callbackUrl" to "/creator/command-center",
            )
            val (status, body) = client.postForm("/api/auth/callback/credentials-creator", fields)

            if (status !in 200..399) {
                throw ApiException.Http(status, body.ifEmpty { "Sign in failed." })
            }

            val errorText = runCatching {
                (json.parseToJsonElement(body) as? JsonObject)
                    ?.get("error")?.jsonPrimitive?.content
            }.getOrNull()
            if (!errorText.isNullOrEmpty()) {
                throw ApiException.Http(
                    401,
                    if (errorText == "CredentialsSignin") "Invalid email or password." else errorText,
                )
            }

            val me: CreatorUser = client.get("/api/me")
            if (!me.isCreatorPortalEligible) {
                clearSessionCookies()
                throw ApiException.Http(403, "This app is for content creator accounts only.")
            }
            currentUser = me
            isAuthenticated = true
            refreshPackageGate()
        } catch (e: ApiException) {
            lastError = e.message
            clearLocalSession()
        } catch (e: Exception) {
            lastError = e.message ?: "Sign in failed."
            clearLocalSession()
        } finally {
            isBusy = false
        }
    }

    /** Mirrors web entry-redirect: unfinished license → onboarding path. */
    suspend fun refreshPackageGate() {
        try {
            val entry: EntryRedirectResponse = client.get("/api/auth/entry-redirect")
            pendingOnboardingPath = entry.path
        } catch (_: Exception) {
            // Don't trap the user forever if /me works but entry-redirect fails.
            pendingOnboardingPath = null
        }
    }

    suspend fun signOut() {
        try {
            val csrf: CsrfResponse = client.get("/api/auth/csrf")
            runCatching {
                client.postForm(
                    "/api/auth/signout",
                    mapOf("csrfToken" to csrf.csrfToken, "json" to "true", "callbackUrl" to "/"),
                )
            }
        } catch (_: Exception) {
        }
        clearSessionCookies()
        clearLocalSession()
    }

    private fun clearLocalSession() {
        currentUser = null
        isAuthenticated = false
        pendingOnboardingPath = null
    }

    private fun clearSessionCookies() {
        client.cookieJar.clear()
    }
}
