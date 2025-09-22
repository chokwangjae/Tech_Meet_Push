package matrix.push.client.usecase

import matrix.commons.datastore.DataStoreRepository
import matrix.commons.datastore.PreferenceKeys
import matrix.commons.log.MatrixLog
import matrix.push.client.modules.network.ApiException
import matrix.push.client.modules.network.PushService
import matrix.push.client.modules.network.data.LoginRequest
import matrix.push.client.usecase.repository.RegisterRepository

/**
 *   @author tarkarn
 *   @since 2025. 7. 23.
 *
 *   사용자 정보 업데이트 및 인증 토큰 발급을 위한 UseCase. (Public Push)
 */
internal class LoginUseCase(
    private val registerRepository: RegisterRepository,
    private val pushService: PushService, // 직접 PushService를 사용하거나 별도 Repository를 만들어도 됨
    private val datastore: DataStoreRepository,
    private val deviceId: String,
    private val appIdentifier: String
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }

    suspend operator fun invoke(userId: String, userName: String, email: String): String {
        // 1. 재로그인을 위해 사용자 정보를 저장
        datastore.setData(PreferenceKeys.Push.Auth.USER_ID, userId)
        datastore.setData(PreferenceKeys.Push.Auth.USER_NAME, userName)
        datastore.setData(PreferenceKeys.Push.Auth.EMAIL, email)
        MatrixLog.i(TAG, "User info saved for re-login.")

        // 2. 실제 로그인 로직 실행
        return performLogin(userId, userName, email)
    }

    /**
     * 401 발생 시 저장된 정보로 재로그인을 수행하는 내부 함수
     */
    internal suspend fun reLogin(): String {
        MatrixLog.i(TAG, "Attempting to re-login due to 401 error.")
        val userId = datastore.getData(PreferenceKeys.Push.Auth.USER_ID, "")
        val userName = datastore.getData(PreferenceKeys.Push.Auth.USER_NAME, "")
        val email = datastore.getData(PreferenceKeys.Push.Auth.EMAIL, "")
        return performLogin(userId, userName, email)
    }

    private suspend fun performLogin(userId: String, userName: String, email: String): String {
        // 1. 기기 등록으로 rId 획득
        val registerResponse = registerRepository.register()
        val rId = registerResponse.body()?.rId
            ?: throw IllegalStateException("Failed to get rId from register API.")

        // 2. rId를 사용하여 로그인 요청
        val loginRequest = LoginRequest(
            userId = userId,
            userName = userName,
            email = email,
            deviceId = deviceId,
            appIdentifier = appIdentifier,
            platform = "AOS",
            rId = rId
        )
        val loginResponse = pushService.login(loginRequest)

        if (loginResponse.isSuccessful) {
            val matrixPushId = loginResponse.body()?.matrixPushId
            if (!matrixPushId.isNullOrEmpty()) {
                // 3. 성공 시 matrixPushId 저장
                datastore.setData(PreferenceKeys.Push.MATRIX_PUSH_ID, matrixPushId)
                MatrixLog.i(TAG, "Login successful. matrixPushId received and stored.")
                MatrixLog.d(TAG, matrixPushId)
                return matrixPushId
            }
        }

        // 로그인 실패 처리
        val code = loginResponse.code()
        val errorBody = loginResponse.errorBody()?.string()
        MatrixLog.e(TAG, "Login API failed: code=$code, body=$errorBody")
        throw ApiException("Login failed", code, errorBody)
    }
}