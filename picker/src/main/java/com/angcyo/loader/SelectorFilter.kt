package com.angcyo.loader

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
interface SelectorFilter {

    /**选择媒体时, 是否要过滤[media]*/
    fun filter(media: LoaderMedia): Boolean
}