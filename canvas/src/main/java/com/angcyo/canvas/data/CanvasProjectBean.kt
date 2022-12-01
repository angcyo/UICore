package com.angcyo.canvas.data

import com.angcyo.http.base.fromJson
import com.angcyo.http.base.listType
import com.angcyo.library.annotation.MM

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
data class CanvasProjectBean(
    /**画布的宽高*/
    @MM
    var width: Double = 0.0,
    @MM
    var height: Double = 0.0,

    /**预览的base64图片
     * (data:image/xxx;base64,xxx) 带协议头
     *
     * Canvas: trying to draw too large(141018708bytes) bitmap.
     * */
    var preview_img: String? = null,

    /**item list 的所有数据
     * [com.angcyo.canvas.data.CanvasProjectItemBean]
     * */
    var data: String? = null,

    /**工程名*/
    var file_name: String? = null,

    /**工程创建时间, 13位毫秒*/
    var create_time: Long = -1,

    /**工程创建时间, 13位毫秒*/
    var update_time: Long = -1,

    /**数据内容版本*/
    var version: Int = 1,

    //---

    /**本地对应的文件路径, 如果有*/
    var _filePath: String? = null,

    /**是否处于调试模式下, 用于debug下方便断点*/
    var _debug: Boolean? = null
)

/**
 * 支持[com.angcyo.canvas.data.CanvasProjectItemBean]
 * 支持[com.angcyo.canvas.data.CanvasProjectBean]
 * */
typealias CanvasOpenDataType = Any

/**json字符串转换成[CanvasProjectBean]*/
fun String.toCanvasProjectBean() = fromJson<CanvasProjectBean>()

/**json字符串转换成[CanvasProjectItemBean]*/
fun String.toCanvasProjectItemBean() = fromJson<CanvasProjectItemBean>()

/**json字符串转换成[List<CanvasProjectItemBean>]*/
fun String.toCanvasProjectItemList() =
    fromJson<List<CanvasProjectItemBean>>(listType(CanvasProjectItemBean::class.java))