package matrix.push.client.modules.network

import matrix.commons.datastore.DataStoreRepository
import matrix.commons.datastore.PreferenceKeys
import retrofit2.Response

/**
 *   @author tarkarn
 *   @since 2025. 7. 23.
 *
 *   API 호출 시 401 처리를 공통화 하기 위한 executor
 */
internal class ApiExecutor(
    private val reLoginUseCase: suspend () -> String,
    private val datastore: DataStoreRepository
) {
    // API 호출 결과를 나타내는 sealed class
    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val code: Int, val message: String?) : Result<Nothing>()
    }

    /**
     * matrixPushId를 필요로 하는 API 호출을 실행하고 401 에러 시 재시도 한다.
     * @param apiCall 실제 Retrofit API를 호출하는 suspend 람다. matrixPushId를 인자로 받는다.
     * @return 성공 시 API 응답 객체, 실패 시 Exception 발생
     */
    suspend fun <T> execute(apiCall: suspend (matrixPushId: String) -> Response<T>): Response<T> {
        val matrixPushId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
        if (matrixPushId.isEmpty()) {
            throw IllegalStateException("matrixPushId is not available. Please login first.")
        }

        var response = apiCall(matrixPushId)

        if (response.code() == 401) {
            // 401 에러 발생: 재로그인 시도
            val newMatrixPushId = reLoginUseCase()
            if (newMatrixPushId.isEmpty()) {
                throw ApiException("Failed to re-authenticate.", 401)
            }

            // 새로운 matrixPushId로 API 재호출
            response = apiCall(newMatrixPushId)
        }

        return response
    }

}
class ApiException(message: String, val code: Int = -1, val errorBody: String? = null) : Exception(message)