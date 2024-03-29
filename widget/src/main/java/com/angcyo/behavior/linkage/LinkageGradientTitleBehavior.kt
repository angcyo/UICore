package com.angcyo.behavior.linkage

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.color
import com.angcyo.library.ex.each
import com.angcyo.library.ex.loadColor
import com.angcyo.library.ex.mH
import com.angcyo.tablayout.evaluateColor
import com.angcyo.widget.R
import com.angcyo.widget.text.DslSpanTextView
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
) : BaseLinkageGradientBehavior(context, attributeSet), ITitleBarBehavior {

    /**标题控件的id, 用于单独操作控制标题控件*/
    var titleTextId: Int = R.id.lib_title_text_view

    /**标题文本渐变开始的颜色*/
    var titleTextColorFrom: Int = Color.TRANSPARENT

    /**标题文本渐变结束的颜色*/
    var titleTextColorTo: Int = context.loadColor(R.color.text_general_color)

    /**[titleTextId]*/
    var backViewId: Int = R.id.lib_title_back_view

    var backIconColorFrom: Int = Color.WHITE
    var backIconColorTo: Int = context.loadColor(R.color.lib_icon_dark_color)

    /**其他[ImageView]控件图标颜色*/
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

    /**整个标题栏背景颜色渐变, 如果背景不是ColorDrawable,则使用alpha*/
    var backgroundColorFrom: Int = Color.TRANSPARENT
    var backgroundColorTo: Int = Color.WHITE

    var enableBackgroundGradient: Boolean = true
    var enableTitleGradient: Boolean = true
    var enableBackIconGradient: Boolean = true
    var enableIconGradient: Boolean = true

    /**隐藏或显示标题*/
    var enableTitleHide: Boolean = false

    init {
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

        enableBackgroundGradient = array.getBoolean(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_enable_background_gradient,
            enableBackgroundGradient
        )
        enableTitleGradient = array.getBoolean(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_enable_title_gradient,
            enableTitleGradient
        )
        enableIconGradient = array.getBoolean(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_enable_icon_gradient,
            enableIconGradient
        )
        enableBackIconGradient = array.getBoolean(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_enable_back_icon_gradient,
            enableBackIconGradient
        )
        enableTitleHide = array.getBoolean(
            R.styleable.LinkageGradientTitleBehavior_Layout_layout_enable_title_hide,
            enableTitleHide
        )

        array.recycle()
    }

    override fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.mH()
    }

    override fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int {
        return childView.mH()
    }

    var _headerScrollY = 0

    override fun onBehaviorScrollTo(
        scrollBehavior: BaseScrollBehavior<*>,
        x: Int,
        y: Int,
        scrollType: Int
    ) {
        if (scrollBehavior is LinkageHeaderBehavior) {
            _headerScrollY = y
            scrollTo(x, y, scrollType)
        } else {
            super.onBehaviorScrollTo(scrollBehavior, x, y, scrollType)
        }
    }

    override fun scrollTo(x: Int, y: Int, scrollType: Int) {
        super.scrollTo(x, min(y, _headerScrollY), scrollType)
    }

    override fun getMaxGradientHeight(): Int {
        return linkageHeaderBehavior?.childView?.mH()?.run {
            this /*- linkageStickyBehavior?.childView.mH()*/ - childView.mH()
        } ?: super.getMaxGradientHeight()
    }

    /**开始渐变*/
    override fun onGradient(percent: Float) {
        val fraction = if (percent > 0) 0f else clamp(-percent, 0f, 1f)

        //背景
        if (enableBackgroundGradient) {
            childView?.apply {
                if (background == null || background is ColorDrawable) {
                    setBackgroundColor(
                        evaluateColor(
                            fraction,
                            backgroundColorFrom,
                            backgroundColorTo
                        )
                    )
                } else {
                    //如果不是纯色的背景, 则使用透明度过渡
                    background?.apply {
                        alpha = (255 * fraction).toInt()
                    }
                }
            }
        }

        if (childView is ViewGroup) {

            val titleTextColor = evaluateColor(fraction, titleTextColorFrom, titleTextColorTo)

            val backIconColor = evaluateColor(fraction, backIconColorFrom, backIconColorTo)

            val iconColor = evaluateColor(fraction, iconColorFrom, iconColorTo)

            (childView as ViewGroup).each(true) { child ->
                when (child) {
                    //文本
                    is TextView -> {
                        if (child.id == titleTextId) {
                            if (enableTitleGradient) {
                                child.setTextColor(titleTextColor)
                            }
                            if (enableTitleHide) {
                                child.visibility = if (fraction >= 0.99) {
                                    View.VISIBLE
                                } else {
                                    View.INVISIBLE
                                }
                            }
                        }

                        if (child is DslSpanTextView) {
                            if (child.id == backViewId) {
                                if (enableBackIconGradient) {
                                    child.setDrawableColor(backIconColor)
                                }
                            } else {
                                if (enableIconGradient) {
                                    child.setDrawableColor(iconColor)
                                }
                            }
                        }
                    }
                    //图片icon
                    is ImageView -> if (child.id == backViewId) {
                        if (enableBackIconGradient) {
                            child.setImageDrawable(child.drawable?.color(backIconColor))
                        }
                    } else {
                        if (enableIconGradient) {
                            child.setImageDrawable(child.drawable?.color(iconColor))
                        }
                    }
                }
            }
        }
    }
}