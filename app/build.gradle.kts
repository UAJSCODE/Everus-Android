import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android.gradle.plugin)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val everusApiBaseUrl = providers.gradleProperty("everusApiBaseUrl")
    .orElse(providers.environmentVariable("EVERUS_API_BASE_URL"))
    .orElse("https://everus-server.onrender.com/")
    .get()
val everusDebugApiBaseUrl = providers.gradleProperty("everusDebugApiBaseUrl")
    .orElse(providers.environmentVariable("EVERUS_DEBUG_API_BASE_URL"))
    .orElse("https://everus-server.onrender.com/")
    .get()

android {
    namespace = "com.ujascode.everus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ujascode.everus"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "EVERUS_API_BASE_URL",
            "\"$everusApiBaseUrl\""
        )
    }

    signingConfigs {
        create("release") {
            val propsFile = file("${rootProject.projectDir}/everus-signing.properties")
            val props = Properties()
            if (propsFile.exists()) {
                propsFile.inputStream().use { props.load(it) }
            }

            val storeFilePath = props.getProperty("RELEASE_KEYSTORE_FILE") ?: System.getenv("RELEASE_KEYSTORE_FILE") ?: throw IllegalArgumentException("RELEASE_KEYSTORE_FILE is not set. Check everus-signing.properties or environment variable RELEASE_KEYSTORE_FILE.")
            val storePassword = props.getProperty("RELEASE_KEYSTORE_PASSWORD") ?: System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: throw IllegalArgumentException("RELEASE_KEYSTORE_PASSWORD is not set. Check everus-signing.properties or environment variable RELEASE_KEYSTORE_PASSWORD.")
            val keyAlias = props.getProperty("RELEASE_KEY_ALIAS") ?: System.getenv("RELEASE_KEY_ALIAS") ?: throw IllegalArgumentException("RELEASE_KEY_ALIAS is not set. Check everus-signing.properties or environment variable RELEASE_KEY_ALIAS.")
            val keyPassword = props.getProperty("RELEASE_KEY_PASSWORD") ?: System.getenv("RELEASE_KEY_PASSWORD") ?: throw IllegalArgumentException("RELEASE_KEY_PASSWORD is not set. Check everus-signing.properties or environment variable RELEASE_KEY_PASSWORD.")

            this.storeFile = file(storeFilePath)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "EVERUS_API_BASE_URL",
                "\"$everusDebugApiBaseUrl\""
            )
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
}
