package com.angcyo.widget.image

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import com.angcyo.library._refreshRateRatio
import com.angcyo.widget.R

/**
 * 图片旋转, 加载提示控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-4-24
 */
class ImageLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    /**是否激活动画*/
    var enableRotateAnimation = true

    /**旋转步长*/
    var rotateStep = 5f

    var _rotateDegrees = 0f

    init {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.ImageLoadingView)
        rotateStep =
            typeArray.getFloat(R.styleable.ImageLoadingView_loading_rotate_step, rotateStep)
        typeArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(_rotateDegrees, width / 2.toFloat(), height / 2.toFloat())
        super.onDraw(canvas)

        if (isEnabled && drawable != null && rotateStep != 0f && enableRotateAnimation) {
            //动画控制
            _rotateDegrees += rotateStep / _refreshRateRatio
            _rotateDegrees = if (_rotateDegrees < 360) _rotateDegrees else _rotateDegrees - 360
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**设置加载的资源*/
    fun setLoadingRes(resId: Int, loading: Boolean = true) {
        enableRotateAnimation = loading
        isEnabled = loading
        setImageResource(resId)
    }

}