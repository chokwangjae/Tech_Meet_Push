package matrix.push.client.usecase

import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 *
 *   메시지를 '읽음'(CONFIRMED) 상태로 변경하는 UseCase.
 *   pushDispatchId를 제공하면 개별 메시지를, 제공하지 않으면 모든 메시지를 처리
 */
internal class MarkMessageAsConfirmedUseCase(
    private val pushMessageRepository: PushMessageRepository
) {

    /**
     * @param pushDispatchId '읽음' 처리할 메시지의 ID.
     *                       null이거나 비어 있으면 모든 '읽지 않은' 메시지를 대상으로 합니다.
     */
    suspend operator fun invoke(pushDispatchId: String? = null) {
        if (!pushDispatchId.isNullOrBlank()) {
            // 시나리오 1: 개별 메시지 처리
            pushMessageRepository.markMessageAsConfirmed(pushDispatchId)
        } else {
            // 시나리오 2: 전체 메시지 처리
            pushMessageRepository.markAllMessagesAsConfirmed()
        }
    }

}