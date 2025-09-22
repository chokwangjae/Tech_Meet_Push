package matrix.push.client.usecase.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import matrix.commons.datastore.DataStoreRepository
import matrix.commons.datastore.PreferenceKeys
import matrix.commons.log.MatrixLog
import matrix.push.client.data.PushMessageData
import matrix.push.client.modules.database.ClientStatus
import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.modules.database.PushMessagesDao
import matrix.push.client.modules.error.MpsError
import matrix.push.client.modules.error.MpsException
import matrix.push.client.modules.network.ApiExecutor
import matrix.push.client.modules.network.PushService
import matrix.push.client.modules.network.data.MessageStatusRequest
import matrix.push.client.modules.network.data.MessageSyncRequest
import matrix.push.client.modules.network.data.MessageSyncResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 *
 *   전달 받은 푸시 메시지를 JSON 파싱 및 DB 저장을 담당
 */
internal class PushMessageImpl(
    private val deviceId: String,
    private val appIdentifier: String,
    private val pushMessageDao: PushMessagesDao,
    private val apiExecutor: ApiExecutor,
    private val pushService: PushService,
    private val datastore: DataStoreRepository,
    private val gson: Gson
) : PushMessageRepository {

    companion object {
        private const val TAG = "PushMessageImpl"
    }

    /**
     * JSON 데이터를 파싱하고 DB에 저장한 후, 저장된 Entity를 반환.
     * 중복된 pushDispatchId가 있으면 저장을 무시하고 null을 반환.
     */
    override suspend fun processAndSaveMessage(jsonData: String): PushMessageEntity? {
        try {
            // 1. JSON을 DTO로 파싱
            val data = gson.fromJson(jsonData, PushMessageData::class.java)

            // 2. DTO를 Room Entity로 변환
            val entity = mapToEntity(data)

            // 3. DB에 저장 시도 (OnConflictStrategy.IGNORE 덕분에 중복이면 삽입 안됨)
            // insert는 삽입된 row의 id를 반환. 실패(무시) 시 -1L 반환.
            val insertedId = pushMessageDao.insertMessage(entity)
            if (insertedId == -1L) {
                MatrixLog.w(TAG, "Duplicate message ignored: ${entity.pushDispatchId}")
                return null // 중복이면 null 반환
            } else {
                return entity.copy(id = insertedId) // Auto-generate된 id를 포함하여 반환
            }
        } catch (e: JsonSyntaxException) {
            MatrixLog.e(TAG, "Failed to parse push message JSON", e)
            throw MpsException(MpsError.UNKNOWN, "Failed to parse push message JSON", e)
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to process and save message", e)
            throw MpsException(MpsError.UNKNOWN, "Failed to save message to DB", e)
        }
    }

    /**
     * 서버에 메시지 수신 상태를 전송한다.
     */
    override suspend fun reportMessageReceived(pushDispatchId: String) {
        reportMessageToServer(pushDispatchId, ClientStatus.RECEIVED)
    }

    /**
     * 서버에 메시지 삭제 상태를 전송한다.
     */
    override suspend fun reportMessageDeleted(pushDispatchId: String) {
        reportMessageToServer(pushDispatchId, ClientStatus.DELETED)
    }

    /**
     * 서버에 메시지 상태를 한번에 전송한다.
     */
    override suspend fun reportMessages(pushMessageEntityList: List<PushMessageEntity>) {
        // 빈 리스트인 경우 처리할 것이 없음
        if (pushMessageEntityList.isEmpty()) {
            MatrixLog.i(TAG, "No messages to report.")
            return
        }

        // 1. matrixPushId 가져오기
        val matrixPushId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
        if (matrixPushId.isEmpty()) {
            MatrixLog.w(TAG, "matrixPushId is not available. Cannot report message status.")
            return
        }

        // 2. 요청 데이터 생성
        val updateData = pushMessageEntityList.map { entity ->
            MessageStatusRequest.UpdateDataItem(
                pushDispatchId = entity.pushDispatchId,
                status = entity.clientStatus
            )
        }

        val requestBody = MessageStatusRequest(
            matrixPushId = matrixPushId,
            updateData = updateData
        )

        // 3. API 호출
        try {
            val response = apiExecutor.execute {
                pushService.messageStatus(requestBody)
            }

            if (response.isSuccessful) {
                MatrixLog.i(TAG, "Successfully reported ${pushMessageEntityList.size} messages.")

                // 4. 성공한 메시지들에 대해 DB 업데이트 (한번에 처리)
                try {
                    val currentTime = LocalDateTime.now()
                    pushMessageEntityList.forEach { entity ->
                        pushMessageDao.updateSseClientSent(
                            pushDispatchId = entity.pushDispatchId,
                            clientStatus = entity.clientStatus.name,
                            sendToServer = "true",
                            updatedAt = currentTime
                        )
                    }
                    MatrixLog.i(TAG, "DB updated: send_to_server is now true for ${pushMessageEntityList.size} messages")

                } catch (dbException: Exception) {
                    MatrixLog.e(TAG, "Failed to update DB after successful API call for ${pushMessageEntityList.size} messages", dbException)
                    // API는 성공했으므로 상위로 예외를 던지지 않고 로그만 남긴다
                }

            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                MatrixLog.e(TAG, "Failed to report message status. Code: ${response.code()}, Message: $errorMessage")

                // 실패한 메시지들에 대해 에러 상태 업데이트
                val failureMessage = "Failed to report message status: ${response.code()}"
                pushMessageEntityList.forEach { entity ->
                    try {
                        pushMessageDao.updateError(entity.pushDispatchId, failureMessage)
                    } catch (dbException: Exception) {
                        MatrixLog.e(TAG, "Failed to update error status for ${entity.pushDispatchId}", dbException)
                    }
                }

                throw MpsException(MpsError.NETWORK_ERROR, failureMessage)
            }
        } catch (e: Exception) {
            // 네트워크 오류 등으로 실패한 경우 모든 메시지에 에러 상태 업데이트
            val errorMessage = e.message ?: "Unknown error during message reporting"
            pushMessageEntityList.forEach { entity ->
                try {
                    pushMessageDao.updateError(entity.pushDispatchId, errorMessage)
                } catch (dbException: Exception) {
                    MatrixLog.e(TAG, "Failed to update error status for ${entity.pushDispatchId}", dbException)
                }
            }

            MatrixLog.e(TAG, "Error reporting message status for ${pushMessageEntityList.size} messages", e)
            throw e as? MpsException ?: MpsException(MpsError.NETWORK_ERROR, "Error reporting message status for multiple messages", e)
        }
    }

    /**
     * 서버에 클라이언트의 메시지 상태를 전달한다.
     */
    private suspend fun reportMessageToServer(pushDispatchId: String, clientStatus: ClientStatus) {
        // 1. matrixPushId 가져오기.
        val matrixPushId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")
        if (matrixPushId.isEmpty()) {
            MatrixLog.w(TAG, "matrixPushId is not available. Cannot report message status.")
            return
        }

        // 3. API 호출
        try {
            val response = apiExecutor.execute {

                // 2. 요청 데이터 생성
                val updateData = mutableListOf<MessageStatusRequest.UpdateDataItem>()
                val updateDataItem = MessageStatusRequest.UpdateDataItem(
                    pushDispatchId = pushDispatchId,
                    status = clientStatus
                )
                updateData.add(updateDataItem)

                val requestBody = MessageStatusRequest(
                    matrixPushId = matrixPushId,
                    updateData = updateData
                )

                pushService.messageStatus(requestBody)
            }

            if (response.isSuccessful) {
                MatrixLog.i(TAG, "Successfully reported message status for $pushDispatchId")

                // DAO 를 사용하여 send_to_server 값을 "true"로 업데이트하고, updatedAt을 현재 시각으로 갱신
                try {
                    pushMessageDao.updateSseClientSent(
                        pushDispatchId = pushDispatchId,
                        clientStatus = clientStatus.name,
                        sendToServer = "true",
                        updatedAt = LocalDateTime.now() // 현재 시각 명시적 전달
                    )
                    MatrixLog.i(TAG, "DB updated: send_to_server is now true for $pushDispatchId")

                } catch (dbException: Exception) {
                    MatrixLog.e(TAG, "Failed to update DB after successful API call for $pushDispatchId", dbException)
                    // API는 성공했으므로 상위로 예외를 던지지 않고 로그만 남긴다.
                }

            } else {
                MatrixLog.e(TAG, "Failed to report message status. Code: ${response.code()}, Message: ${response.errorBody()?.string()}")
                throw MpsException(MpsError.NETWORK_ERROR, "Failed to report message status: ${response.code()}, pushDispatchId: $pushDispatchId")
            }
        } catch (e: Exception) {
            pushMessageDao.updateError(pushDispatchId, e.message.toString())
            MatrixLog.e(TAG, "Error reporting message status for $pushDispatchId", e)
            throw e as? MpsException ?: MpsException(MpsError.NETWORK_ERROR, "Error reporting message status for $pushDispatchId", e)
        }
    }


    /**
     * 서버와 미수신 메시지를 동기화한다.
     */
    override suspend fun syncMissedMessages(lastKnownPushDispatchId: String, limit: Int): Int {

        // 1. 요청에 필요한 데이터 가져오기
        val matrixPushId = datastore.getData(PreferenceKeys.Push.MATRIX_PUSH_ID, "")

        if (matrixPushId.isEmpty() || deviceId.isEmpty() || appIdentifier.isEmpty()) {
            MatrixLog.w(TAG, "Cannot sync messages. Required info (matrixPushId, deviceId, appIdentifier) is missing.")
            throw MpsException(MpsError.INITIALIZATION_FAILED, "Cannot sync messages. Required info is missing.")
        }

        try {
            val response = apiExecutor.execute {
                // 2. API 요청 본문 생성
                val requestBody = MessageSyncRequest(
                    matrixPushId = matrixPushId,
                    details = MessageSyncRequest.MessageSyncDetails(
                        deviceId = deviceId,
                        appIdentifier = appIdentifier,
                        pushDispatchId = lastKnownPushDispatchId,
                        limit = limit.toString()
                    )
                )

                // 3. API 호출
                pushService.messageSync(requestBody)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody.isNullOrEmpty()) {
                    MatrixLog.i(TAG, "Message sync response is empty. No new messages.")
                    return 0
                }

                // 4. 응답 파싱 및 DB 저장
                return processSyncResponse(responseBody)

            } else {
                MatrixLog.e(TAG, "Message sync failed. Code: ${response.code()}, Message: ${response.message()}")
                throw MpsException(MpsError.SYNC_MESSAGES_FAILED, "Message sync failed with code: ${response.code()}")
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Error during message sync", e)
            throw if (e is MpsException) e else MpsException(MpsError.SYNC_MESSAGES_FAILED, "Error during message sync", e)
        }
    }

    /**
     * 동기화 응답을 처리하고 DB에 저장하는 내부 함수
     * @param jsonResponse API로부터 받은 JSON 배열 문자열
     * @return 새로 저장된 메시지의 수
     */
    private suspend fun processSyncResponse(jsonResponse: String): Int {
        try {
            // JSON 배열을 DTO 리스트로 파싱
            val messageList = gson.fromJson<List<MessageSyncResponse>>(
                jsonResponse,
                object : TypeToken<List<MessageSyncResponse>>() {}.type
            )

            if (messageList.isEmpty()) {
                return 0
            }

            // DTO 리스트를 Entity 리스트로 변환
            val entities = messageList.mapNotNull { mapSyncItemToEntity(it) }

            // DB에 한 번에 저장 (중복은 무시됨)
            pushMessageDao.insertMessages(entities)

            MatrixLog.i(TAG, "Synced and saved ${entities.size} new messages.")
            return entities.size

        } catch (e: JsonSyntaxException) {
            MatrixLog.e(TAG, "Failed to parse sync response JSON", e)
            throw MpsException(MpsError.SYNC_MESSAGES_FAILED, "Failed to parse sync response JSON", e)
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to process sync response", e)
            throw MpsException(MpsError.SYNC_MESSAGES_FAILED, "Failed to process sync response", e)
        }
    }

    /**
     * MessageSyncResponseItem DTO를 PushMessageEntity로 변환하는 매퍼 함수
     */
    private fun mapSyncItemToEntity(item: MessageSyncResponse): PushMessageEntity? {
        return try {
            PushMessageEntity(
                pushDispatchId = item.pushDispatchId,
                messageType = item.messageType ?: "",
                messagePriority = item.messagePriority,
                title = item.title,
                body = item.body,
                imageUrl = item.imageUrl,
                payload = item.payload,
                sender = item.sender,
                channelId = item.channelId,
                channelName = item.channelName,
                channelDescription = item.channelDescription,
                // timestamp(String)를 LocalDateTime으로 변환
                createdAt = parseTimestamp(item.timestamp)
            )
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to map sync item to entity for ${item.pushDispatchId}", e)
            null
        }
    }

    /**
     * "yyyyMMddHHmmss" 형식의 문자열을 LocalDateTime으로 파싱
     */
    private fun parseTimestamp(timestamp: String?): LocalDateTime {
        if (timestamp.isNullOrBlank()) {
            return LocalDateTime.now() // 타임스탬프가 없으면 현재 시각으로 대체
        }
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            LocalDateTime.parse(timestamp, formatter)
        } catch (e: Exception) {
            MatrixLog.w(TAG, "Could not parse timestamp '$timestamp'. Using current time instead.")
            LocalDateTime.now() // 파싱 실패 시 현재 시각으로 대체
        }
    }

    /**
     * DTO를 Entity로 변환하는 매퍼 함수
     */
    private fun mapToEntity(data: PushMessageData): PushMessageEntity {
        return PushMessageEntity(
            pushDispatchId = data.pushDispatchId,
            messageType = data.messageType ?: "",
            messagePriority = data.messagePriority,
            title = data.title,
            body = data.body,
            imageUrl = data.imageUrl,
            campaignId = data.campaignId,
            payload = data.payload, // payload는 문자열 그대로 저장
            sender = data.sender,
            channelId = data.channelId,
            channelName = data.channelName,
            channelDescription = data.channelDescription,
            createdAt = LocalDateTime.now()
        )
    }

    /**
     * 단일 메시지를 '읽음' 상태로 변경.
     */
    override suspend fun markMessageAsConfirmed(pushDispatchId: String) {
        try {
            pushMessageDao.markAsConfirmed(pushDispatchId)
            MatrixLog.i(TAG, "Message marked as CONFIRMED: $pushDispatchId")

            reportMessageToServer(pushDispatchId, ClientStatus.CONFIRMED)
            MatrixLog.i(TAG, "markMessageAsConfirmed completed.")

        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to mark message as confirmed", e)
            throw MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, "Failed to mark message as confirmed", e)
        }
    }

    /**
     * 모든 '읽지 않은' 메시지를 '읽음' 상태로 변경.
     */
    override suspend fun markAllMessagesAsConfirmed() {
        try {
            pushMessageDao.markAllAsConfirmed()
            MatrixLog.i(TAG, "All unread messages marked as CONFIRMED")

            val messages = pushMessageDao.getAllMessagesByStatus(ClientStatus.CONFIRMED)
            messages.forEach {
                reportMessageToServer(it.pushDispatchId, ClientStatus.CONFIRMED)
            }

            MatrixLog.i(TAG, "markAllMessagesAsConfirmed completed.")

        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to mark all messages as confirmed", e)
            throw MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, "Failed to mark all messages as confirmed", e)
        }
    }

    /**
     * 단일 메시지를 '읽지 않은' 상태로 변경.
     */
    override suspend fun markMessageAsReceived(pushDispatchId: String) {
        try {
            pushMessageDao.markAsReceived(pushDispatchId)
            MatrixLog.i(TAG, "Message marked as CONFIRMED: $pushDispatchId")

            reportMessageToServer(pushDispatchId, ClientStatus.RECEIVED)
            MatrixLog.i(TAG, "markMessageAsConfirmed received.")

        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to mark message as confirmed", e)
            throw MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, "Failed to mark message as confirmed", e)
        }
    }

    /**
     * 모든 '에러' 메시지를 '읽지 않은' 상태로 변경.
     */
    override suspend fun markAllMessagesAsReceived() {
        try {
            pushMessageDao.markAllAsReceived()
            MatrixLog.i(TAG, "All unread messages marked as RECEIVED")

            val messages = pushMessageDao.getAllMessagesByStatus(ClientStatus.RECEIVED)
            messages.forEach {
                reportMessageToServer(it.pushDispatchId, ClientStatus.RECEIVED)
            }

            MatrixLog.i(TAG, "markAllMessagesAsReceived completed.")

        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to mark all messages as received", e)
            throw MpsException(MpsError.MARK_AS_CONFIRMED_FAILED, "Failed to mark all messages as received", e)
        }
    }

    /**
     * 모든 메시지 조회. (실시간)
     */
    override fun observeAllMessages(): Flow<List<PushMessageEntity>> {
        return try {
            pushMessageDao.observeAllMessages()
        } catch (e: Exception) {
            throw MpsException(MpsError.GET_MESSAGES_FAILED, "Failed to observe messages from DB", e)
        }
    }

    /**
     * 모든 메시지 조회.
     */
    override suspend fun getAllMessages(): List<PushMessageEntity> {
        return try {
            pushMessageDao.getAllMessages()
        } catch (e: Exception) {
            throw MpsException(MpsError.GET_MESSAGES_FAILED, "Failed to get all messages from DB", e)
        }
    }

    /**
     * 특정 메시지 조회
     */
    override suspend fun getMessageById(pushDispatchId: String): PushMessageEntity? {
        return try {
            pushMessageDao.getMessageById(pushDispatchId)
        } catch (e: Exception) {
            throw MpsException(MpsError.GET_MESSAGES_FAILED, "Failed to get all messages from DB", e)
        }
    }

    /**
     * ERROR 상태 메시지 목록 조회
     */
    override suspend fun getErrorMessages(): List<PushMessageEntity> {
        return try {
            pushMessageDao.getErrorMessages()
        } catch (e: Exception) {
            throw MpsException(MpsError.RETRY_MESSAGE_CALLBACK_FAILED, e.message, e)
        }
    }

    /**
     * 서버에 메시지 상태를 재전송 할 메시지 목록 조회
     */
    override suspend fun retryReportMessageStatus(): List<PushMessageEntity> {
        return try {
            pushMessageDao.getRetryTargetMessages()
        } catch (e: Exception) {
            throw MpsException(MpsError.RETRY_MESSAGE_CALLBACK_FAILED, e.message, e)
        }
    }
}