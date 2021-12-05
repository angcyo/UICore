package com.angcyo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.dpi
import com.angcyo.tablayout.viewDrawHeight
import com.angcyo.tablayout.viewDrawWidth

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/05
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DslLoadingButton : DslButton {

    /**旋转步长*/
    var loadingRotateStep = 5f

    /**图标大小*/
    var loadingSize: Int = -1

    /**需要旋转的[Drawable]*/
    var loadingDrawable: Drawable? = null

    /**状态*/
    var isLoading: Boolean = false
        set(value) {
            field = value
            isEnabled = !value
        }

    constructor(context: Context) : super(context) {
        initAttribute(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs, defStyleAttr)
    }

    fun initAttribute(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) {
        val typedArray =
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.DslLoadingButton,
                defStyleAttr,
                0
            )

        loadingDrawable = typedArray.getDrawable(R.styleable.DslLoadingButton_loading_drawable)
            ?: _drawable(R.mipmap.lib_common_loading_tiny, context)

        loadingRotateStep = typedArray.getFloat(
            R.styleable.DslLoadingButton_loading_drawable_rotate_step,
            loadingRotateStep
        )
        loadingSize = typedArray.getDimensionPixelOffset(
            R.styleable.DslLoadingButton_loading_drawable_size,
            24 * dpi
        )

        typedArray.recycle()
    }

    var _rotateDegrees = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        loadingDrawable?.apply {
            if (isLoading || isInEditMode) {
                canvas.save()
                val cx = paddingLeft + viewDrawWidth / 2
                val cy = paddingTop + viewDrawHeight / 2
                canvas.translate(cx.toFloat(), cy.toFloat())
                canvas.rotate(_rotateDegrees)
                setBounds(-loadingSize / 2, -loadingSize / 2, loadingSize / 2, loadingSize / 2)
                draw(canvas)
                canvas.restore()

                _rotateDegrees += loadingRotateStep
                _rotateDegrees = if (_rotateDegrees < 360) _rotateDegrees else _rotateDegrees - 360
                ViewCompat.postInvalidateOnAnimation(this@DslLoadingButton)
            }
        }
    }

    /*override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(WrapClickListener(l))
    }

    inner class WrapClickListener(val origin: OnClickListener?) : OnClickListener {
        override fun onClick(v: View) {
            isLoading = true
            origin?.onClick(v)
        }
    }*/
}