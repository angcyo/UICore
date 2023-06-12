package com.angcyo.canvas.render.data

import android.graphics.RectF
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.renderer.CanvasElementRenderer
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.isSameDirection
import com.angcyo.library.ex.nowTime
import kotlin.math.absoluteValue

/**
 * 智能推荐的参考值
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
data class SmartAssistantReferenceValue(
    /**值, 比例x/y轴的坐标, 需要旋转的角度等*/
    @CanvasInsideCoordinate
    val value: Float,

    /**这个值, 来自那个对象, 可以是对象, 也可以是刻度尺
     * [com.angcyo.canvas.render.core.IRenderer]
     * [com.angcyo.canvas.render.core.CanvasAxisManager]
     * */
    val obj: Any?,

    //---

    /**提示的时间*/
    var smartAssistantTime: Long = nowTime(),

    /**在吸附阶段, 总共忽略的值, 累加值*/
    var ignoreDifferenceValue: Float = 0f
) {

    /**引用的元素Bounds*/
    val refElementBounds: RectF?
        get() = if (obj is CanvasElementRenderer) {
            obj.renderProperty?.getRenderBounds()
        } else {
            null
        }

    /**是否忽略当前的智能提示, 继续使用吸附值*/
    fun ignoreSmartValue(newValue: Float, tx: Float, dx: Float, threshold: Float): Boolean {
        ignoreDifferenceValue += dx
        if (isSameDirection(ignoreDifferenceValue, dx) &&
            ignoreDifferenceValue.absoluteValue < threshold
        ) {
            smartAssistantTime = nowTime()
            return true
        }
        return nowTime() - smartAssistantTime < LibHawkKeys.minAdsorbTime //至少吸附时长
    }

    /**旋转吸附*/
    fun ignoreSmartRotate(newValue: Float, threshold: Float): Boolean {
        if ((value - newValue).absoluteValue < threshold) {
            smartAssistantTime = nowTime()
            return true
        }
        return false
    }
}
