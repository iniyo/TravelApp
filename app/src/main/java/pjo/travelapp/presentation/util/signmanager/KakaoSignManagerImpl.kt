package pjo.travelapp.presentation.util.signmanager

import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

class KakaoSignManagerImpl @Inject constructor(): KakaoSignManager {

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.e(TAG, "로그인 성공 ${token.accessToken}")
        }
    }

    /**
     * 로그인 상태 확인
     * 현재 로그인 상태를 확인 후 콜백을 통해 결과를 전달합니다.
     */
    override fun isLoggedIn(callback: (Boolean) -> Unit) {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            callback(error == null && tokenInfo != null)
        }
    }

    override fun kakaoLogin(context: Context) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패 $error")
                    // 사용자가 취소
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = mCallback) // 카카오 이메일 로그인
                    }
                } else if (token != null) {
                    Log.d(TAG, "로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = mCallback) // 카카오 이메일 로그인
        }
    }
}

