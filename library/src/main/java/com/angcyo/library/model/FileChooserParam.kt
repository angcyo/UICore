package com.angcyo.library.model

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
data class FileChooserParam(
    //选择数量
    var multiLimit: Int = 1,
    //选择文件类型
    var mimeType: String? = null
)