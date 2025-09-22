package matrix.commons.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import matrix.commons.log.MatrixLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.coroutines.CoroutineContext

/**
 * @author tarkarn
 * @since 2022.02.10
 *
 * coroutine.launch 사용 중 예외 발생 시 앱 죽는 현상을 방지하기 위해 추가.
 */
fun CoroutineScope.launchSafety(
    coroutineContext: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit,
    handleException: (Throwable) -> Unit): Job {

    return launch (coroutineContext) {
        runCatching { block() }.onFailure { handleException(it) }
    }
}


/**
 * <String Extension>
 *
 * Text 맨 뒤가 postfix 가 아닌 경우 postfix 를 붙여준다.
 */
fun String.addPostfix(postfix: CharSequence): String {
    return when (isEmpty()) {
        true -> this
        false -> {
            val temp = this.substring(this.length - postfix.length)
            when (temp == postfix) {
                true -> this
                false -> this + postfix
            }
        }
    }
}

/**
 * toByteArray().contentToString() 형식으로 build 시점에 추가한
 * byte 형태의 string resource 들을 String 형태로 가져온다.
 */
fun String.getStringFromByte(): String {
    val byteArr = (trim().substring(1, lastIndex).split(","))
    val bytes = ByteArray(byteArr.size)

    for (i in bytes.indices) {
        bytes[i] = byteArr[i].trim().toByte()
    }

    return bytes.toString(Charsets.UTF_8)
}

/**
 * App Version 을 가져온다.
 */
fun Context.getAppVersion(): String {
    return packageManager.getPackageInfo(packageName, 0).versionName ?: ""
}

/**
 * Used to completely close the app.
 */
fun Activity.closeApplication() {
    finishAndRemoveTask()
    android.os.Process.killProcess(android.os.Process.myPid())
}

/**
 * UTC Time 을 가져온다.
 *
 * @sample "yyyy-MM-dd HH-mm-ss.sss".getUtcDateTimeAsString()
 */
@SuppressLint("SimpleDateFormat")
fun String.getUtcDateTimeAsString(): String {
    return try {
        val sdf = SimpleDateFormat(this)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date())
    } catch (e: Exception) {
        MatrixLog.system("getUtcDateTimeAsString failed.", e)
        ""
    }
}

/**
 * 기기의 설정에 맞는 시간을 가져온다.
 *
 * @sample "yyyy-MM-dd HH-mm-ss.sss".getDateTimeAsString()
 */
@SuppressLint("SimpleDateFormat")
fun String.getDateTimeAsString(): String {
    return try {
        SimpleDateFormat(this).format(Date())
    } catch (e: Exception) {
        MatrixLog.system("getDateTimeAsString failed.", e)
        ""
    }
}

/**
 * first: 망 사업자 (MCC+MNC)
 * second: 망 사업자명 (SK Telecom)
 */
fun Context.getNetworkOperatorInfo(): Pair<String, String> {
    return (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
        .let{ telephonyManager ->
            Pair(telephonyManager.networkOperator, telephonyManager.networkOperatorName)
        }
}

/**
 * 앱 이름 가져오기.
 */
fun Context.getAppName(): String {
    return packageManager.getApplicationLabel(
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.MATCH_UNINSTALLED_PACKAGES
        )
    ).toString()
}


/**
 * Bundle을 Map<String, String>으로 변환하는 헬퍼 함수
 * 다양한 타입(String, Int, Long, Boolean 등)을 안전하게 String으로 변환
 */
fun Bundle.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()

    try {
        // Bundle의 모든 키를 순회
        for (key in keySet()) {
            // get()으로 Any? 타입으로 가져온 후 안전하게 String으로 변환
            get(key)?.let { value ->
                when (value) {
                    is String -> map[key] = value
                    is Int -> map[key] = value.toString()
                    is Long -> map[key] = value.toString()
                    is Boolean -> map[key] = value.toString()
                    is Float -> map[key] = value.toString()
                    is Double -> map[key] = value.toString()
                    else -> {
                        // 기타 타입은 toString()으로 변환
                        Log.d("Extension", "Converting unknown type ${value::class.simpleName} to String for key: $key")
                        map[key] = value.toString()
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Extension", "Error converting Bundle to Map", e)
    }

    return map
}