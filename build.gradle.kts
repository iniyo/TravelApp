// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
    alias(libs.plugins.android.navigation.safrargs) apply false
    alias(libs.plugins.google.dev.ksp) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    kotlin("kapt") version "2.0.0"
}

