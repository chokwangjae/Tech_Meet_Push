package matrix.push.client.usecase

import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author ykkim
 *   @since 2025.8.26
 *
 *   메시지를 '읽지 않은'(RECEIVED) 상태로 변경하는 UseCase.
 *   pushDispatchId를 제공하면 개별 메시지를, 제공하지 않으면 모든 메시지를 처리
 */
internal class MarkMessageAsReceivedUseCase(
    private val pushMessageRepository: PushMessageRepository
) {
    /**
     * @param pushDispatchId '읽지 않은' 처리할 메시지의 ID.
     *                       모든 'ERROR' 상태인 메시지를 대상으로 합니다.
     */
    suspend operator fun invoke(pushDispatchId: String? = null) {
        if (!pushDispatchId.isNullOrBlank()) {
            // 시나리오 1: 개별 메시지 처리
            pushMessageRepository.markMessageAsReceived(pushDispatchId)
        } else {
            // 시나리오 2: 전체 메시지 처리
            pushMessageRepository.markAllMessagesAsReceived()
        }
    }

}