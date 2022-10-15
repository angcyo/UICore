package com.angcyo.library.annotation

/**
 * 标识属性只希望读取, 不希望赋值
 * 作用在类上, 表示所有属性只读, 不写
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-9
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CLASS,
)
annotation class ReadOnly
