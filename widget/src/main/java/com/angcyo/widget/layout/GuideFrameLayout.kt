package com.angcyo.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.createPaint
import com.angcyo.library.ex.have
import com.angcyo.library.ex.isTouchDown
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.toDp
import com.angcyo.library.ex.toDpi
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly


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
        val HORIZONTAL_CENTER = 2
        val RIGHT = 4

        val TOP = 0x1000
        val VERTICAL_CENTER = 0x2000
        val BOTTOM = 0x4000

        /**偏移自身*/
        val OFFSET_SELF = 0x1000000
        val OFFSET_SELF_WIDTH = 0x2000000
        val OFFSET_SELF_HEIGHT = 0x4000000
    }

    /**锚点坐标列表*/
    private var anchorList = mutableListOf<Rect>()

    var guideBackgroundDrawable: Drawable? = null
    var clipAnchor = false
    var clipAnchorRadius = 0f

    /**矩形额外的扩展空间*/
    var clipAnchorInset = 2.toDp()

    var clipAnchorInsetWidth: Float? = null
    var clipAnchorInsetHeight: Float? = null

    /**在指定的锚点区域点击*/
    var onAnchorClick: ((anchorIndex: Int) -> Unit)? = null

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.GuideFrameLayout)
        clipAnchor = array.getBoolean(R.styleable.GuideFrameLayout_r_clip_anchor, clipAnchor)
        clipAnchorRadius = array.getDimensionPixelOffset(
            R.styleable.GuideFrameLayout_r_clip_anchor_radius, 4.toDpi()
        ).toFloat()
        clipAnchorInset = array.getDimensionPixelOffset(
            R.styleable.GuideFrameLayout_r_clip_anchor_inset, clipAnchorInset.toInt()
        ).toFloat()

        if (array.hasValue(R.styleable.GuideFrameLayout_r_clip_anchor_inset_width)) {
            clipAnchorInsetWidth = array.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_r_clip_anchor_inset_width,
                (clipAnchorInsetWidth ?: 0f).toInt()
            ).toFloat()
        }
        if (array.hasValue(R.styleable.GuideFrameLayout_r_clip_anchor_inset_height)) {
            clipAnchorInsetHeight = array.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_r_clip_anchor_inset_height,
                (clipAnchorInsetHeight ?: 0f).toInt()
            ).toFloat()
        }

        guideBackgroundDrawable = array.getDrawable(R.styleable.GuideFrameLayout_r_guide_background)
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
                                ss[0].toInt(),
                                ss[1].toInt(),
                                ss[0].toInt() + ss[2].toInt(),
                                ss[1].toInt() + ss[3].toInt()
                            )
                        )
                    }
                }
            }
        }
        setWillNotDraw(false)
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
                val childWidth = childAt.measuredWidth
                val childHeight = childAt.measuredHeight

                var l = 0
                var t = 0
                var offsetX = layoutParams.offsetX
                var offsetY = layoutParams.offsetY

                if (layoutParams.anchorIndex >= 0 && layoutParams.anchorIndex < anchorList.size) {
                    //需要对齐锚点
                    val anchorRect = anchorList[layoutParams.anchorIndex]
                    if (layoutParams.isAnchor) {
                        //自动设置锚点View的背景为 蚂蚁线
                        if (childAt.background == null && !layoutParams.withAnchor) {
                            childAt.setBgDrawable(_drawable(R.drawable.base_guide_shape_line_dash))
                        }

                        l = anchorRect.centerX() - childWidth / 2
                        t = anchorRect.centerY() - childHeight / 2
                    } else {
                        var offsetSelfWidth = 0
                        var offsetSelfHeight = 0
                        if (layoutParams.guideGravity.have(OFFSET_SELF)) {
                            offsetSelfWidth = childWidth
                            offsetSelfHeight = childHeight
                        } else if (layoutParams.guideGravity.have(OFFSET_SELF_WIDTH)) {
                            offsetSelfWidth = childWidth
                        } else if (layoutParams.guideGravity.have(OFFSET_SELF_HEIGHT)) {
                            offsetSelfHeight = childHeight
                        }

                        if (layoutParams.guideGravity.have(LEFT)) {
                            l = anchorRect.left - offsetSelfWidth
                            offsetX = -offsetX
                        } else if (layoutParams.guideGravity.have(RIGHT)) {
                            l = anchorRect.right - childWidth + offsetSelfWidth
                        } else if (layoutParams.guideGravity.have(HORIZONTAL_CENTER)) {
                            l = anchorRect.centerX() - childWidth / 2
                        }

                        if (layoutParams.guideGravity.have(TOP)) {
                            t = anchorRect.top - offsetSelfHeight
                            offsetY = -offsetY
                        } else if (layoutParams.guideGravity.have(BOTTOM)) {
                            t = anchorRect.bottom - childHeight + offsetSelfHeight
                        } else if (layoutParams.guideGravity.have(VERTICAL_CENTER)) {
                            t = anchorRect.centerY() - childHeight / 2
                        }
                    }

                    childAt.layout(
                        l + offsetX,
                        t + offsetY,
                        l + childWidth + offsetX,
                        t + childHeight + offsetY
                    )
                }
            }
        }
    }

    var anchorPaint = createPaint(Color.WHITE, Paint.Style.FILL).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val tempRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        guideBackgroundDrawable?.let {
            val layoutId = canvas.saveLayer(null, null)
            it.bounds.set(0, 0, right - left, bottom - top)
            it.draw(canvas)
            //绘制锚点区域
            if (clipAnchor) {
                for (i in 0 until anchorList.size) {
                    val rect = anchorList[i]
                    tempRect.set(rect)
                    tempRect.inset(
                        -(clipAnchorInsetWidth ?: clipAnchorInset),
                        -(clipAnchorInsetHeight ?: clipAnchorInset)
                    )
                    canvas.drawRoundRect(tempRect, clipAnchorRadius, clipAnchorRadius, anchorPaint)
                }
            }
            canvas.restoreToCount(layoutId)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.isTouchDown()) {
            val x = event.x
            val y = event.y
            for (i in 0 until anchorList.size) {
                val rect = anchorList[i]
                if (rect.contains(x.toInt(), y.toInt())) {
                    onAnchorClick?.invoke(i)
                    super.onTouchEvent(event)
                    return false
                }
            }
        }
        return true
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
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /**移除自己*/
    fun removeIt() {
        (parent as? ViewGroup)?.removeView(this)
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
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_x, offsetX
            )
            offsetY = a.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_y, offsetY
            )
            offsetWidth = a.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_width, offsetWidth
            )
            offsetHeight = a.getDimensionPixelOffset(
                R.styleable.GuideFrameLayout_Layout_r_guide_offset_height, offsetHeight
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