package matrix.push.client.modules.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * @author tarkarn
 * @since 2025.06.24
 *
 * 푸시 메시지 데이터베이스 접근 인터페이스
 */
@Dao
internal interface PushMessagesDao {

    /**
     * 푸시 메시지 저장
     * @param message 저장할 푸시 메시지 엔티티
     * @return 생성된 ID
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: PushMessageEntity): Long

    /**
     * 여러 푸시 메시지를 한 번에 저장.
     * @param messages 저장할 푸시 메시지 엔티티 리스트
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<PushMessageEntity>)

    /**
     * 타입에 관계없이 가장 최근에 수신된 메시지 1개를 반환한다.
     * @return 가장 최근의 PushMessageEntity 또는 DB가 비어있으면 null
     */
    @Query("SELECT * FROM push_messages ORDER BY received_at DESC LIMIT 1")
    suspend fun getLatestMessage(): PushMessageEntity?

    /**
     * 특정 상태의 메시지 개수를 한 번 조회하여 반환한다.
     * @param clientStatus 조회할 메시지 상태
     * @return 해당 상태의 메시지 개수
     */
    @Query("SELECT COUNT(*) FROM push_messages WHERE client_status = :clientStatus")
    suspend fun getMessageCountByStatus(clientStatus: ClientStatus): Int

    /**
     * 특정 메시지의 상태를 CONFIRMED로 변경.
     * (예: 특정 메시지 상세를 봤을 때 호출)
     */
    @Query("UPDATE push_messages SET client_status = :clientStatus , updated_at = :updatedAt WHERE push_dispatch_id = :pushDispatchId")
    suspend fun markAsConfirmed(pushDispatchId: String, clientStatus: ClientStatus = ClientStatus.CONFIRMED, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * 모든 RECEIVED 상태의 메시지를 CONFIRMED로 변경.
     * (예: 메시지 목록 화면에 진입했을 때 호출)
     */
    @Query("UPDATE push_messages SET client_status = :clientStatus, updated_at = :updatedAt WHERE client_status = 'RECEIVED'")
    suspend fun markAllAsConfirmed(clientStatus: ClientStatus = ClientStatus.CONFIRMED, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * 특정 메시지의 상태를 RECEIVED로 변경.
     * (예: 특정 메시지 상세를 봤을 때 호출)
     */
    @Query("UPDATE push_messages SET client_status = :clientStatus , updated_at = :updatedAt WHERE push_dispatch_id = :pushDispatchId")
    suspend fun markAsReceived(pushDispatchId: String, clientStatus: ClientStatus = ClientStatus.RECEIVED, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * 모든 ERROR 상태의 메시지를 RECEIVED로 변경.
     * (예: 메시지 목록 화면에 진입했을 때 호출)
     */
    @Query("UPDATE push_messages SET client_status = :clientStatus, updated_at = :updatedAt WHERE client_status = 'RECEIVED'")
    suspend fun markAllAsReceived(clientStatus: ClientStatus = ClientStatus.RECEIVED, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * 모든 메시지 조회. (실시간)
     */
    @Query("SELECT * FROM push_messages ORDER BY received_at DESC")
    fun observeAllMessages(): Flow<List<PushMessageEntity>>

    /**
     * 모든 메시지 조회.
     */
    @Query("SELECT * FROM push_messages ORDER BY received_at DESC")
    suspend fun getAllMessages(): List<PushMessageEntity>

    /**
     * client_status 에 일치하는 메시지 조회.
     */
    @Query("SELECT * FROM push_messages WHERE client_status = :clientStatus ORDER BY received_at DESC")
    suspend fun getAllMessagesByStatus(clientStatus: ClientStatus): List<PushMessageEntity>

    /**
     * 특정 메시지 조회
     * @param pushDispatchId 메시지 ID
     * @return 푸시 메시지 엔티티
     */
    @Query("SELECT * FROM push_messages WHERE push_dispatch_id = :pushDispatchId")
    suspend fun getMessageById(pushDispatchId: String): PushMessageEntity?

    /**
     * 재전송 대상 메시지 목록 조회 (RECEIVED 상태)
     * 서버로 전송되지 않은 상태인 모든 메시지를 조회한다
     * @return 재전송 대상 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE (client_status = 'RECEIVED' AND send_to_server = 'false') ORDER BY CAST(received_at AS INTEGER) ASC")
    suspend fun getRetryTargetMessages(): List<PushMessageEntity>

    /**
     * 에러 메시지 업데이트
     * @param pushDispatchId 메시지 ID
     * @param errorMessage 에러 메시지
     * @param updatedAt 업데이트 시각 (밀리초 문자열)
     */
    @Query("UPDATE push_messages SET error_message = :errorMessage, client_status = 'ERROR', updated_at = :updatedAt WHERE push_dispatch_id = :pushDispatchId")
    suspend fun updateError(pushDispatchId: String, errorMessage: String, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * ERROR 상태 메시지 목록 조회 (재전송 대상)
     * @return ERROR 상태 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE client_status = 'ERROR' ORDER BY CAST(received_at AS INTEGER) ASC")
    suspend fun getErrorMessages(): List<PushMessageEntity>

    // ==== FIXME: 아래 쿼리는 필요한지 판단 후 제거.

    /**
     * messageId로 기존 메시지 존재 여부 확인
     * @param pushDispatchId 확인할 메시지 ID
     * @return 존재 여부
     */
    @Query("SELECT EXISTS(SELECT 1 FROM push_messages WHERE push_dispatch_id = :pushDispatchId)")
    suspend fun existsByMessageId(pushDispatchId: String): Boolean

    /**
     * 특정 메시지의 처리 상태 업데이트
     * @param pushDispatchId 메시지 ID
     * @param clientStatus 새로운 처리 상태
     * @param updatedAt 업데이트 시각 (밀리초 문자열)
     */
    @Query("UPDATE push_messages SET client_status = :clientStatus, updated_at = :updatedAt WHERE push_dispatch_id = :pushDispatchId")
    suspend fun updateProcessStatus(pushDispatchId: String, clientStatus: ClientStatus, updatedAt: String = System.currentTimeMillis().toString())

    /**
     * SseClient 전송 상태 업데이트 및 상태 변경
     * @param pushDispatchId 메시지 ID
     * @param sendToServer SseClient 전송 여부 ("true"/"false" 문자열)
     * @param updatedAt 업데이트 시각 (밀리초 문자열)
     */
    @Query("UPDATE push_messages SET send_to_server = :sendToServer, client_status = :clientStatus, updated_at = :updatedAt WHERE push_dispatch_id = :pushDispatchId")
    suspend fun updateSseClientSent(pushDispatchId: String, clientStatus: String, sendToServer: String, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * 메시지 타입별 푸시 메시지 목록 조회
     * @param messageType 메시지 타입
     * @return 푸시 메시지 목록 Flow
     */
    @Query("SELECT * FROM push_messages WHERE message_type = :messageType ORDER BY received_at DESC")
    fun getMessagesByType(messageType: String): Flow<List<PushMessageEntity>>

    /**
     * 특정 기간 내 푸시 메시지 조회 (String 기반 시간 비교)
     * @param startTime 시작 시간 (밀리초 문자열)
     * @param endTime 종료 시간 (밀리초 문자열)
     * @return 푸시 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE CAST(received_at AS INTEGER) BETWEEN CAST(:startTime AS INTEGER) AND CAST(:endTime AS INTEGER) ORDER BY CAST(received_at AS INTEGER) DESC")
    suspend fun getMessagesByPeriod(startTime: LocalDateTime, endTime: LocalDateTime): List<PushMessageEntity>

    /**
     * 미전송 메시지 목록 조회 (서버 전송 실패한 메시지들)
     * @return 미전송 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE send_to_server = 'false' AND client_status != 'ERROR' ORDER BY CAST(received_at AS INTEGER) ASC")
    suspend fun getUnsentMessages(): List<PushMessageEntity>

    /**
     * 전체 사용자의 재전송 대상 메시지 목록 조회 (RECEIVE + ERROR 상태)
     * 서버로 전송되지 않았거나 ERROR 상태인 모든 메시지를 조회한다
     * @return 재전송 대상 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE ((client_status = 'RECEIVE' AND send_to_server = 'false') OR client_status = 'ERROR') ORDER BY CAST(received_at AS INTEGER) ASC")
    suspend fun getAllRetryTargetMessages(): List<PushMessageEntity>

    /**
     * 사용자별 ERROR 상태 메시지 목록 조회
     * @param userId 사용자 ID
     * @return ERROR 상태 메시지 목록
     */
    @Query("SELECT * FROM push_messages WHERE id = :id AND client_status = 'ERROR' ORDER BY CAST(received_at AS INTEGER) ASC")
    suspend fun getErrorMessagesById(id: Long): List<PushMessageEntity>

    /**
     * 오래된 메시지 삭제 (30일 이전)
     * @param cutoffTime 삭제 기준 시간 (밀리초 문자열)
     * @return 삭제된 행 수
     */
    @Query("DELETE FROM push_messages WHERE CAST(received_at AS INTEGER) < CAST(:cutoffTime AS INTEGER)")
    suspend fun deleteOldMessages(cutoffTime: String): Int

    /**
     * 모든 메시지 삭제
     */
    @Query("DELETE FROM push_messages")
    suspend fun deleteAllMessages()

    /**
     * 사용자별 메시지 삭제
     * @param id row id
     */
    @Query("DELETE FROM push_messages WHERE id = :id")
    suspend fun deleteMessagesById(id: Long)
}