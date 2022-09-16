package com.angcyo.library.animation

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Y轴旋转动画
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/16
 */
class YRotateAnimation : Animation() {

    /**旋转角度*/
    var from = 0f
    var to = 180f

    val camera = Camera()
    var _centerX = 0f
    var _centerY = 0f

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)

        //获得中心点坐标
        _centerX = width / 2f
        _centerY = height / 2f
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        val matrix = t.matrix
        matrix.reset()

        camera.apply {
            save()
            val value = from + (to - from) * interpolatedTime
            rotateY(value)
            getMatrix(matrix)//核心
            matrix.preTranslate(-_centerX, -_centerY)
            matrix.postTranslate(_centerX, _centerY)
            restore()
        }
    }

}