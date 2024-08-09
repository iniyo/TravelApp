package pjo.travelapp.data.repo

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지 수신 시 호출됨
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 메시지에 데이터 페이로드가 있는 경우
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // 여기서 데이터 처리를 수행
        }

        // 메시지에 알림 페이로드가 있는 경우
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // 알림 표시를 처리
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}