package com.angcyo.library.component

import android.animation.TypeEvaluator
import android.graphics.Matrix

/**
 * [androidx.transition.TransitionUtils.MatrixEvaluator]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class MatrixEvaluator : TypeEvaluator<Matrix> {

    private val startValues = FloatArray(9)
    private val endValues = FloatArray(9)
    private val tempMatrix = Matrix()

    override fun evaluate(fraction: Float, startValue: Matrix, endValue: Matrix): Matrix {
        startValue.getValues(startValues)
        endValue.getValues(endValues)
        for (i in 0..8) {
            val diff = endValues[i] - startValues[i]
            endValues[i] = startValues[i] + fraction * diff
        }
        tempMatrix.setValues(endValues)
        return tempMatrix
    }
}