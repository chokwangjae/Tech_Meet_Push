package matrix.push.client.usecase.repository

import matrix.push.client.modules.database.PushMessageEntity

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
internal interface NotificationRepository {

    /**
     * PushMessageEntity 정보를 바탕으로 시스템 알림을 표시한다.
     * @param message 표시할 메시지 정보
     */
    suspend fun showNotification(message: PushMessageEntity)
}