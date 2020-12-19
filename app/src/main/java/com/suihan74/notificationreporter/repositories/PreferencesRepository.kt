package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.MutableLiveData
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.utilities.WrappedDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * アプリ設定を扱うリポジトリ
 *
 * TODO: `SharedPreferences`または`DataStore`を扱うようにする
 */
class PreferencesRepository(
    dataStore: WrappedDataStore,
    private val notificationDao: NotificationDao
) {
    /** 画面消灯までの待機時間(ミリ秒) */
    val lightOffInterval = dataStore.getLiveData(PreferencesKey.LIGHT_OFF_INTERVAL)

    /**
     * バックライト消灯後の画面をさらに暗くする度合い
     *
     * 0.0f ~ 1.0f
     */
    val lightLevel = dataStore.getLiveData(PreferencesKey.LIGHT_LEVEL)

    /**
     * デフォルトの通知バー描画設定
     */
    val defaultNotificationSetting = MutableLiveData<NotificationSetting>()

    // ------ //

    suspend fun init() = withContext(Dispatchers.Main) {
        defaultNotificationSetting.value = notificationDao.getDefaultSetting()
    }

    suspend fun updateDefaultNotificationSetting(setting: NotificationSetting) = withContext(Dispatchers.Main) {
        notificationDao.insertDefaultSetting(setting)
    }
}
