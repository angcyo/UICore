package com.angcyo.dialog.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.angcyo.dialog.PopupConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.ex.adjustOrder
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW
import com.angcyo.widget.DslViewHolder
import kotlin.math.max

/**
 * 在锚点[View]上弹出一个具有三角形提示的Popup
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/14
 */
open class AnchorPopupConfig : PopupConfig() {

    init {
        animationStyle = R.style.LibActionPopupAnimation
        autoOffset = true
        autoOffsetCenterInAnchor = true
        autoOffsetCenterInScreen = false
        background = ColorDrawable(Color.TRANSPARENT)
        yoff = 4 * dpi
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initLayout(window, viewHolder)
        initTriangleLayout(viewHolder)
    }

    open fun initTriangleLayout(viewHolder: DslViewHolder) {
        val view = anchor
        if (view != null) {
            val rect = view.getViewRect()

            if (isAnchorInTopArea(view)) {
                //目标在屏幕的上半区
                viewHolder.group(R.id.lib_triangle_wrap_layout)
                    ?.adjustOrder(R.id.lib_triangle_view, R.id.lib_triangle_content_layout)
                viewHolder.view(R.id.lib_triangle_view)?.rotation = 180f
            } else {
                //目标在屏幕的下半区

                //调整三角形的顺序和旋转角度
                viewHolder.group(R.id.lib_triangle_wrap_layout)
                    ?.adjustOrder(R.id.lib_triangle_content_layout, R.id.lib_triangle_view)
                viewHolder.view(R.id.lib_triangle_view)?.rotation = 0f
            }

            //设置三角形的位置
            viewHolder.view(R.id.lib_triangle_view)?.apply {
                val w = mW()
                val h = mH()
                val lp = layoutParams
                if (lp is LinearLayout.LayoutParams) {
                    if (isAnchorInLeftArea(view)) {
                        lp.gravity = Gravity.LEFT
                        lp.leftMargin = max(rect.centerX() - rootViewRect.left - w / 2, 0)
                    } else {
                        lp.gravity = Gravity.RIGHT
                        lp.rightMargin = max(rootViewRect.right - rect.centerX() - w / 2, 0)
                    }
                } else if (lp is FrameLayout.LayoutParams) {
                    if (isAnchorInLeftArea(view)) {
                        if (isAnchorInTopArea(view)) {
                            lp.gravity = Gravity.LEFT or Gravity.TOP
                        } else {
                            lp.gravity = Gravity.LEFT or Gravity.BOTTOM
                        }
                        lp.leftMargin = max(rect.centerX() - rootViewRect.left - w / 2, 0)
                    } else {
                        if (isAnchorInTopArea(view)) {
                            lp.gravity = Gravity.RIGHT or Gravity.TOP
                        } else {
                            lp.gravity = Gravity.RIGHT or Gravity.BOTTOM
                        }
                        lp.rightMargin = max(rootViewRect.right - rect.centerX() - w / 2, 0)
                    }
                }
                layoutParams = lp
            }
        }
    }
}

/**Dsl*/
fun Context.anchorPopupWindow(anchor: View?, config: AnchorPopupConfig.() -> Unit): Any {
    val popupConfig = AnchorPopupConfig()
    popupConfig.anchor = anchor

    /*popupConfig.onInitLayout = { window, viewHolder ->
        //初始化布局
    }*/

    popupConfig.config()
    return popupConfig.show(this)
}