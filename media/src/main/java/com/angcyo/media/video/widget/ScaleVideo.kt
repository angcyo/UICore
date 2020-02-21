package com.angcyo.media.video.widget

import android.graphics.Matrix

class ScaleVideo(private val mViewSize: Size, private val mVideoSize: Size) {
    fun getScaleMatrix(scalableType: ScalableType?): Matrix? {
        return when (scalableType) {
            ScalableType.NONE -> noScale
            ScalableType.FIT_XY -> fitXY()
            ScalableType.FIT_CENTER -> fitCenter()
            ScalableType.FIT_START -> fitStart()
            ScalableType.FIT_END -> fitEnd()
            ScalableType.CENTER_CROP -> getCropScale(PivotPoint.CENTER)
            else -> null
        }
    }

    private fun getMatrix(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float
    ): Matrix {
        val matrix = Matrix()
        matrix.setScale(sx, sy, px, py)
        return matrix
    }

    private fun getMatrix(
        sx: Float,
        sy: Float,
        pivotPoint: PivotPoint
    ): Matrix {
        return when (pivotPoint) {
            PivotPoint.LEFT_TOP -> getMatrix(sx, sy, 0f, 0f)
            PivotPoint.CENTER -> getMatrix(
                sx,
                sy,
                mViewSize.width / 2f,
                mViewSize.height / 2f
            )
            PivotPoint.RIGHT_BOTTOM -> getMatrix(
                sx,
                sy,
                mViewSize.width.toFloat(),
                mViewSize.height.toFloat()
            )
            else -> throw IllegalArgumentException("Illegal PivotPoint")
        }
    }

    private val noScale: Matrix
        get() {
            val sx = mVideoSize.width / mViewSize.width.toFloat()
            val sy = mVideoSize.height / mViewSize.height.toFloat()
            return getMatrix(sx, sy, PivotPoint.LEFT_TOP)
        }

    private fun getFitScale(pivotPoint: PivotPoint): Matrix {
        var sx = mViewSize.width.toFloat() / mVideoSize.width
        var sy = mViewSize.height.toFloat() / mVideoSize.height
        val minScale = Math.min(sx, sy)
        sx = minScale / sx
        sy = minScale / sy
        return getMatrix(sx, sy, pivotPoint)
    }

    private fun getCropScale(pivotPoint: PivotPoint): Matrix {
        var sx = mViewSize.width.toFloat() / mVideoSize.width
        var sy = mViewSize.height.toFloat() / mVideoSize.height
        val maxScale = Math.max(sx, sy)
        sx = maxScale / sx
        sy = maxScale / sy
        return getMatrix(sx, sy, pivotPoint)
    }

    private fun fitXY(): Matrix {
        return getMatrix(1f, 1f, PivotPoint.LEFT_TOP)
    }

    private fun fitStart(): Matrix {
        return getFitScale(PivotPoint.LEFT_TOP)
    }

    private fun fitCenter(): Matrix {
        return getFitScale(PivotPoint.CENTER)
    }

    private fun fitEnd(): Matrix {
        return getFitScale(PivotPoint.RIGHT_BOTTOM)
    }

}