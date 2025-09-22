package matrix.push.sample.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import matrix.push.sample.PushSampleApplication
import matrix.push.client.data.PushMessage
import matrix.push.sample.presentation.screen.setting.SettingsUiState
import matrix.push.sample.presentation.mvi.NavEffect
import org.json.JSONObject

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = MainViewModel::class.simpleName
    }

    private val pushClient = (application as PushSampleApplication).mpsClient

    private val _messages = MutableStateFlow<List<PushMessage>>(emptyList())
    val messages: StateFlow<List<PushMessage>> = _messages.asStateFlow()

    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState.asStateFlow()

    private val _navEffects = MutableSharedFlow<NavEffect>(replay = 1)
    val navEffects = _navEffects

    init {
        observeMessages()
        loadSettings()

        pushClient?.setOnNewMessageListener { message ->
            Log.d(TAG, "New message listener triggered: $message")
            // FCM 수신 시 알림만 생성하고 클릭 시에만 이동하도록 변경
            // 실제 네비게이션은 MainActivity의 onNewIntent에서 처리
        }

        pushClient?.setOnSyncMessageCompleteListener { count ->
            Log.d(TAG, "Sync complete listener triggered. Count: $count")
        }
    }

    /**
     * 푸시/딥링크 인텐트 파싱 후 네비게이션 이펙트 발행.
     * path 예: productDetail, notifications, home
     */
    fun handleDeeplink(path: String?, productId: String?) {
        Log.d(TAG, "handleDeeplink: path=$path, productId=$productId")
        viewModelScope.launch {
            when (path) {
                "productDetail" -> {
                    val id = productId?.takeIf { it.isNotBlank() }
                    if (id != null) {
                        Log.d(TAG, "handleDeeplink: emitting ToProductDetail($id)")
                        _navEffects.emit(NavEffect.ToProductDetail(id))
                    } else {
                        Log.d(TAG, "handleDeeplink: productId is blank, emitting None")
                        _navEffects.emit(NavEffect.None)
                    }
                }
                "notifications" -> {
                    Log.d(TAG, "handleDeeplink: emitting ToNotifications")
                    _navEffects.emit(NavEffect.ToNotifications)
                }
                "home" -> {
                    Log.d(TAG, "handleDeeplink: emitting ToHome")
                    _navEffects.emit(NavEffect.ToHome)
                }
                else -> {
                    Log.d(TAG, "handleDeeplink: unknown path, emitting None")
                    _navEffects.emit(NavEffect.None)
                }
            }
        }
    }

    /**
     * 푸시 메시지 payload 에 productId 가 담겨오면 상세 화면으로 진입한다.
     * payload 는 JSON String 으로 가정.
     */
    fun handlePushPayloadNavigate(payload: String?) {
        Log.d(TAG, "handlePushPayloadNavigate: payload=$payload")
        if (payload.isNullOrBlank()) {
            Log.d(TAG, "handlePushPayloadNavigate: payload is null or blank")
            return
        }
        viewModelScope.launch {
            runCatching {
                val normalized = payload.replace("'", "\"")
                Log.d(TAG, "handlePushPayloadNavigate: normalized=$normalized")
                val json = JSONObject(normalized)
                val productId = json.optString("productId")
                Log.d(TAG, "handlePushPayloadNavigate: extracted productId=$productId")
                if (productId.isNotBlank()) {
                    Log.d(TAG, "handlePushPayloadNavigate: emitting ToProductDetail($productId)")
                    _navEffects.emit(NavEffect.ToProductDetail(productId))
                } else {
                    Log.d(TAG, "handlePushPayloadNavigate: productId is blank")
                }
            }.onFailure { e ->
                Log.e(TAG, "handlePushPayloadNavigate: failed to parse payload", e)
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            val messagesFlow = pushClient?.observeAllMessages() ?: flowOf(emptyList())

            combine(messagesFlow, settingsUiState) { messageList, settings ->
                Log.d(TAG, "Observing messages, new list size: ${messageList.size}")

                val allowedCampaignIds: Set<String>? = settings.campaigns
                    .filter { it.consented }
                    .map { it.campaignId.toString() }
                    .toSet()
                    .takeIf { it.isNotEmpty() }

                val filtered = if (allowedCampaignIds == null) {
                    messageList
                } else {
                    messageList.filter { msg ->
                        val cid = msg.campaignId
                        cid == null || allowedCampaignIds.contains(cid)
                    }
                }

                filtered.sortedByDescending { it.receivedAt }
            }.collect { filteredSorted ->
                _messages.value = filteredSorted
            }
        }
    }

    /**
     * 서버로부터 현재 설정 상태(전체 동의, 캠페인 목록)를 가져와 UI를 초기화합니다.
     */
    fun loadSettings() {
        viewModelScope.launch {
            _settingsUiState.update { it.copy(isLoading = true) }
            try {
                // 가이드에 명시된 getCampaignList()를 호출하여 현재 상태를 '조회'합니다.
                val campaigns = pushClient?.getCampaignList()

                // 전체 동의 여부는 캠페인 리스트의 첫 항목에서 가져옵니다.
                // 만약 리스트가 비어있다면, 서버에 정보가 없는 초기 상태이므로 false를 사용합니다.
                val userConsent = campaigns?.firstOrNull()?.consented ?: false

                _settingsUiState.update {
                    it.copy(
                        isLoading = false,
                        campaigns = campaigns ?: emptyList(),
                        isUserConsented = userConsent
                    )
                }
                Log.d(TAG, "Settings loaded. consent=$userConsent, campaigns=${campaigns?.size ?: 0}")
            } catch (e: Exception) {
                _settingsUiState.update { it.copy(isLoading = false) }
                Log.e(TAG, "Failed to load settings", e)
            }
        }
    }

    /**
     * 사용자가 전체 동의 스위치를 조작했을 때 서버에 변경을 요청합니다.
     * 성공 시에만 UI를 업데이트합니다.
     */
    fun updateUserConsent(isConsented: Boolean) {
        viewModelScope.launch {
            // API 호출 전 현재 상태를 저장해 둡니다.
            val previousState = _settingsUiState.value

            try {
                // 서버에 '업데이트'를 요청합니다.
                val response = pushClient?.updateUserConsent(isConsented)

                // 성공 응답이 있을 경우에만 UI 상태를 업데이트합니다.
                if (response != null) {
                    _settingsUiState.update {
                        it.copy(
                            isUserConsented = response.consented,
                            campaigns = response.campaignDetails
                        )
                    }
                    Log.d(TAG, "User consent updated -> $isConsented, server: ${response.consented}")
                } else {
                    // 응답이 null이면 실패로 간주하고 이전 상태로 유지 (아무것도 하지 않음)
                    Log.w(TAG, "User consent update response is null")
                }
            } catch (e: Exception) {
                // 예외 발생 시에도 UI는 이전 상태로 유지됩니다. (아무것도 하지 않음)
                Log.e(TAG, "Failed to update user consent. UI remains previous.", e)
            }
        }
    }

    /**
     * 사용자가 캠페인별 동의 스위치를 조작했을 때 서버에 변경을 요청합니다.
     * updateCampaignConsent는 UserConsentResponse를 반환합니다.
     * 성공 시에만 UI를 업데이트합니다.
     */
    fun updateCampaignConsent(campaignId: Long, isConsented: Boolean) {
        viewModelScope.launch {
            // API 호출 전 현재 상태를 저장해 둡니다.
            val previousState = _settingsUiState.value

            try {
                // ### 여기가 핵심 수정 포인트 ###
                // updateCampaignConsent 호출 후 UserConsentResponse 타입의 응답을 받습니다.
                val response = pushClient?.updateCampaignConsent(campaignId, isConsented)

                // 성공 응답이 있을 경우에만 UI 상태를 업데이트합니다.
                if (response != null) {
                    // 응답에 포함된 전체 동의 여부와 전체 캠페인 목록으로 UI를 한 번에 업데이트합니다.
                    _settingsUiState.update {
                        it.copy(
                            isUserConsented = response.consented,
                            campaigns = response.campaignDetails
                        )
                    }
                    Log.d(TAG, "Campaign($campaignId) updated -> $isConsented, server consent=${response.consented}")
                } else {
                    // 응답이 null이면 실패로 간주하고 이전 상태로 유지 (아무것도 하지 않음)
                    Log.w(TAG, "Campaign($campaignId) update response is null")
                }

            } catch (e: Exception) {
                // 예외 발생 시에도 UI는 이전 상태로 유지됩니다. (아무것도 하지 않음)
                Log.e(TAG, "Failed to update campaign($campaignId). UI remains previous.", e)
            }
        }
    }

    fun getMessageById(pushDispatchId: String) {
        viewModelScope.launch {
            try {
                val message = pushClient?.getMessageById(pushDispatchId)
                Log.d(TAG, "get message :: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark message as confirmed", e)
            }
        }
    }


    fun markMessageAsConfirmed(pushDispatchId: String) {
        viewModelScope.launch {
            try {
                pushClient?.markMessageAsConfirmed(pushDispatchId)
                Log.d(TAG, "Message($pushDispatchId) marked as confirmed.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark message as confirmed", e)
            }
        }
    }


    fun syncMessages() {
        Log.d(TAG, "Requesting message sync...")
        pushClient?.syncMessages()
    }

    fun markAllMessagesAsConfirmed() {
        viewModelScope.launch {
            try {
                pushClient?.markAllMessagesAsConfirmed()
                Log.d(TAG, "All messages marked as confirmed.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark all messages as confirmed", e)
            }
        }
    }

    fun deleteMessage(pushDispatchId: String) {
        viewModelScope.launch {
            try {
                pushClient?.deleteMessage(pushDispatchId)
                Log.d(TAG, "Message($pushDispatchId) deleted.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete message", e)
            }
        }
    }

}