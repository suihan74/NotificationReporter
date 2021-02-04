package com.suihan74.nedge.scenes.preferences

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.databinding.ActivityPreferencesBinding
import com.suihan74.nedge.databinding.ListHeaderPreferencesMenuBinding
import com.suihan74.nedge.databinding.ListItemPreferencesMenuBinding
import com.suihan74.nedge.scenes.preferences.page.SettingEditorFragment
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.extensions.hideSoftInputMethod
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.launch

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    val viewModel by lazyProvideViewModel {
        PreferencesViewModel(Application.instance)
    }

    private lateinit var binding: ActivityPreferencesBinding

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreferencesBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
        }
        setContentView(binding.root)

        // ページ選択メニュー
        initializeMenu(binding)

        // ページビュー
        binding.contentPager.also { pager ->
            pager.adapter = PageStateAdapter(supportFragmentManager, lifecycle)
            pager.fakeDragBy(0.2f)
        }

        // 広告を初期化
        initializeAdView(binding)
    }

    // スクリーン輪郭線・ノッチ輪郭線の描画がウィンドウアタッチ後でないとできないため
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleScope.launch {
            viewModel.onAttachedToWindow(this@PreferencesActivity, window)
        }
        binding.vm = viewModel
    }

    // ------ //

    /** ページ選択メニューの準備 */
    @Suppress("ClickableViewAccessibility")
    private fun initializeMenu(binding: ActivityPreferencesBinding) {
        // メニューリスト初期化
        val list = binding.menuRecyclerView.also { list ->
            val adapter = BindingListAdapter<MenuItem, ListItemPreferencesMenuBinding>(
                R.layout.list_item_preferences_menu,
                this,
                MenuItem.DiffCallback(),
            ) { binding, item ->
                binding.item = item
                binding.selectedItem = viewModel.selectedMenuItem
            }

            list.adapter = adapter.apply {
                setOnClickItemListener {
                    viewModel.selectedMenuItem.value = it.item
                    binding.motionLayout.setTransition(R.id.close)
                    binding.motionLayout.transitionToEnd()
                }

                submit(
                    items = MenuItem.values().toList(),
                    header = { parent ->
                        ListHeaderPreferencesMenuBinding.inflate(layoutInflater, parent, false).root.also {
                            it.setOnClickListener {}
                            it.setOnLongClickListener { false }
                        }
                    }
                )
            }
        }

        // メニュー操作時に仮想キーボードを閉じる
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                currentFocus?.hideSoftInputMethod(binding.mainLayout)
            }
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {}
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })

        // `MotionLayout`にタッチイベントを伝播させる
        // リスト、各項目のタッチイベント処理で伝播が止まってしまうので、
        // その前に明示的に`MotionLayout`にもイベントを送り付けるようにしている

        list.setOnTouchListener { _, motionEvent ->
            binding.motionLayout.onTouchEvent(motionEvent)
            return@setOnTouchListener false
        }

        list.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                binding.motionLayout.onTouchEvent(e)
                return false
            }
        })
    }

    // ------ //

    /** 通知設定編集画面を開く */
    fun openSettingEditor(entity: NotificationEntity) {
        val fragment = SettingEditorFragment.createInstance(entity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingEditorFrame, fragment)
            .addToBackStack("settingEditorFrame")
            .commit()
    }

    /** 通知設定編集画面を閉じる */
    fun closeSettingEditor() {
        supportFragmentManager.popBackStackImmediate()
    }

    // ------ //

    /** アダプティブバナー広告のサイズ */
    private val adSize : AdSize get() {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density
        val adWidthPx = binding.adViewContainer.width.toFloat().let {
            if (it == 0f) outMetrics.widthPixels.toFloat()
            else it
        }

        val adWidth = (adWidthPx / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    /** 広告を初期化 */
    private fun initializeAdView(binding: ActivityPreferencesBinding) {
        MobileAds.initialize(this) {}

        val adView = AdView(this)
        binding.adViewContainer.addView(adView)
        loadBannerAd(adView)
    }

    private fun loadBannerAd(adView: AdView) {
        adView.adUnitId = getString(R.string.admob_unit_id)
        adView.adSize = adSize
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
