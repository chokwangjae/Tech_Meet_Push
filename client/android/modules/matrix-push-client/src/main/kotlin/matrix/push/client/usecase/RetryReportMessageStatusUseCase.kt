package matrix.push.client.usecase

import matrix.push.client.usecase.repository.PushMessageRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 31.
 *
 *   메시지 수신 상태 서버 전송 Retry
 *   수신, 삭제 처리 완료 후 서버에 상태 값 전달이 제대로 되지 않은 경우 재시도.
 */
internal class RetryReportMessageStatusUseCase(
    val pushMessageRepository: PushMessageRepository
) {

    suspend operator fun invoke() {
        val retryMessages = pushMessageRepository.retryReportMessageStatus()
        pushMessageRepository.reportMessages(retryMessages)
    }
}