plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.aetherglass.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    // Koin
    implementation("io.insert-koin:koin-android:\${koinVersion}")
    implementation("io.insert-koin:koin-androidx-compose:\${koinVersion}")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-android:2.3.9")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
    implementation("io.ktor:ktor-client-logging:2.3.9")
    implementation("io.ktor:ktor-client-auth:2.3.9")
    implementation("io.ktor:ktor-client-auth-jwt:2.3.9")
    implementation("io.ktor:ktor-client-websockets:2.3.9")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:1.0.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.0.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.1.6")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.ktor:ktor-client-mock:2.3.9")
}