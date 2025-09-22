package matrix.push.client

import matrix.push.client.data.PushMessage
import matrix.push.client.modules.database.PushMessageEntity
import java.time.ZoneOffset

/**
 *   @author tarkarn
 *   @since 2025. 7. 17.
 */

/**
 * 내부용 PushMessageEntity를 외부 공개용 PushMessage DTO로 변환하는 확장 함수.
 */
fun PushMessageEntity.toPublicDto(): PushMessage {
    return PushMessage(
        pushDispatchId = this.pushDispatchId,
        messageType = this.messageType,
        title = this.title,
        body = this.body,
        imageUrl = this.imageUrl,
        campaignId = this.campaignId,
        payload = this.payload,
        receivedAt = this.receivedAt.toInstant(ZoneOffset.UTC).toEpochMilli(),
        status = this.clientStatus.name
    )
}

/**
 * Entity 리스트를 DTO 리스트로 변환하는 확장 함수.
 */
fun List<PushMessageEntity>.toPublicDtoList(): List<PushMessage> {
    return this.map { it.toPublicDto() }
}