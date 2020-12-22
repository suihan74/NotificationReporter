package com.suihan74.notificationreporter.services

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.MainThread
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    @MainThread
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        // TODO: blacklist
        if (sbn?.packageName == "com.android.systemui") {
            return
        }

        // ロック画面に表示しない通知を除外する
        if (sbn?.notification?.visibility == Notification.VISIBILITY_SECRET) {
            return
        }

        val app = Application.instance
        app.notificationRepository.pushNotification(sbn)

        if (sbn?.notification != null && app.screenRepository.screenOn.value == false) {
            val intent = Intent(applicationContext, LockScreenActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            applicationContext.startActivity(intent)
            Log.i("Notification", sbn.packageName)
        }
    }
}
