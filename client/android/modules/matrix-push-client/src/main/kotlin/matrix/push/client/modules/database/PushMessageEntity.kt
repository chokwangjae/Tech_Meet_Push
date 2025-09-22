package matrix.push.client.modules.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * @author tarkarn
 * @since 2025.05.16
 *
 * 푸시 메세지 로컬 데이터베이스 저장 용 entity
 */
@Entity(
    tableName = "push_messages",
    indices = [
        Index(value = ["push_dispatch_id"], unique = true), // 중복 방지
    ]
)
data class PushMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 푸시 메시지 발송 건별 고유 ID (서버에서 전송) */
    @ColumnInfo(name = "push_dispatch_id")
    val pushDispatchId: String,

    /** 메시지 타입 (NOTIFICATION, SILENT) */
    @ColumnInfo(name = "message_type")
    val messageType: String,

    /** 메시지 우선순위 */
    @ColumnInfo(name = "message_priority")
    val messagePriority: String? = null,

    /** 알림 제목 */
    @ColumnInfo(name = "title")
    val title: String? = null,

    /** 알림 내용 */
    @ColumnInfo(name = "body")
    val body: String? = null,

    /** 아이콘 설정 - 사용 안함 (고정값 matrix_push_icon 사용) */
    @ColumnInfo(name = "icon")
    val icon: String? = null,

    /** 색상 설정 - 사용 안함 (고정값 #FFFFFF 사용) */
    @ColumnInfo(name = "color")
    val color: String? = null,

    /** 이미지 URL */
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    /** 캠페인 ID */
    @ColumnInfo(name = "campaign_id")
    val campaignId: String? = null,

    /** 추가 데이터 (JSON 형태) */
    @ColumnInfo(name = "payload")
    val payload: String? = null,

    /** 발신자 정보 */
    @ColumnInfo(name = "sender")
    val sender: String? = null,

    /** 알림 채널 ID */
    @ColumnInfo(name = "channel_id")
    val channelId: String? = null,

    /** 알림 채널 이름 */
    @ColumnInfo(name = "channel_name")
    val channelName: String? = null,

    /** 알림 채널 설명 */
    @ColumnInfo(name = "channel_description")
    val channelDescription: String? = null,

    /** 비동기 전송 여부 */
    @ColumnInfo(name = "async_submission")
    val asyncSubmission: String? = null,

    /** 수신 시각 (클라이언트 기준) - 밀리초 문자열 */
    @ColumnInfo(name = "received_at")
    val receivedAt: LocalDateTime = LocalDateTime.now(),

    /** SseClient을 통한 서버 전송 여부 - "true"/"false" 문자열 */
    @ColumnInfo(name = "send_to_server")
    val sendToServer: String = "false",

    /** 처리 상태 (RECEIVED, CONFIRMED 등) */
    @ColumnInfo(name = "client_status")
    val clientStatus: ClientStatus = ClientStatus.RECEIVED,

    /** 에러 메시지 (처리 실패 시) */
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    /** 생성 시각 (서버 기준 메세지 발송 시각) - 밀리초 문자열 */
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /** 수정 시각 - 밀리초 문자열 */
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/** 처리 상태 (서버와 일치 시킴) */
enum class ClientStatus {
    ERROR,
    RECEIVED,
    DELETED,
    CONFIRMED
}