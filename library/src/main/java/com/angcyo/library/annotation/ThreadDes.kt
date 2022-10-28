package com.angcyo.library.annotation

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/28
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
annotation class ThreadDes(val des: String = "" /*简单的描述*/)
