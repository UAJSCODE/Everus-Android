plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android.gradle.plugin)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val everusApiBaseUrl = providers.gradleProperty("everusApiBaseUrl")
    .orElse(providers.environmentVariable("EVERUS_API_BASE_URL"))
    .orElse("https://api.everus.invalid/")
    .get()
val everusDebugApiBaseUrl = providers.gradleProperty("everusDebugApiBaseUrl")
    .orElse(providers.environmentVariable("EVERUS_DEBUG_API_BASE_URL"))
    .orElse("http://10.0.2.2:8080/")
require(everusApiBaseUrl.endsWith('/')) {
    "everusApiBaseUrl must end with '/'"
}
require(everusDebugApiBaseUrl.get().endsWith('/')) {
    "everusDebugApiBaseUrl must end with '/'"
}

android {
    namespace = "com.ujascode.everus"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ujascode.everus"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "EVERUS_API_BASE_URL",
            "\"${everusApiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "EVERUS_API_BASE_URL",
                "\"${everusDebugApiBaseUrl.get().replace("\\", "\\\\").replace("\"", "\\\"")}\""
            )
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.compose.ui:ui-graphics:1.6.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // New dependencies for Phase 1
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.graphics.path)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation(libs.gson)
    implementation(libs.retrofit.converter.gson)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
}
