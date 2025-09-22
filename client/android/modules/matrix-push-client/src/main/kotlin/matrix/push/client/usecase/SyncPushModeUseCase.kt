package matrix.push.client.usecase

import matrix.commons.datastore.PreferenceKeys
import matrix.commons.log.MatrixLog
import matrix.push.client.PushMode
import matrix.push.client.modules.MatrixPushModules
import matrix.push.client.modules.network.ApiException
import matrix.push.client.usecase.repository.RegisterRepository

/**
 *   @author ykkim
 *   @since 2025. 8. 19.
 *
 *    서버 push mode 값 가져와서 내부에 저장하는 UseCase
 */
internal class SyncPushModeUseCase(
    private val registerRepository: RegisterRepository,
) {

    companion object {
        private const val TAG = "SyncPushModeUseCase"
    }

    suspend fun pushMode() {
        val response = registerRepository.register()
        val pushModeStr = response.body()?.pushMode

        val pushMode = pushModeStr?.uppercase()?.let { PushMode.valueOf(it) }
            ?: run {
                val code = response.code()
                val errorBody = response.errorBody()?.string()
                MatrixLog.e(TAG, "Register API failed for PushMode: code=$code, body=$errorBody")
                throw ApiException("Failed to register get PushMode", code, errorBody)
            }

        // Push 모드를 클라이언트 내부에 저장
        MatrixPushModules.getDataStore().setData(PreferenceKeys.Push.PUSH_MODE, pushMode.name)
    }
}