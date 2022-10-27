package com.angcyo.canvas

import com.angcyo.canvas.Reason.Companion.REASON_CODE
import com.angcyo.canvas.Reason.Companion.REASON_FLAG_BOUNDS
import com.angcyo.canvas.Reason.Companion.REASON_FLAG_ROTATE
import com.angcyo.canvas.Reason.Companion.REASON_FLAG_TRANSLATE
import com.angcyo.canvas.Reason.Companion.REASON_USER
import com.angcyo.library.annotation.Implementation

/**
 * 更新数据的原因描述
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/10
 */
data class Reason(
    /**更新数据的原因
     * [REASON_USER]
     * [REASON_CODE]
     * */
    val reason: Int = REASON_USER,
    /**是否要发送通知[dispatchItemBoundsChanged]*/
    val notify: Boolean = true,
    /**Flag
     * [REASON_FLAG_BOUNDS]
     * [REASON_FLAG_TRANSLATE]
     * [REASON_FLAG_ROTATE]
     * */
    val flag: Int = 0,
) {
    companion object {

        //reason

        /**用户主动操作*/
        const val REASON_USER = 1

        /**代码操作*/
        const val REASON_CODE = 2

        /**需要预览操作*/
        @Implementation
        const val REASON_PREVIEW = 3

        //flag

        /**改变item的宽高*/
        const val REASON_FLAG_BOUNDS = 0x01

        /**改变item的x,y*/
        const val REASON_FLAG_TRANSLATE = 0x02

        /**改变item的旋转*/
        const val REASON_FLAG_ROTATE = 0x04

        //val

        val user: Reason
            get() = Reason(REASON_USER)

        val code: Reason
            get() = Reason(REASON_CODE)

        val preview: Reason
            get() = Reason(REASON_PREVIEW)
    }
}
