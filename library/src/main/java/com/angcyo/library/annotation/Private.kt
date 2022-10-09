package com.angcyo.library.annotation

/**
 * 标识属性/方法为私有的
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-9
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class Private
