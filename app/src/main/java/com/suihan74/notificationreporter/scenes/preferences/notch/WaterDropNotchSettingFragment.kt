package com.suihan74.notificationreporter.scenes.preferences.notch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.databinding.FragmentWaterDropNotchSettingBinding
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorFragment
import com.suihan74.utilities.extensions.getEnum
import com.suihan74.utilities.extensions.putEnum
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel

class WaterDropNotchSettingFragment : Fragment() {
    companion object {
        fun createInstance(notchPosition: NotchPosition) = WaterDropNotchSettingFragment().withArguments {
            putEnum(Arg.NOTCH_POSITION.name, notchPosition)
        }

        enum class Arg {
            NOTCH_POSITION
        }
    }

    // ------ //

    private val settingEditorFragment
        get() = requireParentFragment() as SettingEditorFragment

    private val settingEditorViewModel
        get() = settingEditorFragment.viewModel

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val notchPosition = requireArguments().let {
            it.getEnum<NotchPosition>(RectangleNotchSettingFragment.Companion.Arg.NOTCH_POSITION.name)!!
        }
        WaterDropNotchSettingViewModel(notchPosition, settingEditorViewModel)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWaterDropNotchSettingBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }
}
