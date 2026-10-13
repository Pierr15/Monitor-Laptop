plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.aetherglass.presentation"
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
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xopt-in=androidx.compose.runtime.ExperimentalComposeApi",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))

    // Koin
    implementation("io.insert-koin:koin-android:\${koinVersion}")
    implementation("io.insert-koin:koin-androidx-compose:\${koinVersion}")
    implementation("io.insert-koin:koin-androidx-viewmodel:\${koinVersion}")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Window insets
    implementation("androidx.window:window:1.3.0")
    implementation("androidx.window:window-layout:1.3.0")

    // Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha04")

    // Coil for images
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
}