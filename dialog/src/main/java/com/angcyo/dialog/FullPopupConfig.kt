package com.angcyo.dialog

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.view.doOnPreDraw
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.library.ex.getChildOrNull

/**
 * 宽度全屏, 高度撑满锚点布局底部到手机底部
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/02
 */
open class FullPopupConfig : PopupConfig() {

    init {
        width = -1
        height = -1
        animationStyle = R.style.LibFullPopupAnimation
        exactlyHeight = true
        amount = 0.2f
    }

    override fun createContentView(context: Context): View? {
        val rootLayout = FrameLayout(context)
        rootLayout.layoutParams = FrameLayout.LayoutParams(-1, -1)
        if (layoutId != -1) {
            LayoutInflater.from(context).inflate(layoutId, rootLayout, true)
        } else {
            rootLayout.addView(contentView)
        }
        contentView = rootLayout
        return contentView
    }

    override fun initPopupWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {
        super.initPopupWindow(popupWindow, popupViewHolder)
        popupViewHolder.itemView.apply {
            onShowWindow(popupWindow, popupViewHolder)

            if (outsideTouchable) {
                clickIt {
                    popupWindow.dismiss()
                }
            }
        }
        //popupWindow.enterTransition
        //popupWindow.exitTransition
    }

    open fun onShowWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {
        val colorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            Color.parseColor("#00000000"),
            Color.argb((255 * amount).toInt(), 0, 0, 0)
        )
        colorAnimator.addUpdateListener { animation ->
            val color = animation.animatedValue as Int
            popupViewHolder.itemView.setBackgroundColor(color)
        }
        colorAnimator.duration = 300
        colorAnimator.start()

        (contentView as? ViewGroup)?.getChildOrNull(0)?.run {
            doOnPreDraw {
                translationY = (-it.measuredHeight).toFloat()
                animate().translationY(0f).setDuration(300).start()
            }
        }
    }

}