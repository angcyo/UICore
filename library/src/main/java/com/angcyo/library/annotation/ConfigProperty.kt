package com.angcyo.library.annotation

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2025/02/18
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class ConfigProperty(val des: String = "描述当前属性为配置属性")
