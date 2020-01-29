package com.angcyo.pager

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionSet
import com.angcyo.base.dslFHelper
import com.angcyo.base.interceptTouchEvent
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.transition.DslTransition
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

abstract class ViewTransitionFragment : AbsLifecycleFragment() {

    /**过渡回调*/
    var transitionCallback: ViewTransitionCallback = ViewTransitionCallback()

    /**过渡执行协调*/
    val dslTransition = DslTransition()

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initTransitionLayout()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    open fun initTransitionLayout() {
        //防止事件穿透
        _vh.itemView.isClickable = true
        _vh.itemView.visibility = View.INVISIBLE
        _vh.post {
            startTransition(true)
        }
    }

    open fun onTransitionShowStart() {
        _vh.itemView.visibility = View.VISIBLE
    }

    /**显示过渡动画结束*/
    open fun onTransitionShowEnd() {
        transitionCallback.transitionShowFromRect = null
        transitionCallback.transitionShowToRect = null

        activity?.interceptTouchEvent(false)
    }

    open fun onTransitionHideStart() {

    }

    /**隐藏过渡动画结束*/
    open fun onTransitionHideEnd() {
        transitionCallback.transitionHideFromRect = null
        transitionCallback.transitionHideToRect = null
        activity?.interceptTouchEvent(false)
        
        //真正移除界面
        dslFHelper {
            noAnim()
            remove(this@ViewTransitionFragment)
        }
    }

    //拦截默认的返回处理
    override fun onBackPressed(): Boolean {
        if (super.onBackPressed()) {
            backTransition()
        }
        return false
    }

    /**转场动画显示界面*/
    open fun startTransition(start: Boolean) {
        activity?.interceptTouchEvent(true)
        dslTransition.apply {
            sceneRoot = _vh.itemView as? ViewGroup

            transitionCallback.sceneRoot = sceneRoot

            _configTransition(start, _vh)
        }
    }

    /**转场动画关闭界面*/
    open fun backTransition() {
        startTransition(false)
    }

    open fun _configTransition(start: Boolean, vh: DslViewHolder) {
        dslTransition.apply {

            //Capture
            onCaptureStartValues = {
                if (start) {
                    transitionCallback.onCaptureShowStartValues(vh)
                } else {
                    transitionCallback.onCaptureHideStartValues(vh)
                }
            }

            //Capture
            onCaptureEndValues = {
                if (start) {
                    transitionCallback.onCaptureShowEndValues(vh)
                } else {
                    transitionCallback.onCaptureHideEndValues(vh)
                }
            }

            //anim
            onSetTransition = {
                if (start) {
                    transitionCallback.onSetShowTransitionSet(vh, TransitionSet())
                } else {
                    transitionCallback.onSetHideTransitionSet(vh, TransitionSet())
                }
            }

            //callback
            onTransitionStart = {
                if (start) {
                    onTransitionShowStart()
                } else {
                    onTransitionHideStart()
                }
            }

            onTransitionEnd = {
                if (start) {
                    onTransitionShowEnd()
                } else {
                    onTransitionHideEnd()
                }
                sceneRoot = null
            }

            //transition
            if (start) {
                if (!transitionCallback.onStartShowTransition(this@ViewTransitionFragment, vh)) {
                    //不拦截, 执行默认的过渡动画
                    transition()
                }
            } else {
                if (!transitionCallback.onStartHideTransition(this@ViewTransitionFragment, vh)) {
                    //不拦截, 执行默认的过渡动画
                    transition()
                }
            }
        }
    }
}