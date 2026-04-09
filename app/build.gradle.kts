import org.gradle.api.Project

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

fun Project.stringProp(name: String, default: String): String =
    providers.gradleProperty(name).orNull ?: default

fun Project.longProp(name: String, default: Long): Long =
    providers.gradleProperty(name).orNull?.toLongOrNull() ?: default

android {
    namespace = "com.somers.launcher"
    compileSdk = 34

    defaultConfig {
        val activationEndpoint = project.stringProp("somers.activationEndpoint", "https://activation.somers.local/api/v1/activate")
        val activationTimeoutMs = project.longProp("somers.activationTimeoutMs", 15000L)
        val targetAppPackage = project.stringProp("somers.targetAppPackage", "com.somers.target")
        val targetAppActivity = project.stringProp("somers.targetAppActivity", "")

        applicationId = "com.somers.launcher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "ACTIVATION_ENDPOINT", "\"$activationEndpoint\"")
        buildConfigField("long", "ACTIVATION_TIMEOUT_MS", "${activationTimeoutMs}L")
        buildConfigField("String", "TARGET_APP_PACKAGE", "\"$targetAppPackage\"")
        buildConfigField("String", "TARGET_APP_ACTIVITY", "\"$targetAppActivity\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
}
