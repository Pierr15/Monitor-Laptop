# AGENTS.md

## Agent Instructions for Aether Glass

This file provides guidance for AI agents working on the Aether Glass codebase.

## Project Overview

Aether Glass is a three-component system:
1. **Android App** - Kotlin/Jetpack Compose monitoring client
2. **Windows Agent** - C# .NET 8 Worker Service with HTTPS API
3. **Supabase Backend** - Auth, Database, Edge Functions, Realtime

## Code Style Guidelines

### Kotlin (Android)
- Use `kotlinx.coroutines` and `Flow` for async operations
- Prefer `sealed class` for state modeling (Loading, Success, Error, Empty, Offline, Stale)
- Keep business logic out of `@Composable` functions
- Use `ViewModel` with `StateFlow`/`MutableStateFlow` for UI state
- Follow Material 3 theming with custom `GlassTheme`
- Use `Navigation-Compose` with type-safe routes

### C# (Windows Agent)
- Target .NET 8
- Use `Microsoft.Extensions.Hosting` for Worker Service
- ASP.NET Core minimal APIs for HTTP endpoints
- `System.CommandLine` for CLI if needed
- Structured logging with `Serilog` or `Microsoft.Extensions.Logging`
- Nullable reference types enabled

### SQL (Supabase)
- Use `uuid_generate_v4()` for primary keys
- Enable RLS on all tables with user data
- Use `auth.uid()` for ownership checks
- Create indexes for time-series queries
- Use `jsonb` for flexible metadata

## Design System Rules

### Glassmorphism Components
All glass surfaces use:
- `Panel` background: `#202226` at ~85% opacity
- `Glass border`: `white` at 12-22% opacity
- Corner radius: 16dp (cards), 24dp (dock), 12dp (chips)
- Shadow: `0 4px 24px rgba(0,0,0,0.3)`, `0 1px 3px rgba(0,0,0,0.2)` inner highlight
- Blur: `RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)` where supported

### Color Tokens
| Token | Hex | Usage |
|-------|-----|-------|
| Obsidian | `#08090B` | Base background |
| Graphite | `#17181B` | Elevated surfaces |
| Panel | `#202226` | Glass surface base |
| Primary Text | `#F4F4F5` | Headlines, values |
| Secondary Text | `#A8A9AD` | Labels, descriptions |
| Muted Text | `#777A80` | Timestamps, hints |
| Success | `#22C55E` | Connected, healthy |
| Warning | `#F59E0B` | Degraded, caution |
| Danger | `#EF4444` | Critical, offline |

### Floating Dock
- 5 destinations: Home, Performance, Processes, Files, Control
- Capsule shape, 24dp corner radius
- Floats 16dp above bottom inset
- Selected item: scale 1.08, `Graphite` background, `Primary Text` icon
- Unselected: `Muted Text` icon
- Animation: `spring(dampingRatio = 0.8, stiffness = 400)`

## Security Requirements

### Never Commit
- Supabase service-role keys
- JWT signing secrets
- Certificate private keys
- Device tokens or credentials
- Real user data

### Always Validate
- Device ownership on every sensitive operation
- Path traversal in file operations (`Path.GetFullPath` + allowlist check)
- PID existence and process ownership before termination
- Command idempotency keys before execution
- Certificate pinning for agent API

## Testing Standards

### Android
- Unit test ViewModels with `Turbine` or `kotlinx-coroutines-test`
- UI test with `ComposeTestRule` and `createComposeRule`
- Mock API with `MockWebServer`
- Test all state variants: Loading, Success, Error, Empty, Offline, Stale

### Windows Agent
- Unit test telemetry collectors with mocked WMI/Performance Counters
- Integration test API endpoints with `WebApplicationFactory`
- Test authentication middleware
- Test command validation and audit logging

### Supabase
- Test RLS policies with multiple authenticated roles
- Test migration up/down
- Test Edge Function authorization

## Development Workflow

1. **Inspect** current state before changes
2. **Plan** minimal viable change
3. **Implement** in small, verifiable stages
4. **Test** with available tools
5. **Document** assumptions and limitations

## Current Phase

**Phase 1: Foundation and Working Android UI**
- Project initialization ✓
- Glassmorphism design system (in progress)
- Floating dock navigation
- All primary screens with demo data
- State handling (loading, empty, offline, error, stale)

## Key Files to Know

### Android
- `android/app/build.gradle.kts` - App module config
- `android/core/src/.../design/GlassTheme.kt` - Design tokens
- `android/core/src/.../design/components/GlassSurface.kt` - Base glass component
- `android/presentation/src/.../navigation/AppNavHost.kt` - Navigation graph
- `android/presentation/src/.../dock/GlassDock.kt` - Floating dock

### Windows Agent
- `windows-agent/src/AetherGlass.Agent/Program.cs` - Entry point
- `windows-agent/src/AetherGlass.Agent/Services/TelemetryCollector.cs` - Metrics collection
- `windows-agent/src/AetherGlass.Agent/Api/Endpoints.cs` - API endpoints

### Supabase
- `supabase/migrations/001_initial_schema.sql` - Core tables
- `supabase/migrations/002_rls_policies.sql` - Security policies
- `supabase/seed/development_seed.sql` - Test data

## Common Commands

```bash
# Android (when SDK available)
cd android && ./gradlew assembleDebug
cd android && ./gradlew test

# Windows Agent (when .NET SDK available)
cd windows-agent/src/AetherGlass.Agent && dotnet build
cd windows-agent/src/AetherGlass.Agent && dotnet test

# Supabase (when CLI available)
cd supabase && supabase db reset
cd supabase && supabase functions deploy
```

## Assumptions & Limitations

- Tailscale provides private network connectivity
- Android device and Windows laptop on same tailnet
- Supabase project exists with Auth enabled
- Windows agent runs with user permissions (not SYSTEM) where possible
- Biometric hardware available on Android device
- Demo mode uses synthetic data, clearly labeled

## Questions for Humans

- Supabase project URL and anon key (for local development)
- Tailscale auth key for device enrollment
- Certificate strategy for agent HTTPS (self-signed + trust, or ACME)
- Preferred push notification provider (FCM via Supabase Edge Function?)