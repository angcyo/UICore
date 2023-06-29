package com.angcyo.library.canvas.annotation

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/29
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
annotation class ViewCoordinate