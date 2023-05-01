package com.angcyo.drawable.base

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/05/01
 */
interface IProgressDrawable {

    /**不确定的进度*/
    var isIndeterminate: Boolean

    /**当前的进度
     * [0~100]*/
    var progress: Float
}