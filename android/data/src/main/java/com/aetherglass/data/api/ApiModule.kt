package com.aetherglass.data.api

import com.aetherglass.data.api.impl.AgentApiServiceImpl
import com.aetherglass.data.api.impl.SupabaseApiServiceImpl
import com.aetherglass.data.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import com.aetherglass.domain.repository.SecureStorageRepository
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for API services and HTTP clients.
 *
 * Endpoint values are supplied through Android build configuration. The
 * Supabase key here must be the public anon/publishable key only.
 */
val apiModule = module {
    single<HttpClient>(named("agentClient")) {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                // Do not log bodies or headers, which may contain private data.
                level = io.ktor.client.plugins.logging.LogLevel.INFO
            }
            expectSuccess = true
        }
    }

    single<HttpClient>(named("supabaseClient")) {
        val secureStorage = get<SecureStorageRepository>()
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            defaultRequest {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                // Attach the user's access token only to PostgREST requests.
                // Auth endpoints and /auth/v1/user supply their own auth semantics.
                if (url.encodedPath.startsWith("/rest/v1/")) {
                    val accessToken = secureStorage.getString("access_token").getOrNull()
                    if (!accessToken.isNullOrBlank()) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }
            }
            install(Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.INFO
            }
            expectSuccess = true
        }
    }

    single<AgentApiService> {
        AgentApiServiceImpl(
            client = get(named("agentClient")),
            baseUrl = BuildConfig.AGENT_BASE_URL
        )
    }

    single<SupabaseApiService> {
        SupabaseApiServiceImpl(
            client = get(named("supabaseClient")),
            baseUrl = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )
    }
}
