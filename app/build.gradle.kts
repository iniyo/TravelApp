// localProperties에서 값 읽어오기
import java.util.Properties

val properties = Properties()
file("../local.properties").inputStream().use { properties.load(it) }


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.dev.ksp)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.android.navigation.safrargs)
    kotlin("kapt")
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
        manifestPlaceholders["maps_api_key"] = properties.getProperty("maps_api_key")
        /*manifestPlaceholders["metaAppId"] = properties.getProperty("meta_app_id")*/
        manifestPlaceholders["kakaoApiKey"] = properties.getProperty("kakao_native_api_key")
        /*manifestPlaceholders["metaClientToken"] = properties.getProperty("meta_client_token")*/

        javaCompileOptions {
            annotationProcessorOptions {
                arguments.putAll(mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }
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
    // navigation bar
    implementation(libs.chip.navigation.bar)
    // lottie animation
    implementation(libs.lottie)
    // kakao login
    implementation(libs.v2.user)
    // naver login
    implementation(libs.oauth)
    // glide
    implementation(libs.glide)
    // firebase
    implementation(libs.firebase.ui.auth)
    implementation(libs.facebook.login)
    implementation(libs.firebase.auth)
    // google maps service
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.android.maps.utils)
    // retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // flexbox
    implementation(libs.flexbox)
    // kizitonwose calendar
    implementation(libs.view)
    // google material
    implementation(libs.material)
    /**
     * open source end
     */

    /**
     * jetpack, androidx, default
     */
    // viewpager
    implementation(libs.androidx.viewpager2)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // coroutine
    implementation(libs.kotlinx.coroutines.core)

    // navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // splash screen
    implementation(libs.androidx.core.splashscreen)

    // slidingpanelayout
    implementation(libs.androidx.slidingpanelayout)

    // data binding
    kapt(libs.androidx.databinding.compiler)

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