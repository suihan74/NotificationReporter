package com.suihan74.notificationreporter.scenes.lockScreen

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.ActivityLockScreenBinding
import com.suihan74.utilities.extensions.alsoAs
import com.suihan74.utilities.extensions.dp
import com.suihan74.utilities.lazyProvideViewModel


class LockScreenActivity : AppCompatActivity() {
    private val viewModel by lazyProvideViewModel {
        val app = Application.instance
        LockScreenViewModel(
                batteryRepo = app.batteryRepository,
                notificationRepo = app.notificationRepository,
                prefRepo = app.preferencesRepository
        )
    }

    // ------ //

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlapLockScreenAndKeepScreenOn()

        val binding = DataBindingUtil.setContentView<ActivityLockScreenBinding>(
                this,
                R.layout.activity_lock_screen
        ).also {
            it.vm = viewModel
            it.lifecycleOwner = this
        }

        // 画面上部にスワイプして画面を終了する
        binding.motionLayout.also {
            it.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (currentId == R.id.end) {
                        finish()
                    }
                }

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                }

                override fun onTransitionTrigger(
                        p0: MotionLayout?,
                        p1: Int,
                        p2: Boolean,
                        p3: Float
                ) {
                }
            })
        }

        // バックライトを最低レベルにする
        viewModel.lightOff.observe(this, { lightOff ->
            window.attributes = window.attributes.also { lp ->
                lp.screenBrightness =
                        if (lightOff) 0.01f  // 1/256以下の値にするとバグる機種がある
                        else -1.0f  // システムの値
            }
        })

        ContextCompat.getDrawable(this, R.drawable.shape_edge_light).alsoAs<GradientDrawable> { edgeShape ->
            edgeShape.setStroke(3.dp.toInt(), viewModel.notificationBarColor.value ?: Color.GREEN)
            edgeShape.cornerRadius = viewModel.screenCornerRadius.value ?: 0f
            binding.notificationBar.setImageDrawable(edgeShape)
        }
    }

    /**
     * 常にフルスクリーンでロック画面より上に表示し，画面を完全に消灯しない
     */
    private fun overlapLockScreenAndKeepScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        else {
            @Suppress("deprecation")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        window.decorView.let { decorView ->
            val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            decorView.systemUiVisibility = flags

            decorView.setOnSystemUiVisibilityChangeListener {
                decorView.systemUiVisibility = flags
            }
        }
    }

    /** 戻るボタンを無効化する */
    override fun onBackPressed() {
        // do nothing
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun finish() {
        super.finish()
        Application.instance.notificationRepository.clearNotifications()
    }
}
