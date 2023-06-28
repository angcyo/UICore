package com.angcyo.library.canvas.annotation

/**
 * 画板内部坐标, 相对于画板原点的坐标
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/2/23
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.CLASS,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class CanvasInsideCoordinate
