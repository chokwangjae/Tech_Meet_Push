package matrix.push.client.usecase

import matrix.commons.log.MatrixLog
import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.usecase.repository.NotificationRepository
import matrix.push.client.usecase.repository.PushMessageRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
internal class ProcessPushMessageUseCase(
    private val pushMessageRepository: PushMessageRepository,
    private val notificationRepository: NotificationRepository
) {

    companion object {
        private const val TAG = "ProcessPushMessageUseCase"
    }

    // UseCase의 실행을 동기화하기 위한 Mutex 객체
    private val mutex = Mutex()

    /**
     * 수신된 메시지를 처리하고, 새로 저장된 경우에만 해당 Entity를 반환한다.
     * @param messageData 파싱된 메시지 데이터 객체
     * @return 새로 저장되고 처리된 PushMessageEntity, 또는 중복/실패 시 null
     */
    suspend operator fun invoke(epcPayloadJson: String): PushMessageEntity? {

        // 다른 코루틴은 이 블록이 끝날 때까지 대기(suspend)한다.
        return mutex.withLock {

            // 1. 메시지 파싱 및 저장 시도
            // processAndSaveMessage는 중복 시 null을 반환
            val newEntity = pushMessageRepository.processAndSaveMessage(epcPayloadJson)
                ?: return@withLock null // ★ 중복이거나 저장 실패 시, 여기서 즉시 null을 반환하고 종료

            // 2. 메시지 수신 여부 서버로 전달.
            pushMessageRepository.reportMessageReceived(newEntity.pushDispatchId)

            // 3. 메시지 타입 확인
            if (newEntity.messageType.equals("NOTIFICATION", ignoreCase = true)) {

                // 4. 알림 타입이면, 알림 표시 요청
                notificationRepository.showNotification(newEntity)
            } else {

                // TODO: "SILENT" 푸시 등 다른 타입에 대한 처리 (이곳은 알림을 보여줄 필요 없음)
                MatrixLog.i(TAG, "Silent push received and saved: ${newEntity.pushDispatchId}")
            }

            // 5. 성공적으로 처리된 새 Entity를 반환
            return@withLock newEntity
        }
    }
}