plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.aetherglass.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aetherglass.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.lifecycle.ExperimentalLifecycleApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xopt-in=androidx.compose.runtime.ExperimentalComposeApi"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    packagingOptions {
        resources.excludes += listOf(
            "META-INF/*.kotlin_module",
            "META-INF/proguard/*"
        )
    }
}

dependencies {
    // Core Android
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":presentation"))

    // Activity & Fragment
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Material 3
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

    // Window insets
    implementation("androidx.window:window:1.3.0")
    implementation("androidx.window:window-layout:1.3.0")

    // Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha04")

    // Security
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
}

signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}