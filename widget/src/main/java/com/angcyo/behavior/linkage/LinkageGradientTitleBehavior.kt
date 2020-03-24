package com.angcyo.behavior.linkage

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.behavior.ScrollBehaviorListener
import com.angcyo.library.ex.color
import com.angcyo.library.ex.loadColor
import com.angcyo.tablayout.clamp
import com.angcyo.tablayout.evaluateColor
import com.angcyo.widget.R
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.each
import com.angcyo.widget.base.mH
import kotlin.math.min

/**
 * 渐变标题栏颜色的行为. 配合[LinkageHeaderBehavior]使用
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/24
 */
open class LinkageGradientTitleBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseLinkageBehavior(context, attributeSet), ScrollBehaviorListener, ITitleBarBehavior {

    var titleTextId: Int = R.id.lib_title_text_view
    var backViewId: Int = R.id.lib_title_back_view

    var backgroundColorFrom: Int = Color.TRANSPARENT
    var backgroundColorTo: Int = Color.WHITE

    var titleTextColorFrom: Int = Color.TRANSPARENT
    var titleTextColorTo: Int = context.loadColor(R.color.text_general_color)

    var iconColorFrom: Int = Color.WHITE
        set(value) {
            field = value
            backIconColorFrom = value
        }
    var iconColorTo: Int = context.loadColor(R.color.lib_icon_dark_color)
        set(value) {
            field = value
            backIconColorTo = value
        }

    var backIconColorFrom: Int = Color.WHITE
    var backIconColorTo: Int = context.loadColor(R.color.lib_icon_dark_color)

    init {
        onBehaviorScrollTo = { _, y ->
            val percent = -y * 1f / childView.mH()
            onGradient(clamp(percent, 0f, 1f))
        }

        val array =
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.LinkageGradientTitleBehavior_Layout
            )
        titleTextId = array.getResourceId(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_title_text_id,
            titleTextId
        )
        backViewId = array.getResourceId(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_title_back_id,
            backViewId
        )
        titleTextColorFrom = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_title_text_color_from,
            titleTextColorFrom
        )
        titleTextColorTo = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_title_text_color_to,
            titleTextColorTo
        )

        backgroundColorFrom = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_background_color_from,
            backgroundColorFrom
        )
        backgroundColorTo = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_background_color_to,
            backgroundColorTo
        )

        iconColorFrom = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_icon_color_from,
            iconColorFrom
        )
        iconColorTo = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_icon_color_to,
            iconColorTo
        )

        backIconColorFrom = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_back_icon_color_from,
            backIconColorFrom
        )
        backIconColorTo = array.getColor(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_back_icon_color_to,
            backIconColorTo
        )
        array.recycle()
    }

    override fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.mH()
    }

    override fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int {
        return childView.mH()
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        super.layoutDependsOn(parent, child, dependency)
        headerView.behavior().apply {
            if (this is LinkageHeaderBehavior) {
                this.addScrollListener(this@LinkageGradientTitleBehavior)
            }
        }
        return false
    }

    override fun onBehaviorScrollTo(x: Int, y: Int) {
        //转发给自己处理
        if (y > 0) {
            _gestureScrollY = 0
            scrollTo(x, y)
        } else {
            //L.w("$_ovserScrollY $_nestedScrollY $_gestureScrollY")
            scrollTo(0, min(y, _gestureScrollY))
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        if (target == headerScrollView) {
            _gestureScrollY -= dyConsumed
            scrollTo(0, _gestureScrollY)
        }
    }

    //LinkageHeaderBehavior回调的是OverScroll值.
    var _gestureScrollY = 0

    override fun onGestureScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (_nestedScrollView != headerScrollView) {
            _gestureScrollY -= distanceY.toInt()
            scrollTo(0, _gestureScrollY)
        }
        //L.e("...$distanceX $distanceY $_gestureScrollY")
        return false
    }

    /**开始渐变*/
    open fun onGradient(percent: Float) {
        //背景
        childView?.setBackgroundColor(
            evaluateColor(
                percent,
                backgroundColorFrom,
                backgroundColorTo
            )
        )

        if (childView is ViewGroup) {
            (childView as ViewGroup).each {
                when (it) {
                    //文本
                    is TextView -> if (it.id == titleTextId) {
                        it.setTextColor(
                            evaluateColor(
                                percent,
                                titleTextColorFrom,
                                titleTextColorTo
                            )
                        )
                    }
                    //图片icon
                    is ImageView -> if (it.id == backViewId) {
                        it.setImageDrawable(
                            it.drawable?.color(
                                evaluateColor(
                                    percent,
                                    backIconColorFrom,
                                    backIconColorTo
                                )
                            )
                        )
                    } else {
                        it.setImageDrawable(
                            it.drawable?.color(
                                evaluateColor(
                                    percent,
                                    iconColorFrom,
                                    iconColorTo
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}