package matrix.push.client.usecase

import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 *
 *   전체 메시지 조회.
 */
internal class GetAllMessagesUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    suspend operator fun invoke(): List<PushMessageEntity> {
        return pushMessageRepository.getAllMessages()
    }
}