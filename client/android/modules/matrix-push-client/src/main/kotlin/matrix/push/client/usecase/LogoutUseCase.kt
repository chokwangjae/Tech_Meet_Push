package matrix.push.client.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import matrix.push.client.usecase.repository.LogoutRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 31.
 *
 *   로그아웃 처리를 위한 UseCase
 */
class LogoutUseCase(
    val logoutRepository: LogoutRepository,
    val scope: CoroutineScope
) {

    operator fun invoke() {
        scope.launch {
            logoutRepository.logout()
        }
    }
}