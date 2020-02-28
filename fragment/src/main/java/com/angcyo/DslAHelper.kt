package com.angcyo

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.*
import android.view.View
import android.view.Window
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.angcyo.activity.FragmentWrapActivity
import com.angcyo.activity.JumpActivity
import com.angcyo.base.RevertWindowTransitionListener
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslAHelper(val context: Context) {

    /**需要启动的[Intent]*/
    val startIntentConfig = mutableListOf<IntentConfig>()

    /**关闭自身*/
    var finishSelf: Boolean = false

    /**是否使用[Finish]方法关闭[Activity], 默认使用[onBackPressed]*/
    var finishWithFinish: Boolean = false

    //<editor-fold desc="start操作">

    fun start(intent: Intent, action: IntentConfig.() -> Unit = {}) {
        val config = IntentConfig(intent)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        config.action()
        if (context is Activity) {
            config.configWindow(context.window)
        }

        //跳板Activity
        if (config.useJumpActivity) {
            val jumpIntent = Intent(config.intent)
            jumpIntent.component = ComponentName(context, JumpActivity::class.java)
            jumpIntent.putExtra(JumpActivity.KEY_JUMP_TARGET, intent)
            jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            jumpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            config.intent = jumpIntent
        }

        startIntentConfig.add(config)
    }

    fun start(aClass: Class<*>, action: IntentConfig.() -> Unit = {}) {
        val intent = Intent(context, aClass)
        start(intent, action)
    }

    /**使用[FragmentWrapActivity]包裹启动[Fragment]*/
    fun start(
        fragment: Class<out Fragment>,
        singTask: Boolean = true,
        action: IntentConfig.() -> Unit = {}
    ) {
        start(FragmentWrapActivity.getIntent(context, fragment, singTask), action)
    }

    //</editor-fold desc="start操作">

    fun doIt() {
        startIntentConfig.forEach {
            try {

                //op
                if (context !is Activity) {
                    it.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                //共享元素配置
                var transitionOptions: Bundle? = null
                if (enableWindowTransition || it.sharedElementList.isNotEmpty()) {
                    _supportTransition {
                        transitionOptions = if (it.sharedElementList.isNotEmpty()) {
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (context as Activity),
                                *it.sharedElementList.toTypedArray()
                            ).toBundle()
                        } else {
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity)
                                .toBundle()
                        }

                        //https://developer.android.com/training/transitions/start-activity#custom-trans
                        context.window.apply {
                            //requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                            //requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                        }
                    }
                }

                //ForResult
                if (it.requestCode != -1 && context is Activity) {
                    ActivityCompat.startActivityForResult(
                        context,
                        it.intent,
                        it.requestCode,
                        transitionOptions
                    )
                } else {
                    ActivityCompat.startActivity(
                        context,
                        it.intent,
                        transitionOptions
                    )
                }

                //anim
                if (context is Activity) {
                    if (it.enterAnim != -1 || it.exitAnim != -1) {
                        context.overridePendingTransition(it.enterAnim, it.exitAnim)
                    }
                }
            } catch (e: Exception) {
                L.e("启动Activity失败:$e")
            }
        }

        if (finishSelf && context is Activity) {
            if (finishWithFinish) {
                context.finish()
            } else {
                context.onBackPressed()
            }
        }
    }

    //<editor-fold desc="finish 操作">

    /**关闭当前[context]*/
    fun finish(withBackPress: Boolean = false, action: IntentConfig.() -> Unit = {}) {
        if (context is Activity) {
            val config = IntentConfig(Intent())
            config.action()

            context.setResult(config.resultCode, config.resultData)
            config.configWindow(context.window)

            if (withBackPress) {
                context.onBackPressed()
            } else {
                context.finish()
            }
            if (config.enterAnim != -1 || config.exitAnim != -1) {
                context.overridePendingTransition(config.enterAnim, config.exitAnim)
            }
        } else {
            L.e("context 必须是 Activity, 才能执行 finish()")
        }
    }

    //</editor-fold desc="finish 操作">

    //<editor-fold desc="转场动画配置">

    //https://developer.android.com/training/transitions/start-activity#custom-trans

    /**
     * 转场动画支持.
     * 步骤1: 获取共享元素属性值
     * 步骤2: 传递属性
     * 步骤3: 播放动画
     */

    //是否支持转场动画
    fun _supportTransition(action: () -> Unit) {
        if (context is Activity &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        ) {
            action()
        }
    }

    /**默认的共享元素转场动画*/
    fun _defaultElementTransition(): TransitionSet {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(ChangeBounds())
        _supportTransition {
            transitionSet.addTransition(ChangeTransform())
            transitionSet.addTransition(ChangeClipBounds())
            transitionSet.addTransition(ChangeImageTransform())
            //transitionSet.addTransition(ChangeScroll()) //图片过渡效果, 请勿设置此项
        }
        return transitionSet
    }

    /**排除目标*/
    fun _excludeTarget(transition: Transition, config: IntentConfig, exclude: Boolean = true) {
        config.sharedElementList.forEach {
            transition.excludeTarget(it.first, exclude)
        }
    }

    fun _excludeDecor(transition: Transition, exclude: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition.excludeTarget(android.R.id.navigationBarBackground, exclude)
            transition.excludeTarget(android.R.id.statusBarBackground, exclude)
        }
    }

    /**添加目标*/
    fun _addTarget(transition: Transition, config: IntentConfig) {
        config.sharedElementList.forEach {
            transition.addTarget(it.first)
        }
    }

    //</editor-fold desc="转场动画配置">

    //<editor-fold desc="启动转场动画配置">

    /**开启转场动画*/
    var enableWindowTransition: Boolean = false

    //可以单独设置窗口的过渡动画(非共享元素的动画), 而无需设置共享元素
    var windowExitTransition: Transition? = Fade(Fade.OUT)
        set(value) {
            field = value
            enableWindowTransition = true
        }
    var windowEnterTransition: Transition? = Fade(Fade.IN)
        set(value) {
            field = value
            enableWindowTransition = true
        }

    var elementEnterTransition: Transition? = _defaultElementTransition()
        set(value) {
            field = value
            enableWindowTransition = true
        }
    var elementExitTransition: Transition? = _defaultElementTransition()
        set(value) {
            field = value
            enableWindowTransition = true
        }

    /**启动[Activity]后, 调用此方法开始转场动画, 必须在[Activity]的[onCreate]中调用*/
    fun transition(action: IntentConfig.() -> Unit = {}) {

        _supportTransition {
            if (context is Activity) {
                context.window.apply {
                    //requestFeature() must be called before adding content
                    //requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                    //requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

                    //https://www.jianshu.com/p/4d23b8a37a5d
                    //setExitTransition()     //当A start B时，使A中的View退出场景的transition
                    //setEnterTransition()   //当A start B时，使B中的View进入场景的transition
                    //setReturnTransition()  //当B 返回 A时，使B中的View退出场景的transition
                    //setReenterTransition() //当B 返回 A时，使A中的View进入场景的transition

                    val revertWindowTransitionListener =
                        RevertWindowTransitionListener(this, this.decorView.background)

                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    windowEnterTransition?.run {
                        enterTransition = this
                        reenterTransition = this
                        //addListener(revertWindowTransitionListener)
                    }

                    windowExitTransition?.run {
                        exitTransition = this
                        returnTransition = this
                        //addListener(revertWindowTransitionListener)
                    }

                    elementEnterTransition?.run {
                        sharedElementEnterTransition = this
                        sharedElementReenterTransition = this
                        addListener(revertWindowTransitionListener)
                    }

                    elementExitTransition?.run {
                        sharedElementExitTransition = this
                        sharedElementReturnTransition = this
                        addListener(revertWindowTransitionListener)
                    }


                    sharedElementsUseOverlay = true

                    //allowEnterTransitionOverlap = true
                    //allowReturnTransitionOverlap = true
                }
            }
        }

        val config = IntentConfig(Intent())
        config.action()

        if (context is Activity) {
            config.configWindow(context.window)

            _supportTransition {
                if (config.excludeDecor) {
                    windowEnterTransition?.run { _excludeDecor(this) }
                    windowExitTransition?.run { _excludeDecor(this) }
                }

//                if (config.excludeTarget) {
//                    elementEnterTransition?.run { _addTarget(this, config) }
//                    elementExitTransition?.run { _addTarget(this, config) }
//                }
            }
        }

        for (pair in config.sharedElementList) {
            pair.first?.run { ViewCompat.setTransitionName(this, pair.second) }
        }
    }

    //</editor-fold desc="启动转场动画配置">
}

data class IntentConfig(
    var intent: Intent,

    //-1 默认动画, 0无动画
    var enterAnim: Int = -1,
    var exitAnim: Int = -1,

    //ForResult
    var requestCode: Int = -1,

    //Result 只在 finish 操作有效
    var resultCode: Int = Activity.RESULT_CANCELED,
    var resultData: Intent? = null,

    //转场动画

    //window过得动画, 不需要包括状态栏和导航栏
    var excludeDecor: Boolean = true,

    //共享元素
    val sharedElementList: MutableList<Pair<View, String>> = mutableListOf(),

    //Window配置
    var configWindow: (Window) -> Unit = {},

    //是否使用跳板[JumpActivity]
    var useJumpActivity: Boolean = false
)

/**去掉系统默认的动画*/
fun IntentConfig.noAnim() {
    enterAnim = 0
    exitAnim = 0
}

/**传递Json数据*/
fun IntentConfig.putData(data: Any?, key: String = BUNDLE_KEY_JSON) {
    intent.putData(data, key)
}

/**设置共享元素[View], 和对应的[Key]*/
fun IntentConfig.transition(sharedElement: View?, sharedElementName: String? = null) {
    sharedElement?.run {
        sharedElementList.add(
            Pair(
                this,
                sharedElementName
                    ?: ViewCompat.getTransitionName(this)
                    ?: this.javaClass.name
            )
        )
    }
}