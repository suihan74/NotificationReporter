package com.suihan74.notificationreporter.services

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
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
        val prefRepo = app.preferencesRepository

        GlobalScope.launch {
            app.notificationRepository.pushNotification(sbn)

            // 通知を行わない時間帯では画面遷移しない
            val silentTimeZoneStart = prefRepo.getGeneralSetting(PreferencesKey.SILENT_TIMEZONE_START)
            val silentTimeZoneEnd = prefRepo.getGeneralSetting(PreferencesKey.SILENT_TIMEZONE_END)
            val now = LocalTime.now().toSecondOfDay()
            val considerDateChange = silentTimeZoneStart > silentTimeZoneEnd
            if (considerDateChange) {
                if (now >= silentTimeZoneStart || now <= silentTimeZoneEnd) {
                    return@launch
                }
            }
            else {
                if (now in silentTimeZoneStart..silentTimeZoneEnd) {
                    return@launch
                }
            }

            if (sbn?.notification != null && app.screenRepository.screenOn.value == false) {
                val intent = Intent(applicationContext, LockScreenActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                applicationContext.startActivity(intent)
                Log.i("Notification", sbn.packageName)
            }
        }
    }
}
