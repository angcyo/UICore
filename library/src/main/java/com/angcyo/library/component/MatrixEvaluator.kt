package com.angcyo.library.component

import android.animation.TypeEvaluator
import android.graphics.Matrix

/**
 * [androidx.transition.TransitionUtils.MatrixEvaluator]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class MatrixEvaluator : TypeEvaluator<Matrix> {

    val mTempStartValues = FloatArray(9)
    val mTempEndValues = FloatArray(9)
    val mTempMatrix = Matrix()

    override fun evaluate(fraction: Float, startValue: Matrix, endValue: Matrix): Matrix {
        startValue.getValues(mTempStartValues)
        endValue.getValues(mTempEndValues)
        for (i in 0..8) {
            val diff = mTempEndValues[i] - mTempStartValues[i]
            mTempEndValues[i] = mTempStartValues[i] + fraction * diff
        }
        mTempMatrix.setValues(mTempEndValues)
        return mTempMatrix
    }
}