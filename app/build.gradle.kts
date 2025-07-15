import java.io.FileInputStream
import java.util.Properties

// ① app 모듈 내부(.app 디렉터리)에 있는 env 파일 로드
val envFile = projectDir.resolve("env")
val envProps = Properties().apply {
    if (envFile.exists()) load(envFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.snswithai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.snswithai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val imgApiKey = localProperties.getProperty("IMG_API_KEY", "")
        buildConfigField("String", "IMG_API_KEY", "\"$imgApiKey\"")
        val elevenKey = localProperties.getProperty("ELEVEN_LABS_API_KEY", "")
        buildConfigField("string", "elevenKey", "\"$elevenKey\"")
    }
    buildFeatures{
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.firebase:firebase-ai")

    // HTTP & JSON
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Room
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)

    // Coil
    implementation("io.coil-kt:coil:2.4.0")

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
