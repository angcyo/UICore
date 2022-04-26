package com.angcyo.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.fragment.R
import com.angcyo.http.base.toJson
import com.angcyo.library.L
import com.angcyo.library._isNavigationBarShow
import com.angcyo.library.component.NetUtils
import com.angcyo.library.ex.*
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.getFieldValue
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
fun Activity.showDebugInfoView(
    show: Boolean = true,
    debug: Boolean = isDebug(),
    gravity: Int = Gravity.BOTTOM
) {
    val decorView = window.decorView
    val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT)

    val tag = "debug_info_view"
    val debugTextView = decorView.findViewWithTag<TextView>(tag)

    if (debug && show) {
        val textView = if (debugTextView == null) {
            val textView = TextView(this)
            textView.tag = tag
            textView.textSize = 9f
            textView.setTextColor(Color.WHITE)
            val dp2 = 1 * resources.displayMetrics.density
            val padding = dp2.toInt() * 4
            textView.setPadding(padding, padding, padding, padding)
            textView.setShadowLayer(dp2 * 2, dp2, dp2, Color.BLACK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textView.elevation = 2 * dp
            }

            val layoutParams = FrameLayout.LayoutParams(-2, -2)
            (decorView as ViewGroup).addView(textView, layoutParams)

            textView
        } else {
            debugTextView
        }

        val layoutParams: FrameLayout.LayoutParams =
            textView.layoutParams as FrameLayout.LayoutParams

        //显示正常模式, 小圆点
        fun showNormal(textView: TextView, enable: Boolean = true) {
            textView.layoutParams = textView.layoutParams.apply {
                if (enable) {
                    width =
                        if (_isNavigationBarShow || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            //导航栏显示了
                            18 * dpi
                        } else {
                            //导航栏没有显示, 圆点放大一点.
                            //Android P 之后, 手机屏幕四个角都是圆弧的了
                            24 * dpi
                        }
                    height = width
                } else {
                    width = -2
                    height = -2
                }
            }
            if (enable) {
                textView.text = null
                textView.setBackgroundResource(R.drawable.lib_theme_gradient_circle_solid_shape)
            } else {
                textView.background = null
            }
        }

        //显示简单文本模式
        fun showSingleText(textView: TextView) {
            showNormal(textView, false)
            textView.text = span {
                append(this@showDebugInfoView.simpleHash())
                    .append(" $taskId")
                    .appendln()
                (this@showDebugInfoView as? FragmentActivity)?.supportFragmentManager?.logAllFragment(
                    _builder,
                    false,
                    "\\-"
                )
                _builder.safe()
            }
        }

        //显示详细文本模式
        fun showDetailText(textView: TextView) {
            showNormal(textView, false)

            //real
            val realMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(realMetrics)

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

                NetUtils.localIPAddress?.toString()?.apply {
                    appendln(this)
                }

                val statusBarHeight = getStatusBarHeight()
                val navBarHeight = max(
                    decorView.measuredWidth - contentView.measuredWidth,
                    decorView.measuredHeight - contentView.measuredHeight
                )
                append("sh:").append(statusBarHeight).append(" ")
                    .append(statusBarHeight / displayMetrics.density)
                append(" nh:").append(navBarHeight).append(" ")
                    .append(getNavBarHeight())
                    .append(" ")
                    .append(getNavBarHeight() / displayMetrics.density)
                    .append(" ")
                    .append(_isNavigationBarShow)
                    .appendLine()

                append("wp:").append(displayMetrics.widthPixels)
                append(" hp:").appendln(displayMetrics.heightPixels)

                append("realW:").append(realMetrics.widthPixels)
                append(" realH:").appendln(realMetrics.heightPixels)

                append("decorW:").append(decorView.measuredWidth)
                append(" decorH:").appendln(decorView.measuredHeight)

                append("contentW:").append(contentView.measuredWidth)
                append(" contentH:").appendln(contentView.measuredHeight)

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

                appendln()
                Device.beautifyDeviceLog(this)
            }
        }

        layoutParams.gravity = gravity
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            && decorView.bottom > contentView.bottom
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
        textView.layoutParams = layoutParams

        //默认显示正常模式
        showNormal(textView)

        textView.setOnClickListener {
            if (it.isSelected) {
                //到达最大模式后
                showNormal(textView)
                it.isSelected = false
            } else {
                showSingleText(textView)
                it.isSelected = true
            }
        }

        textView.setOnLongClickListener {
            textView.text.copy(textView.context)
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            showDetailText(textView)
            it.isSelected = true
            true
        }

        textView.onDoubleTap {
            showDebugInfoView(false, debug, gravity)
            false
        }

        if (decorView is FrameLayout) {
            if (this is FragmentActivity) {
                if (decorView.tag is FragmentManager.FragmentLifecycleCallbacks) {

                } else {
                    val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                            super.onFragmentResumed(fm, f)
                            showDebugInfoView(show, debug, gravity)
                        }
                    }
                    supportFragmentManager.registerFragmentLifecycleCallbacks(callback, true)
                    decorView.tag = callback
                }
            }
        }
    } else {
        //移除已经存在TextView
        if (debugTextView != null) {
            (decorView as? ViewGroup)?.removeView(debugTextView)
        }
        if (this is FragmentActivity) {
            if (decorView.tag is FragmentManager.FragmentLifecycleCallbacks) {
                supportFragmentManager.unregisterFragmentLifecycleCallbacks(decorView.tag as FragmentManager.FragmentLifecycleCallbacks)
                decorView.tag = null
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

fun Activity.activityInfo(): ActivityInfo? =
    getFieldValue(Activity::class.java, "mActivityInfo") as? ActivityInfo

fun Activity.logActivityInfo(debug: Boolean = isDebug()) {
    if (debug) {
        //系统Fragment操作日志输出
        //FragmentManager.enableDebugLogging(BuildConfig.DEBUG);
        val className = this.javaClass.simpleName
        val parentActivityIntent = parentActivityIntent
        val supportParentActivityIntent: Intent? = if (this is AppCompatActivity) {
            supportParentActivityIntent
        } else {
            null
        }

        val mActivityInfo = activityInfo()

        L.v("$className parentActivityIntent:$parentActivityIntent")
        L.v("$className supportParentActivityIntent:$supportParentActivityIntent")

        mActivityInfo?.also {
            L.v(buildString {
                append(className)
                append(" taskId:$taskId")
                append(" root:$isTaskRoot")
                append(" taskAffinity:${mActivityInfo.taskAffinity}")
            })

            L.d(buildString {
                appendln()
                appendln("${this@logActivityInfo.simpleHash()} ActivityInfo->↓")
                appendln(it.toJson())
            })
        }
    }
}