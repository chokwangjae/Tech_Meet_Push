package matrix.push.client.usecase

import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author ykkim
 *   @since 2025. 8. 26.
 *
 *   pushDispatchId 에 맞는 개별 메시지 조회
 */
internal class GetMessagesByIdUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    suspend fun getMessageById(pushDispatchId: String): PushMessageEntity? {
        return pushMessageRepository.getMessageById(pushDispatchId = pushDispatchId)
    }
}