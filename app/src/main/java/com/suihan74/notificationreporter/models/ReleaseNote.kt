package com.suihan74.notificationreporter.models

import androidx.recyclerview.widget.DiffUtil
import org.threeten.bp.LocalDate

/**
 * アプリの更新履歴情報
 */
data class ReleaseNote(
    /** バージョン文字列(major.minor.fix) */
    val version: String,

    /** 更新内容(複数行) */
    val description: String,

    /** 更新日(uuuu-MM-dd (JST)) */
    val timestamp: LocalDate
) {
    class DiffCallback : DiffUtil.ItemCallback<ReleaseNote>() {
        override fun areItemsTheSame(oldItem: ReleaseNote, newItem: ReleaseNote) =
            oldItem.version == newItem.version

        override fun areContentsTheSame(oldItem: ReleaseNote, newItem: ReleaseNote) =
            oldItem.version == newItem.version &&
                    oldItem.description == newItem.description &&
                    oldItem.timestamp == newItem.timestamp
    }
}
