package com.angcyo.canvas.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.IComponent
import com.angcyo.library.ex.emptyRectF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseComponent : IComponent {

    /**是否激活当前组件*/
    var enable: Boolean = true

    //缓存
    val _tempMatrix: Matrix = Matrix()
    val _tempPoint: PointF = PointF()
    val _tempRect: RectF = emptyRectF()
}