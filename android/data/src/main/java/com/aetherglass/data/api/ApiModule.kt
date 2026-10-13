package com.aetherglass.data.api

import com.aetherglass.data.api.impl.AgentApiServiceImpl
import com.aetherglass.data.api.impl.SupabaseApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.core.qualifier.named
import javax.inject.Singleton

/**
 * Koin module for API services and HTTP clients.
 */
val apiModule = module {
    // HTTP Client for Windows Agent API
    single<HttpClient> { named("agentClient") ->
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.BODY
            }
            expectSuccess = false
        }
    }

    // HTTP Client for Supabase API
    single<HttpClient> { named("supabaseClient") ->
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.BODY
            }
            expectSuccess = false
        }
    }

    // Agent API Service
    single<AgentApiService> {
        AgentApiServiceImpl(
            client = get(named("agentClient")),
            baseUrl = "https://localhost:8443" // TODO: Get from config/secure storage
        )
    }

    // Supabase API Service
    single<SupabaseApiService> {
        SupabaseApiServiceImpl(
            client = get(named("supabaseClient")),
            baseUrl = "https://your-project.supabase.co", // TODO: Get from config
            anonKey = "your-anon-key" // TODO: Get from config
        )
    }
}