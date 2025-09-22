package matrix.push.client.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import matrix.commons.log.MatrixLog
import matrix.push.client.ScopeManager
import matrix.push.client.modules.MatrixPushModules

/**
 * @author tarkarn
 * @since 2025.06.24
 *
 * Matrix Push SDK 내장 FCM 서비스 (SDK 내부용)
 *
 * 사용법:
 * 1. 앱의 AndroidManifest.xml에서 이 서비스를 등록
 * 2. MatrixPushClient.Builder로 설정 초기화
 * 3. 자동으로 FCM 메시지 처리
 */
internal class MatrixPushService : FirebaseMessagingService() {

    companion object Companion {
        private const val TAG = "MatrixPushService"
    }

    /**
     * FCM 토큰 갱신 시 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MatrixLog.i(TAG, "onNewToken() | new FCM token received")

        // 신규 발행 토큰 등록 로직
        ScopeManager.pushClientScope.launch {
            try {
                MatrixPushModules.getUpdateNewFcmTokenUseCase().invoke(token)
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Failed to update new FCM token on server.", e)
            }
        }
    }

    /**
     * FCM 메시지 처리 (data-only 메시지로 통일하여 모든 타입 내부 처리)
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        MatrixLog.i(TAG, "onMessageReceived() | From: ${remoteMessage.from}")
        MatrixLog.i(TAG, "onMessageReceived() | Data: ${remoteMessage.data}")

        try {
            // 1. BroadcastReceiver를 호출할 Intent 생성
            val intent = Intent(applicationContext, MatrixPushReceiver::class.java).apply {
                // Action은 필수가 아니지만, 구분을 위해 설정할 수 있습니다.
                action = MatrixPushReceiver.ACTION_MATRIX_REMOTE_MESSAGE

                // RemoteMessage 객체를 Intent에 담아 전달
                putExtra(MatrixPushReceiver.EXTRA_REMOTE_MESSAGE, remoteMessage)
            }

            // 2. Broadcast 전송
            applicationContext.sendBroadcast(intent)

            MatrixLog.i(TAG, "Broadcast sent to MatrixPushHandler. Service job is done.")
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to send broadcast to MatrixPushReceiver.", e)
        }
    }
}