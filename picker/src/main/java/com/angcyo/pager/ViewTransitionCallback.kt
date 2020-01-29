package com.angcyo.pager

import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.transition.*
import com.angcyo.transition.ColorTransition
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setWidthHeight

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class ViewTransitionCallback {

    /**根布局, 通常也是背景动画视图*/
    var sceneRoot: ViewGroup? = null

    var backgroundStartColor: Int = Color.TRANSPARENT

    var backgroundEndColor: Int = Color.BLACK

    var transitionShowFromRect: Rect? = null
    var transitionShowToRect: Rect? = null

    var transitionHideFromRect: Rect? = null
    var transitionHideToRect: Rect? = null

    /**背景过渡视图*/
    open fun backgroundTransitionView(viewHolder: DslViewHolder): View {
        return sceneRoot ?: viewHolder.itemView
    }

    /**转场动画视图*/
    open fun transitionTargetView(viewHolder: DslViewHolder): View? {
        return null
    }

    //<editor-fold desc="show过渡">

    /**界面显示时, 动画开始的值设置*/
    open fun onCaptureShowStartValues(viewHolder: DslViewHolder) {
        //背景颜色动画
        backgroundTransitionView(viewHolder).setBackgroundColor(backgroundStartColor)

        transitionTargetView(viewHolder)?.apply {
            transitionShowFromRect?.let {
                translationX = it.left.toFloat()
                translationY = it.top.toFloat()

                setWidthHeight(it.width(), it.height())
            }
        }
    }

    /**界面显示时, 动画结束后的值设置*/
    open fun onCaptureShowEndValues(viewHolder: DslViewHolder) {
        backgroundTransitionView(viewHolder).setBackgroundColor(backgroundEndColor)

        transitionTargetView(viewHolder)?.apply {
            translationX = (transitionShowToRect?.left ?: 0).toFloat()
            translationY = (transitionShowToRect?.top ?: 0).toFloat()

            setWidthHeight(
                transitionShowToRect?.width() ?: -1,
                transitionShowToRect?.height() ?: -1
            )
        }
    }

    /**开始show的转场动画, 返回true, 拦截过渡*/
    open fun onStartShowTransition(
        fragment: ViewTransitionFragment,
        viewHolder: DslViewHolder
    ): Boolean {
        return false
    }

    open fun onSetShowTransitionSet(
        viewHolder: DslViewHolder,
        transitionSet: TransitionSet
    ): TransitionSet {
        transitionSet.apply {
            addTransition(ColorTransition().addTarget(backgroundTransitionView(viewHolder)))
            addTransition(Fade(Fade.OUT))
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeImageTransform())
            addTransition(Fade(Fade.IN))
        }
        return transitionSet
    }

    //</editor-fold desc="show过渡">

    //<editor-fold desc="hide过渡">

    /**界面关闭, 动画开始时的值(通过可以不设置此处)*/
    open fun onCaptureHideStartValues(viewHolder: DslViewHolder) {
        //backgroundTransitionView(viewHolder).setBackgroundColor(backgroundEndColor)
        //就是用当前设置的背景颜色
        transitionTargetView(viewHolder)?.apply {
            transitionHideFromRect?.let {
                translationX = it.left.toFloat()
                translationY = it.top.toFloat()

                setWidthHeight(it.width(), it.height())
            }
        }
    }

    /**界面关闭, 动画需要结束的值*/
    open fun onCaptureHideEndValues(viewHolder: DslViewHolder) {
        backgroundTransitionView(viewHolder).setBackgroundColor(backgroundStartColor)

        transitionTargetView(viewHolder)?.apply {
            transitionHideToRect?.let {
                translationX = it.left.toFloat()
                translationY = it.top.toFloat()

                setWidthHeight(it.width(), it.height())
            }
        }
    }

    /**开始hide的转场动画, 返回true, 拦截过渡*/
    open fun onStartHideTransition(
        fragment: ViewTransitionFragment,
        viewHolder: DslViewHolder
    ): Boolean {
        return false
    }

    open fun onSetHideTransitionSet(
        viewHolder: DslViewHolder,
        transitionSet: TransitionSet
    ): TransitionSet {
        return onSetShowTransitionSet(viewHolder, transitionSet)
    }
    //</editor-fold desc="hide过渡">

}
