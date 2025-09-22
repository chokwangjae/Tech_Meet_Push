package matrix.push.client.modules.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 *
 *   Room 을 통해 데이터 저장 시 지원하지 않는 타입을 컨버팅 하기 위한 객체
 */
internal object TimeConverters {

    // --- LocalDateTime <-> Long 변환 ---

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): LocalDateTime? {
        // DB의 Long 값을 LocalDateTime 객체로 변환
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        // LocalDateTime 객체를 DB에 저장할 Long 값으로 변환
        return date?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }

    // --- ClientStatus Enum <-> String 변환 ---

    @TypeConverter
    @JvmStatic
    fun fromClientStatus(value: String?): ClientStatus? {
        // DB의 String 값을 ClientStatus Enum으로 변환
        return value?.let { ClientStatus.valueOf(it) }
    }

    @TypeConverter
    @JvmStatic
    fun clientStatusToString(status: ClientStatus?): String? {
        // ClientStatus Enum을 DB에 저장할 String 값으로 변환
        return status?.name
    }
}