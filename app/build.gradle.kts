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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.snswithai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val elevenKey: String = envProps.getProperty("ELEVEN_LABS_API_KEY", "")
        buildConfigField("String", "ELEVEN_LABS_API_KEY", "\"$elevenKey\"")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-ai")
    // HTTP 요청용
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // JSON 직렬화/역직렬화용
    implementation("com.google.code.gson:gson:2.10.1")
    // Realtime Database KTX
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("io.coil-kt:coil:2.4.0")
}