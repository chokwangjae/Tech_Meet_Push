package matrix.push.client.usecase.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.imageLoader
import coil.request.ImageRequest
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import matrix.commons.log.MatrixLog
import matrix.commons.push.PushIntentKey
import matrix.commons.utils.Utils
import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.modules.database.ClientStatus
import matrix.push.client.modules.database.PushMessageEntity
import matrix.push.client.modules.database.PushMessagesDao
import me.leolin.shortcutbadger.ShortcutBadger

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 *
 *   전달받은 푸시 메시지로 시스템 알림 생성을 담당.
 */
internal class NotificationImpl(
    private val context: Context,
    private val pushMessageDao: PushMessagesDao,
    private val gson: Gson
) : NotificationRepository {

    companion object {
        private const val TAG = "NotificationImpl"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun showNotification(message: PushMessageEntity) {

        // badge update.
        updateBadgeCount()

        val notificationManager = NotificationManagerCompat.from(context)

        // 1. 알림 채널 생성 (Oreo 이상)
        createNotificationChannelIfNeeded(notificationManager, message)

        // 2. 알림 탭 시 실행될 Intent 생성
        val pendingIntent = createLaunchAppIntent(message)

        // 3. 알림 빌더 생성
        val notificationBuilder = NotificationCompat.Builder(context, MatrixPushClientOptions.channelId)
            .setSmallIcon(Utils.getResourceID(context, MatrixPushClientOptions.defaultSmallIcon, "drawable")) // ★ 고정 아이콘 사용
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. 이미지가 있으면 다운로드하여 BigPictureStyle 적용
        if (!message.imageUrl.isNullOrBlank()) {
            try {
                val bitmap = loadImage(message.imageUrl)
                if (bitmap != null) {
                    notificationBuilder
                        .setLargeIcon(bitmap)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .setBigContentTitle(message.title)
                                .setSummaryText(message.body)
                        )
                    MatrixLog.d(TAG, "Image loaded and set for notification")
                } else {
                    MatrixLog.w(TAG, "Image bitmap is null")
                }
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Image loading failed: ${message.imageUrl}", e)
            }
        }

        // 5. 알림 표시
        // pushDispatchId의 해시코드를 사용하여 각 알림에 고유한 ID 부여
        notificationManager.notify(message.pushDispatchId.hashCode(), notificationBuilder.build())
    }


    private fun createNotificationChannelIfNeeded(manager: NotificationManagerCompat, message: PushMessageEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = MatrixPushClientOptions.channelId
            val channelName = MatrixPushClientOptions.channelName
            val channelDescription = MatrixPushClientOptions.channelDescription
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 알림 클릭 시 앱의 런처 액티비티를 실행하는 PendingIntent를 생성
     */
    private fun createLaunchAppIntent(message: PushMessageEntity): PendingIntent? {

        // 1. 패키지 매니저를 통해 앱의 기본 실행(LAUNCHER) 인텐트를 가져온다.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent == null) {
            MatrixLog.e(TAG, "No launch intent found for package: ${context.packageName}")
            return null
        }

        // 2. 푸시 메시지 데이터를 인텐트에 추가
        //    앱이 실행될 때 어떤 푸시를 통해 실행되었는지 알 수 있도록.
        launchIntent.apply {
            putExtra(PushIntentKey.PUSH_DISPATCH_ID, message.pushDispatchId)
            putExtra(PushIntentKey.MESSAGE_TYPE, message.messageType)
            putExtra(PushIntentKey.TITLE, message.title)
            putExtra(PushIntentKey.BODY, message.body)
            putExtra(PushIntentKey.IMAGE_URL, message.imageUrl)
            putExtra(PushIntentKey.CAMPAIGN_ID, message.campaignId)
            putExtra(PushIntentKey.PAYLOAD, message.payload)
            putExtra(PushIntentKey.ASYNC_SUBMISSION, message.asyncSubmission)
            putExtra(PushIntentKey.RECEIVED_AT, message.receivedAt.toString())
            putExtra(PushIntentKey.STATUS, message.clientStatus.name)
        }

        // 3. 인텐트 플래그 설정
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // 4. PendingIntent 생성
        // 각 PendingIntent가 고유하도록 requestCode에 pushDispatchId 해시코드를 사용.
        return PendingIntent.getActivity(context, message.pushDispatchId.hashCode(), launchIntent, flags)
    }

    /**
     * 이미지 로딩
     */
    private fun loadImage(imageUrl: String): Bitmap? {
        return runBlocking {
            try {
                val imageLoader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()
                MatrixLog.d(TAG, "Image request created for: $imageUrl")
                (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
            } catch (e: Exception) {
                MatrixLog.e(TAG, "Image load failed: $imageUrl", e)
                null
            }
        }
    }

    /**
     * DB에서 읽지 않은 메시지 수를 조회하여 앱 아이콘 배지를 업데이트
     */
    private suspend fun updateBadgeCount() {
        try {
            // DB에서 RECEIVED 상태인 메시지의 총 개수를 조회
            val unreadCount = pushMessageDao.getMessageCountByStatus(ClientStatus.RECEIVED)

            if (unreadCount > 0) {
                ShortcutBadger.applyCount(context, unreadCount)
                MatrixLog.i(TAG, "Badge count updated to $unreadCount")
            } else {
                // 이 경우는 거의 없지만, 방어적으로 추가
                ShortcutBadger.removeCount(context)
                MatrixLog.i(TAG, "All messages read, badge removed.")
            }
        } catch (e: Exception) {
            MatrixLog.e(TAG, "Failed to update badge count", e)
        }
    }
}