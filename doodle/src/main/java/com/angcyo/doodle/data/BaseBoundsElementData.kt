package com.angcyo.doodle.data

import android.graphics.RectF

/**
 * 带[Bounds]的元素数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
abstract class BaseBoundsElementData : BaseElementData() {

    /**元素需要绘制的位置*/
    var bounds: RectF = RectF()

}