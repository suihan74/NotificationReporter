package com.suihan74.notificationreporter.models

import android.graphics.Color
import com.suihan74.utilities.extensions.dp
import kotlinx.serialization.Serializable

/**
 * 通知表示の設定
 */
@Serializable
data class NotificationSetting (
    /** 線の色 */
    val color: Int = Color.WHITE,

    /** 線の太さ */
    val thickness: Float = 3.dp,

    /**
     * 線の効果
     *
     * `DashPathEffect`, `DashPathEffect`, `CornerPathEffect`, `DiscretePathEffect`,
     * `SumPathEffect`, `ComposePathEffect`
     */
//    val pathEffect: PathEffect? = null,

    /** スクリーン輪郭線の描画設定 */
    val outlinesSetting: OutlinesSetting = OutlinesSetting(),

    /** 画面上部ノッチ描画設定 */
    val topNotchSetting: NotchSetting = NotchSetting.createInstance(NotchType.NONE),

    /** 画面下部ノッチ描画設定 */
    val bottomNotchSetting: NotchSetting = NotchSetting.createInstance(NotchType.RECTANGLE),
)
