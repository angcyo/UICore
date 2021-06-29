package com.angcyo.core.component.accessibility

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.angcyo.acc.R
import com.angcyo.library.app
import com.angcyo.library.ex._color
import com.angcyo.library.ex._string
import com.angcyo.library.ex.dpi
import com.angcyo.widget.span.span

/**
 * 模仿360 滑动找到 辅助工具, 并开启的Toast提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/24 14:46
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityTip {
    private var toast: Toast? = null

    fun show() {
        show(_string(R.string.lib_accessibility_label))
    }

    fun show(tip: String) {
        tip(
            span {
                append("请找到")
                append(tip) {
                    fontSize = 18 * dpi
                    foregroundColor = _color(R.color.colorPrimary)
                }
                append("并开启")
            }
        )
    }

    fun show(tipText: CharSequence, tipImageResId: Int) {
        show(app(), tipText, tipImageResId)
    }

    fun tip(tipText: CharSequence) {
        show(tipText, R.drawable.lib_ic_info)
    }

    fun ok(tipText: CharSequence) {
        show(tipText, R.drawable.lib_ic_succeed)
    }

    private fun show(context: Context, tipText: CharSequence, tipImageResId: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            toast?.cancel()
            toast = null
        }

        val layout = if (toast == null) {
            Toast.makeText(context, "", Toast.LENGTH_LONG).run {
                toast = this
                val layout = LayoutInflater.from(context)
                    .inflate(R.layout.lib_accessibility_toast_tip, FrameLayout(context), false)
                (layout.findViewById<View>(R.id.lib_text_view) as TextView).text = tipText
                view = layout
                setGravity(Gravity.END, 0, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    layout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                layout
            }
        } else {
            toast!!.view
        }

        val titleView = find<TextView>(layout, R.id.lib_text_view)
        val imageView = find<ImageView>(layout, R.id.lib_image_view)

        if (titleView != null) {
            titleView.text = tipText
        }
        imageView?.setImageResource(tipImageResId)
        toast?.show()
    }

    private fun <T> find(view: View?, id: Int): T? {
        return view?.findViewById<View>(id) as? T?
    }
}
