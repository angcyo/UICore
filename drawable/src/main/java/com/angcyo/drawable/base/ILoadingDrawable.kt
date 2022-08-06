package com.angcyo.drawable.base

/**
 * 动画控制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/06
 */
interface ILoadingDrawable {

    /**是否加载中*/
    fun isLoading(): Boolean

    /**开始动画*/
    fun startLoading()

    /**结束动画*/
    fun stopLoading()

}