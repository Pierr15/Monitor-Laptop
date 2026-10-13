package com.aetherglass.data.repository

import com.aetherglass.data.api.AuthResponse
import com.aetherglass.data.api.SignInRequest
import com.aetherglass.data.api.SignUpRequest
import com.aetherglass.data.api.SupabaseApiService
import com.aetherglass.domain.model.UserProfile
import com.aetherglass.domain.repository.AuthRepository
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseApi: SupabaseApiService,
    private val secureStorage: SecureStorageRepository
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override fun observeAuthState() = _authState.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return supabaseApi.signIn(SignInRequest(email, password)).flatMap { response ->
            secureStorage.putString("access_token", response.access_token)
            secureStorage.putString("refresh_token", response.refresh_token)
            Result.success(toUserProfile(response.user))
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<UserProfile> {
        return supabaseApi.signUp(SignUpRequest(email, password, mapOf("name" to name))).flatMap { response ->
            secureStorage.putString("access_token", response.access_token)
            secureStorage.putString("refresh_token", response.refresh_token)
            Result.success(toUserProfile(response.user))
        }
    }

    override suspend fun signInWithOAuth(provider: OAuthProvider): Result<UserProfile> {
        // TODO: Implement OAuth flow with WebView or Chrome Custom Tab
        return Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "OAuth not implemented"))
    }

    override suspend fun signOut(): Result<Unit> {
        return supabaseApi.signOut().onSuccess {
            secureStorage.clear()
            _authState.value = AuthState.Unauthenticated(null)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        // TODO: Implement password reset via Supabase
        return Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "Password reset not implemented"))
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<UserProfile> {
        // TODO: Implement password update via Supabase
        return Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "Password update not implemented"))
    }

    override suspend fun getCurrentUser(): Result<UserProfile?> {
        val token = secureStorage.getString("access_token").getOrNull()
        return if (token != null) {
            supabaseApi.getUser(token).map { toUserProfile(it) }
        } else {
            Result.success(null)
        }
    }

    override suspend fun refreshSession(): Result<UserProfile> {
        val refreshToken = secureStorage.getString("refresh_token").getOrNull()
        return if (refreshToken != null) {
            supabaseApi.refreshToken(com.aetherglass.data.api.RefreshTokenRequest(refreshToken)).flatMap { response ->
                secureStorage.putString("access_token", response.access_token)
                secureStorage.putString("refresh_token", response.refresh_token)
                Result.success(toUserProfile(response.user))
            }
        } else {
            Result.failure(com.aetherglass.util.Error("NO_REFRESH_TOKEN", "No refresh token available"))
        }
    }

    private fun toUserProfile(response: com.aetherglass.data.api.UserResponse): UserProfile {
        return UserProfile(
            id = response.id,
            email = response.email,
            name = response.user_metadata["name"],
            avatarUrl = response.user_metadata["avatar_url"],
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).parse(response.created_at)?.time ?: 0,
            updatedAt = System.currentTimeMillis()
        )
    }
}