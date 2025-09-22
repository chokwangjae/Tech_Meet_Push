package matrix.commons.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone


object Utils {

    @JvmStatic
    private var applicationContext: Context? = null

    /**
     * Generates a random string of specified length.
     *
     * @param length The length of the random string to be generated.
     * @return A random string of specified length.
     */
    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * 상위 Package에서 Resource ID를 얻어온다.
     *
     * @param id 가져올 Resource ID값
     * @param type 가져올 Resource Type
     */
    fun getResourceID(id: String, type: String): Int {
        return applicationContext!!.run {
            getResourceID(this, id, type)
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getResourceID(context: Context, id: String, type: String): Int {
        return context.run {
            val resources = packageManager.getResourcesForApplication(packageName)
            resources.getIdentifier(id, type, packageName)
        }
    }

    /**
     * Query String을 Map으로 변환.
     */
    fun parseQueryToMap(queryStr: String): Map<String, String> {
        return queryStr.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }
    }

    /**
     * Matrix Mobile 환경인지 Check함수
     */
    fun checkMatrixMobile() : Boolean{
        return try {
            Class.forName("matrix.mobile.MatrixMobile")
            true
        }catch (e : ClassNotFoundException){
            false
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceUUID(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return androidId ?: "unknown_uuid_${System.currentTimeMillis()}" // ANDROID_ID가 null인 극히 드문 경우를 대비
    }

    /**
     * UTC 기반 시간대.
     */
    @SuppressLint("SimpleDateFormat")
    fun getTimestampByDateFormatter(format: String = "yyyyMMddHHmmss"): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern(format)
                .format(ZonedDateTime.now(ZoneOffset.UTC))
        } else {
            val formatter = SimpleDateFormat(format)
            formatter.timeZone = TimeZone.getTimeZone("UTC")

            return formatter.format(Date())
        }
    }
}