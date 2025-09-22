package matrix.push.client.usecase.repository

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import matrix.commons.log.MatrixLog
import matrix.commons.utils.fromJson
import matrix.push.client.PushMode
import matrix.push.client.modules.MatrixPushModules
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.NetworkModules
import matrix.push.client.modules.network.data.ErrorResponse
import matrix.push.client.modules.network.data.RegisterRequest
import matrix.push.client.modules.network.data.RegisterResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 *
 *   토큰 등록 관련 단위 기능 구현체.
 */
class RegisterImpl(val deviceId: String, val appIdentifier: String) : RegisterRepository {


    companion object Companion {
        private const val TAG = "RegisterImpl"
    }

    // Mutex 인스턴스 생성: 토큰 업데이트 로직을 보호할 자물쇠 역할
    private val updateMutex = Mutex()

    // 마지막 '성공' 상태를 캐시할 변수들
    @Volatile private var lastSuccessfulToken: String? = null
    @Volatile private var lastSuccessfulResponse: RegisterResponse? = null
    /**
     * 기기 등록 및 푸시 토큰 업데이트. (일반적으로 사용)
     * FCM 토큰을 비동기적으로 가져온 후, 서버에 등록을 요청한다.
     */
    override suspend fun register(): Response<RegisterResponse> {
        val pushMode = MatrixPushModules.getPushMode()

        val fcmToken = when(pushMode) {
            PushMode.PUBLIC -> getFcmToken()
            PushMode.PRIVATE -> ""
            else -> ""
        }

        return updateTokenOnServerIfNeeded(fcmToken)
    }

    /**
     * Mutex를 사용하여 토큰 등록/업데이트를 동기화하고, 결과를 캐시하여 중복 API 호출을 방지
     */
    override suspend fun updateTokenOnServerIfNeeded(newToken: String): Response<RegisterResponse> {
        return updateMutex.withLock {
            try {
                // 캐시된 정보와 현재 토큰을 비교하여 작업이 이미 성공적으로 완료되었는지 확인
                if (newToken == lastSuccessfulToken && lastSuccessfulResponse != null) {
                    MatrixLog.i(TAG, "Token is already registered. Returning cached successful response.")
                    // 캐시된 유효한 성공 응답을 반환.
                    return@withLock Response.success(lastSuccessfulResponse!!)
                }

                // 캐시에 없거나 토큰이 다른 경우, 네트워크 요청을 진행
                MatrixLog.i(TAG, "Proceeding with server update for token: $newToken")

                val body = RegisterRequest(
                    appIdentifier = appIdentifier,
                    deviceId = deviceId,
                    platform = "AOS",
                    pushToken = newToken
                )

                val response = safeNetworkCall {
                    NetworkModules.pushService.register(body)
                }

                // 요청이 성공하고 body가 null이 아닐 때만 결과를 캐시.
                if (response.isSuccessful && response.body() != null) {
                    MatrixLog.i(TAG, "Server update successful. Caching response for future calls.")
                    lastSuccessfulToken = newToken
                    lastSuccessfulResponse = response.body()
                    response
                } else {

                    val statusCode = response.code()
                    val errorResponse = response.errorBody()?.string().fromJson<ErrorResponse>()

                    when (statusCode) {
                        HttpURLConnection.HTTP_BAD_REQUEST -> {

                            when (errorResponse.code) {
                                // 서버에 등록되지 않은 APP_ID.
                                "AP0006",
                                    // 서버에 없는 권한으로 사용자 등록 오류. (ROLE_ID 확인)
                                "OA0001"-> {
                                    throw MpsException(
                                        error =MpsError.NETWORK_ERROR,
                                        details ="Failed to register token. (${errorResponse.message} - ${errorResponse.code})"
                                    )
                                }
                            }
                        }

                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                            throw MpsException(
                                error = MpsError.NETWORK_ERROR,
                                details ="Failed to register token. (${errorResponse.message} - ${errorResponse.code})"
                            )
                        }
                    }

                    throw MpsException(MpsError.NETWORK_ERROR, "Failed to register token, server responded with code: ${response.code()}")
                }
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Failed to update token on server", e)
                throw e as? MpsException
                    ?: MpsException(MpsError.NETWORK_ERROR, "Failed to update token on server", e)
            }
        }
    }

    /**
     * Firebase의 콜백 기반 API를 suspend 함수로 변환하는 로직
     */
    private suspend fun getFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (continuation.isActive) {
                if (task.isSuccessful) {
                    val token = task.result
                    MatrixLog.i(TAG, "Fetched FCM token: $token")
                    continuation.resume(token) // 성공 시 토큰으로 코루틴 재개
                } else {
                    val e = task.exception ?: MpsException(MpsError.REGISTER_FAILED,"Unknown error fetching FCM token")
                    MatrixLog.e(TAG, "Fetching FCM registration token failed", e)
                    continuation.resumeWithException(MpsException(MpsError.REGISTER_FAILED, "Fetching FCM registration token failed", e)) // 실패 시 예외로 코루틴 재개
                }
            }
        }
    }


    /**
     * 안전한 네트워크 호출을 위한 래퍼 함수
     * 네트워크 오류 발생 시 예외를 catch하고 적절한 응답을 반환
     */
    private suspend inline fun <T> safeNetworkCall(
        crossinline call: suspend () -> Response<T>
    ): Response<T> {
        return try {
            call()
        } catch (ce: ConnectException) {
            MatrixLog.e(TAG, "Network connection failed: ${ce.message}", ce)

            // 빈 응답 반환 (서버 연결 실패)
            Response.error(503,
                "Service Unavailable: Cannot connect to server".toResponseBody(null)
            )
        } catch (ste: SocketTimeoutException) {
            MatrixLog.e(TAG, "Network timeout: ${ste.message}", ste)

            // 타임아웃 응답 반환
            Response.error(408, "Request Timeout".toResponseBody(null))
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Unexpected network error: ${e.message}", e)

            // 일반적인 서버 오류 응답 반환
            Response.error(500, "Internal Server Error".toResponseBody(null))
        }
    }
}