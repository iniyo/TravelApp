package pjo.travelapp

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        setupGlobalExceptionHandler()
        setupGlobalLoggingHandler()
    }


    private fun setupGlobalExceptionHandler() {
        // 기존 UncaughtExceptionHandler 저장
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        // 새로운 UncaughtExceptionHandler 설정
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(throwable)
            // 기존 핸들러 호출 (앱 종료 등의 처리)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // CoroutineExceptionHandler 설정
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            handleUncaughtException(throwable)
        }

        // 전역적으로 사용될 CoroutineScope 설정
        GlobalScope.launch(coroutineExceptionHandler) { } // 핸들러만 설정함
    }

    private fun setupGlobalLoggingHandler() {
        // 모든 스레드에서 발생하는 예외를 잡기 위한 글로벌 핸들러 설정
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logNonFatalError("Thread ${thread.name}에서 처리되지 않은 예외 발생", throwable)
        }
    }

    private fun handleUncaughtException(throwable: Throwable) {
        // Crashlytics로 예외를 로그
        FirebaseCrashlytics.getInstance().recordException(throwable)

        // 앱을 종료
        exitProcess(1) // EXIT_PROCESS를 1로 정의했다고 가정
    }

    // 일반적인 에러 로그를 보내는 메서드
    private fun logNonFatalError(errorMessage: String, throwable: Throwable? = null) {
        FirebaseCrashlytics.getInstance().log(errorMessage)
        throwable?.let {
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }


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

    /*private fun setFacebookSdk() {
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
    }*/
}