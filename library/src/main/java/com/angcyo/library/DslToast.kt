package com.angcyo.library

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.angcyo.library.ex.*
import java.lang.ref.WeakReference

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object DslToast {

    var LENGTH_SHORT_TIME = 2000L
    var LENGTH_LONG_TIME = 4000L

    fun show(context: Context = app(), action: ToastConfig.() -> Unit) {
        val config = ToastConfig()
        config.action()

        if (config.withActivity == null) {
            _showWithToast(context, config)
        } else {
            _showWithActivity(config)
        }
    }

    var _toastRef: WeakReference<Toast>? = null

    /**使用[Toast]展示提示信息*/
    fun _showWithToast(context: Context, config: ToastConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            _toastRef?.get()?.cancel()
            _toastRef = null
        }

        if (_toastRef?.get() == null) {
            _toastRef = WeakReference(Toast.makeText(context, "", config.duration).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            })
        }

        _toastRef?.get()?.apply {
            if (config.fullScreen) {
                initFullScreenToast(this, config.fullMargin * 2) {
                    windowAnimations = config.toastAnimation
                }
            }

            duration = config.duration

            setGravity(config.gravity, config.xOffset, config.yOffset)

            if (config.layoutId == undefined_int) {
                //没有自定义的布局
                setText(config.text)
            } else {
                view = _inflateLayout(context, config)
            }

            show()
        }
    }

    fun initFullScreenToast(
        toast: Toast,
        usedWidth: Int,
        action: WindowManager.LayoutParams.() -> Unit = {}
    ) {
        try {
            val mTN = toast::class.java.getDeclaredField("mTN")
            mTN.isAccessible = true
            val mTNObj = mTN.get(toast)

            val mParams = mTNObj.javaClass.getDeclaredField("mParams")
            mParams.isAccessible = true
            val params = mParams.get(mTNObj) as WindowManager.LayoutParams
            params.width = getScreenWidth().coerceAtMost(getScreenHeight()) - usedWidth
            params.height = -2
            //params.gravity = Gravity.TOP//无法生效, 请在Toast对象里面设置
            params.action()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var _viewRef: WeakReference<View>? = null
    var _lastViewTag = mutableListOf<Int>()

    fun _showWithActivity(config: ToastConfig) {

        _viewRef?.get()?.let { rootView ->
            (rootView.parent as? ViewGroup)?.let { viewGroup ->
                when (config.removeLastView) {
                    1 -> {
                        for (tag in _lastViewTag) {
                            val view: View? = viewGroup.findViewWithTag(tag)
                            view?.run { viewGroup.removeView(this) }
                        }
                        _lastViewTag.clear()
                        _viewRef = null
                    }
                    0 -> {
                        val lastTag = _lastViewTag.lastOrNull()

                        if (lastTag != null && lastTag != config.layoutId) {
                            val view: View? = viewGroup.findViewWithTag(lastTag)
                            view?.run { viewGroup.removeView(this) }
                            _lastViewTag.remove(lastTag)
                            _viewRef = null
                        }
                    }
                    else -> {
                        //no op
                    }
                }
            }
        }

        if (config.layoutId == undefined_int) {
            return
        }

        //已经存在相同布局
        val tag = _viewRef?.get()?.tag
        if (tag != null && tag == config.layoutId) {
            _viewRef?.get()?.run {
                _removeRunnable(this)
                _initLayout(this, config)
                _hideTagView(this, config.duration)
            }
            return
        }

        //创建新的布局
        config.withActivity?.apply {
            val contentLayout: FrameLayout? =
                window.findViewById(Window.ID_ANDROID_CONTENT)

            val layout = _inflateLayout(this, config)

            if (config.fullScreen) {
                contentLayout?.addView(layout, FrameLayout.LayoutParams(-1, -2).apply {
                    if (config.yOffset > 0) {
                        topMargin = config.yOffset + getStatusBarHeight()
                    }
                    leftMargin = config.fullMargin
                    rightMargin = config.fullMargin
                    gravity = config.gravity
                })
            } else {
                contentLayout?.addView(layout)
            }

            _viewRef = WeakReference(layout)

            _hideTagView(layout, config.duration)

            //显示view的动画
//            layout.alpha = 0f
//            layout.animate()
//                .alphaBy(1f)
//                .setDuration(300)
//                .start()

            config.windowEnterAnimation()?.run {
                layout.startAnimation(this)
            }
        }
    }

    /**移除[view]*/
    fun _removeView(view: View) {
        (view.parent as? ViewGroup)?.let { viewGroup ->
            viewGroup.removeView(view)
            view.tag?.let {
                if (it is Int) {
                    _lastViewTag.remove(it)
                }
            }
        }
    }

    /**自动隐藏[view]*/
    fun _hideTagView(view: View, duration: Int) {
        _removeRunnable(view)
        val runnable = Runnable {
            //隐藏view的动画
//            view.animate()
//                .alpha(0f)
//                .translationY((-view.measuredHeight).toFloat())
//                .withEndAction {
//                    _removeView(view)
//                    _viewRef = null
//                }
//                .setDuration(300)
//                .start()
            val tagStyle = view.getTag(R.id.lib_tag_animation_style)
            if (tagStyle is Int) {
                tagStyle.windowExitAnimation()?.run {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationRepeat(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            _removeView(view)
                            _viewRef = null
                        }

                        override fun onAnimationStart(animation: Animation?) {
                        }
                    })
                    view.startAnimation(this)
                }.elseNull {
                    _removeView(view)
                    _viewRef = null
                }
            }
        }
        when (duration) {
            Toast.LENGTH_LONG -> {
                view.postDelayed(runnable, LENGTH_LONG_TIME)
            }
            Toast.LENGTH_SHORT -> {
                view.postDelayed(runnable, LENGTH_SHORT_TIME)
            }
            else -> {
                view.postDelayed(runnable, duration.toLong())
            }
        }
        view.setTag(R.id.tag, runnable)
    }

    /**移除[view]的自动隐藏[Runnable]*/
    fun _removeRunnable(view: View) {
        val tag = view.getTag(R.id.tag)
        if (tag is Runnable) {
            view.removeCallbacks(tag)
            view.setTag(R.id.tag, null)
        }
    }

    fun _inflateLayout(context: Context, config: ToastConfig): View {
        val layout = LayoutInflater.from(context)
            .inflate(config.layoutId, FrameLayout(context), false)

        _initLayout(layout, config)

        return layout
    }

    fun _initLayout(layout: View, config: ToastConfig) {
        layout.findViewById<TextView>(R.id.lib_text_view)?.apply {
            if (config.text.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                this.text = config.text
            }
        }
        layout.findViewById<ImageView>(R.id.lib_image_view)?.apply {
            if (config.icon == undefined_res) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                setImageResource(config.icon)
            }
        }

        layout.tag = config.layoutId

        layout.setTag(R.id.lib_tag_animation_style, config.toastAnimation)

        if (!_lastViewTag.contains(config.layoutId)) {
            _lastViewTag.add(config.layoutId)
        }

        config.onBindView(layout)
    }
}

data class ToastConfig(
    var withActivity: Activity? = null, //附着在Activity上, 不用toast展示
    var removeLastView: Int = 0,//1:移除全部 0:移除最后一个不相同的layoutId -1:不移除
    var duration: Int = Toast.LENGTH_SHORT,//非0和1, 在activity模式下可以指定任意隐藏时长(毫秒)
    var text: CharSequence = "",
    @DrawableRes
    var icon: Int = undefined_res,
    var layoutId: Int = undefined_int,
    var gravity: Int = Gravity.CENTER_HORIZONTAL or Gravity.TOP,
    var xOffset: Int = 0,
    var yOffset: Int = getDimen(R.dimen.action_bar_height) + 10 * dpi, //Toast模式下,从状态栏的底部开始算
    var fullScreen: Boolean = true,//全屏模式
    var fullMargin: Int = 20 * dpi,//全屏模式下, 宽度左右的margin

    @StyleRes
    var toastAnimation: Int = R.style.LibToastTopAnimation, //动画
    var onBindView: (rootView: View) -> Unit = {}
)

private fun Int.windowEnterAnimation(context: Context = app()): Animation? {
    val animTypedArray =
        context.obtainStyledAttributes(this, intArrayOf(android.R.attr.windowEnterAnimation))
    val animRes = animTypedArray.getResourceId(0, -1)
    animTypedArray.recycle()

    return if (animRes > 0) {
        AnimationUtils.loadAnimation(context, animRes)
    } else null
}

private fun Int.windowExitAnimation(context: Context = app()): Animation? {
    val animTypedArray =
        context.obtainStyledAttributes(this, intArrayOf(android.R.attr.windowExitAnimation))
    val animRes = animTypedArray.getResourceId(0, -1)
    animTypedArray.recycle()

    return if (animRes > 0) {
        AnimationUtils.loadAnimation(context, animRes)
    } else null
}

fun ToastConfig.windowEnterAnimation(context: Context = app()): Animation? {
    return toastAnimation.windowEnterAnimation(context)
}

fun ToastConfig.windowExitAnimation(context: Context = app()): Animation? {
    return toastAnimation.windowExitAnimation(context)
}

/**全量配置*/
fun toast(action: ToastConfig.() -> Unit) {
    DslToast.show(action = action)
}

/**文本 ico 布局 简化配置*/
fun toast(
    text: CharSequence?,
    @DrawableRes icon: Int = undefined_res,
    @LayoutRes layoutId: Int = R.layout.lib_toast_layout,
    action: ToastConfig.() -> Unit = {}
) {
    DslToast.show {
        this.layoutId = layoutId
        this.text = text ?: ""
        this.icon = icon

        this.action()
    }
}

/**文本 ico QQ布局 简化配置*/
fun toastQQ(
    text: CharSequence?,
    @DrawableRes icon: Int = undefined_res,
    @LayoutRes layoutId: Int = R.layout.lib_toast_qq_layout,
    action: ToastConfig.() -> Unit = {}
) {
    toast(text, icon, layoutId, action)
}

/**文本 ico WX布局 简化配置*/
fun toastWX(
    text: CharSequence?,
    @DrawableRes icon: Int = undefined_res,
    @LayoutRes layoutId: Int = R.layout.lib_toast_wx_layout,
    action: ToastConfig.() -> Unit = {}
) {
    toast(text, icon, layoutId) {
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        fullMargin = 0
        yOffset = 0
        toastAnimation = R.style.LibToastBottomAnimation
        action()
    }
}