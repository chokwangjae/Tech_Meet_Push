package matrix.push.client.usecase.repository

import matrix.commons.log.MatrixLog
import matrix.push.client.modules.MatrixPushModules
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.ApiExecutor
import matrix.push.client.modules.network.PushService
import matrix.push.client.modules.network.data.LogoutRequest

/**
 *   @author tarkarn
 *   @since 2025. 7. 31.
 *
 *   로그아웃 처리를 위한 구현체.
 */
internal class LogoutImpl(
    val deviceId: String,
    val appIdentifier: String,
    val pushService: PushService,
    val apiExecutor: ApiExecutor
) : LogoutRepository {

    companion object {
        private val TAG = LogoutRepository::class.simpleName ?: "LogoutRepository"
    }

    override suspend fun logout() {
        try {
            val response = apiExecutor.execute {
                val requestBody = LogoutRequest(
                    matrixPushId = MatrixPushModules.getMatrixPushId(),
                    appIdentifier = appIdentifier,
                    deviceId = deviceId,
                    platform = "AOS"
                )

                pushService.logout(requestBody)
            }

            if (response.isSuccessful) {
                MatrixLog.i(TAG, "logout completed.")
            } else {
                throw MpsException(
                    MpsError.LOGOUT_FAILED,
                    "Failed to logout with code: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to logout.", e)

            if (e is MpsException) {
                throw e
            } else {
                throw MpsException(MpsError.LOGOUT_FAILED, "Failed to logout", e)
            }
        }
    }
}