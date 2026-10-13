# Aether Glass

A functional Android application for remotely monitoring and safely controlling a personal Windows laptop, with a complementary Windows agent and Supabase cloud backend.

## Architecture

```
aether-glass/
├── android/           # Android client (Kotlin + Jetpack Compose)
│   ├── app/           # Application entry point
│   ├── core/          # Security, networking, logging, design system
│   ├── data/          # API clients, repositories, DTOs, local settings
│   ├── domain/        # Models, use cases, repository interfaces
│   └── presentation/  # Screens, ViewModels, navigation
├── windows-agent/     # Windows agent (C# .NET Worker Service)
│   ├── src/           # Agent source code
│   └── tests/         # Unit and integration tests
├── supabase/          # Backend schema and functions
│   ├── migrations/    # Database migrations
│   ├── functions/     # Edge Functions
│   └── seed/          # Development seed data
├── scripts/           # Development helpers
└── docs/              # Documentation
```

## Technology Stack

### Android Client
- Kotlin + Jetpack Compose + Material 3
- Custom glassmorphism design system
- AndroidX Navigation Compose
- ViewModel + Coroutines/Flow
- Ktor Client for HTTPS/WebSocket
- Android BiometricPrompt + Keystore
- WorkManager for background tasks

### Windows Agent
- C# .NET 8 Worker Service
- ASP.NET Core for authenticated HTTPS API
- WebSocket for live telemetry
- Windows-native APIs (WMI, Performance Counters, etc.)

### Cloud (Supabase)
- Supabase Auth (Email/OAuth)
- PostgreSQL with Row Level Security
- Edge Functions for privileged operations
- Realtime for live updates (where appropriate)

## Quick Start

### Prerequisites
- Android Studio (latest stable)
- .NET 8 SDK
- Supabase CLI
- Tailscale on both devices

### Android App
```bash
cd android
./gradlew assembleDebug
```

### Windows Agent
```bash
cd windows-agent/src/AetherGlass.Agent
dotnet build
dotnet run
```

### Supabase
```bash
cd supabase
supabase db reset
supabase functions deploy
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Security Model](docs/SECURITY.md)
- [API Contract](docs/API.md)
- [Database Schema](docs/DATABASE.md)
- [UI Design System](docs/UI_DESIGN.md)
- [Android Setup](docs/SETUP_ANDROID.md)
- [Windows Agent Setup](docs/SETUP_WINDOWS_AGENT.md)
- [Supabase Setup](docs/SETUP_SUPABASE.md)
- [Testing Guide](docs/TESTING.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

## Security

- All API traffic over TLS
- Supabase service-role key never in client code
- Android Keystore for local secrets
- Biometric/PIN confirmation for critical actions
- Command idempotency and audit logging
- Path traversal prevention for file operations
- RLS policies on all user-facing tables

## License

Proprietary - Personal use only