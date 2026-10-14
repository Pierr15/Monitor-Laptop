package com.aetherglass.data.api

import com.aetherglass.data.api.impl.AgentApiServiceImpl
import com.aetherglass.data.api.impl.SupabaseApiServiceImpl
import com.aetherglass.data.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
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
