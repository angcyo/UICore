package com.angcyo.github

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.angcyo.widget.R
import kotlin.math.min

/**
 * https://github.com/iielse/switchbutton
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class SwitchView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        private const val STATE_SWITCH_ON = 4
        private const val STATE_SWITCH_ON2 = 3
        private const val STATE_SWITCH_OFF2 = 2
        private const val STATE_SWITCH_OFF = 1
    }

    private val interpolator = AccelerateInterpolator(2f)
    private val paint: Paint = Paint()
    private val sPath: Path = Path()
    private val bPath: Path = Path()
    private val bRectF = RectF()
    private var sAnim = 0f
    private var bAnim = 0f
    private var shadowGradient: RadialGradient? = null
    protected var ratioAspect = 0.68f // (0,1]
    protected var animationSpeed = 0.1f // (0,1]
    private var state: Int
    private var lastState: Int
    private var isCanVisibleDrawing = false
    private var mOnClickListener: OnClickListener? = null
    protected var colorPrimary: Int
    protected var colorPrimaryDark: Int
    protected var colorOff: Int
    protected var colorOffDark: Int
    protected var colorShadow: Int
    protected var colorBar: Int
    protected var colorBackground: Int
    protected var hasShadow: Boolean
    protected var isOpened: Boolean
    private var sRight = 0f
    private var sCenterX = 0f
    private var sCenterY = 0f
    private var sScale = 0f
    private var bOffset = 0f
    private var bRadius = 0f
    private var bStrokeWidth = 0f
    private var bWidth = 0f
    private var bLeft = 0f
    private var bRight = 0f
    private var bOnLeftX = 0f
    private var bOn2LeftX = 0f
    private var bOff2LeftX = 0f
    private var bOffLeftX = 0f
    private var shadowReservedHeight = 0f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val DEFAULT_COLOR_PRIMARY = -0xb4289d
        val DEFAULT_COLOR_PRIMARY_DARK = -0xc539ae
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchView)
        colorPrimary = a.getColor(R.styleable.SwitchView_primaryColor, DEFAULT_COLOR_PRIMARY)
        colorPrimaryDark =
            a.getColor(R.styleable.SwitchView_primaryColorDark, DEFAULT_COLOR_PRIMARY_DARK)
        colorOff = a.getColor(R.styleable.SwitchView_offColor, -0x1c1c1d)
        colorOffDark = a.getColor(R.styleable.SwitchView_offColorDark, -0x404041)
        colorShadow = a.getColor(R.styleable.SwitchView_shadowColor, -0xcccccd)
        colorBar = a.getColor(R.styleable.SwitchView_barColor, -0x1)
        colorBackground = a.getColor(R.styleable.SwitchView_bgColor, -0x1)
        ratioAspect = a.getFloat(R.styleable.SwitchView_ratioAspect, 0.68f)
        hasShadow = a.getBoolean(R.styleable.SwitchView_hasShadow, true)
        isOpened = a.getBoolean(R.styleable.SwitchView_isOpened, false)
        state = if (isOpened) STATE_SWITCH_ON else STATE_SWITCH_OFF
        lastState = state
        a.recycle()
        if (colorPrimary == DEFAULT_COLOR_PRIMARY && colorPrimaryDark == DEFAULT_COLOR_PRIMARY_DARK) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val primaryColorTypedValue = TypedValue()
                    context.theme.resolveAttribute(
                        R.attr.colorPrimary,
                        primaryColorTypedValue,
                        true
                    )
                    if (primaryColorTypedValue.data > 0) colorPrimary = primaryColorTypedValue.data
                    val primaryColorDarkTypedValue = TypedValue()
                    context.theme.resolveAttribute(
                        R.attr.colorPrimaryDark,
                        primaryColorDarkTypedValue,
                        true
                    )
                    if (primaryColorDarkTypedValue.data > 0) colorPrimaryDark =
                        primaryColorDarkTypedValue.data
                }
            } catch (ignore: Exception) {
            }
        }
    }

    fun setColor(newColorPrimary: Int, newColorPrimaryDark: Int) {
        setColor(newColorPrimary, newColorPrimaryDark, colorOff, colorOffDark)
    }

    fun setColor(
        newColorPrimary: Int,
        newColorPrimaryDark: Int,
        newColorOff: Int,
        newColorOffDark: Int
    ) {
        setColor(newColorPrimary, newColorPrimaryDark, newColorOff, newColorOffDark, colorShadow)
    }

    fun setColor(
        newColorPrimary: Int,
        newColorPrimaryDark: Int,
        newColorOff: Int,
        newColorOffDark: Int,
        newColorShadow: Int
    ) {
        colorPrimary = newColorPrimary
        colorPrimaryDark = newColorPrimaryDark
        colorOff = newColorOff
        colorOffDark = newColorOffDark
        colorShadow = newColorShadow
        invalidate()
    }

    fun setColor(
        newColorPrimary: Int,
        newColorPrimaryDark: Int,
        newColorOff: Int,
        newColorOffDark: Int,
        newColorShadow: Int,
        newColorBar: Int,
        newColorBackground: Int
    ) {
        colorPrimary = newColorPrimary
        colorPrimaryDark = newColorPrimaryDark
        colorOff = newColorOff
        colorOffDark = newColorOffDark
        colorShadow = newColorShadow
        colorBar = newColorBar
        colorBackground = newColorBackground
        invalidate()
    }

    fun setShadow(shadow: Boolean) {
        hasShadow = shadow
        invalidate()
    }

    fun setIsOpened(opened: Boolean) {
        val wishState = if (opened) STATE_SWITCH_ON else STATE_SWITCH_OFF
        if (wishState == state) {
            return
        }
        refreshState(wishState)
    }

    fun toggleSwitch(isOpened: Boolean) {
        val wishState = if (isOpened) STATE_SWITCH_ON else STATE_SWITCH_OFF
        if (wishState == state) {
            return
        }
        if (wishState == STATE_SWITCH_ON && (state == STATE_SWITCH_OFF || state == STATE_SWITCH_OFF2)
            || wishState == STATE_SWITCH_OFF && (state == STATE_SWITCH_ON || state == STATE_SWITCH_ON2)
        ) {
            sAnim = 1f
        }
        bAnim = 1f
        refreshState(wishState)
    }

    private fun refreshState(newState: Int) {
        if (!isOpened && newState == STATE_SWITCH_ON) {
            isOpened = true
        } else if (isOpened && newState == STATE_SWITCH_OFF) {
            isOpened = false
        }
        lastState = state
        state = newState
        postInvalidate()
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var resultWidth: Int
        if (widthMode == MeasureSpec.EXACTLY) {
            resultWidth = widthSize
        } else {
            resultWidth = (((56 * resources.displayMetrics.density + 0.5f)
                    + paddingLeft + paddingRight).toInt())
            if (widthMode == MeasureSpec.AT_MOST) {
                resultWidth = min(resultWidth, widthSize)
            }
        }
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var resultHeight: Int
        if (heightMode == MeasureSpec.EXACTLY) {
            resultHeight = heightSize
        } else {
            resultHeight = (resultWidth * ratioAspect).toInt() + paddingTop + paddingBottom
            if (heightMode == MeasureSpec.AT_MOST) {
                resultHeight = min(resultHeight, heightSize)
            }
        }
        setMeasuredDimension(resultWidth, resultHeight)
    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isCanVisibleDrawing = w > paddingLeft + paddingRight && h > paddingTop + paddingBottom
        if (isCanVisibleDrawing) {
            val actuallyDrawingAreaWidth: Int = w - paddingLeft - paddingRight
            val actuallyDrawingAreaHeight: Int = h - paddingTop - paddingBottom
            val actuallyDrawingAreaLeft: Int
            val actuallyDrawingAreaRight: Int
            val actuallyDrawingAreaTop: Int
            val actuallyDrawingAreaBottom: Int
            if (actuallyDrawingAreaWidth * ratioAspect < actuallyDrawingAreaHeight) {
                actuallyDrawingAreaLeft = paddingLeft
                actuallyDrawingAreaRight = w - paddingRight
                val heightExtraSize =
                    (actuallyDrawingAreaHeight - actuallyDrawingAreaWidth * ratioAspect).toInt()
                actuallyDrawingAreaTop = paddingTop + heightExtraSize / 2
                actuallyDrawingAreaBottom = height - paddingBottom - heightExtraSize / 2
            } else {
                val widthExtraSize =
                    (actuallyDrawingAreaWidth - actuallyDrawingAreaHeight / ratioAspect).toInt()
                actuallyDrawingAreaLeft = paddingLeft + widthExtraSize / 2
                actuallyDrawingAreaRight = width - paddingRight - widthExtraSize / 2
                actuallyDrawingAreaTop = paddingTop
                actuallyDrawingAreaBottom = height - paddingBottom
            }
            shadowReservedHeight = ((actuallyDrawingAreaBottom - actuallyDrawingAreaTop) * 0.07f)
            val sLeft = actuallyDrawingAreaLeft.toFloat()
            val sTop = actuallyDrawingAreaTop + shadowReservedHeight
            sRight = actuallyDrawingAreaRight.toFloat()
            val sBottom = actuallyDrawingAreaBottom - shadowReservedHeight
            val sHeight = sBottom - sTop
            sCenterX = (sRight + sLeft) / 2
            sCenterY = (sBottom + sTop) / 2
            bLeft = sLeft
            bWidth = sBottom - sTop
            bRight = sLeft + bWidth
            val halfHeightOfS = bWidth / 2 // OfB
            bRadius = halfHeightOfS * 0.95f
            bOffset = bRadius * 0.2f // offset of switching
            bStrokeWidth = (halfHeightOfS - bRadius) * 2
            bOnLeftX = sRight - bWidth
            bOn2LeftX = bOnLeftX - bOffset
            bOffLeftX = sLeft
            bOff2LeftX = bOffLeftX + bOffset
            sScale = 1 - bStrokeWidth / sHeight
            sPath.reset()
            val sRectF = RectF()
            sRectF.top = sTop
            sRectF.bottom = sBottom
            sRectF.left = sLeft
            sRectF.right = sLeft + sHeight
            sPath.arcTo(sRectF, 90f, 180f)
            sRectF.left = sRight - sHeight
            sRectF.right = sRight
            sPath.arcTo(sRectF, 270f, 180f)
            sPath.close()
            bRectF.left = bLeft
            bRectF.right = bRight
            bRectF.top = sTop + bStrokeWidth / 2 // bTop = sTop
            bRectF.bottom = sBottom - bStrokeWidth / 2 // bBottom = sBottom
            val bCenterX = (bRight + bLeft) / 2
            val bCenterY = (sBottom + sTop) / 2
            val red = colorShadow shr 16 and 0xFF
            val green = colorShadow shr 8 and 0xFF
            val blue = colorShadow and 0xFF
            shadowGradient = RadialGradient(
                bCenterX, bCenterY, bRadius, Color.argb(200, red, green, blue),
                Color.argb(25, red, green, blue), Shader.TileMode.CLAMP
            )
        }
    }

    private fun calcBPath(percent: Float) {
        bPath.reset()
        bRectF.left = bLeft + bStrokeWidth / 2
        bRectF.right = bRight - bStrokeWidth / 2
        bPath.arcTo(bRectF, 90f, 180f)
        bRectF.left = bLeft + percent * bOffset + bStrokeWidth / 2
        bRectF.right = bRight + percent * bOffset - bStrokeWidth / 2
        bPath.arcTo(bRectF, 270f, 180f)
        bPath.close()
    }

    private fun calcBTranslate(percent: Float): Float {
        var result = 0f
        when (state - lastState) {
            1 -> if (state == STATE_SWITCH_OFF2) {
                result = bOffLeftX // off -> off2
            } else if (state == STATE_SWITCH_ON) {
                result = bOnLeftX - (bOnLeftX - bOn2LeftX) * percent // on2 -> on
            }
            2 -> if (state == STATE_SWITCH_ON) {
                result = bOnLeftX - (bOnLeftX - bOffLeftX) * percent // off2 -> on
            } else if (state == STATE_SWITCH_ON2) {
                result = bOn2LeftX - (bOn2LeftX - bOffLeftX) * percent // off -> on2
            }
            3 -> result = bOnLeftX - (bOnLeftX - bOffLeftX) * percent // off -> on
            -1 -> if (state == STATE_SWITCH_ON2) {
                result = bOn2LeftX + (bOnLeftX - bOn2LeftX) * percent // on -> on2
            } else if (state == STATE_SWITCH_OFF) {
                result = bOffLeftX // off2 -> off
            }
            -2 -> if (state == STATE_SWITCH_OFF) {
                result = bOffLeftX + (bOn2LeftX - bOffLeftX) * percent // on2 -> off
            } else if (state == STATE_SWITCH_OFF2) {
                result = bOff2LeftX + (bOnLeftX - bOff2LeftX) * percent // on -> off2
            }
            -3 -> result = bOffLeftX + (bOnLeftX - bOffLeftX) * percent // on -> off
            0 -> if (state == STATE_SWITCH_OFF) {
                result = bOffLeftX //  off -> off
            } else if (state == STATE_SWITCH_ON) {
                result = bOnLeftX // on -> on
            }
            else -> if (state == STATE_SWITCH_OFF) {
                result = bOffLeftX
            } else if (state == STATE_SWITCH_ON) {
                result = bOnLeftX
            }
        }
        return result - bOffLeftX
    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isCanVisibleDrawing) return
        paint.isAntiAlias = true
        val isOn = state == STATE_SWITCH_ON || state == STATE_SWITCH_ON2
        // Draw background
        paint.style = Paint.Style.FILL
        paint.color = if (isOn) colorPrimary else colorOff
        canvas.drawPath(sPath, paint)
        sAnim = if (sAnim - animationSpeed > 0) sAnim - animationSpeed else 0f
        bAnim = if (bAnim - animationSpeed > 0) bAnim - animationSpeed else 0f
        val dsAnim = interpolator.getInterpolation(sAnim)
        val dbAnim = interpolator.getInterpolation(bAnim)
        // Draw background animation
        val scale = sScale * if (isOn) dsAnim else 1 - dsAnim
        val scaleOffset = (sRight - sCenterX - bRadius) * if (isOn) 1 - dsAnim else dsAnim
        canvas.save()
        canvas.scale(scale, scale, sCenterX + scaleOffset, sCenterY)
        paint.color = colorBackground
        canvas.drawPath(sPath, paint)
        canvas.restore()
        // To prepare center bar path
        canvas.save()
        canvas.translate(calcBTranslate(dbAnim), shadowReservedHeight)
        val isState2 = state == STATE_SWITCH_ON2 || state == STATE_SWITCH_OFF2
        calcBPath(if (isState2) 1 - dbAnim else dbAnim)
        // Use center bar path to draw shadow
        if (hasShadow) {
            paint.style = Paint.Style.FILL
            paint.shader = shadowGradient
            canvas.drawPath(bPath, paint)
            paint.shader = null
        }
        canvas.translate(0f, -shadowReservedHeight)
        // draw bar
        canvas.scale(0.98f, 0.98f, bWidth / 2, bWidth / 2)
        paint.style = Paint.Style.FILL
        paint.color = colorBar
        canvas.drawPath(bPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = bStrokeWidth * 0.5f
        paint.color = if (isOn) colorPrimaryDark else colorOffDark
        canvas.drawPath(bPath, paint)
        canvas.restore()
        paint.reset()
        if (sAnim > 0 || bAnim > 0) invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if ((state == STATE_SWITCH_ON || state == STATE_SWITCH_OFF) && sAnim * bAnim == 0f) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> return true
                MotionEvent.ACTION_UP -> {
                    lastState = state
                    bAnim = 1f
                    if (state == STATE_SWITCH_OFF) {
                        refreshState(STATE_SWITCH_OFF2)
                        listener.toggleToOn(this)
                    } else if (state == STATE_SWITCH_ON) {
                        refreshState(STATE_SWITCH_ON2)
                        listener.toggleToOff(this)
                    }
                    if (mOnClickListener != null) {
                        mOnClickListener?.onClick(this)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        mOnClickListener = l
    }

    interface OnStateChangedListener {
        fun toggleToOn(view: SwitchView?)
        fun toggleToOff(view: SwitchView?)
    }

    private var listener: OnStateChangedListener = object : OnStateChangedListener {
        override fun toggleToOn(view: SwitchView?) {
            toggleSwitch(true)
        }

        override fun toggleToOff(view: SwitchView?) {
            toggleSwitch(false)
        }
    }

    fun setOnStateChangedListener(listener: OnStateChangedListener?) {
        requireNotNull(listener) { "empty listener" }
        this.listener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable? = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.isOpened = isOpened
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        isOpened = ss.isOpened
        this.state = if (isOpened) STATE_SWITCH_ON else STATE_SWITCH_OFF
        invalidate()
    }

    private class SavedState : BaseSavedState {
        var isOpened = false

        constructor(superState: Parcelable?) : super(superState) {}

        constructor(`in`: Parcel) : super(`in`) {
            isOpened = 1 == `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (isOpened) 1 else 0)
        }

        // fixed by Night99 https://github.com/g19980115
        override fun describeContents(): Int {
            return 0
        }
    }
}