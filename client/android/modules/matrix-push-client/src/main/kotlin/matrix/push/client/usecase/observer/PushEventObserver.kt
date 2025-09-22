package matrix.push.client.usecase.observer

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import matrix.commons.log.MatrixLog
import matrix.push.client.data.PushMessageData
import matrix.push.client.usecase.OnNewMessageListener
import matrix.push.client.usecase.OnSyncMessageCompleteListener
import matrix.push.client.usecase.ProcessPushMessageUseCase
import matrix.push.client.usecase.repository.SseRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */

internal class PushEventObserver(
    private val sseRepository: SseRepository,
    private val processPushMessageUseCase: ProcessPushMessageUseCase,
    private val clientScope: CoroutineScope,
    private val sseScope: CoroutineScope,
    private val gson: Gson

) {

    companion object {
        private val TAG = PushEventObserver::class.simpleName ?: "PushEventObserver"
    }

    // 새로운 메시지를 전달할 리스너
    private var onNewMessageListener: OnNewMessageListener? = null

    fun setOnNewMessageListener(listener: OnNewMessageListener) {
        this.onNewMessageListener = listener
    }

    // 메시지 동기화 완료 전달 리스너
    private var onSyncMessageCompleteListener: OnSyncMessageCompleteListener? = null
    fun setOnSyncMessageCompleteListener(listener: OnSyncMessageCompleteListener) {
        this.onSyncMessageCompleteListener = listener
    }

    /**
     * SSE 스트림(String/JSON)을 구독.
     */
    fun startObserving() {
        sseScope.launch {
            sseRepository.events.collect { jsonData ->

                // SSE로부터 받은 JSON 문자열을 그대로 UseCase로 전달
                clientScope.launch {
                    processAndNotify(jsonData)
                }
            }
        }
    }

    /**
     * FCM 메시지(PushMessageData 객체)를 처리.
     */
    fun processFcmMessage(messageData: PushMessageData) {
        clientScope.launch {
            // 받은 PushMessageData 객체를 다시 JSON 문자열로 변환
            val jsonData = gson.toJson(messageData)

            // 변환된 JSON 문자열을 UseCase로 전달
            processAndNotify(jsonData)
        }
    }


    /**
     * ★ 공통 로직: JSON 문자열을 받아 UseCase를 실행하고 결과를 처리
     */
    private suspend fun processAndNotify(epcPayloadJson: String) {
        val newlyProcessedMessage = processPushMessageUseCase(epcPayloadJson)

        if (newlyProcessedMessage != null) {
            MatrixLog.i(TAG, "A new unique message has been processed: ${newlyProcessedMessage.pushDispatchId}")
            onNewMessageListener?.onNewMessage(newlyProcessedMessage)
        }
    }

    /**
     * 미수신 메시지 동기화 완료 시 콜백 처리.
     * @param count 동기화 된 메시지 카운트.
     */
    fun onSyncMessages(count: Int) {
        onSyncMessageCompleteListener?.onComplete(count)
    }
}