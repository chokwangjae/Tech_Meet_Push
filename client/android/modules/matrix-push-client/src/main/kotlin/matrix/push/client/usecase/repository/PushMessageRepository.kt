package matrix.push.client.usecase.repository

import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.modules.database.PushMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
internal interface PushMessageRepository {

    /**
     * JSON 데이터를 파싱하고 DB에 저장한 후, 저장된 Entity를 반환한다.
     * 중복된 pushDispatchId가 있으면 저장을 무시하고 null을 반환한다.
     * @param jsonData 수신된 raw JSON 문자열
     * @return 저장에 성공한 PushMessageEntity 또는 null
     */
    suspend fun processAndSaveMessage(jsonData: String): PushMessageEntity?

    /**
     * 서버에 메시지 상태를 전송한다. (수신)
     * @param pushDispatchId 수신 확인을 보낼 메시지의 고유 ID
     */
    suspend fun reportMessageReceived(pushDispatchId: String)

    /**
     * 서버에 메시지 상태를 전송한다. (삭제)
     * @param pushDispatchId 삭제 요청을 보낼 메시지의 고유 ID
     */
    suspend fun reportMessageDeleted(pushDispatchId: String)

    /**
     * 서버에 메시지 상태를 전송한다.
     * @param pushMessageEntityList 메시지 상태를 전송할 메시지 엔티티 리스트.
     */
    suspend fun reportMessages(pushMessageEntityList: List<PushMessageEntity>)

    /**
     * 서버와 미수신 메시지를 동기화한다.
     * @param lastKnownPushDispatchId 클라이언트가 알고 있는 마지막 메시지의 ID. 이 ID 이후의 메시지를 요청.
     * @param limit 한 번에 동기화할 최대 메시지 수.
     * @return 성공적으로 저장된 새로운 메시지의 수.
     */
    suspend fun syncMissedMessages(lastKnownPushDispatchId: String, limit: Int = MatrixPushClientOptions.syncMessagesLimit): Int

    /**
     * 단일 메시지를 '읽음' 상태로 변경.
     */
    suspend fun markMessageAsConfirmed(pushDispatchId: String)

    /**
     * 모든 '읽지 않은' 메시지를 '읽음' 상태로 변경.
     */
    suspend fun markAllMessagesAsConfirmed()

    /**
     * 단일 메시지를 '읽음' 상태로 변경.
     */
    suspend fun markMessageAsReceived(pushDispatchId: String)

    /**
     * 모든 '읽지 않은' 메시지를 '읽지 않은' 상태로 변경.
     */
    suspend fun markAllMessagesAsReceived()

    /**
     * 모든 메시지 조회. (실시간)
     */
    fun observeAllMessages(): Flow<List<PushMessageEntity>>

    /**
     * 모든 메시지 조회.
     */
    suspend fun getAllMessages(): List<PushMessageEntity>

    /**
     * pushDispatchId로 메시지 조회.
     */
    suspend fun getMessageById(pushDispatchId: String): PushMessageEntity?

    /**
     * 'ERROR' 상태인 메시지 조회
     */
    suspend fun getErrorMessages(): List<PushMessageEntity>

    /**
     * 서버에 메시지 상태를 재전송 할 메시지 목록 조회
     */
    suspend fun retryReportMessageStatus(): List<PushMessageEntity>
}