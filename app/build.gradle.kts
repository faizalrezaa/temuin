import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val productionApiUrl = "https://remissly-crumbier-michelle.ngrok-free.dev/api/v1/"
val debugApiUrl = providers.gradleProperty("temuinDebugApiUrl").orElse(productionApiUrl).get()
val debugApiUri = URI(debugApiUrl)
require(debugApiUri.scheme in listOf("http", "https") && debugApiUri.host != null &&
    debugApiUri.userInfo == null && debugApiUri.query == null && debugApiUri.fragment == null &&
    debugApiUrl.endsWith("/api/v1/")
) { "temuinDebugApiUrl must be an HTTP(S) URL ending in /api/v1/" }

android {
    namespace = "com.example.temuin"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.temuin"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"$productionApiUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiUrl\"")
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // Tambahkan pustaka ikon extended untuk Jetpack Compose
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // AndroidX Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ViewModel KTX & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // MapLibre Android SDK (OpenStreetMap) - 16 KB Page Size Compatible
    implementation("org.maplibre.gl:android-sdk:11.8.8")

    // AndroidX Graphics Path (16 KB Page Size Compatible)
    implementation("androidx.graphics:graphics-path:1.1.0")
}