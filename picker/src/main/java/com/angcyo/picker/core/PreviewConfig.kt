package com.angcyo.picker.core

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/18
 */
data class PreviewConfig(

    /**是否只预览选中的媒体, 否则就是当前文件夹的媒体*/
    var previewSelectorList: Boolean = false,

    /**预览开始的位置*/
    var previewStartPosition: Int = 0
)