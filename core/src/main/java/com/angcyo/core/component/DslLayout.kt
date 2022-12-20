package com.angcyo.core.component

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import androidx.annotation.AnyThread
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.view.doOnPreDraw
import com.angcyo.core.R
import com.angcyo.library.L
import com.angcyo.library.component.ThreadExecutor
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.animationOf
import com.angcyo.library.ex.onAnimationEnd
import com.angcyo.library.ex.undefined_int
import com.angcyo.library.ex.undefined_res
import com.angcyo.library.isMain
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder

/**
 * 另一种方式显示[DslToast], 会包含在状态栏下
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/12/20
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object DslLayout {

    /**显示的动画*/
    private fun enterAnimation(config: LayoutConfig): Animation? {
        val style = config.layoutAnimation
        val attrs = intArrayOf(android.R.attr.windowEnterAnimation)
        val typedArray = lastContext.obtainStyledAttributes(style, attrs)
        val enterAnim = typedArray.getResourceId(0, -1)
        typedArray.recycle()
        return animationOf(id = enterAnim)
    }

    private fun exitAnimation(config: LayoutConfig): Animation? {
        val style = config.layoutAnimation
        val attrs = intArrayOf(android.R.attr.windowExitAnimation)
        val typedArray = lastContext.obtainStyledAttributes(style, attrs)
        val exitAnim = typedArray.getResourceId(0, -1)
        typedArray.recycle()
        return animationOf(id = exitAnim)
    }

    private fun container(config: LayoutConfig): ViewGroup? {
        val context = lastContext
        //容器
        var container: ViewGroup? = config.layoutContainer
        if (container == null) {
            if (context is Activity) {
                container = context.window.findViewById(Window.ID_ANDROID_CONTENT)
            }
        }
        return container
    }

    /**开始显示*/
    @AnyThread
    fun show(config: LayoutConfig) {
        if (!isMain()) {
            //回到主线程调用
            ThreadExecutor.onMain { show(config) }
            return
        }

        val context = lastContext
        //容器
        val container: ViewGroup? = container(config)
        if (container == null) {
            L.w("容器为null,取消操作")
            return
        }

        //init
        container.apply {
            val rootView: View
            val oldView = findViewWithTag<View>(config.layoutId)
            val fromOld = oldView != null
            if (oldView == null) {
                rootView = LayoutInflater.from(context).inflate(config.layoutId, this, false)
                rootView.tag = config.layoutId
            } else {
                rootView = oldView
            }
            //render
            val viewHolder = rootView.dslViewHolder()
            config.renderLayoutAction(viewHolder, config)

            //add
            if (rootView.parent == null) {
                addView(rootView)
            }
            config.onLayoutAdd(viewHolder)

            //anim
            if (!fromOld) {
                rootView.doOnPreDraw {
                    enterAnimation(config)?.let { rootView.startAnimation(it) }
                }
            }
            //auto hide
            val tag = R.id.lib_auto_hide_runnable
            val tagRunnable = rootView.getTag(tag)
            if (tagRunnable is Runnable) {
                rootView.removeCallbacks(tagRunnable)
            }
            if (config.autoHideDelay > 0) {
                val autoHideRunnable = Runnable {
                    val exitRunnable = Runnable {
                        try {
                            config.onLayoutRemove(viewHolder)
                            removeView(rootView)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val exitAnimation = exitAnimation(config)
                    if (exitAnimation == null) {
                        exitRunnable.run()
                    } else {
                        exitAnimation.onAnimationEnd {
                            rootView.post(exitRunnable)
                        }
                        rootView.startAnimation(exitAnimation)
                    }
                }
                rootView.setTag(tag, autoHideRunnable)
                rootView.postDelayed(autoHideRunnable, config.autoHideDelay)
            }
        }
    }

    /**隐藏*/
    @AnyThread
    fun hide(config: LayoutConfig) {
        if (!isMain()) {
            //回到主线程调用
            ThreadExecutor.onMain { hide(config) }
            return
        }
        val container: ViewGroup? = container(config)
        if (container == null) {
            L.w("容器为null,取消操作")
            return
        }
        container.apply {
            val oldView = findViewWithTag<View>(config.layoutId)
            if (oldView == null) {
                return
            } else {
                val tag = R.id.lib_auto_hide_runnable
                val tagRunnable = oldView.getTag(tag)
                if (tagRunnable is Runnable) {
                    oldView.removeCallbacks(tagRunnable)
                }

                //remove
                val rootView = oldView
                val viewHolder = rootView.dslViewHolder()
                val exitRunnable = Runnable {
                    try {
                        config.onLayoutRemove(viewHolder)
                        removeView(rootView)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val exitAnimation = exitAnimation(config)
                if (exitAnimation == null) {
                    exitRunnable.run()
                } else {
                    exitAnimation.onAnimationEnd {
                        rootView.post(exitRunnable)
                    }
                    rootView.startAnimation(exitAnimation)
                }
            }
        }
    }
}

/**数据参数配置*/
data class LayoutConfig(
    /**布局id*/
    var layoutId: Int = undefined_int,
    /**容器, 不指定则会使用[Window.ID_ANDROID_CONTENT]*/
    var layoutContainer: ViewGroup? = null,
    /**[windowEnterAnimation]进入动画
     * [windowExitAnimation]退出动画
     * [R.style.LibToastTopAnimation]
     * [R.style.LibToastBottomAnimation]
     * */
    var layoutAnimation: Int = 0, //动画
    /**多少毫秒之后, 自动移除布局. 大于0生效*/
    var autoHideDelay: Long = -1,
    /**回调*/
    var onLayoutAdd: (DslViewHolder) -> Unit = {},
    var onLayoutRemove: (DslViewHolder) -> Unit = {},
    /**渲染回调*/
    var renderLayoutAction: DslViewHolder.(config: LayoutConfig) -> Unit = {}
)

//---dsl---

@AnyThread
fun layoutQQ(
    text: CharSequence?,
    @DrawableRes icon: Int = R.drawable.lib_ic_info,
    @LayoutRes layoutId: Int = R.layout.lib_toast_qq_layout,
    action: LayoutConfig.() -> Unit = {}
) {
    LayoutConfig().apply {
        this.layoutId = layoutId
        this.autoHideDelay = 2000
        this.layoutAnimation = R.style.LibToastTopAnimation //动画
        renderLayoutAction = {
            img(R.id.lib_image_view)?.apply {
                if (icon == undefined_res) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    setImageResource(icon)
                }
            }
            tv(R.id.lib_text_view)?.text = text
        }
        action()
        DslLayout.show(this)
    }
}

@AnyThread
fun layoutWX(
    text: CharSequence?,
    @DrawableRes icon: Int = undefined_res,
    @LayoutRes layoutId: Int = R.layout.lib_toast_wx_layout,
    action: LayoutConfig.() -> Unit = {}
) {
    LayoutConfig().apply {
        this.layoutId = layoutId
        this.autoHideDelay = 2000
        this.layoutAnimation = R.style.LibToastBottomAnimation //动画
        renderLayoutAction = {
            img(R.id.lib_image_view)?.apply {
                if (icon == undefined_res) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    setImageResource(icon)
                }
            }
            tv(R.id.lib_text_view)?.text = text
        }
        action()
        DslLayout.show(this)
    }
}

/**在[Activity]中渲染一个布局*/
fun renderLayout(@LayoutRes layoutId: Int, action: LayoutConfig.() -> Unit = {}) {
    LayoutConfig().apply {
        this.layoutId = layoutId
        action()
        DslLayout.show(this)
    }
}