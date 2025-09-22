package matrix.push.client.usecase

import kotlinx.coroutines.flow.Flow
import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 *
 *   실시간으로 전체 메시지를 감지하기 위한 useCase.
 */
internal class ObserveMessagesUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    operator fun invoke(): Flow<List<PushMessageEntity>> {
        return pushMessageRepository.observeAllMessages()
    }
}