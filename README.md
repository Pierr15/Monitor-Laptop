# Aether Glass

Aether Glass is a companion project for monitoring a Windows laptop from an Android device. It is organized as an Android client, a Windows-side agent, and a Supabase backend, with a dark glassmorphism interface and a floating bottom dock.

> **Project status:** Treat this repository as a development project until you have built and tested each component in your own environment. A successful Android build does not by itself mean remote monitoring, authentication, or laptop-control actions are fully configured. Do not expose an agent API to the public internet or use it on a device you do not own or administer.

## What it is for

The intended workflow is to use the Android app as the control panel and the Windows agent as the local endpoint that gathers laptop information and handles supported commands.

The Android navigation is designed around these areas:

- **Home**: overview and device connection/status information.
- **Performance**: system performance metrics.
- **Processes**: inspect running processes and, if the connected agent supports it, request process actions.
- **Files**: browse supported files or file operations exposed by the agent.
- **Control**: access supported laptop-control actions.

The screens and actions available in a particular build depend on backend configuration and agent connectivity. Verify each action before relying on it. Sensitive actions should be used only on your own device and should require confirmation where provided.

## How the components fit together

```text
Android phone
  └─ Aether Glass app (Kotlin / Jetpack Compose)
       ├─ Supabase: authentication and cloud-backed data
       └─ Authenticated connection to Windows agent
             └─ Windows laptop: telemetry and supported local commands
```

- **Android client** (`android/`): UI, navigation, app state, networking, and local secure storage.
- **Windows agent** (`windows-agent/`): Windows-side service/API for device telemetry and supported commands.
- **Supabase** (`supabase/`): database migrations, backend functions, authentication-related integration, and policies where implemented.
- **Scripts and documentation**: development helpers and project notes, if present in the checked-out revision.

Supabase and the Windows agent have separate responsibilities. Supabase does not automatically make a Windows laptop reachable, and installing the Android app alone does not install or start the Windows agent.

## Requirements

Install the tools relevant to the components you intend to run:

- **Android:** Android Studio with a compatible Android SDK, JDK version required by the Gradle project, and Android SDK Platform Tools (ADB).
- **Windows agent:** Windows and the .NET SDK/runtime version targeted by the agent project. The project is intended for .NET 8.
- **Backend:** Supabase project and Supabase CLI if you are running database migrations or deploying Edge Functions.
- **Network:** a trusted connection between the phone and laptop. If using Tailscale, install and sign in on both devices and confirm they can reach each other.
- **Git:** to clone the repository and retrieve updates.

Check the project Gradle files and agent project files for exact versions before installing additional SDKs.

## 1. Get the project

Clone the repository and enter its directory:

```bash
git clone https://github.com/Pierr15/Monitor-Laptop.git
cd Monitor-Laptop
```

Open the `android/` directory in Android Studio. Let Gradle sync complete, and resolve any SDK or JDK prompts shown by Android Studio before building.

## 2. Build and run the Android app

### Current repository limitations

The current tree is an Android-only prototype. It does **not** contain the documented `windows-agent/` or `supabase/` backend directories, so live laptop telemetry, agent commands, and cloud-backed authentication cannot work until those components are implemented and configured.

This repository currently does not include the complete Gradle Wrapper (the wrapper JAR and wrapper properties are missing). You can open `android/` in Android Studio, or install Gradle 8.6 locally and build with `gradle assembleDebug`. To generate and commit the Wrapper files, run the following from `android/` after installing Gradle:

```bash
gradle wrapper --gradle-version 8.6
```

The agent URL and Supabase configuration are build-time properties, not hard-coded production endpoints. Supply values in `android/gradle.properties` for local work or as Gradle project properties, for example:

```properties
AGENT_BASE_URL=https://<your-private-agent-host>:8443
SUPABASE_URL=https://<your-project-ref>.supabase.co
SUPABASE_ANON_KEY=<your-public-anon-or-publishable-key>
```

Do not place a Supabase service-role key, signing password, or private key in source control. A production agent needs trusted TLS and server-side authentication. The placeholder localhost endpoint only points to the Android device itself and is not a working remote laptop connection.


### With Android Studio

1. Open the repository's `android/` folder as a project.
2. Wait for Gradle sync to finish.
3. Select an Android emulator or connect a physical Android phone with USB debugging enabled.
4. Run the `app` configuration.
5. Grant only the Android permissions that the app requests and that you understand.

### From a terminal

The Gradle Wrapper is not committed in this repository yet. Until it is generated and committed, install Gradle 8.6 locally and run:

On Windows PowerShell:

```powershell
cd android
gradle assembleDebug
gradle test
```

On macOS/Linux:

```bash
cd android
gradle assembleDebug
gradle test
```

To generate the Wrapper from an installed Gradle 8.6 distribution, run this once from `android/`, then commit the generated `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` files:

```bash
gradle wrapper --gradle-version 8.6
```

The debug APK is normally generated under `android/app/build/outputs/apk/debug/`. The exact output depends on the Gradle configuration.

To install it on a connected Android device with ADB:

```bash
adb devices
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

Run the install command from the repository root. If Android Studio creates a differently named APK, use the actual generated filename. A debug APK is for development and testing, not a signed production release.

## 3. Configure and run the Windows agent

The Android app cannot collect live information from a laptop unless a compatible Windows-side endpoint is running and reachable.

1. Inspect `windows-agent/` and locate the agent's actual `.csproj` file.
2. Read the agent's configuration and launch settings before starting it. Set required values using local development configuration or environment variables.
3. From the directory containing the relevant project file, restore dependencies and build:

   ```powershell
   dotnet restore
   dotnet build
   ```

4. Run the project using the project path you found:

   ```powershell
   dotnet run --project <path-to-agent.csproj>
   ```

5. Read the startup logs to learn which address/port it binds to and whether initialization succeeded. Do not assume a port or URL that the project has not configured.
6. Configure the Android client to use that endpoint using the project's supported configuration mechanism.

If the agent is hosted on the laptop and the phone connects over Tailscale, use the laptop's reachable Tailscale address and the configured agent port. Confirm firewall rules permit the intended private-network traffic. Do not bind an unauthenticated control API to all network interfaces or expose it through router port forwarding.

The commands above are a general .NET workflow. If the repository revision does not contain an agent project or its configuration is incomplete, the agent cannot be started until those files and settings are supplied.

## 4. Configure Supabase (only if needed)

Supabase setup is required for features that depend on the cloud backend. You will need your own Supabase project and the project's expected configuration values.

1. Review the files under `supabase/` and the Android configuration to identify the exact project URL, public/anon key, migrations, and Edge Functions used by this revision.
2. Configure the Android app with the **public client key only** using the mechanism expected by the project. Never place a service-role key, database password, JWT signing secret, or other server credential in Android resources, source control, or an APK.
3. Install and authenticate the Supabase CLI if you intend to manage the backend from this repository.
4. Link the local project to your own Supabase project using the CLI's interactive setup, after checking the target project carefully.
5. Review every migration and function before applying or deploying it. Confirm Row Level Security (RLS) policies and access rules are appropriate for your data.

Common CLI commands, to be run from the directory containing the Supabase project configuration:

```bash
supabase login
supabase status
supabase db push
supabase functions list
```

These commands are not interchangeable: `supabase db push` applies local migrations to the linked remote project, while deploying functions requires an explicit deploy command for the relevant function(s). Only run database or deployment commands after reviewing their target and contents. Do not run `supabase db reset` against a remote project; reset is intended for local development and can destroy local data.

Do not assume a backend is configured simply because the `supabase/` directory exists. Check the actual migrations, function directories, and app configuration in the branch you have checked out.

## 5. Connect the phone and laptop

1. Start the Windows agent and note its configured endpoint from the logs/configuration.
2. Make sure the phone and laptop can reach each other over the intended private network. For Tailscale, both devices must be signed in to the same tailnet or otherwise permitted to communicate by its access rules.
3. Enter or select the endpoint in the app's setup/device screen if that option is implemented in your build.
4. Complete sign-in or device pairing using the project's implemented flow.
5. Confirm the app reports the device as connected and that its last-updated timestamp or telemetry changes.
6. Test read-only telemetry first. Only then test a non-destructive control action, if available.

If the app offers demo data or a demo mode, treat those values as sample data, not evidence that the laptop agent is connected.

## 6. Everyday usage

### Check laptop status

Open **Home** and check the displayed device status and freshness of the information. A stale timestamp, offline label, or missing telemetry usually means the app has not received a recent update. Check the agent process and network connection before troubleshooting the UI.

### Review performance

Open **Performance** to view the metrics exposed by the current build. Values depend on what the Windows agent collects and returns. Compare readings over time rather than assuming every displayed value is a real-time hardware sensor.

### Inspect processes

Open **Processes** to review the available process list. Before requesting a process-related action, verify the process name and PID carefully. Do not terminate Windows system processes or applications whose purpose you do not understand; doing so can cause data loss or destabilize Windows.

### Use file features

Open **Files** only after confirming the app is connected to the correct laptop. Review the selected path before any file operation. File access should be restricted to explicitly permitted locations; never attempt to bypass Windows permissions or access another person's files.

### Send control commands

Open **Control** and read the action description before proceeding. Use biometric/PIN confirmation if the app requests it. Save your work before shutdown, restart, or other disruptive actions. If a confirmation dialog does not appear where expected, cancel and investigate rather than trying to bypass the safeguard.

### Protect the connection

Use the app only with devices and accounts you own or are authorized to administer. Keep Android, Windows, .NET, and backend dependencies updated. Prefer private-network access, strong account authentication, TLS, and least-privilege permissions.

## 7. Troubleshooting

| Symptom | Checks |
|---|---|
| Gradle sync/build fails | Confirm Android Studio's JDK and SDK versions, network access for dependency downloads, and the first meaningful Gradle error. |
| APK will not install | Check Android version/ABI compatibility, available storage, and whether an older package signature conflicts with the debug build. |
| App opens but shows no device data | Confirm the Windows agent is running, the endpoint is correct, and the phone can reach it over the private network. |
| Connection times out | Check the agent's listening address/port, Windows Firewall, Tailscale connectivity/access rules, and whether TLS settings match the client. |
| Sign-in or cloud features fail | Check the configured Supabase URL/public key, authentication settings, network connectivity, RLS policies, and backend logs. Never troubleshoot by putting a service-role key in the app. |
| Control action fails | Check agent logs, device permissions, command support in this build, and whether the request was rejected by validation or confirmation requirements. |
| Data appears unchanged | Check whether the current screen is using demo/sample data, whether the agent is publishing updates, and the timestamp/state shown in the UI. |

For useful diagnostics, record the exact error message, the step that failed, the Android/Windows versions, and relevant redacted logs. Remove tokens, email addresses, private IP details where sensitive, and other credentials before sharing logs publicly.

## Project structure

```text
Monitor-Laptop/
├── android/
│   ├── app/             # Android application module
│   ├── core/            # Shared core utilities and design system
│   ├── data/            # API clients, DTOs, repositories, data sources
│   ├── domain/          # Domain models, use cases, repository contracts
│   └── presentation/    # Screens, ViewModels, navigation
├── windows-agent/       # Windows-side .NET agent, if included in this revision
├── supabase/            # Database migrations/functions, if included
├── scripts/             # Development helpers, if included
└── AGENTS.md            # Coding and security guidance for AI agents
```

The precise files available can vary by branch and commit. Inspect the checked-out tree rather than assuming every listed component is fully implemented.

## Technology overview

- **Android:** Kotlin, Jetpack Compose, Material 3, coroutines/Flow, ViewModels, and a custom glassmorphism design system.
- **Windows:** intended .NET 8 worker/API architecture with Windows system telemetry and authenticated requests.
- **Cloud:** Supabase Auth, PostgreSQL/RLS, Edge Functions, and Realtime where implemented.
- **Network:** authenticated HTTPS/WebSocket communication where configured; Tailscale can provide private network reachability.

## Security checklist

- Never commit credentials, device tokens, signing keys, private certificates, or real user data.
- Never embed Supabase service-role keys in Android code or client configuration.
- Keep the agent on a trusted network and require authentication for sensitive requests.
- Verify device ownership and validate all paths, process IDs, and commands on the Windows side.
- Keep confirmation steps for disruptive actions and review audit/error logs.
- Test with a non-critical device/account before relying on the system.

## License

The repository currently describes this project as **Proprietary - Personal use only**. Check the repository's license terms before redistributing or reusing the code.
