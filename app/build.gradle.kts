// localProperties에서 값 읽어오기
import java.util.Properties

val properties = Properties()
file("../local.properties").inputStream().use { properties.load(it) }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.android.navigation.safrargs)
    alias(libs.plugins.google.dev.ksp)
    alias(libs.plugins.firebase.crashlytics)
    id("kotlin-kapt")
}

android {
    namespace = "pjo.travelapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "pjo.travelapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Manifest에 값을 전달
        manifestPlaceholders["maps_api_key"] = properties.getProperty("maps_api_key")
        /*manifestPlaceholders["metaAppId"] = properties.getProperty("meta_app_id")*/
        manifestPlaceholders["kakaoApiKey"] = properties.getProperty("kakao_native_api_key")
        /*manifestPlaceholders["metaClientToken"] = properties.getProperty("meta_client_token")*/

        buildConfigField("String", "maps_api_url", properties.getProperty("maps_api_url"))
        buildConfigField("String", "place_base_url", properties.getProperty("place_base_url"))
        buildConfigField("String", "route_base_url", properties.getProperty("route_base_url"))
        buildConfigField("String", "skyscanner_base_url", properties.getProperty("skyscanner_base_url"))

        /*javaCompileOptions {
            annotationProcessorOptions {
                arguments.putAll(mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }*/
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
    implementation(libs.places)
    // flexbox
    implementation(libs.flexbox)
    // kizitonwose calendar
    implementation(libs.view)
    // google material
    implementation(libs.material)
    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    // okhttp loggin interceptor
    implementation(libs.logging.interceptor)
    // viewpager circle indicator
    implementation(libs.dotsindicator)
    // groupie
    implementation(libs.groupie)
    implementation(libs.groupie.viewbinding)
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
    implementation(libs.car.ui.lib)
    implementation(libs.firebase.crashlytics.ktx)
    kapt(libs.hilt.compiler)

    // paging
    implementation(libs.androidx.paging.runtime.ktx)

    // coroutine
    implementation(libs.kotlinx.coroutines.core)

    // navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // splash screen
    implementation(libs.androidx.core.splashscreen)

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

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
kapt {
    correctErrorTypes = true
}
