package com.angcyo.library.component

import android.animation.TypeEvaluator
import android.graphics.Rect

/**
 * This evaluator can be used to perform type interpolation between `Rect` values.
 * [androidx.transition.RectEvaluator]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/13
 */

class RectEvaluator : TypeEvaluator<Rect> {
    /**
     * When null, a new Rect is returned on every evaluate call. When non-null,
     * mRect will be modified and returned on every evaluate.
     */
    private var mRect: Rect? = null

    /**
     * Construct a RectEvaluator that returns a new Rect on every evaluate call.
     * To avoid creating an object for each evaluate call,
     * [RectEvaluator.RectEvaluator] should be used
     * whenever possible.
     */
    constructor() {}

    /**
     * Constructs a RectEvaluator that modifies and returns `reuseRect`
     * in [.evaluate] calls.
     * The value returned from
     * [.evaluate] should
     * not be cached because it will change over time as the object is reused on each
     * call.
     *
     * @param reuseRect A Rect to be modified and returned by evaluate.
     */
    constructor(reuseRect: Rect?) {
        mRect = reuseRect
    }

    /**
     * This function returns the result of linearly interpolating the start and
     * end Rect values, with `fraction` representing the proportion
     * between the start and end values. The calculation is a simple parametric
     * calculation on each of the separate components in the Rect objects
     * (left, top, right, and bottom).
     *
     *
     * If [.RectEvaluator] was used to construct
     * this RectEvaluator, the object returned will be the `reuseRect`
     * passed into the constructor.
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start Rect
     * @param endValue   The end Rect
     * @return A linear interpolation between the start and end values, given the
     * `fraction` parameter.
     */
    override fun evaluate(fraction: Float, startValue: Rect, endValue: Rect): Rect {
        val left = startValue.left + ((endValue.left - startValue.left) * fraction).toInt()
        val top = startValue.top + ((endValue.top - startValue.top) * fraction).toInt()
        val right = startValue.right + ((endValue.right - startValue.right) * fraction).toInt()
        val bottom = startValue.bottom + ((endValue.bottom - startValue.bottom) * fraction).toInt()
        return if (mRect == null) {
            Rect(left, top, right, bottom)
        } else {
            mRect?.set(left, top, right, bottom)
            mRect!!
        }
    }
}
