package com.angcyo.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.fragment.R
import com.angcyo.library.app
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.safe
import com.angcyo.library.ex.simpleName
import com.angcyo.library.getStatusBarHeight
import com.angcyo.widget.base.onDoubleTap
import com.angcyo.widget.span.span
import kotlin.math.max
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
    val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT)

    val tag = "debug_info_view"
    val debugTextView = decorView.findViewWithTag<View>(tag)

    if (debugTextView != null) {
        (decorView as? ViewGroup)?.removeView(debugTextView)
    }

    if (debug && show) {
        if (decorView is FrameLayout) {

            if (this is FragmentActivity) {
                if (decorView.tag is FragmentManager.FragmentLifecycleCallbacks) {

                } else {
                    val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                            super.onFragmentResumed(fm, f)
                            showDebugInfoView(show, debug)
                        }
                    }
                    supportFragmentManager.registerFragmentLifecycleCallbacks(callback, true)
                    decorView.tag = callback
                }
            }

            val textView = TextView(this)
            textView.tag = tag
            textView.textSize = 9f
            textView.setTextColor(Color.WHITE)
            val dp2 = 1 * resources.displayMetrics.density
            val padding = dp2.toInt() * 4
            textView.setPadding(padding, padding, padding, padding)
            textView.setShadowLayer(dp2 * 2, dp2, dp2, Color.BLACK)

            textView.text = span {
                append(this@showDebugInfoView.simpleName()).appendln()
                (this@showDebugInfoView as? FragmentActivity)?.supportFragmentManager?.logAllFragment(
                    _builder,
                    false,
                    "\\-"
                )
                _builder.safe()
            }

            val layoutParams = FrameLayout.LayoutParams(-2, -2)
            layoutParams.gravity = Gravity.BOTTOM
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                && decorView.getBottom() > contentView.bottom
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
                    appendln(this@showDebugInfoView.javaClass.name)
                    (this@showDebugInfoView as? FragmentActivity)?.supportFragmentManager?.logAllFragment(
                        this,
                        true,
                        "\\-"
                    )

                    val statusBarHeight = getStatusBarHeight()
                    val navBarHeight = max(
                        decorView.measuredWidth - contentView.measuredWidth,
                        decorView.measuredHeight - contentView.measuredHeight
                    )
                    append("sh:").append(statusBarHeight).append(" ")
                        .append(statusBarHeight / displayMetrics.density)
                    append(" nh:").append(navBarHeight).append(" ")
                        .appendln(navBarHeight / displayMetrics.density)

                    append("wp:").append(displayMetrics.widthPixels)
                    append(" hp:").appendln(displayMetrics.heightPixels)

                    append("dw:").append(decorView.measuredWidth)
                    append(" dh:").appendln(decorView.measuredHeight)

                    append("cw:").append(contentView.measuredWidth)
                    append(" ch:").appendln(contentView.measuredHeight)

                    append("wDp:").append(widthDp)
                    append(" hDp:").append(heightDp)
                    append(" dp:").append(displayMetrics.density)
                    append(" sp:").append(displayMetrics.scaledDensity)
                    append(" dpi:").appendln(displayMetrics.densityDpi)

                    append("w:").append("%.02f".format(width))
                    append(" h:").append("%.02f".format(height))
                    append(" inches:").appendln("%.02f".format(screenInches))

                    val rect = Rect()
                    val point = Point()
                    decorView.getGlobalVisibleRect(rect, point)
                    append(" d:").append(rect)
                    append(" d:").append(point).appendln()

                    contentView.getGlobalVisibleRect(rect, point)
                    append(" c:").append(rect)
                    append(" c:").append(point)

                    decorView.getWindowVisibleDisplayFrame(rect)
                    appendln()
                    append(" frame:").append(rect)
                }
            }

            textView.setOnLongClickListener {
                textView.text.copy(textView.context)
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                false
            }

            textView.onDoubleTap {
                showDebugInfoView(false)
                false
            }
        }
    }
}

/**
 * 打印[FragmentManager]中所有的[Fragment],
 * 打印状态[isResumed]的[Fragment]中[childFragmentManager]
 *  */
fun FragmentManager.logAllFragment(
    builder: Appendable,
    fullName: Boolean = false,
    pre: String? = null
): Appendable {
    fragments.forEachIndexed { index, fragment ->

        val start = (builder as? SpannableStringBuilder)?.length ?: 0

        pre?.run { builder.append(this) }
        builder.append("$index ")

        var name = if (fullName) {
            fragment.javaClass.name
        } else {
            fragment.javaClass.simpleName
        }

        if (fragment.isResumed) {
            name = "$name √"
        }

        builder.appendln(name)

        if (fragment.isResumed) {
            if (builder is SpannableStringBuilder) {
                val end = (builder as? SpannableStringBuilder)?.length ?: 0
                builder.setSpan(
                    ForegroundColorSpan(getColor(R.color.colorAccent)),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            fragment.childFragmentManager.apply {
                logAllFragment(builder, fullName, pre?.run { "    $this" })
            }
        }
    }
    return builder
}

/**复制文本*/
fun CharSequence.copy(context: Context = app()) {
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