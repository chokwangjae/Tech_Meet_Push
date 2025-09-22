package matrix.push.client.data

import android.os.Parcelable
import matrix.commons.log.MatrixLog
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.Throws

/**
 * @author tarkarn
 * @since 2025.06.24
 *
 * 서버로부터 받은 푸시 메시지 데이터를 담는 data class.
 * PushMessageDto의 toMap() 메서드를 통해 생성된 Map<String, String>에서 파싱하여 사용.
 * 모든 필드는 Map에 해당 키가 없을 경우를 대비해 nullable로 선언.
 */
@Parcelize
data class PushMessageData(
    val pushDispatchId: String,
    val messageId: String?,
    val messageType: String?,
    val messagePriority: String?,
    val title: String?,
    val body: String?,
    val icon: String?,             // 사용 안함 (고정값 matrix_push_icon 사용)
    val color: String?,           // 사용 안함 (고정값 #FFFFFF 사용)
    val imageUrl: String?,
    val campaignId: String?,
    val payload: String?,         // JSON 문자열 형태
    val asyncSubmission: String?, // JSON 문자열 형태
    val sender: String?,
    val timestamp: String?,
    val channelId: String?,             // Android Only
    val channelName: String?,           // Android Only
    val channelDescription: String?     // Android Only
) : Parcelable {

    companion object {
        private const val TAG = "PushMessageData"

        /**
         * Firebase RemoteMessage.data 또는
         * 유사한 Map<String, String>에서 PushMessageData 객체를 생성.
         */
        @Throws(NullPointerException::class)
        fun fromMap(data: Map<String, String?>): PushMessageData {
            return PushMessageData(
                pushDispatchId = data["pushDispatchId"]!!,
                messageId = data["messageId"],
                messageType = data["messageType"],
                messagePriority = data["messagePriority"],
                title = data["title"],
                body = data["body"],
                icon = data["icon"],
                color = data["color"],
                imageUrl = data["imageUrl"],
                campaignId = data["campaignId"],
                payload = data["payload"],
                asyncSubmission = data["asyncSubmission"],
                sender = data["sender"],
                timestamp = data["timestamp"],
                channelId = data["channelId"],
                channelName = data["channelName"],
                channelDescription = data["channelDescription"]
            )
        }
    }

    fun getPayloadAsJsonObject(): JSONObject? {
        if (payload.isNullOrBlank()) {
            return null
        }
        return try {
            JSONObject(payload)
        } catch (e: JSONException) {
            MatrixLog.e(TAG, "Failed to parse payload string to JSONObject: $payload", e)
            null
        }
    }

    /**
     * asyncSubmission 문자열을 JSONObject로 파싱.
     * 파싱에 실패하면 null을 반환.
     */
    fun getAsyncSubmissionAsJsonObject(): JSONObject? {
        if (asyncSubmission.isNullOrBlank()) { // null 또는 빈 문자열 체크
            return null
        }
        return try {
            JSONObject(asyncSubmission)
        } catch (e: JSONException) {
            MatrixLog.e(TAG, "Failed to parse asyncSubmission string to JSONObject: $asyncSubmission", e)
            null
        }
    }
}
