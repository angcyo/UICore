package com.angcyo.transition

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
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

    /**过渡时长*/
    var transitionDuration = -1L

    /**多次重复调用时, 自动结束之前的转换*/
    var autoEndTransition = true

    /**设置需要的转场过渡动画*/
    var onSetTransition: () -> TransitionSet = {
        TransitionSet().apply {
            addTransition(ColorTransition())
            addTransition(AlphaTransition())
            addTransition(Fade(Fade.OUT))
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeImageTransform())
            addTransition(Fade(Fade.IN))
            //addTransition(ChangeScroll()) //图片过渡效果, 请勿设置此项

            if (transitionDuration > 0) {
                duration = transitionDuration
            }
        }
    }

    /**动画结束回调*/
    var onTransitionEnd: (ViewGroup) -> Unit = {

    }

    /**动画开始回调*/
    var onTransitionStart: (ViewGroup) -> Unit = {

    }

    /**动画监听*/
    var transitionListener: TransitionListenerAdapter = object : TransitionListenerAdapter() {
        override fun onTransitionStart(transition: Transition) {
            super.onTransitionStart(transition)
            sceneRoot?.run { this@DslTransition.onTransitionStart(this) }
        }

        override fun onTransitionEnd(transition: Transition) {
            transition.removeListener(this)
            sceneRoot?.run { this@DslTransition.onTransitionEnd(this) }
        }
    }

    //<editor-fold desc="转场">

    /**核心1: 设置动画开始的值*/
    var onCaptureStartValues: (ViewGroup) -> Unit = {

    }

    /**核心2: 设置动画结束的值*/
    var onCaptureEndValues: (ViewGroup) -> Unit = {

    }

    /**开始过渡动画*/
    fun transition(delay: Long = 0) {
        doIt(delay)
    }

    /**开始转场动画, 注意开启条件[ViewCompat.isLaidOut(sceneRoot)], sceneRoot必须布局过至少一次*/
    fun doIt(delay: Long = 0) {
        sceneRoot?.run {
            if (autoEndTransition) {
                TransitionManager.endTransitions(this)//结束所有的转换
            }
            onCaptureStartValues(this)
            postDelayed({
                TransitionManager.beginDelayedTransition(
                    this,
                    onSetTransition().addListener(transitionListener)
                )
                onCaptureEndValues(this)
                //在某些时候需要手动触发绘制,否则可能不会执行, 只要有属性修改就不需要调用
                //postInvalidateOnAnimation()
            }, delay)
        } ?: L.w("sceneRoot is null.")
    }

    //</editor-fold desc="转场">

    //<editor-fold desc="Scene 转场">

    var targetScene: Scene? = null

    var onSceneExit: (ViewGroup) -> Unit = {}
    var onSceneEnter: (ViewGroup) -> Unit = {}

    /**[scene]的方式进行场景转换*/
    fun scene() {
        sceneRoot?.run {
            targetScene?.let {
                it.setExitAction {
                    onSceneExit(this@run)
                }
                it.setEnterAction {
                    onSceneEnter(this@run)
                }
                TransitionManager.go(it, onSetTransition())
            } ?: L.w("targetScene is null.")
        } ?: L.w("sceneRoot is null.")
    }

    //</editor-fold desc="Scene 转场">
}

fun ViewGroup.transition(action: DslTransition.() -> Unit) {
    dslTransition(this, action)
}

/**开始转场, 通过[onCaptureStartValues] [onCaptureEndValues] 差值执行动画
 * [com.angcyo.transition.DslTransition.onCaptureStartValues]
 * [com.angcyo.transition.DslTransition.onCaptureEndValues]
 *
 * dslTransition(viewGroup) {
 *   onCaptureEndValues = {
 *     dialogViewHolder.action()
 *   }
 * }
 * */
fun dslTransition(sceneRoot: ViewGroup?, action: DslTransition.() -> Unit) {
    val dslTransition = DslTransition()
    dslTransition.apply {
        this.sceneRoot = sceneRoot
        action()
        doIt()
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