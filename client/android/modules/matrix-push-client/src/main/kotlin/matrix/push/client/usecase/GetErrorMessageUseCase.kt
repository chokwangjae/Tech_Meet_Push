package matrix.push.client.usecase

import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author ykkim
 *   @since 2025. 8. 26.
 *
 *   'ERROR' 상태인 메시지 조회.
 */
internal class GetErrorMessageUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    suspend fun getErrorMessage(): List<PushMessageEntity> {
       return pushMessageRepository.getErrorMessages()
    }
}