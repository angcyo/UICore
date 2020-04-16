package com.angcyo.pager.dslitem

import android.net.Uri
import com.angcyo.image.dslitem.DslPhotoViewItem
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri

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
}