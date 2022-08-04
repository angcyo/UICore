package com.angcyo.pager

import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.transition.*
import com.angcyo.library.L
import com.angcyo.library.ex.setWidthHeight
import com.angcyo.transition.ColorTransition
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class ViewTransitionCallback {

    /**过渡延迟[postDelayed], 当结束状态加载需要一段加载时间时, 这个延迟就有很重要的作用
     * [com.angcyo.pager.ViewTransitionFragment.initTransitionLayout] 预先加载[EndValues]需要的资源, 再启动转场.*/
    var transitionShowDelay: Long = 0L
    //var transitionHideDelay: Long = 0L

    /**界面状态栏的颜色*/
    var startBarColor: Int = Color.TRANSPARENT

    /**根布局, 通常也是背景动画视图*/
    var sceneRoot: ViewGroup? = null

    var backgroundStartColor: Int = Color.TRANSPARENT

    var backgroundEndColor: Int = Color.BLACK

    var transitionShowFromRect: Rect? = null
    var transitionShowToRect: Rect? = null

    var transitionHideFromRect: Rect? = null
    var transitionHideToRect: Rect? = null

    /**过渡动画需要隐藏的视图id列表*/
    var transitionOverlayViewIds = mutableListOf(
        R.id.lib_transition_overlay_view,
        R.id.lib_transition_overlay_view1,
        R.id.lib_transition_overlay_view2,
        R.id.lib_transition_overlay_view3,
        R.id.lib_transition_overlay_view4,
        R.id.lib_transition_overlay_view5,
        R.id.lib_transition_overlay_view6,
        R.id.lib_transition_overlay_view7,
        R.id.lib_transition_overlay_view8,
        R.id.lib_transition_overlay_view9
    )

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

        //转场动画 矩阵坐标设置
        transitionTargetView(viewHolder)?.apply {
            transitionShowFromRect?.let {
                translationX = it.left.toFloat()
                translationY = it.top.toFloat()

                setWidthHeight(it.width(), it.height())

                L.i("过渡从:$it")
            }
        }

        //隐藏干扰元素
        transitionOverlayViewIds.forEach {
            viewHolder.gone(it)
        }
    }

    /**界面显示时, 动画结束后的值设置*/
    open fun onCaptureShowEndValues(viewHolder: DslViewHolder) {
        backgroundTransitionView(viewHolder).setBackgroundColor(backgroundEndColor)

        transitionTargetView(viewHolder)?.apply {
            val x = (transitionShowToRect?.left ?: 0).toFloat()
            val y = (transitionShowToRect?.top ?: 0).toFloat()
            translationX = x
            translationY = y

            val w = transitionShowToRect?.width() ?: -1
            val h = transitionShowToRect?.height() ?: -1
            setWidthHeight(w, h)

            L.i("过渡到:x:$x y:$y w:$w, h:$h")
        }

        transitionOverlayViewIds.forEach {
            viewHolder.visible(it)
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
            //.addTarget(backgroundTransitionView(viewHolder))
            addTransition(ColorTransition())

            //addTransition(Fade(Fade.OUT))
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())

            addTransition(ChangeClipBounds())
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
            //关闭Outline, 这个很重要. 否则只能看到[View]的一部分
            this.clipToOutline = false

            visibility = View.VISIBLE

            transitionHideFromRect?.let {
                translationX = it.left.toFloat()
                translationY = it.top.toFloat()

                setWidthHeight(it.width(), it.height())

                L.i("隐藏从:$it")
            }
        }

        //隐藏干扰元素
        transitionOverlayViewIds.forEach {
            viewHolder.gone(it)
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

                L.i("隐藏到:$it")
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
        transitionSet.apply {
            //.addTarget(backgroundTransitionView(viewHolder))
            addTransition(ColorTransition())

            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())

            addTransition(ChangeClipBounds())
            addTransition(Fade(Fade.OUT))
            //addTransition(Fade(Fade.IN))
        }
        return transitionSet
    }
    //</editor-fold desc="hide过渡">

    //<editor-fold desc="事件回调">

    var initFragment: (fragment: ViewTransitionFragment) -> Unit = {}

    //</editor-fold desc="事件回调">

}
