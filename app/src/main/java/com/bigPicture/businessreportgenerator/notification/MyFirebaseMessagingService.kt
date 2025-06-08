package com.bigPicture.businessreportgenerator.notification

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새 토큰 수신: $token")
        val prefs = applicationContext.getSharedPreferences("user_prefs", 0)
        prefs.edit { putString("fcm_token", token) }

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, "FCM 토큰이 새로 발급되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "제목 없음"
            val message = remoteMessage.data["message"] ?: "내용 없음"
            NotificationHelper.showNotification(
                applicationContext,
                System.currentTimeMillis().toInt(),
                title,
                message
            )
        }

        // Notification 메시지 처리 (포그라운드에서만 호출됨)
        remoteMessage.notification?.let {
            val title = it.title ?: "제목 없음"
            val message = remoteMessage.data["message"] ?: "내용 없음"
            NotificationHelper.showNotification(
                applicationContext,
                System.currentTimeMillis().toInt(),
                title,
                message
            )
        }
    }
}