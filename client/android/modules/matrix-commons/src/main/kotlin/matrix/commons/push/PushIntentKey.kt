package matrix.commons.push

/**
 *   @author tarkarn
 *   @since 2025. 7. 30.
 *
 *   Matrix Push 에서 사용 할 Intent Key
 */
object PushIntentKey {

    /**
     * 푸시 메시지를 구분하기 위한 Unique ID
     */
    const val PUSH_DISPATCH_ID = "mps_dispatch_id"

    /**
     * 푸시 메시지 타입 (NOTIFICATION, SILENT)
     */
    const val MESSAGE_TYPE = "mps_message_type"

    /**
     * 푸시 메시지 제목
     */
    const val TITLE = "mps_title"

    /**
     * 푸시 메시지 내용
     */

    const val BODY = "mps_body"

    /**
     * 푸시 메시지 이미지 주소
     */

    const val IMAGE_URL = "mps_image_url"

    /**
     * 푸시 메시지 캠페인(채널) ID
     */

    const val CAMPAIGN_ID = "mps_campaign_id"

    /**
     * 푸시 메시지 payload
     * 사용자는 해당 항목으로 커스터마이징 한 데이터를 전송할 수 있다.
     */

    const val PAYLOAD = "mps_payload"

    /**
     * 푸시 메시지 asyncSubmission
     * WebSquare 전용 기능 (어떻게 될지 모름)
     */

    const val ASYNC_SUBMISSION = "mps_async_submission"

    /**
     * 푸시 메시지 수신 시각
     */
    const val RECEIVED_AT = "mps_received_at"

    /**
     * 푸시 메시지 상태
     * RECEIVED, DELETED, CONFIRMED
     */
    const val STATUS = "mps_status"
}