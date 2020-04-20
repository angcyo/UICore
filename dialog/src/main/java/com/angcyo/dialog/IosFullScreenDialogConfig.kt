package com.angcyo.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.KeyEvent.KEYCODE_BACK
import android.view.Window
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.base.translucentStatusBar
import com.angcyo.behavior.ScrollBehaviorListener
import com.angcyo.behavior.effect.TouchBackBehavior
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dsladapter.filter.batchLoad
import com.angcyo.library._screenHeight
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.evaluateColor
import com.angcyo.library.ex.toColorInt
import com.angcyo.tablayout.clamp
import com.angcyo.widget.ActivityScreenshotImageView
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.doAnimate
import com.angcyo.widget.base.mH
import com.angcyo.widget.recycler.initDslAdapter

/**
 * 模拟IOS全屏样式对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class IosFullScreenDialogConfig : BaseDialogConfig() {

    /**变暗的颜色*/
    var amountColor: Int = "#33000000".toColorInt()

    init {
        animStyleResId = R.style.LibNoAnimation
        dialogLayoutId = R.layout.lib_dialog_ios_full_screen_layout

        //navigationBarColor = Color.TRANSPARENT
        //navigationBarDividerColor = Color.TRANSPARENT
        //statusBarColor = Color.TRANSPARENT

        //dialogGravity = Gravity.TOP

        //取消变暗
        amount = 0f

        //全屏的三金刚属性
        dialogWidth = -1
        //很关键的一点, 高度一定要撑满全屏. 撑满之后, 如果导航栏显示了, 内部View布局会有点BUG, 顶部偏移有问题.
        dialogHeight = -1 //getRootHeight()
        setDialogBgColor(Color.TRANSPARENT)
    }

    override fun configWindow(window: Window) {
        super.configWindow(window)
        //开启布局全屏, 体验更佳
        window.enableLayoutFullScreen(true)
        window.translucentStatusBar(true)
//        window.translucentNavigationBar(true)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        var touchBackBehavior: TouchBackBehavior? = null

        dialogViewHolder.view(R.id.touch_back_layout).behavior()?.apply {
            if (this is TouchBackBehavior) {
                touchBackBehavior = this
                this.addScrollListener(object : ScrollBehaviorListener {
                    override fun onBehaviorScrollTo(x: Int, y: Int) {
                        //L.i("-> x:$x y:$y")
                        onTouchBackTo(dialogViewHolder, y)

                        //关闭对话框
                        if (y >= childView.mH()) {
                            dialog.cancel()
                        }
                    }
                })
            }
        }

        fun back() {
            touchBackBehavior?.scrollToClose() ?: dialog.cancel()
        }

        negativeButtonListener = { dialog, dialogViewHolder ->
            back()
        }

        positiveButtonListener = { dialog, dialogViewHolder ->
            back()
        }

        //拦截返回按钮
        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KEYCODE_BACK) {
                back()
                true
            } else {
                false
            }
        }

        //进入时的动画
        dialogViewHolder.itemView.doAnimate {
            touchBackBehavior?.apply {
                scrollTo(0, touchBackBehavior?.childView.mH() - 1)
                scrollDuration = 600
                scrollToNormal()
            }
        }

        //对话框内容
        dialogViewHolder.rv(R.id.lib_recycler_view)?.initDslAdapter {
            batchLoad()
            for (i in 0..30) {
                DslDialogTextItem()() {
                    itemHeight = 100 * dpi
                    itemText = "测试文本 $i"
                }
            }
        }
    }

    private fun onTouchBackTo(holder: DslViewHolder, y: Int) {
        holder.v<ActivityScreenshotImageView>(R.id.activity_screenshot_view)?.apply {
            var zoomScale: Float = 1 - (_screenHeight - y) * 0.00002f
            if (zoomScale > 1) zoomScale = 1f

            scaleX = zoomScale
            scaleY = zoomScale

            val factor = clamp((_screenHeight - y * 1f) / _screenHeight, 0f, 1f)
            radius = 15 * dpi * factor

            //颜色过渡提示
            holder.view(R.id.color_view)
                ?.setBackgroundColor(evaluateColor(factor, Color.TRANSPARENT, amountColor))
        }
    }
}