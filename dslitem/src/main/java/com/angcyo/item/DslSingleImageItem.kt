package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.R
import com.angcyo.item.style.IImageItem
import com.angcyo.item.style.ImageItemConfig

/**单纯的图片显示item
 * [DslImageItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/12
 */
class DslSingleImageItem : DslAdapterItem(), IImageItem {

    override var imageItemConfig: ImageItemConfig = ImageItemConfig()

    init {
        itemLayoutId = R.layout.dsl_single_only_image_item
    }

}