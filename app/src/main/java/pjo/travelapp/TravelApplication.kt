package pjo.travelapp

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()



    }

    private fun setNaverSdk() {
        /** Naver Login Module Initialize */
        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)
    }

    private fun setKakaoSdk() {
        KakaoSdk.init(this, BuildConfig.kakao_native_api_key)
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