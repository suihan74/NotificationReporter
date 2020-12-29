package com.suihan74.notificationreporter.scenes.preferences

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityPreferencesBinding
import com.suihan74.notificationreporter.databinding.ListItemPreferencesMenuBinding
import com.suihan74.utilities.BindingListAdapter
import com.suihan74.utilities.lazyProvideViewModel

/**
 * 設定画面
 */
class PreferencesActivity : AppCompatActivity() {

    val viewModel by lazyProvideViewModel {
        val app = Application.instance
        PreferencesViewModel(app.preferencesRepository)
    }

    private lateinit var binding: ActivityPreferencesBinding

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferencesActivity)

        hideSystemUI()

        binding = DataBindingUtil.setContentView<ActivityPreferencesBinding>(
            this,
            R.layout.activity_preferences
        ).also {
            it.lifecycleOwner = this
        }

        // ページ選択メニュー
        initializeMenu(binding)

        // ページビュー
        binding.contentPager.also { pager ->
            pager.adapter = PageStateAdapter(supportFragmentManager, lifecycle)
            pager.fakeDragBy(0.2f)
        }
    }

    // スクリーン輪郭線・ノッチ輪郭線の描画がウィンドウアタッチ後でないとできないため
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.vm = viewModel
    }

    /** ダイアログなどから復帰時にImmersiveモードを再適用する */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    // ------ //

    /** ページ選択メニューの準備 */
    @Suppress("ClickableViewAccessibility")
    private fun initializeMenu(binding: ActivityPreferencesBinding) {
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
                setOnClickItemListener { binding ->
                    viewModel.selectedMenuItem.value = binding.item
                }

                submitList(MenuItem.values().toList())
            }
        }

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

    fun hideSystemUI() {
        // 全画面表示する
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}
