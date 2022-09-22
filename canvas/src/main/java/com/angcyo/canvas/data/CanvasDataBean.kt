package com.angcyo.canvas.data

import com.angcyo.library.annotation.MM

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
data class CanvasDataBean(
    /**画布的宽高*/
    @MM
    val width: Float = 0f,
    @MM
    val height: Float = 0f,

    /**预览的base64图片*/
    val preview_img: String? = null,

    /**item list 的所有数据
     * [com.angcyo.canvas.data.ItemDataBean]
     * */
    val data: String? = null,

    /**工程名*/
    val projectName: String? = null
)