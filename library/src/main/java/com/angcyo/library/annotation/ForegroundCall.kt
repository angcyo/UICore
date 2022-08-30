package com.angcyo.library.annotation

/**
 * 标识方法需要app在前台才能调用
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/30
 */

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ForegroundCall
