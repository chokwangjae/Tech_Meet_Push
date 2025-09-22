package matrix.push.client.usecase

import matrix.push.client.usecase.repository.RegisterRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 *
 *   MatrixPushService 의 onNewToken 으로 토큰이 갱신 되는 경우에
 *   토큰을 서버에 업데이트 처리 한다.
 */
class UpdateNewFcmTokenUseCase(private val registerRepository: RegisterRepository) {

    suspend operator fun invoke(token: String) {
        registerRepository.updateTokenOnServerIfNeeded(token)
    }
}