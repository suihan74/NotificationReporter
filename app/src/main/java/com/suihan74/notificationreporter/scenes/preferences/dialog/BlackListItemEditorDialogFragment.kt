package com.suihan74.notificationreporter.scenes.preferences.dialog

import android.app.Dialog
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.BlackListEntity
import com.suihan74.notificationreporter.databinding.FragmentBlackListItemEditorBinding
import com.suihan74.notificationreporter.models.KeywordMatchingType
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.page.BlackListItem
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ブラックリスト項目を編集するダイアログ
 */
class BlackListItemEditorDialogFragment : DialogFragment() {

    companion object {
        fun createInstance(item: BlackListItem, repository: PreferencesRepository) = BlackListItemEditorDialogFragment().also {
            it.lifecycleScope.launchWhenCreated {
                it.viewModel.initialize(item, repository)
            }
        }
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        DialogViewModel()
    }

    // ------ //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentBlackListItemEditorBinding.inflate(layoutInflater, null, false).also {
            it.vm = viewModel
            it.lifecycleOwner = parentFragment?.viewLifecycleOwner ?: requireActivity()
        }
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.prefs_black_list_item_editor_title)
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.complete(this)
            }
            .create()
    }

    // ------ //

    class DialogViewModel : ViewModel() {
        private var prefsRepo : PreferencesRepository? = null

        private var entity : BlackListEntity? = null

        val applicationInfo = MutableLiveData<ApplicationInfo>()

        val keyword = MutableLiveData<String>()

        val keywordMatchingType = MutableLiveData<KeywordMatchingType>()

        // ------ //

        suspend fun initialize(item: BlackListItem, repository: PreferencesRepository) = withContext(Dispatchers.Main) {
            prefsRepo = repository
            entity = item.entity
            applicationInfo.value = item.appInfo
            keyword.value = item.entity.keyword
            keywordMatchingType.value = item.entity.keywordMatchingType
        }

        fun complete(dialog: BlackListItemEditorDialogFragment) = viewModelScope.launch(Dispatchers.Main) {
            try {
                val entity = entity!!.copy(
                    keyword = keyword.value!!,
                    keywordMatchingType = keywordMatchingType.value!!
                )
                prefsRepo!!.updateBlackListEntity(entity)
            }
            catch (e: Throwable) {
                Log.e("BlackListItemEditor", Log.getStackTraceString(e))
            }
            dialog.dismiss()
        }
    }
}
