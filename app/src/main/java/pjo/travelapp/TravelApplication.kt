package pjo.travelapp

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Facebook SDK 자동 초기화 활성화
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()

        // AppEventsLogger 활성화
        AppEventsLogger.activateApp(this)

        // 초기화 로그 확인
        if (FacebookSdk.isInitialized()) {
            Log.d("TravelApplication", "Facebook SDK initialized successfully.")
        } else {
            Log.d("TravelApplication", "Failed to initialize Facebook SDK.")
        }
    }
}