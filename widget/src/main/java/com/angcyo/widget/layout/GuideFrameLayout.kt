package com.angcyo.widget.layout

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import com.angcyo.library.ex._drawable
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import com.angcyo.widget.base.setBgDrawable


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：显示引导界面的布局
 * 创建人员：Robi
 * 创建时间：2017/12/26 17:26
 * 修改人员：Robi
 * 修改时间：2017/12/26 17:26
 * 修改备注：
 * Version: 1.0.0
 */
class GuideFrameLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    companion object {
        val LEFT = 1
        val TOP = 2
        val RIGHT = 3
        val BOTTOM = 4
        val CENTER = 5

        val LEFT_CENTER = 6
        val TOP_CENTER = 7
        val RIGHT_CENTER = 8
        val BOTTOM_CENTER = 9
    }

    /*锚点坐标列表*/
    private var anchorList = mutableListOf<Rect>()

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.GuideFrameLayout)
        if (isInEditMode) {
            val anchors = array.getString(R.styleable.GuideFrameLayout_r_guide_anchors)
            anchors?.let {
                val splits = it.split(":")
                if (splits.isNotEmpty()) {
                    anchorList.clear()
                    for (element in splits) {
                        val ss = element.split(",")
                        anchorList.add(
                            Rect(
                                ss[0].toInt(), ss[1].toInt(),
                                ss[0].toInt() + ss[2].toInt(), ss[1].toInt() + ss[3].toInt()
                            )
                        )
                    }
                }
            }
        }
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //修正大小
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.anchorIndex >= 0 && layoutParams.anchorIndex < anchorList.size) {
                    //需要对齐锚点
                    val anchorRect = anchorList[layoutParams.anchorIndex]

                    val offsetWidth = layoutParams.offsetWidth
                    val offsetHeight = layoutParams.offsetHeight

                    if (layoutParams.isAnchor) {
                        val w = anchorRect.width() + offsetWidth
                        val h = anchorRect.height() + offsetHeight
                        childAt.measure(exactly(w), exactly(h))
                    }
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        /*获取锚点*/
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.withAnchor) {
                    anchorList.add(Rect(childAt.left, childAt.top, childAt.right, childAt.bottom))
                }
            }
        }

        //修正坐标
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.anchorIndex >= 0 && layoutParams.anchorIndex < anchorList.size) {
                    //需要对齐锚点
                    val anchorRect = anchorList[layoutParams.anchorIndex]
                    val offsetX = layoutParams.offsetX
                    val offsetY = layoutParams.offsetY

                    if (layoutParams.isAnchor) {
                        //自动设置锚点View的背景为 蚂蚁线
                        if (childAt.background == null && !layoutParams.withAnchor) {
                            childAt.setBgDrawable(_drawable(R.drawable.base_guide_shape_line_dash))
                        }
                        val l = anchorRect.centerX() - childAt.measuredWidth / 2
                        val t = anchorRect.centerY() - childAt.measuredHeight / 2
                        childAt.layout(
                            l + offsetX,
                            t + offsetY,
                            l + childAt.measuredWidth + offsetX,
                            t + childAt.measuredHeight + offsetY
                        )
                    } else {
                        when (layoutParams.guideGravity) {
                            LEFT -> {
                                val l = anchorRect.left - offsetX - childAt.measuredWidth
                                val t = anchorRect.top + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            TOP -> {
                                val t = anchorRect.top - offsetY - childAt.measuredHeight
                                val l = anchorRect.left + offsetX
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            RIGHT -> {
                                val l = anchorRect.right + offsetX
                                val t = anchorRect.top + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            BOTTOM -> {
                                val l = anchorRect.left + offsetX
                                val t = anchorRect.bottom + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            CENTER -> {
                                val l = anchorRect.centerX() - childAt.measuredWidth / 2 + offsetX
                                val t = anchorRect.centerY() - childAt.measuredHeight / 2 + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            LEFT_CENTER -> {
                                val l = anchorRect.left - offsetX - childAt.measuredWidth
                                val t = anchorRect.centerY() - childAt.measuredHeight / 2 + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            TOP_CENTER -> {
                                val t = anchorRect.top - offsetY - childAt.measuredHeight
                                val l = anchorRect.centerX() - childAt.measuredWidth / 2 + offsetX
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            RIGHT_CENTER -> {
                                val l = anchorRect.right + offsetX
                                val t = anchorRect.centerY() - childAt.measuredHeight / 2 + offsetY
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            BOTTOM_CENTER -> {
                                val t = anchorRect.bottom + offsetY
                                val l = anchorRect.centerX() - childAt.measuredWidth / 2 + offsetX
                                childAt.layout(
                                    l,
                                    t,
                                    l + childAt.measuredWidth,
                                    t + childAt.measuredHeight
                                )
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    fun addAnchorList(anchors: List<Rect>) {
        anchorList.clear()
        anchorList.addAll(anchors)
    }

    fun addAnchor(anchor: Rect) {
        anchorList.add(anchor)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(lp)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    class LayoutParams : FrameLayout.LayoutParams {
        var anchorIndex = -1
        var guideGravity = 0
        var offsetX = 0
        var offsetY = 0

        /*只当 isAnchor=true 时有效*/
        var offsetWidth = 0
        var offsetHeight = 0
        var isAnchor = false
        var withAnchor = false

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.GuideFrameLayout_Layout)
            anchorIndex =
                a.getInt(R.styleable.GuideFrameLayout_Layout_r_guide_show_in_anchor, anchorIndex)
            guideGravity =
                a.getInt(R.styleable.GuideFrameLayout_Layout_r_guide_gravity, guideGravity)
            offsetX = a.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_x,
                offsetX
            )
            offsetY = a.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_y,
                offsetY
            )
            offsetWidth =
                a.getDimensionPixelOffset(
                    R.styleable.GuideFrameLayout_Layout_r_guide_offset_width,
                    offsetWidth
                )
            offsetHeight =
                a.getDimensionPixelOffset(
                    R.styleable.GuideFrameLayout_Layout_r_guide_offset_height,
                    offsetHeight
                )
            isAnchor = a.getBoolean(R.styleable.GuideFrameLayout_Layout_r_guide_is_anchor, isAnchor)
            withAnchor =
                a.getBoolean(R.styleable.GuideFrameLayout_Layout_r_guide_with_anchor, withAnchor)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
    }
}