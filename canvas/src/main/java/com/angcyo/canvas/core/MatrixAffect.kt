package com.angcyo.canvas.core

/**
 * 当前的元素绘制受到 [CanvasViewBox]的Matrix影响
 * [com.angcyo.library.unit.PixelValueUnit]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/20
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class MatrixAffect
