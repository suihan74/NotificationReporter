package com.suihan74.notificationreporter.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.FragmentSettingsListBinding
import com.suihan74.notificationreporter.databinding.ListHeaderNotificationSettingItemsBinding
import com.suihan74.notificationreporter.databinding.ListItemNotificationSettingItemsBinding
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dialog.ApplicationSelectionDialogFragment
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 通知表示設定リスト画面
 */
class SettingsListFragment : Fragment() {
    companion object {
        fun createInstance() = SettingsListFragment()
    }

    // ------ //

    private val preferencesActivity
        get() = requireActivity() as PreferencesActivity

    // ------ //

    private val viewModel by lazyProvideViewModel {
        SettingsListViewModel(Application.instance)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsListBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        // リスト初期化
        initializeRecyclerView(binding.recyclerView)

        // 設定追加ボタン
        binding.addSettingButton.setOnClickListener {
            val dialog = ApplicationSelectionDialogFragment.createInstance { _, appInfo ->
                val entity = NotificationEntity(
                    appName = appInfo.packageName,
                    setting = viewModel.defaultSettingEntity?.setting ?: NotificationSetting()
                )
                preferencesActivity.openSettingEditor(entity)
            }
            dialog.show(childFragmentManager, null)
        }

        return binding.root
    }

    // ------ //

    /**
     * 通知表示設定リスト初期化
     */
    private fun initializeRecyclerView(recyclerView: RecyclerView) {
        val adapter = BindingListAdapter<SettingItem, ListItemNotificationSettingItemsBinding>(
            R.layout.list_item_notification_setting_items,
            viewLifecycleOwner,
            SettingItem.DiffCallback()
        ) { binding, item -> binding.item = item }

        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        adapter.setOnClickItemListener {
            val entity = it.item!!.entity
            preferencesActivity.openSettingEditor(entity)
        }

        viewModel.settings.observe(viewLifecycleOwner, {
            adapter.submit(
                items = it,
                header = { parent ->
                    val binding = ListHeaderNotificationSettingItemsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    binding.mainLayout.setOnClickListener {
                        viewModel.defaultSettingEntity?.let { entity ->
                            preferencesActivity.openSettingEditor(entity)
                        }
                    }
                    binding.root
                }
            )
        })
    }
}
