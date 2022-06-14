package com.angcyo.widget.progress

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.ex.dpi
import kotlin.math.abs
import kotlin.math.floor

/**
 * 大块的圆角矩形[android.widget.SeekBar]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
open class DslBlockSeekBar(context: Context, attributeSet: AttributeSet? = null) :
    DslProgressBar(context, attributeSet) {

    init {
        progressMinHeight = 40 * dpi
    }

    //<editor-fold desc="Touch事件">

    //手势检测
    val _gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                _onTouchMoveTo(e.x, e.y)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val absX = abs(distanceX)
                val absY = abs(distanceY)

                var handle = false
                if (absX > absY && e2 != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    _onTouchMoveTo(e2.x, e2.y)
                    handle = true
                }
                return handle
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        _gestureDetector.onTouchEvent(event)
        return true
    }

    /**手指移动*/
    open fun _onTouchMoveTo(x: Float, y: Float) {
        val progress: Int =
            floor(((x - _progressBound.left) / _progressBound.width() * progressMaxValue).toDouble()).toInt()

        progressValue = validProgress(progress)

        onSeekBarConfig?.apply { onSeekChanged(progressValue, _progressFraction, true) }
    }

    //</editor-fold desc="Touch事件">

    /**回调监听*/
    var onSeekBarConfig: SeekBarConfig? = null

    override fun setProgress(progress: Int, fromProgress: Int, animDuration: Long) {
        super.setProgress(progress, fromProgress, animDuration)
        onSeekBarConfig?.apply { onSeekChanged(validProgress(progress), _progressFraction, false) }
    }

    fun config(action: SeekBarConfig.() -> Unit) {
        if (onSeekBarConfig == null) {
            onSeekBarConfig = SeekBarConfig()
        }
        onSeekBarConfig?.action()
    }

}