package com.angcyo.library.model

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
data class FileChooserParam(
    /**是否是多选*/
    var isMultiLimit: Boolean = false,
    /**选择文件类型
     * ```
     *  ["image\*"]选择图片
     * ```
     * */
    var mimeType: String? = null
) {

    val _selectorLimit: Int
        get() = if (isMultiLimit) 9 else 1

}