package matrix.push.client.usecase

import matrix.commons.log.MatrixLog
import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.modules.database.PushMessagesDao
import matrix.push.client.usecase.observer.PushEventObserver
import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 16.
 *
 *   미수신 메시지 처리를 위한 useCase.
 */
internal class SyncMessageUseCase(
    private val pushMessageRepository: PushMessageRepository,
    private val pushMessagesDao: PushMessagesDao,
    private val pushEventObserver: PushEventObserver
) {

    companion object {
        private const val TAG = "SyncMessageUseCase"
    }

    suspend operator fun invoke() {
        // 1. DB에서 가장 최근에 받은 메시지를 직접 조회
        val latestMessage = pushMessagesDao.getLatestMessage()

        // 2. 해당 메시지의 pushDispatchId를 사용. 없으면 초기값 "0" 사용. 공백이면 모든 데이터 요청.
        val lastKnownId = latestMessage?.pushDispatchId ?: ""

        MatrixLog.i(TAG, "Syncing messages after last known ID: $lastKnownId")

        // 3. 동기화 함수 호출
        val newMessagesCount = pushMessageRepository.syncMissedMessages(
            lastKnownPushDispatchId = lastKnownId,
            limit = MatrixPushClientOptions.syncMessagesLimit // 필요에 따라 limit 조절
        )

        if (newMessagesCount > 0) {
            MatrixLog.i(TAG, "Successfully synced $newMessagesCount new messages.")
        } else {
            MatrixLog.i(TAG, "No new messages to sync.")
        }

        pushEventObserver.onSyncMessages(newMessagesCount)
    }
}