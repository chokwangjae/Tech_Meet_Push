package matrix.push.client.usecase

import matrix.push.client.MatrixPushClient
import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.modules.error.MpsException

/**
 *   @author tarkarn
 *   @since 2025. 7. 16.
 *
 *   콜백 리스너 람다 모음.
 */

// 이벤트를 전달하기 위한 콜백 인터페이스 또는 람다 정의
fun interface OnNewMessageListener {
    fun onNewMessage(message: PushMessageEntity)

}

// 미수신 메시지 동기화 완료 콜백.
fun interface OnSyncMessageCompleteListener {
    fun onComplete(messageCount: Int)
}

// 에러 콜백 리스너.
fun interface OnErrorListener {
    fun onError(mpsException: MpsException)
}

// 푸시 모듈 초기화 완료 콜백 리스너.
fun interface OnInitializedListener {
    fun onInitialized(client: MatrixPushClient)
}