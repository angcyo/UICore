package com.angcyo.canvas

/**
 * 更新数据的原因描述
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/10
 */
data class Reason(
    val reason: Int = REASON_USER,//更新数据的原因
    val notify: Boolean = true //是否要发送通知
) {
    companion object {
        /**用户主动操作*/
        const val REASON_USER = 1

        /**代码操作*/
        const val REASON_CODE = 2
    }
}
