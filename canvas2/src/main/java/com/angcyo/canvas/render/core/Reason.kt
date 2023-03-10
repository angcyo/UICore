package com.angcyo.canvas.render.core

import com.angcyo.canvas.render.core.Reason.Companion.REASON_CODE
import com.angcyo.canvas.render.core.Reason.Companion.REASON_INIT
import com.angcyo.canvas.render.core.Reason.Companion.REASON_PREVIEW
import com.angcyo.canvas.render.core.Reason.Companion.REASON_USER

/**
 * 更新数据的原因描述
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023-2-24
 */
data class Reason(
    /**更新数据的原因
     * [REASON_INIT]
     * [REASON_USER]
     * [REASON_CODE]
     * [REASON_PREVIEW]
     * */
    val reason: Int = REASON_USER,

    /**
     * 当前的操作, 是由什么标志位产生的, 如果有.
     * 渲染标识[com.angcyo.canvas.render.core.IRenderer.renderFlags]
     * */
    var renderFlag: Int? = null,

    /**
     * 当前的操作, 是由什么控制类型产生的, 如果有.
     * 控制类型, 不同的控制类型标识, 进行不同的操作处理
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_DELETE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_ROTATE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_SCALE]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_LOCK]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_WIDTH]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_HEIGHT]
     * [com.angcyo.canvas.render.core.component.BaseControlPoint.CONTROL_TYPE_KEEP_GROUP_PROPERTY]
     * */
    var controlType: Int? = null,
) {
    companion object {

        //---reason

        /**初始化操作*/
        const val REASON_INIT = 1

        /**用户主动操作*/
        const val REASON_USER = REASON_INIT shl 1

        /**代码操作*/
        const val REASON_CODE = REASON_USER shl 1

        /**预览操作, 比如正在进行的操作*/
        const val REASON_PREVIEW = REASON_CODE shl 1

        //val

        val init: Reason
            get() = Reason(REASON_INIT)

        val user: Reason
            get() = Reason(REASON_USER)

        val code: Reason
            get() = Reason(REASON_CODE)

        val preview: Reason
            get() = Reason(REASON_PREVIEW)
    }
}
