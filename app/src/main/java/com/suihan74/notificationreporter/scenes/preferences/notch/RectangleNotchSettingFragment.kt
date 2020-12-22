package com.suihan74.notificationreporter.scenes.preferences.notch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.databinding.FragmentRectangleNotchSettingBinding
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel

class RectangleNotchSettingFragment : Fragment() {
    companion object {
        fun createInstance(settingKey: String) = RectangleNotchSettingFragment().withArguments {
            putString(ARG_SETTING_KEY, settingKey)
        }

        private const val ARG_SETTING_KEY = "ARG_SETTING_KEY"
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val settingKey = requireArguments().getString(ARG_SETTING_KEY)!!
        val app = Application.instance
        RectangleNotchSettingViewModel(app.preferencesRepository, settingKey)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRectangleNotchSettingBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.widthAdjustmentSlider.addOnChangeListener(viewModel.widthAdjustmentChangeListener)

        binding.heightAdjustmentSlider.addOnChangeListener(viewModel.heightAdjustmentChangeListener)

        binding.leftTopRadiusSlider.addOnChangeListener(viewModel.leftTopRadiusChangeListener)

        binding.rightTopRadiusSlider.addOnChangeListener(viewModel.rightTopRadiusChangeListener)

        binding.leftBottomRadiusSlider.addOnChangeListener(viewModel.leftBottomRadiusChangeListener)

        binding.rightBottomRadiusSlider.addOnChangeListener(viewModel.rightBottomRadiusChangeListener)

        return binding.root
    }
}
