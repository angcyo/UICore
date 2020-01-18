package com.angcyo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
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

    //<editor-fold desc="start操作">

    fun start(intent: Intent, action: IntentConfig.() -> Unit = {}) {
        val config = IntentConfig(intent)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        config.action()
        startIntentConfig.add(config)
    }

    fun start(aClass: Class<*>, action: IntentConfig.() -> Unit = {}) {
        val intent = Intent(context, aClass)
        start(intent, action)
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
                if (windowEnterTransition != null ||
                    windowExitTransition != null ||
                    it.sharedElementList.isNotEmpty()
                ) {
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
            context.onBackPressed()
        }
    }

    //<editor-fold desc="finish 操作">

    /**关闭当前[context]*/
    open fun finish(withBackPress: Boolean = false, action: IntentConfig.() -> Unit = {}) {
        if (context is Activity) {
            val config = IntentConfig(Intent())
            context.setResult(config.resultCode, config.resultData)

            config.action()

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

    //</editor-fold desc="转场动画配置">

    //<editor-fold desc="启动转场动画配置">

    //可以单独设置窗口的过渡动画, 而无需设置共享元素
    var windowExitTransition: Transition? = Fade()
    var windowEnterTransition: Transition? = Fade()

    var elementEnterTransition: Transition? = null
    var elementExitTransition: Transition? = null

    /**启动[Activity]后, 调用此方法开始转场动画*/
    fun transition(action: IntentConfig.() -> Unit = {}) {

        _supportTransition {
            if (context is Activity) {
                context.window.apply {
                    //requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                    //requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

                    enterTransition = windowEnterTransition
                    exitTransition = windowExitTransition

                    sharedElementEnterTransition = elementEnterTransition
                    sharedElementExitTransition = elementExitTransition
                }
            }
        }

        val config = IntentConfig(Intent())

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

    //共享元素
    val sharedElementList: MutableList<Pair<View, String>> = mutableListOf()
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
fun IntentConfig.transition(sharedElement: View, sharedElementName: String? = null) {
    sharedElementList.add(
        Pair(
            sharedElement,
            sharedElementName
                ?: ViewCompat.getTransitionName(sharedElement)
                ?: sharedElement.javaClass.name
        )
    )
}