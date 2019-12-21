package com.angcyo.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.angcyo.library.ex.isDebug
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 */

/**
 * 添加一个TextView,用来提示当前的Activity类
 * */
fun Activity.showDebugInfoView(show: Boolean = true, debug: Boolean = isDebug()) {
    val decorView = window.decorView
    val rootView = window.findViewById<View>(Window.ID_ANDROID_CONTENT)
    val tag = "debug_info_view"
    val debugTextView = decorView.findViewWithTag<View>(tag)

    if (debugTextView != null) {
        (decorView as? ViewGroup)?.removeView(debugTextView)
    }

    if (debug && show) {
        if (decorView is FrameLayout) {
            val textView = TextView(this)
            textView.tag = tag
            textView.textSize = 9f
            textView.setTextColor(Color.WHITE)
            val dp2 = 1 * resources.displayMetrics.density
            val padding = dp2.toInt() * 4
            textView.setPadding(padding, padding, padding, padding)
            textView.setShadowLayer(dp2 * 2, dp2, dp2, Color.BLACK)
            textView.text = javaClass.simpleName
            val layoutParams = FrameLayout.LayoutParams(-2, -2)
            layoutParams.gravity = Gravity.BOTTOM
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                && decorView.getBottom() > rootView.bottom
            ) { //显示了导航栏
                val resources = resources
                val resourceId =
                    resources.getIdentifier("navigation_bar_height", "dimen", "android")
                var navBarHeight = 0
                if (resourceId > 0) {
                    navBarHeight = resources.getDimensionPixelSize(resourceId)
                }
                layoutParams.bottomMargin = navBarHeight
            }
            (decorView as ViewGroup).addView(textView, layoutParams)

            textView.setOnClickListener {

                val displayMetrics = resources.displayMetrics
                val widthDp: Float = displayMetrics.widthPixels / displayMetrics.density
                val heightDp: Float = displayMetrics.heightPixels / displayMetrics.density

                // 屏幕尺寸
                val width = displayMetrics.widthPixels / displayMetrics.xdpi
                //displayMetrics.heightPixels / displayMetrics.ydpi
                val height = decorView.measuredHeight / displayMetrics.ydpi
                val x = width.toDouble().pow(2.0)
                val y = height.toDouble().pow(2.0)
                val screenInches = sqrt(x + y)

                textView.text = buildString {
                    append("wp:").append(displayMetrics.widthPixels)
                    append(" hp:").appendln(displayMetrics.heightPixels)

                    append("dw:").append(decorView.measuredWidth)
                    append(" dh:").appendln(decorView.measuredHeight)

                    append("cw:").append(rootView.measuredWidth)
                    append(" ch:").appendln(rootView.measuredHeight)

                    append("wDp:").append(widthDp)
                    append(" hDp:").append(heightDp)
                    append(" dp:").append(displayMetrics.density)
                    append(" sp:").append(displayMetrics.scaledDensity)
                    append(" dpi:").appendln(displayMetrics.densityDpi)

                    append("w:").append("%.02f".format(width))
                    append(" h:").append("%.02f".format(height))
                    append(" inches:").append("%.02f".format(screenInches))
                }
            }

            textView.setOnLongClickListener {
                textView.text.copy(textView.context)
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                false
            }
        }
    }
}

/**复制文本*/
fun CharSequence.copy(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        clipboard.setPrimaryClip(ClipData.newPlainText("text", this))
    } catch (e: Exception) {
        e.printStackTrace()
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "text",
                this.subSequence(0, 100).toString() + "...more"
            )
        )
    }
}