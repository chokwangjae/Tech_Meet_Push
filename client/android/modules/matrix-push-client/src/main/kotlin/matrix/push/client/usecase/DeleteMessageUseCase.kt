package matrix.push.client.usecase

import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 29.
 *
 *   푸시 메시지 삭제 시 후 처리를 위한 UseCase.
 */
internal class DeleteMessageUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    suspend operator fun invoke(pushDisPatchId: String) {
        pushMessageRepository.reportMessageDeleted(pushDisPatchId)
    }
}