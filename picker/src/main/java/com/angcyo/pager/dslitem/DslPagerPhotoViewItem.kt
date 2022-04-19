package com.angcyo.pager.dslitem

import android.net.Uri
import android.widget.ImageView
import com.angcyo.image.dslitem.DslPhotoViewItem
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslPagerPhotoViewItem : DslPhotoViewItem() {

    var itemLoaderMedia: LoaderMedia? = null
        get() = field ?: (itemData as? LoaderMedia)

    override var itemLoadUri: Uri? = null
        get() {
            return itemLoaderMedia?.loadUri() ?: field
        }

    override fun loadImage(itemHolder: DslViewHolder, imageView: ImageView) {
        val bitmap = itemLoaderMedia?.bitmap
        if (bitmap == null) {
            super.loadImage(itemHolder, imageView)
        } else {
            imageView.setImageBitmap(bitmap)
            onImageLoadSucceed(itemHolder, imageView)
        }
    }
}