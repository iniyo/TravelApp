package pjo.travelapp

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import pjo.travelapp.presentation.ui.consts.EXIT_PROCESS
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

@HiltAndroidApp
class TravelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setKakaoSdk()
        setNaverSdk()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        /*Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            handleUncaughtException(throwable)
        }*/
    }
    /*private fun handleUncaughtException(throwable: Throwable) {
        // Crashlytics로 예외를 로그
        FirebaseCrashlytics.getInstance().recordException(throwable)

        // 앱을 종료
        exitProcess(EXIT_PROCESS)
    }
*/

    private fun setKakaoSdk() {
        KakaoSdk.init(this, BuildConfig.kakao_native_api_key)
    }

    private fun setNaverSdk() {
        /** Naver Login Module Initialize */
        val naverClientId = BuildConfig.naver_client_id
        val naverClientSecret = BuildConfig.naver_client_secret
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret, naverClientName)
    }

    private fun setFacebookSdk() {
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