package com.angcyo.widget.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import com.angcyo.drawable.DslGravity
import com.angcyo.library.ex.drawRect
import com.angcyo.library.ex.initBounds
import com.angcyo.library.ex.toColorInt
import com.angcyo.library.ex.viewRect
import com.angcyo.widget.R
import com.angcyo.widget.drawable.DslAttrBadgeDrawable
import com.angcyo.widget.text.IBadgeView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

open class DslImageView : ShapeImageView, IBadgeView {

    /**点击的时候, 是否显示蒙层*/
    var showTouchMask: Boolean = true

    /**蒙层[Drawable]*/
    var touchMaskDrawable: Drawable? = ColorDrawable("#1A000000".toColorInt())

    /**覆盖层绘制列表对象*/
    val overlayList = mutableListOf<OverlayDrawable>()

    /**默认add覆盖层的偏移距离*/
    var overlayOffsetX: Int = 0
    var overlayOffsetY: Int = 0

    val _dslGravity = DslGravity()
    var _isTouchHold = false

    /**角标绘制*/
    override var dslBadeDrawable = DslAttrBadgeDrawable()

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslImageView)

        showTouchMask =
            typedArray.getBoolean(R.styleable.DslImageView_r_show_touch_mask, showTouchMask)

        if (typedArray.hasValue(R.styleable.DslImageView_r_touch_mask_drawable)) {
            touchMaskDrawable =
                typedArray.getDrawable(R.styleable.DslImageView_r_touch_mask_drawable)
        }

        overlayOffsetX = typedArray.getDimensionPixelOffset(
            R.styleable.DslImageView_r_overlay_drawable_offset_x,
            overlayOffsetX
        )
        overlayOffsetY = typedArray.getDimensionPixelOffset(
            R.styleable.DslImageView_r_overlay_drawable_offset_y,
            overlayOffsetY
        )

        addOverlayDrawable(
            typedArray.getDrawable(R.styleable.DslImageView_r_overlay_drawable_lt)?.initBounds(),
            Gravity.LEFT or Gravity.TOP
        )
        addOverlayDrawable(
            typedArray.getDrawable(R.styleable.DslImageView_r_overlay_drawable_rt)?.initBounds(),
            Gravity.RIGHT or Gravity.TOP
        )
        addOverlayDrawable(
            typedArray.getDrawable(R.styleable.DslImageView_r_overlay_drawable_lb)?.initBounds(),
            Gravity.LEFT or Gravity.BOTTOM
        )
        addOverlayDrawable(
            typedArray.getDrawable(R.styleable.DslImageView_r_overlay_drawable_rb)?.initBounds(),
            Gravity.RIGHT or Gravity.BOTTOM
        )
        addOverlayDrawable(
            typedArray.getDrawable(R.styleable.DslImageView_r_overlay_drawable_center)
                ?.initBounds(),
            Gravity.CENTER
        )

        typedArray.recycle()

        dslBadeDrawable.apply {
            initAttribute(context, attributeSet)
            callback = this@DslImageView
            dslGravity.gravityRelativeCenter = false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isClickable) {
            return super.onTouchEvent(event)
        }
        if (showTouchMask) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    _isTouchHold = true
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    _isTouchHold = false
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event) || showTouchMask
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //draw overlay
        viewRect(_dslGravity.gravityBounds)
        overlayList.forEach {
            val overlayDrawable = it.drawable
            if (overlayDrawable != null) {
                if (overlayDrawable.bounds.isEmpty) {
                    viewRect(overlayDrawable.bounds)
                }
                _dslGravity.gravityRelativeCenter = false
                _dslGravity.gravity = it.gravity
                _dslGravity.gravityOffsetX = it.offsetX
                _dslGravity.gravityOffsetY = it.offsetY
                _dslGravity.applyGravity(
                    overlayDrawable.bounds.width().toFloat(),
                    overlayDrawable.bounds.height().toFloat()
                ) { _, _ ->
                    canvas.save()
                    canvas.translate(
                        _dslGravity._gravityLeft.toFloat(),
                        _dslGravity._gravityTop.toFloat()
                    )
                    overlayDrawable.draw(canvas)
                    canvas.restore()
                }
            }
        }

        dslBadeDrawable.apply {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }

        //draw click drawable
        if (maskDrawable == null) {
            onDrawInMask(canvas)
        }
    }

    override fun onDrawInMask(canvas: Canvas) {
        super.onDrawInMask(canvas)
        if (showTouchMask && _isTouchHold) {
            touchMaskDrawable?.apply {
                drawRect(bounds)
                draw(canvas)
            }
        }
    }

    /**添加覆盖绘制层*/
    fun addOverlayDrawable(
        drawable: Drawable?,
        gravity: Int = Gravity.CENTER,
        offsetX: Int = overlayOffsetX,
        offsetY: Int = overlayOffsetY
    ) {
        drawable?.run {
            overlayList.add(OverlayDrawable(this, gravity, offsetX, offsetY))
            postInvalidateOnAnimation()
        }
    }

    /**移除覆盖绘制层*/
    fun removeOverlayDrawable(drawable: Drawable?) {
        drawable?.run {
            overlayList.removeAll { it.drawable == this }
            postInvalidateOnAnimation()
        }
    }

    /**清空所有覆盖层*/
    fun clearOverlay() {
        overlayList.clear()
        postInvalidateOnAnimation()
    }

    /**角标的文本, 空字符串会绘制成小圆点*/
    fun updateBadge(text: String? = null, action: DslAttrBadgeDrawable.() -> Unit = {}) {
        dslBadeDrawable.apply {
            drawBadge = true
            //badgeGravity = Gravity.TOP or Gravity.RIGHT
            badgeText = text
            //badgeCircleRadius
            //badgeOffsetY = 4 * dpi
            //cornerRadius(25 * dp)
            action()
        }
    }

    data class OverlayDrawable(
        val drawable: Drawable?,
        val gravity: Int,
        val offsetX: Int,
        val offsetY: Int
    )
}