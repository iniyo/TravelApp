// localProperties에서 값 읽어오기
import java.util.Properties
import java.io.FileInputStream
val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.dev.ksp)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.android.navigation.safrargs)
}

android {
    namespace = "pjo.travelapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "pjo.travelapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Manifest에 값을 전달
        manifestPlaceholders["kakaoApiKey"] = properties.getProperty("kakao_native_api_key")
        manifestPlaceholders["mapsApiKey"] = properties.getProperty("maps_api_key")
        manifestPlaceholders["metaClientToken"] = properties.getProperty("meta_client_token")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    /**
     * open source
     */
    // rolling textview
    implementation(libs.rollingtext)
    // lottie animation
    implementation(libs.lottie)
    // kakao login
    implementation(libs.v2.user)
    // naver login
    implementation(libs.oauth)
    /**
     * open source end
     */

    /**
     * jetpack, androidx, default
     */
    // glide
    implementation(libs.glide)

    // viewpager
    implementation(libs.androidx.viewpager2)

    // navigation bar
    implementation(libs.chip.navigation.bar)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.auth)
    ksp(libs.hilt.compiler)

    // viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // coroutine
    implementation(libs.kotlinx.coroutines.core)

    // retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // flexbox
    implementation(libs.flexbox)

    // firebase
    implementation(libs.firebase.ui.auth)
    implementation(libs.facebook.login)

    // splash screen
    implementation(libs.androidx.core.splashscreen)

    // default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    /**
     * jetpack, androidx, default end
     */
}