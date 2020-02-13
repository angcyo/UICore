package com.angcyo.pager.dslitem

import android.graphics.drawable.Drawable
import android.net.Uri

/**
 * 根据要加载的Url, 获取对应的缩略图
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/23
 */

interface IPlaceholderDrawableProvider {
    /**获取[loadUri]对应的占位图, 如果有*/
    fun getPlaceholderDrawable(loadUri: Uri?): Drawable?
}