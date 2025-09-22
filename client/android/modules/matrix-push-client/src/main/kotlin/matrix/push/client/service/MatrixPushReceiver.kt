package matrix.push.client.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.RemoteMessage
import matrix.commons.log.MatrixLog
import matrix.commons.utils.toJson
import matrix.push.client.ScopeManager
import matrix.push.client.data.PushMessageData
import matrix.push.client.modules.MatrixPushModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 */
class MatrixPushReceiver : BroadcastReceiver() {

    companion object Companion {
        private const val TAG = "MatrixPushReceiver"
        const val ACTION_MATRIX_REMOTE_MESSAGE = "matrix.push.client.service.REMOTE_MESSAGE"
        const val EXTRA_REMOTE_MESSAGE = "matrix.push.client.extra.REMOTE_MESSAGE"

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        MatrixLog.d(TAG, "onReceive() | action: ${intent?.action ?: ""}")

        if (context == null || intent == null) {
            MatrixLog.e(TAG, "onReceive() called with null context or intent.")
            return
        }

        when (intent.action) {
            ACTION_MATRIX_REMOTE_MESSAGE -> {
                handleFcmMessage(context, intent)
            }
            else -> {
                MatrixLog.w(TAG, "Received unknown action: ${intent.action}")
            }
        }
    }

    private fun handleFcmMessage(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        ScopeManager.pushClientScope.launch {
            try {
                val remoteMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_REMOTE_MESSAGE, RemoteMessage::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_REMOTE_MESSAGE)
                }

                if (remoteMessage != null) {
                    processMessageInBackground(context.applicationContext, remoteMessage)
                } else {
                    MatrixLog.w(TAG, "RemoteMessage is null in Intent.")
                }
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Error handling message in background.", e)
            } finally {
                pendingResult.finish()
                MatrixLog.d(TAG, "Background work finished.")
            }
        }
    }

    /**
     * 앱 종료 상태에서 수신된 메시지를 처리하는 핵심 로직
     */
    private suspend fun processMessageInBackground(context: Context, remoteMessage: RemoteMessage) {
        try {
            val success = waitForSdkReady()
            if (!success) {
                MatrixLog.e(TAG, "SDK is not ready after waiting. Aborting message processing.")
                return
            }

            // SDK가 준비되었으므로, 이제 메시지 처리만 하면 됨
            val pushMessageData = PushMessageData.fromMap(remoteMessage.data)
            MatrixPushModules.getPushEventObserver().processFcmMessage(pushMessageData)
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to process message in background.", e)
        }
    }

    /**
     * SDK가 초기화되고, SSE 연결이 완료(matrixPushId 획득)될 때까지 대기하는 함수
     */
    private suspend fun waitForSdkReady(): Boolean {
        // 최대 10초간 대기
        repeat(10) {
            // isReady()는 inject 여부 확인
            if (MatrixPushModules.isReady()) {
                return true
            }
            delay(1000) // 1초 대기
        }
        return false
    }
}