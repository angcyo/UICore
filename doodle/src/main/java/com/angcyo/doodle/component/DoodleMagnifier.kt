package com.angcyo.doodle.component

import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.angcyo.doodle.core.IDoodleItem
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dp

/**
 * 放大镜
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/11
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleMagnifier(val targetView: View) : IDoodleItem {

    /**是否激活*/
    var isEnable: Boolean = false

    /**放大的倍数*/
    var magnifierScale: Float = 4f

    /**放大镜的半径*/
    var radius: Float = 50 * dp

    /**边距*/
    var margining: Float = 10 * dp

    /**绘制的路径*/
    val magnifierPath: Path = Path()

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //阴影
        setShadowLayer(3f, 3f, 3f, Color.BLACK)
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1 * dp
    }

    //---

    /**放大镜内容*/
    var magnifierCanvas: Canvas? = null
    var magnifierBitmap: Bitmap? = null

    var _touchX: Float = 0f
    var _touchY: Float = 0f

    /**更新坐标数据*/
    @CallPoint
    fun update(event: MotionEvent) {
        _touchX = event.x
        _touchY = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val size = (radius * 2).toInt()
                magnifierBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                    magnifierCanvas = MagnifierCanvas(this)
                }
                magnifierPath.addCircle(radius, radius, radius, Path.Direction.CW)
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                magnifierBitmap?.recycle()
                magnifierBitmap = null
                magnifierCanvas = null
            }
        }
    }

    /**绘制*/
    @CallPoint
    fun onDraw(canvas: Canvas) {
        magnifierCanvas?.apply {
            //移动画布到手指下方的中点
            withClip(magnifierPath) {
                withScale(magnifierScale, magnifierScale, radius, radius) {
                    withTranslation(-_touchX + radius, -_touchY + radius) {
                        targetView.draw(this)
                    }
                }
            }
        }
        magnifierBitmap?.let {
            //核心放大效果
            var translationX = margining
            var translationY = margining

            val size = radius * 2
            val offset = margining * 2

            if (_touchX + offset + size < targetView.measuredWidth) {
                //右边够距离
                translationX = _touchX + offset
            } else {
                translationX = _touchX - offset - size
            }

            if (_touchY - offset - size > 0) {
                //顶部够距离
                translationY = _touchY - offset - size
            } else {
                translationY = _touchY + offset
            }

            /*if (_touchX < targetView.measuredWidth / 2) {
                translationX = targetView.measuredWidth - radius * 2 - margining
            }*/
            /*if (_touchY < targetView.measuredHeight / 2) {
                translationY = targetView.measuredHeight - radius * 2 - margining
            }*/
            canvas.withTranslation(translationX, translationY) {
                drawPath(magnifierPath, paint)
                drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    /**标识类*/
    class MagnifierCanvas(bitmap: Bitmap) : Canvas(bitmap)

}