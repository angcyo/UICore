package com.angcyo.canvas

import android.graphics.Typeface

/**
 * 字体信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/07
 */
data class TypefaceInfo(
    //字体显示的名字
    val name: String,
    //字体
    val typeface: Typeface,
    //字体的本地路径, 如果有
    val filePath: String? = null,
    //字体是否重复
    var isRepeat: Boolean = false
)
