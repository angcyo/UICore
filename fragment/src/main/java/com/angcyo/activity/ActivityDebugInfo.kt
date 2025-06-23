package com.angcyo.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.angcyo.activity.ActivityDebugInfo.DEFAULT_NORMAL_SIZE
import com.angcyo.drawable.isGravityTop
import com.angcyo.fragment.R
import com.angcyo.library.L
import com.angcyo.library._bottomInset
import com.angcyo.library._isNavigationBarShow
import com.angcyo.library._screenCornerRadius
import com.angcyo.library.app
import com.angcyo.library.component.ActivityLifecycleCallbacksAdapter
import com.angcyo.library.component.NetUtils
import com.angcyo.library.component._delay
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

/**调试配置信息*/
data class ActivityDebugInfoConfig(
    /**是否需要显示调试信息*/
    var show: Boolean = true,
    /**调试信息显示的位置*/
    var gravity: Int = Gravity.BOTTOM,
    /**调试view的tag*/
    var tag: String = ActivityDebugInfo.tag,
    /**延迟显示的时长*/
    var delay: Long = Anim.ANIM_DURATION
)

interface IActivityDebugInfo {
    /**配置回调*/
    fun configActivityDebugInfo(config: ActivityDebugInfoConfig)
}

object ActivityDebugInfo {

    var tag = "lib_debug_info_view"

    /**小圆点的大小dp单位*/
    var DEFAULT_NORMAL_SIZE = 20

    /**安装Activity调试信息*/
    fun install(context: Context = app()) {
        val app = context.applicationContext
        if (app is Application) {
            app.registerActivityLifecycleCallbacks(ActivityListener())
        }
    }

    /**监听*/
    private class ActivityListener : ActivityLifecycleCallbacksAdapter() {

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            show(activity)
        }

        /**显示*/
        private fun show(activity: Activity) {
            val config = ActivityDebugInfoConfig()
            if (activity is IActivityDebugInfo) {
                activity.configActivityDebugInfo(config)
            }
            _delay(config.delay) {
                activity.showDebugInfoView(config)
            }
        }
    }
}

/**
 * 添加一个TextView,用来提示当前的Activity类
 * */
fun Activity.showDebugInfoView(config: ActivityDebugInfoConfig) {

    val show = config.show
    val gravity = config.gravity
    val tag = config.tag

    val decorView = window.decorView
    val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT)

    val debugTextView = decorView.findViewWithTag<TextView>(tag)

    if (show) {
        val textView = if (debugTextView == null) {
            val textView = TextView(this)
            textView.tag = tag
            textView.textSize = 9f //sp
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

        val statusBarHeight = getStatusBarHeight()

        //显示正常模式, 小圆点
        fun showNormal(textView: TextView, enable: Boolean = true) {
            textView.layoutParams = textView.layoutParams.apply {
                if (enable) {
                    width =
                        if (_isNavigationBarShow || Build.VERSION.SDK_INT < Build.VERSION_CODES.P || _bottomInset > 0) {
                            //导航栏显示了
                            DEFAULT_NORMAL_SIZE * dpi
                        } else {
                            //导航栏没有显示.
                            if (_screenCornerRadius > 0) {
                                //屏幕圆角大小, 手机屏幕四个角都是圆弧的了
                                //_screenCornerRadius + 8 * dpi //* 2 //(DEFAULT_NORMAL_SIZE + 8) * dpi
                                DEFAULT_NORMAL_SIZE * dpi + _screenCornerRadius / 4
                            } else {
                                DEFAULT_NORMAL_SIZE * dpi
                            }
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
                append(this@showDebugInfoView.simpleHash()).append(" $taskId").appendln()
                (this@showDebugInfoView as? FragmentActivity)?.supportFragmentManager?.logAllFragment(
                    _builder, false, "\\-"
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
                appendLine(packageName)

                //
                activityInfo()?.let {
                    appendLine("/${it.taskAffinity}")
                }

                //
                appendLine(this@showDebugInfoView.javaClass.name)
                (this@showDebugInfoView as? FragmentActivity)?.supportFragmentManager?.logAllFragment(
                    this, true, "\\-"
                )

                NetUtils.localIPAddress?.toString()?.apply {
                    appendLine(this)
                }
                val navBarHeight = max(
                    decorView.measuredWidth - contentView.measuredWidth,
                    decorView.measuredHeight - contentView.measuredHeight
                )
                append("sh:").append(statusBarHeight).append(" ")
                    .append(statusBarHeight / displayMetrics.density)
                append(" nh:").append(navBarHeight).append(" ").append(getNavBarHeight())
                    .append(" ").append(getNavBarHeight() / displayMetrics.density).append(" ")
                    .append(_isNavigationBarShow).appendLine()

                append("wp:").append(displayMetrics.widthPixels)
                append(" hp:").appendLine(displayMetrics.heightPixels)

                append("realW:").append(realMetrics.widthPixels)
                append(" realH:").appendLine(realMetrics.heightPixels)

                append("decorW:").append(decorView.measuredWidth)
                append(" decorH:").appendLine(decorView.measuredHeight)

                append("contentW:").append(contentView.measuredWidth)
                append(" contentH:").appendLine(contentView.measuredHeight)

                append("wDp:").append(widthDp)
                append(" hDp:").append(heightDp)
                append(" dp:").append(displayMetrics.density)
                append(" sp:").append(displayMetrics.scaledDensity)
                append(" dpi:").appendLine(displayMetrics.densityDpi)

                append("w:").append("%.02f".format(width))
                append(" h:").append("%.02f".format(height))
                append(" inches:").appendLine("%.02f".format(screenInches))

                val rect = Rect()
                val point = Point()
                decorView.getGlobalVisibleRect(rect, point)
                append(" d:").append(rect)
                append(" d:").append(point).appendLine()

                contentView.getGlobalVisibleRect(rect, point)
                append(" c:").append(rect)
                append(" c:").append(point)

                decorView.getWindowVisibleDisplayFrame(rect)
                appendLine()
                append(" frame:").append(rect)

                appendLine()
                Device.beautifyDeviceLog(this)
            }
        }

        layoutParams.gravity = gravity
        if (decorView.bottom > contentView.bottom && decorView.measuredHeight > decorView.measuredWidth /*竖屏模式*/) { //显示了导航栏
            val resources = resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            var navBarHeight = 0
            if (resourceId != 0) {
                navBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            layoutParams.bottomMargin = navBarHeight
        }
        if (layoutParams.gravity.isGravityTop()) {
            layoutParams.topMargin = statusBarHeight
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
            //双击隐藏
            showDebugInfoView(ActivityDebugInfoConfig(false))
            false
        }

        if (decorView is FrameLayout) {
            if (this is FragmentActivity) {
                if (decorView.tag is FragmentManager.FragmentLifecycleCallbacks) {

                } else {
                    val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                            super.onFragmentResumed(fm, f)
                            showDebugInfoView(config)
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
    builder: Appendable, fullName: Boolean = false, pre: String? = null
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

        builder.appendLine(name)

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

fun Activity.logActivityInfo(debug: Boolean = isShowDebug()) {
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
                appendLine()
                appendLine("${this@logActivityInfo.simpleHash()} ActivityInfo->↓")
                appendLine(it)
            })
        }
    }
}