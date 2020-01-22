package com.angcyo.transition

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.doOnPreDraw
import androidx.transition.*
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/19
 */

class DslTransition {
    /**根布局*/
    var sceneRoot: ViewGroup? = null

    /**设置需要的转场过渡动画*/
    var onSetTransition: () -> TransitionSet = {
        TransitionSet().apply {
            addTransition(Fade(Fade.OUT))
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeImageTransform())
            addTransition(Fade(Fade.IN))
            //addTransition(ChangeScroll()) //图片过渡效果, 请勿设置此项
        }
    }

    //<editor-fold desc="转场">

    /**设置动画开始的值*/
    var onCaptureStartValues: (ViewGroup) -> Unit = {

    }

    /**设置动画结束的值*/
    var onCaptureEndValues: (ViewGroup) -> Unit = {

    }

    /**开始转场*/
    fun transition() {
        sceneRoot?.run {
            onCaptureStartValues(this)
            doOnPreDraw {
                TransitionManager.beginDelayedTransition(this, onSetTransition())
                onCaptureEndValues(this)
            }
        } ?: L.w("sceneRoot is null.")
    }

    //</editor-fold desc="转场">

    //<editor-fold desc="Scene 转场">

    var targetScene: Scene? = null

    var sceneExit: (ViewGroup) -> Unit = {}
    var sceneEnter: (ViewGroup) -> Unit = {}

    /**场景转换*/
    fun scene() {
        sceneRoot?.run {
            targetScene?.let {
                it.setExitAction {
                    sceneExit(this@run)
                }
                it.setEnterAction {
                    sceneEnter(this@run)
                }
                TransitionManager.go(it, onSetTransition())
            } ?: L.w("targetScene is null.")
        } ?: L.w("sceneRoot is null.")
    }

    //</editor-fold desc="Scene 转场">
}

/**开始转场, 通过[onCaptureStartValues] [onCaptureEndValues] 差值执行动画*/
fun dslTransition(sceneRoot: ViewGroup?, action: DslTransition.() -> Unit) {
    val dslTransition = DslTransition()
    dslTransition.apply {
        this.sceneRoot = sceneRoot
        action()
        transition()
    }
}

/**场景过渡到[layoutId]*/
fun dslTransition(
    sceneRoot: ViewGroup?,
    @LayoutRes layoutId: Int,
    action: DslTransition.() -> Unit = {}
) {
    sceneRoot?.run {
        dslTransition(this, Scene.getSceneForLayout(this, layoutId, context), action)
    }
}

/**场景过渡到[layout]*/
fun dslTransition(
    sceneRoot: ViewGroup?,
    layout: View,
    action: DslTransition.() -> Unit = {}
) {
    sceneRoot?.run {
        dslTransition(this, Scene(this, layout), action)
    }
}

/**场景过渡到[Scene]*/
fun dslTransition(sceneRoot: ViewGroup?, scene: Scene, action: DslTransition.() -> Unit = {}) {
    val dslTransition = DslTransition()
    dslTransition.apply {
        this.sceneRoot = sceneRoot
        this.targetScene = scene
        action()
        scene()
    }
}