package com.angcyo.canvas.core

/**
 * 标识方法是外部调用的入口
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/09
 */

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class EntryPoint
