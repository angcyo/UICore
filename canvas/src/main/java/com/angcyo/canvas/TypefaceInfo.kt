package com.angcyo.canvas

import android.graphics.Typeface

/**
 * 字体信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/07
 */
data class TypefaceInfo(
    /**字体显示的名字*/
    val name: String,
    /**字体对象*/
    val typeface: Typeface,
    /**字体的本地路径, 如果有*/
    val filePath: String? = null,
    /**导入字体时, 字体是否重复了*/
    var isRepeat: Boolean = false,
    /**是否是自定义的字体, 自定义的字体支持删除操作*/
    var isCustom: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypefaceInfo

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
